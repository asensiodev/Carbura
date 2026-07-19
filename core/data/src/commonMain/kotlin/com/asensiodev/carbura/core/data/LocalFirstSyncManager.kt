package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.sync.SyncResult
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class LocalFirstSyncManager(
    private val authGateway: AuthGateway,
    private val profileGateway: RemoteUserProfileGateway,
    private val local: LocalSyncDataSource,
    private val remote: RemoteSyncDataSource,
) : SyncManager {
    private val mutex = Mutex()
    private val _status = MutableStateFlow(SyncStatus())

    override val status: StateFlow<SyncStatus> = _status

    override suspend fun syncNow(): SyncResult =
        mutex.withLock {
            _status.update { it.copy(isSyncing = true, lastErrorMessage = null) }
            try {
                val syncedAt = syncActiveFamily()
                _status.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncedAtMillis = syncedAt,
                        lastErrorMessage = null,
                    )
                }
                SyncResult.Success(syncedAt)
            } catch (error: CancellationException) {
                _status.update { it.copy(isSyncing = false, lastErrorMessage = null) }
                throw error
            } catch (error: Throwable) {
                val message = error.message ?: error::class.simpleName ?: "Sync failed"
                _status.update { it.copy(isSyncing = false, lastErrorMessage = message) }
                SyncResult.Failure(message)
            }
        }

    private suspend fun syncActiveFamily(): Long {
        val familyId = resolveFamilyId()
        local.adoptLegacyLocalFamily(familyId)
        pushPendingChanges(familyId)
        pullAndMerge(familyId)
        return currentTimeMillis()
    }

    private suspend fun resolveFamilyId(): FamilyId {
        val session = authGateway.currentSession() ?: error("No active session")
        return profileGateway.getProfileForUser(UserId(session.user.id))?.familyId
            ?: error("No active family")
    }

    private suspend fun pushPendingChanges(familyId: FamilyId) {
        val remoteVehicles = remote.getVehicles(familyId).associateBy { it.id }
        val pendingVehicles = local.getPendingVehicles().filter { it.familyId == familyId.value }
        val vehiclesToPush =
            pendingVehicles.filter { localVehicle ->
                val remoteVehicle = remoteVehicles[localVehicle.id]
                remoteVehicle == null || localVehicle.updatedAt >= remoteVehicle.updatedAt
            }
        remote.upsertVehicles(vehiclesToPush)
        vehiclesToPush.forEach { local.markVehicleSynced(it.id) }

        val remoteMaintenance = remote.getMaintenanceRecords(familyId).associateBy { it.id }
        val pendingMaintenance = local.getPendingMaintenanceRecords().filter { it.familyId == familyId.value }
        val maintenanceToPush =
            pendingMaintenance.filter { localRecord ->
                val remoteRecord = remoteMaintenance[localRecord.id]
                remoteRecord == null || localRecord.updatedAt >= remoteRecord.updatedAt
            }
        remote.upsertMaintenanceRecords(maintenanceToPush)
        maintenanceToPush.forEach { local.markMaintenanceRecordSynced(it.id) }

        val remoteReminders = remote.getReminders(familyId).associateBy { it.id }
        val pendingReminders = local.getPendingReminders().filter { it.familyId == familyId.value }
        val remindersToPush =
            pendingReminders.filter { localReminder ->
                val remoteReminder = remoteReminders[localReminder.id]
                remoteReminder == null || localReminder.updatedAt >= remoteReminder.updatedAt
            }
        remote.upsertReminders(remindersToPush)
        remindersToPush.forEach { local.markReminderSynced(it.id) }
    }

    private suspend fun pullAndMerge(familyId: FamilyId) {
        mergeVehicles(familyId, remote.getVehicles(familyId))
        mergeMaintenanceRecords(familyId, remote.getMaintenanceRecords(familyId))
        mergeReminders(familyId, remote.getReminders(familyId))
    }

    private suspend fun mergeVehicles(
        familyId: FamilyId,
        remoteVehicles: List<SyncVehicle>,
    ) {
        val localVehicles = local.getVehicles(familyId).associateBy { it.id }
        remoteVehicles.forEach { remoteVehicle ->
            val localVehicle = localVehicles[remoteVehicle.id]
            if (localVehicle == null || !localVehicle.pendingSync || remoteVehicle.updatedAt >= localVehicle.updatedAt) {
                local.upsertSyncedVehicle(remoteVehicle)
            }
        }
    }

    private suspend fun mergeMaintenanceRecords(
        familyId: FamilyId,
        remoteRecords: List<SyncMaintenanceRecord>,
    ) {
        val localRecords = local.getMaintenanceRecords(familyId).associateBy { it.id }
        remoteRecords.forEach { remoteRecord ->
            val localRecord = localRecords[remoteRecord.id]
            if (localRecord == null || !localRecord.pendingSync || remoteRecord.updatedAt >= localRecord.updatedAt) {
                local.upsertSyncedMaintenanceRecord(remoteRecord)
            }
        }
    }

    private suspend fun mergeReminders(
        familyId: FamilyId,
        remoteReminders: List<SyncReminder>,
    ) {
        val localReminders = local.getReminders(familyId).associateBy { it.id }
        remoteReminders.forEach { remoteReminder ->
            val localReminder = localReminders[remoteReminder.id]
            if (localReminder == null || !localReminder.pendingSync || remoteReminder.updatedAt >= localReminder.updatedAt) {
                local.upsertSyncedReminder(remoteReminder)
            }
        }
    }
}
