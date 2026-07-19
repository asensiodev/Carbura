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
    fun migrationFromVersionFourPreservesRemindersAndCreatesEmptyOutbox() =
        runTest {
            val databaseFile = File.createTempFile("carbura-migration", ".db")
            val driver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
            try {
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

                CarburaDatabase.Schema.migrate(driver, oldVersion = 4, newVersion = 5)
                val database = CarburaDatabase(driver)

                val reminder = database.carburaDatabaseQueries.selectSyncRemindersByFamily("family-1").executeAsOne()
                assertEquals("reminder-1", reminder.id)
                assertEquals("2026-09-01", reminder.dueDate)
                assertEquals(1234L, reminder.updatedAt)
                assertEquals(1L, reminder.pendingSync)
                assertTrue(
                    database.carburaDatabaseQueries
                        .selectDesiredNotifications()
                        .executeAsList()
                        .isEmpty(),
                )
                assertEquals(null, database.carburaDatabaseQueries.selectNotificationRevision("reminder-1").executeAsOneOrNull())

                database.carburaDatabaseQueries.replaceNotificationRevision("reminder-1", 1)
                database.carburaDatabaseQueries.replaceDesiredNotification("reminder-1", "Cancel", null, 1)
                assertEquals(
                    "Cancel",
                    database.carburaDatabaseQueries
                        .selectDesiredNotifications()
                        .executeAsOne()
                        .action,
                )
            } finally {
                driver.close()
                databaseFile.delete()
            }
        }
}
