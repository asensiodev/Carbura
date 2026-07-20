package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.auth.AuthSession
import com.asensiodev.carbura.core.domain.auth.AuthUser
import com.asensiodev.carbura.core.domain.sync.SyncResult
import com.asensiodev.carbura.core.domain.user.RemoteUserProfile
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.VehicleType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalFirstSyncManagerTest {
    private val familyId = FamilyId("family-1")

    @Test
    fun syncPushesPendingLocalVehiclesAndMarksThemSynced() =
        runTest {
            val localVehicle = vehicle(id = "vehicle-1", updatedAt = 20, pendingSync = true)
            val local = FakeLocalSyncDataSource(vehicles = mutableListOf(localVehicle))
            val remote = FakeRemoteSyncDataSource()
            val syncManager = syncManager(local, remote)

            val result = syncManager.syncNow()

            assertIs<SyncResult.Success>(result)
            assertEquals(listOf(localVehicle), remote.vehicles)
            assertEquals(false, local.vehicles.single().pendingSync)
        }

    @Test
    fun syncPushesEditedVehicleWithSameIdentityAndClearsPendingState() =
        runTest {
            val editedVehicle =
                vehicle(
                    id = "vehicle-1",
                    name = "Edited vehicle",
                    odometerKm = 15000,
                    updatedAt = 20,
                    pendingSync = true,
                )
            val local = FakeLocalSyncDataSource(vehicles = mutableListOf(editedVehicle))
            val remote =
                FakeRemoteSyncDataSource(
                    vehicles =
                        mutableListOf(
                            vehicle(
                                id = "vehicle-1",
                                name = "Original vehicle",
                                odometerKm = 12000,
                                updatedAt = 10,
                                pendingSync = false,
                            ),
                        ),
                )

            val result = syncManager(local, remote).syncNow()

            assertIs<SyncResult.Success>(result)
            assertEquals("vehicle-1", remote.vehicles.single().id)
            assertEquals("Edited vehicle", remote.vehicles.single().name)
            assertEquals(15000, remote.vehicles.single().currentOdometerKm)
            assertFalse(local.vehicles.single().pendingSync)
        }

    @Test
    fun syncPushesVehiclePlanningFieldsAndPullCanClearThem() =
        runTest {
            val localVehicle =
                vehicle(
                    id = "vehicle-1",
                    nextItvDate = "2027-05-10",
                    insuranceRenewalDate = "2027-01-20",
                    nextServiceOdometerKm = 25000,
                    updatedAt = 20,
                    pendingSync = true,
                )
            val local = FakeLocalSyncDataSource(vehicles = mutableListOf(localVehicle))
            val remote = FakeRemoteSyncDataSource()
            val manager = syncManager(local, remote)

            manager.syncNow()
            assertEquals("2027-05-10", remote.vehicles.single().nextItvDate)
            assertEquals(25000, remote.vehicles.single().nextServiceOdometerKm)

            remote.vehicles[0] =
                remote.vehicles.single().copy(
                    nextItvDate = null,
                    insuranceRenewalDate = null,
                    nextServiceOdometerKm = null,
                    updatedAt = 30,
                )
            manager.syncNow()

            assertEquals(null, local.vehicles.single().nextItvDate)
            assertEquals(null, local.vehicles.single().insuranceRenewalDate)
            assertEquals(null, local.vehicles.single().nextServiceOdometerKm)
        }

    @Test
    fun syncKeepsNewerPendingLocalRecordOverOlderRemote() =
        runTest {
            val localVehicle = vehicle(id = "vehicle-1", updatedAt = 20, pendingSync = true)
            val remoteVehicle = vehicle(id = "vehicle-1", updatedAt = 10, pendingSync = false)
            val local = FakeLocalSyncDataSource(vehicles = mutableListOf(localVehicle))
            val remote = FakeRemoteSyncDataSource(vehicles = mutableListOf(remoteVehicle))
            val syncManager = syncManager(local, remote)

            syncManager.syncNow()

            assertEquals(20, remote.vehicles.single().updatedAt)
            assertEquals(false, local.vehicles.single().pendingSync)
        }

    @Test
    fun syncAppliesNewerRemoteRecordOverPendingLocalRecord() =
        runTest {
            val localVehicle = vehicle(id = "vehicle-1", name = "Local", updatedAt = 10, pendingSync = true)
            val remoteVehicle = vehicle(id = "vehicle-1", name = "Remote", updatedAt = 20, pendingSync = false)
            val local = FakeLocalSyncDataSource(vehicles = mutableListOf(localVehicle))
            val remote = FakeRemoteSyncDataSource(vehicles = mutableListOf(remoteVehicle))
            val syncManager = syncManager(local, remote)

            syncManager.syncNow()

            assertEquals("Remote", local.vehicles.single().name)
            assertEquals(false, local.vehicles.single().pendingSync)
        }

    @Test
    fun syncFailurePreservesPendingLocalData() =
        runTest {
            val localVehicle = vehicle(id = "vehicle-1", updatedAt = 20, pendingSync = true)
            val local = FakeLocalSyncDataSource(vehicles = mutableListOf(localVehicle))
            val remote = FakeRemoteSyncDataSource(shouldFail = true)
            val syncManager = syncManager(local, remote)

            val result = syncManager.syncNow()

            assertIs<SyncResult.Failure>(result)
            assertTrue(local.vehicles.single().pendingSync)
        }

    @Test
    fun silentSyncFailureKeepsDiagnosticWithoutRequestingGlobalFeedback() =
        runTest {
            val syncManager = syncManager(FakeLocalSyncDataSource(), FakeRemoteSyncDataSource(shouldFail = true))

            val result = syncManager.syncNowSilently()

            assertIs<SyncResult.Failure>(result)
            assertTrue(
                syncManager.status.value.lastErrorMessage
                    ?.isNotBlank() == true,
            )
            assertNull(syncManager.status.value.failureId)
        }

    @Test
    fun acknowledgedFailureRetainsDiagnosticWithoutReplayingFeedback() =
        runTest {
            val syncManager = syncManager(FakeLocalSyncDataSource(), FakeRemoteSyncDataSource(shouldFail = true))
            syncManager.syncNow()
            val failureId = requireNotNull(syncManager.status.value.failureId)

            syncManager.acknowledgeFailure(failureId)

            assertEquals(failureId, syncManager.status.value.acknowledgedFailureId)
            assertTrue(
                syncManager.status.value.lastErrorMessage
                    ?.isNotBlank() == true,
            )
        }

    @Test
    fun staleAcknowledgementDoesNotConsumeNewerFailure() =
        runTest {
            val syncManager = syncManager(FakeLocalSyncDataSource(), FakeRemoteSyncDataSource(shouldFail = true))
            syncManager.syncNow()
            val firstFailureId = requireNotNull(syncManager.status.value.failureId)
            syncManager.syncNow()
            val secondFailureId = requireNotNull(syncManager.status.value.failureId)

            syncManager.acknowledgeFailure(firstFailureId)

            assertTrue(secondFailureId != firstFailureId)
            assertNull(syncManager.status.value.acknowledgedFailureId)
        }

    @Test
    fun cancelledRetryRetainsPreviousFailureDiagnosticAndFeedback() =
        runTest {
            val remote = FakeRemoteSyncDataSource(shouldFail = true)
            val syncManager = syncManager(FakeLocalSyncDataSource(), remote)
            syncManager.syncNow()
            val failureStatus = syncManager.status.value
            remote.shouldFail = false
            remote.shouldCancel = true

            assertFailsWith<CancellationException> { syncManager.syncNow() }

            assertEquals(failureStatus.lastErrorMessage, syncManager.status.value.lastErrorMessage)
            assertEquals(failureStatus.failureId, syncManager.status.value.failureId)
            assertEquals(failureStatus.acknowledgedFailureId, syncManager.status.value.acknowledgedFailureId)
        }

    @Test
    fun cancelledSyncDoesNotReportFailure() =
        runTest {
            val syncManager = syncManager(FakeLocalSyncDataSource(), FakeRemoteSyncDataSource(shouldCancel = true))

            assertFailsWith<CancellationException> { syncManager.syncNow() }

            assertFalse(syncManager.status.value.isSyncing)
            assertNull(syncManager.status.value.lastErrorMessage)
        }

    @Test
    fun cancellingActiveSyncReleasesMutexAndAllowsRetry() =
        runTest {
            val remote = FakeRemoteSyncDataSource(blockFirstVehicleRead = true)
            val syncManager = syncManager(FakeLocalSyncDataSource(), remote)
            val activeSync = launch { syncManager.syncNow() }
            remote.firstVehicleReadStarted.await()

            activeSync.cancelAndJoin()

            assertFalse(syncManager.status.value.isSyncing)
            assertNull(syncManager.status.value.lastErrorMessage)
            assertIs<SyncResult.Success>(syncManager.syncNow())
        }

    @Test
    fun cancellingMutexWaiterDoesNotCancelActiveSyncOrBlockLaterCallers() =
        runTest {
            val remote = FakeRemoteSyncDataSource(blockFirstVehicleRead = true)
            val syncManager = syncManager(FakeLocalSyncDataSource(), remote)
            var activeResult: SyncResult? = null
            val activeSync = launch { activeResult = syncManager.syncNow() }
            remote.firstVehicleReadStarted.await()
            val waitingSync = launch { syncManager.syncNow() }
            yield()

            waitingSync.cancelAndJoin()

            assertTrue(activeSync.isActive)
            assertTrue(syncManager.status.value.isSyncing)
            remote.releaseFirstVehicleRead.complete(Unit)
            activeSync.join()
            assertIs<SyncResult.Success>(activeResult)
            assertIs<SyncResult.Success>(syncManager.syncNow())
        }

    @Test
    fun concurrentSyncRequestsDoNotOverlapRemoteWork() =
        runTest {
            val remote = FakeRemoteSyncDataSource(blockFirstVehicleRead = true)
            val syncManager = syncManager(FakeLocalSyncDataSource(), remote)
            val results = mutableListOf<SyncResult>()
            val first = launch { results += syncManager.syncNow() }
            remote.firstVehicleReadStarted.await()
            val second = launch { results += syncManager.syncNow() }
            yield()

            assertEquals(1, remote.vehicleReadCalls)
            remote.releaseFirstVehicleRead.complete(Unit)
            joinAll(first, second)

            assertEquals(2, results.size)
            assertTrue(results.all { it is SyncResult.Success })
        }

    @Test
    fun syncRetryClearsPreviouslyPendingLocalVehicle() =
        runTest {
            val localVehicle = vehicle(id = "vehicle-1", updatedAt = 20, pendingSync = true)
            val local = FakeLocalSyncDataSource(vehicles = mutableListOf(localVehicle))
            val remote = FakeRemoteSyncDataSource(shouldFail = true)
            val syncManager = syncManager(local, remote)

            assertIs<SyncResult.Failure>(syncManager.syncNow())
            remote.shouldFail = false
            val result = syncManager.syncNow()

            assertIs<SyncResult.Success>(result)
            assertEquals(listOf(localVehicle), remote.vehicles)
            assertFalse(local.vehicles.single().pendingSync)
        }

    @Test
    fun syncFailurePreservesPendingDeletedVehicleTombstone() =
        runTest {
            val deletedVehicle = vehicle(id = "vehicle-1", updatedAt = 20, pendingSync = true, deletedAt = 20)
            val local = FakeLocalSyncDataSource(vehicles = mutableListOf(deletedVehicle))
            val remote = FakeRemoteSyncDataSource(shouldFail = true)
            val syncManager = syncManager(local, remote)

            val result = syncManager.syncNow()

            assertIs<SyncResult.Failure>(result)
            assertEquals(20, local.vehicles.single().deletedAt)
            assertTrue(local.vehicles.single().pendingSync)
        }

    @Test
    fun syncPushesDeletedVehicleTombstoneAndMarksItSynced() =
        runTest {
            val deletedVehicle = vehicle(id = "vehicle-1", updatedAt = 20, pendingSync = true, deletedAt = 20)
            val local = FakeLocalSyncDataSource(vehicles = mutableListOf(deletedVehicle))
            val remote = FakeRemoteSyncDataSource()
            val syncManager = syncManager(local, remote)

            val result = syncManager.syncNow()

            assertIs<SyncResult.Success>(result)
            assertEquals(20, remote.vehicles.single().deletedAt)
            assertFalse(local.vehicles.single().pendingSync)
        }

    @Test
    fun syncPullsRemoteFamilyDataIntoEmptyLocalStore() =
        runTest {
            val remoteVehicle = vehicle(id = "vehicle-1", updatedAt = 20, pendingSync = false)
            val remoteMaintenance = maintenanceRecord(id = "maintenance-1", vehicleId = remoteVehicle.id, updatedAt = 21)
            val remoteReminder = reminder(id = "reminder-1", vehicleId = remoteVehicle.id, updatedAt = 22)
            val local = FakeLocalSyncDataSource()
            val remote =
                FakeRemoteSyncDataSource(
                    vehicles = mutableListOf(remoteVehicle),
                    maintenanceRecords = mutableListOf(remoteMaintenance),
                    reminders = mutableListOf(remoteReminder),
                )
            val syncManager = syncManager(local, remote)

            val result = syncManager.syncNow()

            assertIs<SyncResult.Success>(result)
            assertEquals(listOf(remoteVehicle), local.vehicles)
            assertEquals(listOf(remoteMaintenance), local.maintenanceRecords)
            assertEquals(listOf(remoteReminder), local.reminders)
        }

    private fun syncManager(
        local: LocalSyncDataSource,
        remote: RemoteSyncDataSource,
    ): LocalFirstSyncManager =
        LocalFirstSyncManager(
            authGateway = FakeAuthGateway(),
            profileGateway = FakeRemoteUserProfileGateway(familyId),
            local = local,
            remote = remote,
        )

    private fun vehicle(
        id: String,
        name: String = "Vehicle",
        odometerKm: Int = 1,
        nextItvDate: String? = null,
        insuranceRenewalDate: String? = null,
        nextServiceOdometerKm: Int? = null,
        updatedAt: Long,
        pendingSync: Boolean,
        deletedAt: Long? = null,
    ): SyncVehicle =
        SyncVehicle(
            id = id,
            familyId = familyId.value,
            name = name,
            type = VehicleType.Car,
            brand = null,
            model = null,
            licensePlate = null,
            currentOdometerKm = odometerKm,
            nextItvDate = nextItvDate,
            insuranceRenewalDate = insuranceRenewalDate,
            nextServiceOdometerKm = nextServiceOdometerKm,
            updatedAt = updatedAt,
            pendingSync = pendingSync,
            deletedAt = deletedAt,
        )

    private fun maintenanceRecord(
        id: String,
        vehicleId: String,
        updatedAt: Long,
        pendingSync: Boolean = false,
    ): SyncMaintenanceRecord =
        SyncMaintenanceRecord(
            id = id,
            familyId = familyId.value,
            vehicleId = vehicleId,
            maintenanceTypeId = "type-$id",
            maintenanceTypeCode = "Custom",
            performedOn = "2026-07-01",
            odometerKm = 1,
            costCents = null,
            currency = "EUR",
            workshop = null,
            notes = null,
            nextDueDate = null,
            updatedAt = updatedAt,
            pendingSync = pendingSync,
            deletedAt = null,
        )

    private fun reminder(
        id: String,
        vehicleId: String,
        updatedAt: Long,
        pendingSync: Boolean = false,
    ): SyncReminder =
        SyncReminder(
            id = id,
            familyId = familyId.value,
            vehicleId = vehicleId,
            maintenanceTypeId = null,
            title = "Reminder",
            dueDate = "2026-08-01",
            dueOdometerKm = null,
            notifyDaysBefore = 7,
            isCompleted = false,
            updatedAt = updatedAt,
            pendingSync = pendingSync,
            deletedAt = null,
        )
}

private class FakeAuthGateway : AuthGateway {
    override suspend fun currentSession(): AuthSession =
        AuthSession(
            accessToken = "token",
            user = AuthUser(id = "user-1", email = null, displayName = null),
        )

    override suspend fun signInWithGoogle(): AuthSession = currentSession()

    override suspend fun signInWithGoogle(idToken: String): AuthSession = currentSession()

    override suspend fun signOut() = Unit

    override suspend fun deleteAccount() = Unit
}

private class FakeRemoteUserProfileGateway(
    private val familyId: FamilyId,
) : RemoteUserProfileGateway {
    override suspend fun getProfileForUser(userId: UserId): RemoteUserProfile =
        RemoteUserProfile(
            userId = userId,
            familyId = familyId,
            familyName = "Family",
            displayName = "User",
            email = null,
        )

    override suspend fun ensureProfile(
        displayName: String,
        email: String?,
    ): RemoteUserProfile = getProfileForUser(UserId("user-1"))
}

private class FakeLocalSyncDataSource(
    val vehicles: MutableList<SyncVehicle> = mutableListOf(),
    val maintenanceRecords: MutableList<SyncMaintenanceRecord> = mutableListOf(),
    val reminders: MutableList<SyncReminder> = mutableListOf(),
) : LocalSyncDataSource {
    override suspend fun getPendingVehicles(): List<SyncVehicle> = vehicles.filter { it.pendingSync }

    override suspend fun getPendingMaintenanceRecords(): List<SyncMaintenanceRecord> = maintenanceRecords.filter { it.pendingSync }

    override suspend fun getPendingReminders(): List<SyncReminder> = reminders.filter { it.pendingSync }

    override suspend fun getVehicles(familyId: FamilyId): List<SyncVehicle> = vehicles.filter { it.familyId == familyId.value }

    override suspend fun getMaintenanceRecords(familyId: FamilyId): List<SyncMaintenanceRecord> =
        maintenanceRecords.filter { it.familyId == familyId.value }

    override suspend fun getReminders(familyId: FamilyId): List<SyncReminder> = reminders.filter { it.familyId == familyId.value }

    override suspend fun upsertSyncedVehicle(vehicle: SyncVehicle) {
        vehicles.removeAll { it.id == vehicle.id }
        vehicles += vehicle.copy(pendingSync = false)
    }

    override suspend fun upsertSyncedMaintenanceRecord(record: SyncMaintenanceRecord) {
        maintenanceRecords.removeAll { it.id == record.id }
        maintenanceRecords += record.copy(pendingSync = false)
    }

    override suspend fun upsertSyncedReminder(reminder: SyncReminder) {
        reminders.removeAll { it.id == reminder.id }
        reminders += reminder.copy(pendingSync = false)
    }

    override suspend fun markVehicleSynced(id: String) {
        val index = vehicles.indexOfFirst { it.id == id }
        vehicles[index] = vehicles[index].copy(pendingSync = false)
    }

    override suspend fun markMaintenanceRecordSynced(id: String) {
        val index = maintenanceRecords.indexOfFirst { it.id == id }
        maintenanceRecords[index] = maintenanceRecords[index].copy(pendingSync = false)
    }

    override suspend fun markReminderSynced(id: String) {
        val index = reminders.indexOfFirst { it.id == id }
        reminders[index] = reminders[index].copy(pendingSync = false)
    }

    override suspend fun adoptLegacyLocalFamily(familyId: FamilyId) {
        vehicles.replaceAll { vehicle ->
            if (vehicle.familyId == "local-family") {
                vehicle.copy(familyId = familyId.value, pendingSync = true)
            } else {
                vehicle
            }
        }
    }
}

private class FakeRemoteSyncDataSource(
    val vehicles: MutableList<SyncVehicle> = mutableListOf(),
    val maintenanceRecords: MutableList<SyncMaintenanceRecord> = mutableListOf(),
    val reminders: MutableList<SyncReminder> = mutableListOf(),
    var shouldFail: Boolean = false,
    var shouldCancel: Boolean = false,
    private val blockFirstVehicleRead: Boolean = false,
) : RemoteSyncDataSource {
    val firstVehicleReadStarted = CompletableDeferred<Unit>()
    val releaseFirstVehicleRead = CompletableDeferred<Unit>()
    var vehicleReadCalls: Int = 0
        private set

    override suspend fun upsertVehicles(vehicles: List<SyncVehicle>) {
        failIfNeeded()
        vehicles.forEach { vehicle ->
            this.vehicles.removeAll { it.id == vehicle.id }
            this.vehicles += vehicle
        }
    }

    override suspend fun upsertMaintenanceRecords(records: List<SyncMaintenanceRecord>) {
        failIfNeeded()
        records.forEach { record ->
            maintenanceRecords.removeAll { it.id == record.id }
            maintenanceRecords += record
        }
    }

    override suspend fun upsertReminders(reminders: List<SyncReminder>) {
        failIfNeeded()
        reminders.forEach { reminder ->
            this.reminders.removeAll { it.id == reminder.id }
            this.reminders += reminder
        }
    }

    override suspend fun getVehicles(familyId: FamilyId): List<SyncVehicle> {
        vehicleReadCalls += 1
        if (blockFirstVehicleRead && vehicleReadCalls == 1) {
            firstVehicleReadStarted.complete(Unit)
            releaseFirstVehicleRead.await()
        }
        failIfNeeded()
        return vehicles.filter { it.familyId == familyId.value }
    }

    override suspend fun getMaintenanceRecords(familyId: FamilyId): List<SyncMaintenanceRecord> {
        failIfNeeded()
        return maintenanceRecords.filter { it.familyId == familyId.value }
    }

    override suspend fun getReminders(familyId: FamilyId): List<SyncReminder> {
        failIfNeeded()
        return reminders.filter { it.familyId == familyId.value }
    }

    private fun failIfNeeded() {
        if (shouldCancel) throw CancellationException("sync cancelled")
        if (shouldFail) error("remote failed")
    }
}
