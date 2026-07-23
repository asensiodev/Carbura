package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.sync.SyncResult
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.withLock

internal class LocalFirstSyncManager(
    private val authGateway: AuthGateway,
    private val profileGateway: RemoteUserProfileGateway,
    private val local: LocalSyncDataSource,
    private val remote: RemoteSyncDataSource,
    private val operationLock: SyncOperationLock = SyncOperationLock(),
    private val familyScope: ActiveFamilyScopeGateway,
) : SyncManager {
    private val _status = MutableStateFlow(SyncStatus())
    private var nextFailureId = 0L

    override val status: StateFlow<SyncStatus> = _status

    override suspend fun syncNow(): SyncResult = syncNow(reportFailure = true)

    override suspend fun syncNowSilently(): SyncResult = syncNow(reportFailure = false)

    private suspend fun syncNow(reportFailure: Boolean): SyncResult =
        operationLock.mutex.withLock {
            _status.update { it.copy(isSyncing = true) }
            try {
                val syncedAt = syncActiveFamily()
                _status.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncedAtMillis = syncedAt,
                        lastErrorMessage = null,
                        failureId = null,
                        acknowledgedFailureId = null,
                    )
                }
                SyncResult.Success(syncedAt)
            } catch (error: CancellationException) {
                _status.update { it.copy(isSyncing = false) }
                throw error
            } catch (error: Throwable) {
                val message = "Sync failed"
                if (reportFailure) nextFailureId += 1L
                _status.update {
                    it.copy(
                        isSyncing = false,
                        lastErrorMessage = message,
                        failureId = nextFailureId.takeIf { reportFailure },
                        acknowledgedFailureId = null,
                    )
                }
                SyncResult.Failure(message)
            }
        }

    override fun acknowledgeFailure(failureId: Long) {
        _status.update { status ->
            if (status.failureId == failureId) {
                status.copy(acknowledgedFailureId = failureId)
            } else {
                status
            }
        }
    }

    private suspend fun syncActiveFamily(): Long {
        val scope = resolveFamilyScope()
        pushPendingChanges(scope)
        pullAndMerge(scope)
        return currentTimeMillis()
    }

    private suspend fun resolveFamilyScope(): ActiveFamilyScope {
        val session = authGateway.currentSession() ?: error("No active session")
        val userId = UserId(session.user.id)
        val familyId = profileGateway.getProfileForUser(userId)?.familyId ?: error("No active family")
        return familyScope.activateAuthenticated(userId, familyId)
    }

    private suspend fun pushPendingChanges(scope: ActiveFamilyScope) {
        val familyId = scope.familyId
        val remoteVehicles = remote.getVehicles(familyId).associateBy { it.id }
        val pendingVehicles = local.getPendingVehicles(scope)
        val vehiclesToPush =
            pendingVehicles.filter { localVehicle ->
                val remoteVehicle = remoteVehicles[localVehicle.id]
                remoteVehicle == null || localVehicle.updatedAt >= remoteVehicle.updatedAt
            }
        remote.upsertVehicles(vehiclesToPush)
        vehiclesToPush.forEach { local.markVehicleSynced(scope, it.id, it.updatedAt) }

        val remoteMaintenance = remote.getMaintenanceRecords(familyId).associateBy { it.id }
        val pendingMaintenance = local.getPendingMaintenanceRecords(scope)
        val maintenanceToPush =
            pendingMaintenance.filter { localRecord ->
                val remoteRecord = remoteMaintenance[localRecord.id]
                remoteRecord == null || localRecord.updatedAt >= remoteRecord.updatedAt
            }
        remote.upsertMaintenanceRecords(maintenanceToPush)
        maintenanceToPush.forEach { local.markMaintenanceRecordSynced(scope, it.id, it.updatedAt) }

        val remoteReminders = remote.getReminders(familyId).associateBy { it.id }
        val pendingReminders = local.getPendingReminders(scope)
        val remindersToPush =
            pendingReminders.filter { localReminder ->
                val remoteReminder = remoteReminders[localReminder.id]
                remoteReminder == null || localReminder.updatedAt >= remoteReminder.updatedAt
            }
        remote.upsertReminders(remindersToPush)
        remindersToPush.forEach { local.markReminderSynced(scope, it.id, it.updatedAt) }
    }

    private suspend fun pullAndMerge(scope: ActiveFamilyScope) {
        mergeVehicles(scope, remote.getVehicles(scope.familyId))
        mergeMaintenanceRecords(scope, remote.getMaintenanceRecords(scope.familyId))
        mergeReminders(scope, remote.getReminders(scope.familyId))
    }

    private suspend fun mergeVehicles(
        scope: ActiveFamilyScope,
        remoteVehicles: List<SyncVehicle>,
    ) {
        val localVehicles = local.getVehicles(scope).associateBy { it.id }
        remoteVehicles.forEach { remoteVehicle ->
            val localVehicle = localVehicles[remoteVehicle.id]
            if (localVehicle == null || !localVehicle.pendingSync || remoteVehicle.updatedAt >= localVehicle.updatedAt) {
                local.upsertSyncedVehicle(scope, remoteVehicle)
            }
        }
    }

    private suspend fun mergeMaintenanceRecords(
        scope: ActiveFamilyScope,
        remoteRecords: List<SyncMaintenanceRecord>,
    ) {
        val localRecords = local.getMaintenanceRecords(scope).associateBy { it.id }
        remoteRecords.forEach { remoteRecord ->
            val localRecord = localRecords[remoteRecord.id]
            if (localRecord == null || !localRecord.pendingSync || remoteRecord.updatedAt >= localRecord.updatedAt) {
                local.upsertSyncedMaintenanceRecord(scope, remoteRecord)
            }
        }
    }

    private suspend fun mergeReminders(
        scope: ActiveFamilyScope,
        remoteReminders: List<SyncReminder>,
    ) {
        val localReminders = local.getReminders(scope).associateBy { it.id }
        remoteReminders.forEach { remoteReminder ->
            val localReminder = localReminders[remoteReminder.id]
            if (localReminder == null || !localReminder.pendingSync || remoteReminder.updatedAt >= localReminder.updatedAt) {
                local.upsertSyncedReminder(scope, remoteReminder)
            }
        }
    }
}
