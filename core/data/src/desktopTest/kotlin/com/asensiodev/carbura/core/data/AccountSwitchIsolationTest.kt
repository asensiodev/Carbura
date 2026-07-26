package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.family.StaleFamilyScopeException
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AccountSwitchIsolationTest {
    @Test
    fun switchedAccountCannotReadMutateAcknowledgeOrNotifyPreviousFamily() =
        withDatabase { database ->
            val scopes = SqlDelightActiveFamilyScopeGateway(database)
            val familyA = FamilyId("family-a")
            val scopeA = scopes.activateAuthenticated(UserId("user-a"), familyA)
            val vehicles = LocalVehicleRepository(database)
            val sync = SqlDelightLocalSyncDataSource(database)
            val outbox = SqlDelightNotificationOutbox(database)
            vehicles.saveVehicle(scopeA, vehicle(familyA, "Account A"))
            outbox.recordCancel(scopeA, ReminderId("reminder"))
            val notification = outbox.pending(scopeA).single()

            val familyB = FamilyId("family-b")
            val scopeB = scopes.activateAuthenticated(UserId("user-b"), familyB)

            assertTrue(vehicles.observeVehicles(scopeB).isEmpty())
            assertFailsWith<StaleFamilyScopeException> { vehicles.observeVehicles(scopeA) }
            assertFailsWith<StaleFamilyScopeException> { vehicles.deleteVehicle(scopeA, VehicleId("shared")) }
            assertFailsWith<StaleFamilyScopeException> { sync.markVehicleSynced(scopeA, "shared", 10) }
            assertFailsWith<StaleFamilyScopeException> {
                outbox.acknowledge(scopeA, notification.reminderId, notification.revision)
            }

            vehicles.saveVehicle(scopeB, vehicle(familyB, "Account B"))
            assertEquals("Account B", vehicles.observeVehicles(scopeB).single().name)
            assertEquals(
                1L,
                database.carburaDatabaseQueries
                    .selectPendingSyncVehicles(familyA.value)
                    .executeAsOne()
                    .pendingSync,
            )
            assertEquals(
                1,
                database.carburaDatabaseQueries
                    .selectDesiredNotifications(familyA.value)
                    .executeAsList()
                    .size,
            )
        }

    @Test
    fun localModeCannotAccessAuthenticatedCacheAndOriginalAccountCanReturn() =
        withDatabase { database ->
            val scopes = SqlDelightActiveFamilyScopeGateway(database)
            val family = FamilyId("family-a")
            val authenticated = scopes.activateAuthenticated(UserId("user-a"), family)
            val repository = LocalVehicleRepository(database)
            repository.saveVehicle(authenticated, vehicle(family, "Cached"))

            val local = scopes.activateLocal()

            assertTrue(repository.observeVehicles(local).isEmpty())
            assertFailsWith<StaleFamilyScopeException> { repository.observeVehicles(authenticated) }
            val restored = scopes.activateAuthenticated(UserId("user-a"), family)
            assertEquals("Cached", repository.observeVehicles(restored).single().name)
        }

    @Test
    fun acknowledgementClearsOnlyTheUploadedSqlDelightVersion() =
        withDatabase { database ->
            val scopes = SqlDelightActiveFamilyScopeGateway(database)
            val family = FamilyId("family-a")
            val scope = scopes.activateAuthenticated(UserId("user-a"), family)
            LocalVehicleRepository(database).saveVehicle(scope, vehicle(family, "Pending"))
            val sync = SqlDelightLocalSyncDataSource(database)
            val pending = database.carburaDatabaseQueries.selectPendingSyncVehicles(family.value).executeAsOne()

            sync.markVehicleSynced(scope, pending.id, pending.updatedAt - 1)
            assertEquals(
                1L,
                database.carburaDatabaseQueries
                    .selectPendingSyncVehicles(family.value)
                    .executeAsOne()
                    .pendingSync,
            )

            sync.markVehicleSynced(scope, pending.id, pending.updatedAt)
            assertTrue(
                database.carburaDatabaseQueries
                    .selectPendingSyncVehicles(family.value)
                    .executeAsList()
                    .isEmpty(),
            )
        }

    private fun vehicle(
        familyId: FamilyId,
        name: String,
    ) = Vehicle(VehicleId("shared"), familyId, name, VehicleType.Car, currentOdometerKm = 1)

    private fun withDatabase(block: suspend (CarburaDatabase) -> Unit) =
        runTest {
            val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
            try {
                CarburaDatabase.Schema.create(driver)
                block(CarburaDatabase(driver))
            } finally {
                driver.close()
            }
        }
}
