package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesktopDatabaseTest {
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
    }

    @Test
    fun firstRunCreatesDirectoryAndExistingDatabaseReopens() {
        val root = Files.createTempDirectory("carbura-desktop")
        val dataDirectory = root.resolve("nested/Carbura")
        val firstDriver = createDesktopSqlDriver(dataDirectory)
        try {
            val database = CarburaDatabase(firstDriver)
            database.carburaDatabaseQueries.replaceNotificationRevision("reminder", 3)
        } finally {
            firstDriver.close()
        }

        assertTrue(dataDirectory.resolve("carbura.db").exists())

        val reopenedDriver = createDesktopSqlDriver(dataDirectory)
        try {
            val revision =
                CarburaDatabase(reopenedDriver)
                    .carburaDatabaseQueries
                    .selectNotificationRevision("reminder")
                    .executeAsOne()
            assertEquals(3L, revision)
        } finally {
            reopenedDriver.close()
            root.toFile().deleteRecursively()
        }
    }
}
