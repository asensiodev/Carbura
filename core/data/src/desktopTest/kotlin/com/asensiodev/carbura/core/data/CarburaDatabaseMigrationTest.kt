package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CarburaDatabaseMigrationTest {
    @Test
    fun migrationFromVersionSixNamespacesRowsAndPreservesExistingData() =
        runTest {
            val databaseFile = File.createTempFile("carbura-family-scope-migration", ".db")
            val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
            try {
                driver.execute(
                    null,
                    "CREATE TABLE vehicles (id TEXT NOT NULL PRIMARY KEY, familyId TEXT NOT NULL, name TEXT NOT NULL, type TEXT NOT NULL, brand TEXT, model TEXT, licensePlate TEXT, currentOdometerKm INTEGER NOT NULL, nextItvDate TEXT, insuranceRenewalDate TEXT, nextServiceOdometerKm INTEGER, updatedAt INTEGER NOT NULL DEFAULT 0, pendingSync INTEGER NOT NULL DEFAULT 0, deletedAt INTEGER)",
                    0,
                )
                driver.execute(
                    null,
                    "CREATE TABLE maintenanceRecords (id TEXT NOT NULL PRIMARY KEY, familyId TEXT NOT NULL, vehicleId TEXT NOT NULL, maintenanceTypeId TEXT NOT NULL, maintenanceTypeCode TEXT, maintenanceTypeLabel TEXT, performedOn TEXT NOT NULL, odometerKm INTEGER, costCents INTEGER, currency TEXT NOT NULL, workshop TEXT, notes TEXT, nextDueDate TEXT, updatedAt INTEGER NOT NULL DEFAULT 0, pendingSync INTEGER NOT NULL DEFAULT 0, deletedAt INTEGER)",
                    0,
                )
                driver.execute(
                    null,
                    "CREATE TABLE reminders (id TEXT NOT NULL PRIMARY KEY, familyId TEXT NOT NULL, vehicleId TEXT NOT NULL, maintenanceTypeId TEXT, title TEXT NOT NULL, dueDate TEXT, dueOdometerKm INTEGER, notifyDaysBefore INTEGER NOT NULL, isCompleted INTEGER NOT NULL DEFAULT 0, updatedAt INTEGER NOT NULL DEFAULT 0, pendingSync INTEGER NOT NULL DEFAULT 0, deletedAt INTEGER)",
                    0,
                )
                driver.execute(
                    null,
                    "CREATE TABLE desiredNotificationOutbox (reminderId TEXT NOT NULL PRIMARY KEY, action TEXT NOT NULL, payload TEXT, revision INTEGER NOT NULL)",
                    0,
                )
                driver.execute(
                    null,
                    "CREATE TABLE notificationRevisions (reminderId TEXT NOT NULL PRIMARY KEY, revision INTEGER NOT NULL)",
                    0,
                )
                driver.execute(
                    null,
                    "INSERT INTO vehicles VALUES ('shared', 'family-1', 'Vehicle', 'Car', NULL, NULL, NULL, 1, NULL, NULL, NULL, 10, 1, NULL)",
                    0,
                )
                driver.execute(
                    null,
                    "INSERT INTO reminders VALUES ('reminder-1', 'family-1', 'shared', NULL, 'Reminder', '2027-01-01', NULL, 7, 0, 11, 1, NULL)",
                    0,
                )
                driver.execute(null, "INSERT INTO desiredNotificationOutbox VALUES ('reminder-1', 'Schedule', NULL, 2)", 0)
                driver.execute(null, "INSERT INTO notificationRevisions VALUES ('reminder-1', 2)", 0)

                CarburaDatabase.Schema.migrate(driver, oldVersion = 6, newVersion = 7)
                val queries = CarburaDatabase(driver).carburaDatabaseQueries

                assertEquals("Vehicle", queries.selectVehicleByFamilyAndId("family-1", "shared").executeAsOne().name)
                assertEquals("family-1", queries.selectDesiredNotifications("family-1").executeAsOne().familyId)
                assertEquals(2L, queries.selectNotificationRevision("family-1", "reminder-1").executeAsOne())
                assertEquals("local-family", queries.selectActiveFamilyScope().executeAsOne().familyId)
            } finally {
                driver.close()
                databaseFile.delete()
            }
        }

    @Test
    fun migrationFromVersionFivePreservesMaintenanceWithEmptyCustomLabel() =
        runTest {
            val databaseFile = File.createTempFile("carbura-maintenance-label-migration", ".db")
            val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
            try {
                driver.execute(
                    identifier = null,
                    sql =
                        """
                        CREATE TABLE maintenanceRecords (
                            id TEXT NOT NULL PRIMARY KEY, familyId TEXT NOT NULL, vehicleId TEXT NOT NULL,
                            maintenanceTypeId TEXT NOT NULL, maintenanceTypeCode TEXT, performedOn TEXT NOT NULL,
                            odometerKm INTEGER, costCents INTEGER, currency TEXT NOT NULL, workshop TEXT, notes TEXT,
                            nextDueDate TEXT, updatedAt INTEGER NOT NULL DEFAULT 0,
                            pendingSync INTEGER NOT NULL DEFAULT 0, deletedAt INTEGER
                        )
                        """.trimIndent(),
                    parameters = 0,
                )
                driver.execute(
                    identifier = null,
                    sql =
                        """
                        INSERT INTO maintenanceRecords VALUES (
                            'record-1', 'family-1', 'vehicle-1', 'type-ebike-check', 'Custom', '2026-07-20',
                            1000, NULL, 'EUR', NULL, NULL, NULL, 1234, 1, NULL
                        )
                        """.trimIndent(),
                    parameters = 0,
                )

                CarburaDatabase.Schema.migrate(driver, oldVersion = 5, newVersion = 6)
                val record =
                    CarburaDatabase(driver)
                        .carburaDatabaseQueries
                        .selectActiveMaintenanceRecord("record-1", "family-1", "vehicle-1")
                        .executeAsOne()

                assertEquals("type-ebike-check", record.maintenanceTypeId)
                assertEquals(null, record.maintenanceTypeLabel)
            } finally {
                driver.close()
                databaseFile.delete()
            }
        }

    @Test
    fun migrationFromVersionFourPreservesRemindersAndCreatesEmptyOutbox() =
        runTest {
            val databaseFile = File.createTempFile("carbura-migration", ".db")
            val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
            try {
                driver.execute(
                    null,
                    "CREATE TABLE vehicles (id TEXT NOT NULL PRIMARY KEY, familyId TEXT NOT NULL, name TEXT NOT NULL, type TEXT NOT NULL, brand TEXT, model TEXT, licensePlate TEXT, currentOdometerKm INTEGER NOT NULL, updatedAt INTEGER NOT NULL DEFAULT 0, pendingSync INTEGER NOT NULL DEFAULT 0, deletedAt INTEGER, nextItvDate TEXT, insuranceRenewalDate TEXT, nextServiceOdometerKm INTEGER)",
                    0,
                )
                driver.execute(
                    null,
                    "CREATE TABLE maintenanceRecords (id TEXT NOT NULL PRIMARY KEY, familyId TEXT NOT NULL, vehicleId TEXT NOT NULL, maintenanceTypeId TEXT NOT NULL, maintenanceTypeCode TEXT, performedOn TEXT NOT NULL, odometerKm INTEGER, costCents INTEGER, currency TEXT NOT NULL, workshop TEXT, notes TEXT, nextDueDate TEXT, updatedAt INTEGER NOT NULL DEFAULT 0, pendingSync INTEGER NOT NULL DEFAULT 0, deletedAt INTEGER)",
                    0,
                )
                driver.execute(
                    identifier = null,
                    sql =
                        """
                        CREATE TABLE reminders (
                            id TEXT NOT NULL PRIMARY KEY,
                            familyId TEXT NOT NULL,
                            vehicleId TEXT NOT NULL,
                            maintenanceTypeId TEXT,
                            title TEXT NOT NULL,
                            dueDate TEXT,
                            dueOdometerKm INTEGER,
                            notifyDaysBefore INTEGER NOT NULL,
                            isCompleted INTEGER NOT NULL DEFAULT 0,
                            updatedAt INTEGER NOT NULL DEFAULT 0,
                            pendingSync INTEGER NOT NULL DEFAULT 0,
                            deletedAt INTEGER
                        )
                        """.trimIndent(),
                    parameters = 0,
                )
                driver.execute(
                    identifier = null,
                    sql =
                        """
                        INSERT INTO reminders VALUES (
                            'reminder-1', 'family-1', 'vehicle-1', NULL, 'ITV',
                            '2026-09-01', NULL, 30, 0, 1234, 1, NULL
                        )
                        """.trimIndent(),
                    parameters = 0,
                )

                CarburaDatabase.Schema.migrate(driver, oldVersion = 4, newVersion = 7)
                val database = CarburaDatabase(driver)

                val reminder = database.carburaDatabaseQueries.selectSyncRemindersByFamily("family-1").executeAsOne()
                assertEquals("reminder-1", reminder.id)
                assertEquals("2026-09-01", reminder.dueDate)
                assertEquals(1234L, reminder.updatedAt)
                assertEquals(1L, reminder.pendingSync)
                assertTrue(
                    database.carburaDatabaseQueries
                        .selectDesiredNotifications("family-1")
                        .executeAsList()
                        .isEmpty(),
                )
                assertEquals(
                    null,
                    database.carburaDatabaseQueries.selectNotificationRevision("family-1", "reminder-1").executeAsOneOrNull(),
                )

                database.carburaDatabaseQueries.replaceNotificationRevision("family-1", "reminder-1", 1)
                database.carburaDatabaseQueries.replaceDesiredNotification("family-1", "reminder-1", "Cancel", null, 1)
                assertEquals(
                    "Cancel",
                    database.carburaDatabaseQueries
                        .selectDesiredNotifications("family-1")
                        .executeAsOne()
                        .action,
                )
            } finally {
                driver.close()
                databaseFile.delete()
            }
        }
}
