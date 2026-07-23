package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.sync.LocalDataDecision
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.VehicleType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LocalDataAdoptionTest {
    @Test
    fun snapshotDigestChangesWithVersionDeletionOwnershipAndPendingState() =
        withDatabase { database ->
            insertVehicle(database, "vehicle", LOCAL, updatedAt = 1, pending = 0)
            val gateway = SqlDelightLocalDataAdoptionGateway(database)
            val original = gateway.unresolvedSnapshot()

            database.carburaDatabaseQueries.upsertVehicle(
                "vehicle",
                LOCAL,
                "Vehicle",
                "Car",
                null,
                null,
                null,
                1,
                null,
                null,
                null,
                2,
                1,
                3,
            )

            val changed = gateway.unresolvedSnapshot()
            assertNotNull(original)
            assertNotNull(changed)
            assertNotEquals(original.digest, changed.digest)
            assertEquals(1, changed.counts.vehicles)
        }

    @Test
    fun decisionsAreScopedByUserFamilyAndSnapshot() =
        withDatabase { database ->
            insertVehicle(database, "vehicle", LOCAL)
            val gateway = SqlDelightLocalDataAdoptionGateway(database)
            val digest = gateway.unresolvedSnapshot()!!.digest

            gateway.exclude(USER, FAMILY, digest)

            assertEquals(LocalDataDecision.Exclude, gateway.decision(USER, FAMILY, digest))
            assertNull(gateway.decision(UserId("other-user"), FAMILY, digest))
            assertNull(gateway.decision(USER, FamilyId("other-family"), digest))
            assertNull(gateway.decision(USER, FAMILY, "other-snapshot"))
        }

    @Test
    fun cancelIsANoOp() =
        withDatabase { database ->
            insertVehicle(database, "vehicle", LOCAL)
            val gateway = SqlDelightLocalDataAdoptionGateway(database)
            val snapshot = gateway.unresolvedSnapshot()!!

            gateway.cancel()

            assertEquals(snapshot, gateway.unresolvedSnapshot())
            assertNull(gateway.decision(USER, FAMILY, snapshot.digest))
        }

    @Test
    fun importNamespacesIdsAndPreservesRelationshipsTombstonesAndPendingFlags() =
        withDatabase { database ->
            insertVehicle(database, "vehicle", LOCAL)
            insertMaintenance(database, "record", LOCAL, "vehicle", deletedAt = 4)
            insertReminder(database, "reminder", LOCAL, "vehicle")
            database.carburaDatabaseQueries.replaceDesiredNotification(
                LOCAL,
                "reminder",
                "Schedule",
                """{"reminderId":"reminder","familyId":"$LOCAL","vehicleId":"vehicle","maintenanceTypeId":null,"title":"Reminder","dueDate":null,"dueOdometerKm":null,"notifyDaysBefore":0,"isCompleted":false,"alerts":[]}""",
                1,
            )
            val gateway = SqlDelightLocalDataAdoptionGateway(database)
            val digest = gateway.unresolvedSnapshot()!!.digest

            gateway.import(USER, FAMILY, digest)

            val vehicle = database.carburaDatabaseQueries.selectSyncVehiclesByFamily(FAMILY.value).executeAsOne()
            val record = database.carburaDatabaseQueries.selectSyncMaintenanceRecordsByFamily(FAMILY.value).executeAsOne()
            assertNotEquals("vehicle", vehicle.id)
            assertNotEquals("record", record.id)
            assertEquals(vehicle.id, record.vehicleId)
            assertEquals(4L, record.deletedAt)
            assertEquals(1L, record.pendingSync)
            val reminder = database.carburaDatabaseQueries.selectSyncRemindersByFamily(FAMILY.value).executeAsOne()
            assertNotEquals("reminder", reminder.id)
            assertEquals(vehicle.id, reminder.vehicleId)
            val outbox =
                database.carburaDatabaseQueries
                    .selectDesiredNotificationById(FAMILY.value, reminder.id)
                    .executeAsOne()
            val payload = DesiredNotificationPayloadCodec().decode(requireNotNull(outbox.payload))
            assertEquals(reminder.id, payload.reminder.id.value)
            assertEquals(FAMILY, payload.reminder.familyId)
            assertEquals(vehicle.id, payload.reminder.vehicleId.value)
            assertEquals(LocalDataDecision.Import, gateway.decision(USER, FAMILY, digest))
        }

    @Test
    fun vehicleAndChildCollisionsRemapLegacyRowsWithoutOverwritingTargetRows() =
        withDatabase { database ->
            insertVehicle(database, "vehicle", LOCAL, name = "Legacy")
            insertMaintenance(database, "record", LOCAL, "vehicle")
            insertReminder(database, "reminder", LOCAL, "vehicle")
            insertVehicle(database, "vehicle", FAMILY.value, name = "Target")
            insertMaintenance(database, "record", FAMILY.value, "vehicle")
            insertReminder(database, "reminder", FAMILY.value, "vehicle")
            val gateway = SqlDelightLocalDataAdoptionGateway(database)

            gateway.import(USER, FAMILY, gateway.unresolvedSnapshot()!!.digest)

            val vehicles = database.carburaDatabaseQueries.selectSyncVehiclesByFamily(FAMILY.value).executeAsList()
            assertEquals(2, vehicles.size)
            assertEquals("Target", vehicles.single { it.id == "vehicle" }.name)
            val legacyVehicle = vehicles.single { it.name == "Legacy" }
            assertNotEquals("vehicle", legacyVehicle.id)
            val records = database.carburaDatabaseQueries.selectSyncMaintenanceRecordsByFamily(FAMILY.value).executeAsList()
            assertEquals(2, records.size)
            assertEquals(legacyVehicle.id, records.single { it.id != "record" }.vehicleId)
            val reminders = database.carburaDatabaseQueries.selectSyncRemindersByFamily(FAMILY.value).executeAsList()
            assertEquals(2, reminders.size)
            assertEquals(legacyVehicle.id, reminders.single { it.id != "reminder" }.vehicleId)
        }

    @Test
    fun generatedReminderIdsFollowRemappedMaintenanceAndAvoidOwnCollisions() =
        withDatabase { database ->
            insertVehicle(database, "vehicle", LOCAL)
            insertMaintenance(database, "record", LOCAL, "vehicle")
            insertReminder(database, "maintenance-reminder:record", LOCAL, "vehicle")
            insertReminder(database, "planned-maintenance-reminder:record", LOCAL, "vehicle")
            insertMaintenance(database, "record", FAMILY.value, "target-vehicle")
            val gateway = SqlDelightLocalDataAdoptionGateway(database)

            gateway.import(USER, FAMILY, gateway.unresolvedSnapshot()!!.digest)

            val importedRecord =
                database.carburaDatabaseQueries.selectSyncMaintenanceRecordsByFamily(FAMILY.value).executeAsList().single {
                    it.id !=
                        "record"
                }
            val reminderIds =
                database.carburaDatabaseQueries
                    .selectSyncRemindersByFamily(FAMILY.value)
                    .executeAsList()
                    .map { it.id }
            assertTrue("maintenance-reminder:${importedRecord.id}" in reminderIds)
            assertTrue("planned-maintenance-reminder:${importedRecord.id}" in reminderIds)
        }

    @Test
    fun excludedCollisionSurvivesAuthenticatedUpsert() =
        withDatabase { database ->
            insertVehicle(database, "shared", LOCAL, name = "Excluded")
            val gateway = SqlDelightLocalDataAdoptionGateway(database)
            val digest = gateway.unresolvedSnapshot()!!.digest
            gateway.exclude(USER, FAMILY, digest)
            val scope = database.activateTestFamily(FAMILY)

            SqlDelightLocalSyncDataSource(database).upsertSyncedVehicle(
                scope,
                SyncVehicle("shared", FAMILY.value, "Remote", VehicleType.Car, null, null, null, 1, null, null, null, 5, false, null),
            )

            assertEquals(
                "Excluded",
                database.carburaDatabaseQueries
                    .selectVehicleByFamilyAndId(LOCAL, "shared")
                    .executeAsOne()
                    .name,
            )
            assertEquals(
                "Remote",
                database.carburaDatabaseQueries
                    .selectVehicleByFamilyAndId(FAMILY.value, "shared")
                    .executeAsOne()
                    .name,
            )
        }

    @Test
    fun staleImportRollsBackAndCommittedImportIsIdempotent() =
        withDatabase { database ->
            insertVehicle(database, "vehicle", LOCAL)
            val gateway = SqlDelightLocalDataAdoptionGateway(database)
            val digest = gateway.unresolvedSnapshot()!!.digest
            insertReminder(database, "changed", LOCAL, "vehicle")

            assertFailsWith<IllegalArgumentException> { gateway.import(USER, FAMILY, digest) }
            assertNotNull(database.carburaDatabaseQueries.selectVehicleByFamilyAndId(LOCAL, "vehicle").executeAsOneOrNull())
            assertNull(gateway.decision(USER, FAMILY, digest))

            val currentDigest = gateway.unresolvedSnapshot()!!.digest
            gateway.import(USER, FAMILY, currentDigest)
            gateway.import(USER, FAMILY, currentDigest)
            assertEquals(
                1,
                database.carburaDatabaseQueries
                    .selectSyncVehiclesByFamily(FAMILY.value)
                    .executeAsList()
                    .size,
            )
            assertEquals(
                1,
                database.carburaDatabaseQueries
                    .selectSyncRemindersByFamily(FAMILY.value)
                    .executeAsList()
                    .size,
            )
        }

    @Test
    fun importedRowsRemainPendingAfterFailedFirstSync() =
        withDatabase { database ->
            insertVehicle(database, "vehicle", LOCAL)
            val gateway = SqlDelightLocalDataAdoptionGateway(database)
            gateway.import(USER, FAMILY, gateway.unresolvedSnapshot()!!.digest)
            val scope = database.activateTestFamily(FAMILY)

            val pendingBeforeRetry = SqlDelightLocalSyncDataSource(database).getPendingVehicles(scope)

            assertEquals(1, pendingBeforeRetry.size)
            assertNotEquals("vehicle", pendingBeforeRetry.single().id)
            assertTrue(pendingBeforeRetry.single().pendingSync)
        }

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

    private fun insertVehicle(
        database: CarburaDatabase,
        id: String,
        family: String,
        name: String = "Vehicle",
        updatedAt: Long = 1,
        pending: Long = 1,
    ) = database.carburaDatabaseQueries.upsertVehicle(
        id,
        family,
        name,
        "Car",
        null,
        null,
        null,
        1,
        null,
        null,
        null,
        updatedAt,
        pending,
        null,
    )

    private fun insertMaintenance(
        database: CarburaDatabase,
        id: String,
        family: String,
        vehicleId: String,
        deletedAt: Long? = null,
    ) = database.carburaDatabaseQueries.upsertMaintenanceRecord(
        id,
        family,
        vehicleId,
        "type",
        "Custom",
        null,
        "2026-01-01",
        1,
        null,
        "EUR",
        null,
        null,
        null,
        1,
        1,
        deletedAt,
    )

    private fun insertReminder(
        database: CarburaDatabase,
        id: String,
        family: String,
        vehicleId: String,
    ) = database.carburaDatabaseQueries.upsertReminder(id, family, vehicleId, null, id, "2027-01-01", null, 7, 0, 1, 1, null)

    private companion object {
        const val LOCAL = "local-family"
        val USER = UserId("user")
        val FAMILY = FamilyId("family")
    }
}
