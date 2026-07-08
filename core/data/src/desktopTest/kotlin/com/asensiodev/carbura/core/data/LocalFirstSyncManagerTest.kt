package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.auth.AuthGateway
import com.asensiodev.carbura.core.auth.AuthSession
import com.asensiodev.carbura.core.auth.AuthUser
import com.asensiodev.carbura.core.domain.SyncResult
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.VehicleType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LocalFirstSyncManagerTest {
    private val familyId = FamilyId("family-1")

    @Test
    fun syncPushesPendingLocalVehiclesAndMarksThemSynced() = runTest {
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
    fun syncKeepsNewerPendingLocalRecordOverOlderRemote() = runTest {
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
    fun syncAppliesNewerRemoteRecordOverPendingLocalRecord() = runTest {
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
    fun syncFailurePreservesPendingLocalData() = runTest {
        val localVehicle = vehicle(id = "vehicle-1", updatedAt = 20, pendingSync = true)
        val local = FakeLocalSyncDataSource(vehicles = mutableListOf(localVehicle))
        val remote = FakeRemoteSyncDataSource(shouldFail = true)
        val syncManager = syncManager(local, remote)

        val result = syncManager.syncNow()

        assertIs<SyncResult.Failure>(result)
        assertTrue(local.vehicles.single().pendingSync)
    }

    private fun syncManager(
        local: LocalSyncDataSource,
        remote: RemoteSyncDataSource,
    ): LocalFirstSyncManager = LocalFirstSyncManager(
        authGateway = FakeAuthGateway(),
        profileGateway = FakeRemoteUserProfileGateway(familyId),
        local = local,
        remote = remote,
    )

    private fun vehicle(
        id: String,
        name: String = "Vehicle",
        updatedAt: Long,
        pendingSync: Boolean,
    ): SyncVehicle = SyncVehicle(
        id = id,
        familyId = familyId.value,
        name = name,
        type = VehicleType.Car,
        brand = null,
        model = null,
        licensePlate = null,
        currentOdometerKm = 1,
        updatedAt = updatedAt,
        pendingSync = pendingSync,
        deletedAt = null,
    )
}

private class FakeAuthGateway : AuthGateway {
    override suspend fun currentSession(): AuthSession = AuthSession(
        accessToken = "token",
        user = AuthUser(id = "user-1", email = null, displayName = null),
    )

    override suspend fun signInWithGoogle(): AuthSession = currentSession()
    override suspend fun signInWithGoogle(idToken: String): AuthSession = currentSession()
    override suspend fun signOut() = Unit
}

private class FakeRemoteUserProfileGateway(
    private val familyId: FamilyId,
) : RemoteUserProfileGateway {
    override suspend fun getProfileForUser(userId: UserId): RemoteUserProfile = RemoteUserProfile(
        userId = userId,
        familyId = familyId,
        familyName = "Family",
        displayName = "User",
        email = null,
    )

    override suspend fun ensureProfile(displayName: String, email: String?): RemoteUserProfile =
        getProfileForUser(UserId("user-1"))
}

private class FakeLocalSyncDataSource(
    val vehicles: MutableList<SyncVehicle> = mutableListOf(),
) : LocalSyncDataSource {
    override suspend fun getPendingVehicles(): List<SyncVehicle> = vehicles.filter { it.pendingSync }
    override suspend fun getPendingMaintenanceRecords(): List<SyncMaintenanceRecord> = emptyList()
    override suspend fun getPendingReminders(): List<SyncReminder> = emptyList()
    override suspend fun getVehicles(familyId: FamilyId): List<SyncVehicle> = vehicles.filter { it.familyId == familyId.value }
    override suspend fun getMaintenanceRecords(familyId: FamilyId): List<SyncMaintenanceRecord> = emptyList()
    override suspend fun getReminders(familyId: FamilyId): List<SyncReminder> = emptyList()

    override suspend fun upsertSyncedVehicle(vehicle: SyncVehicle) {
        vehicles.removeAll { it.id == vehicle.id }
        vehicles += vehicle.copy(pendingSync = false)
    }

    override suspend fun upsertSyncedMaintenanceRecord(record: SyncMaintenanceRecord) = Unit
    override suspend fun upsertSyncedReminder(reminder: SyncReminder) = Unit

    override suspend fun markVehicleSynced(id: String) {
        val index = vehicles.indexOfFirst { it.id == id }
        vehicles[index] = vehicles[index].copy(pendingSync = false)
    }

    override suspend fun markMaintenanceRecordSynced(id: String) = Unit
    override suspend fun markReminderSynced(id: String) = Unit
}

private class FakeRemoteSyncDataSource(
    val vehicles: MutableList<SyncVehicle> = mutableListOf(),
    private val shouldFail: Boolean = false,
) : RemoteSyncDataSource {
    override suspend fun upsertVehicles(vehicles: List<SyncVehicle>) {
        failIfNeeded()
        vehicles.forEach { vehicle ->
            this.vehicles.removeAll { it.id == vehicle.id }
            this.vehicles += vehicle
        }
    }

    override suspend fun upsertMaintenanceRecords(records: List<SyncMaintenanceRecord>) {
        failIfNeeded()
    }

    override suspend fun upsertReminders(reminders: List<SyncReminder>) {
        failIfNeeded()
    }

    override suspend fun getVehicles(familyId: FamilyId): List<SyncVehicle> {
        failIfNeeded()
        return vehicles.filter { it.familyId == familyId.value }
    }

    override suspend fun getMaintenanceRecords(familyId: FamilyId): List<SyncMaintenanceRecord> {
        failIfNeeded()
        return emptyList()
    }

    override suspend fun getReminders(familyId: FamilyId): List<SyncReminder> {
        failIfNeeded()
        return emptyList()
    }

    private fun failIfNeeded() {
        if (shouldFail) error("remote failed")
    }
}
