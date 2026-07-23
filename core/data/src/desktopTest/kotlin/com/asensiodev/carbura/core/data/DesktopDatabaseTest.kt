package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import java.nio.file.Files
import java.sql.DriverManager
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopDatabaseTest {
    @Test
    fun existingUnversionedVersionFiveDatabaseMigratesWithoutDeletingData() {
        val directory = Files.createTempDirectory("carbura-desktop-legacy")
        val databaseFile = directory.resolve("carbura.db")
        DriverManager.getConnection("jdbc:sqlite:${databaseFile.toAbsolutePath()}").use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    "CREATE TABLE vehicles (id TEXT NOT NULL PRIMARY KEY, familyId TEXT NOT NULL, name TEXT NOT NULL, type TEXT NOT NULL, brand TEXT, model TEXT, licensePlate TEXT, currentOdometerKm INTEGER NOT NULL, nextItvDate TEXT, insuranceRenewalDate TEXT, nextServiceOdometerKm INTEGER, updatedAt INTEGER NOT NULL DEFAULT 0, pendingSync INTEGER NOT NULL DEFAULT 0, deletedAt INTEGER)",
                )
                statement.execute(
                    "CREATE TABLE maintenanceRecords (id TEXT NOT NULL PRIMARY KEY, familyId TEXT NOT NULL, vehicleId TEXT NOT NULL, maintenanceTypeId TEXT NOT NULL, maintenanceTypeCode TEXT, performedOn TEXT NOT NULL, odometerKm INTEGER, costCents INTEGER, currency TEXT NOT NULL, workshop TEXT, notes TEXT, nextDueDate TEXT, updatedAt INTEGER NOT NULL DEFAULT 0, pendingSync INTEGER NOT NULL DEFAULT 0, deletedAt INTEGER)",
                )
                statement.execute(
                    "CREATE TABLE reminders (id TEXT NOT NULL PRIMARY KEY, familyId TEXT NOT NULL, vehicleId TEXT NOT NULL, maintenanceTypeId TEXT, title TEXT NOT NULL, dueDate TEXT, dueOdometerKm INTEGER, notifyDaysBefore INTEGER NOT NULL, isCompleted INTEGER NOT NULL DEFAULT 0, updatedAt INTEGER NOT NULL DEFAULT 0, pendingSync INTEGER NOT NULL DEFAULT 0, deletedAt INTEGER)",
                )
                statement.execute(
                    "CREATE TABLE desiredNotificationOutbox (reminderId TEXT NOT NULL PRIMARY KEY, action TEXT NOT NULL, payload TEXT, revision INTEGER NOT NULL)",
                )
                statement.execute(
                    "CREATE TABLE notificationRevisions (reminderId TEXT NOT NULL PRIMARY KEY, revision INTEGER NOT NULL)",
                )
                statement.execute(
                    "INSERT INTO vehicles VALUES ('vehicle', 'local-family', 'Legacy', 'Car', NULL, NULL, NULL, 1, NULL, NULL, NULL, 1, 1, NULL)",
                )
            }
        }

        val driver = createDesktopSqlDriver(directory)
        try {
            val queries = CarburaDatabase(driver).carburaDatabaseQueries
            assertEquals("Legacy", queries.selectVehicleByFamilyAndId("local-family", "vehicle").executeAsOne().name)
            assertEquals("local-family", queries.selectActiveFamilyScope().executeAsOne().familyId)
            assertTrue(Files.exists(directory.resolve("carbura.db.v5.backup")))
            DriverManager.getConnection("jdbc:sqlite:${databaseFile.toAbsolutePath()}").use { connection ->
                connection.createStatement().executeQuery("PRAGMA user_version").use { result ->
                    assertTrue(result.next())
                    assertEquals(CarburaDatabase.Schema.version, result.getLong(1))
                }
            }
        } finally {
            driver.close()
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun dataDirectoryUsesNativeOperatingSystemConventions() {
        assertEquals(
            "/Users/test/Library/Application Support/Carbura",
            desktopDataDirectory("Mac OS X", "/Users/test", null, null).toString(),
        )
        assertEquals(
            "C:\\Users\\test\\AppData\\Roaming/Carbura",
            desktopDataDirectory("Windows 11", "C:\\Users\\test", "C:\\Users\\test\\AppData\\Roaming", null).toString(),
        )
        assertEquals(
            "/home/test/.data/Carbura",
            desktopDataDirectory("Linux", "/home/test", null, "/home/test/.data").toString(),
        )
        assertEquals(
            "C:\\Users\\test/AppData/Roaming/Carbura",
            desktopDataDirectory("Windows 11", "C:\\Users\\test", "", null).toString(),
        )
        assertEquals(
            "/home/test/.local/share/Carbura",
            desktopDataDirectory("Linux", "/home/test", null, "").toString(),
        )
    }

    @Test
    fun databasePathUsesTheDriverFileName() {
        assertEquals(
            "/tmp/Carbura/carbura.db",
            desktopDatabasePath(
                java.nio.file.Path
                    .of("/tmp/Carbura"),
            ).toString(),
        )
    }

    @Test
    fun firstRunCreatesDirectoryAndExistingDatabaseReopens() {
        val root = Files.createTempDirectory("carbura-desktop")
        val dataDirectory = root.resolve("nested/Carbura")
        val firstDriver = createDesktopSqlDriver(dataDirectory)
        try {
            val database = CarburaDatabase(firstDriver)
            database.carburaDatabaseQueries.replaceNotificationRevision("family", "reminder", 3)
        } finally {
            firstDriver.close()
        }

        assertTrue(desktopDatabasePath(dataDirectory).exists())

        val reopenedDriver = createDesktopSqlDriver(dataDirectory)
        try {
            val revision =
                CarburaDatabase(reopenedDriver)
                    .carburaDatabaseQueries
                    .selectNotificationRevision("family", "reminder")
                    .executeAsOne()
            assertEquals(3L, revision)
        } finally {
            reopenedDriver.close()
            root.toFile().deleteRecursively()
        }
    }
}
