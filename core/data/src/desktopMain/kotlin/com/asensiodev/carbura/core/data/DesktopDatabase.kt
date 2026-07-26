package com.asensiodev.carbura.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

fun desktopDataDirectory(
    osName: String = System.getProperty("os.name").orEmpty(),
    userHome: String = System.getProperty("user.home").orEmpty(),
    appData: String? = System.getenv("APPDATA"),
    xdgDataHome: String? = System.getenv("XDG_DATA_HOME"),
): Path {
    val home = Path.of(userHome)
    return when {
        osName.contains("mac", ignoreCase = true) -> home.resolve("Library/Application Support/Carbura")
        osName.contains("win", ignoreCase = true) ->
            Path.of(appData?.takeIf(String::isNotBlank) ?: home.resolve("AppData/Roaming").toString()).resolve("Carbura")
        else -> Path.of(xdgDataHome?.takeIf(String::isNotBlank) ?: home.resolve(".local/share").toString()).resolve("Carbura")
    }
}

fun desktopDatabasePath(dataDirectory: Path = desktopDataDirectory()): Path = dataDirectory.resolve(DESKTOP_DATABASE_NAME)

internal fun createDesktopSqlDriver(dataDirectory: Path = desktopDataDirectory()): SqlDriver {
    Files.createDirectories(dataDirectory)
    val databaseFile = desktopDatabasePath(dataDirectory)
    val requiresSchema = Files.notExists(databaseFile)
    val databaseUrl = "jdbc:sqlite:${databaseFile.toAbsolutePath()}"
    val existingVersion = if (requiresSchema) null else desktopSchemaVersion(databaseUrl)
    if (existingVersion != null && existingVersion < CarburaDatabase.Schema.version) {
        val backup = databaseFile.resolveSibling("$DESKTOP_DATABASE_NAME.v$existingVersion.backup")
        if (Files.notExists(backup)) Files.copy(databaseFile, backup, StandardCopyOption.COPY_ATTRIBUTES)
    }
    val driver = JdbcSqliteDriver(databaseUrl)
    try {
        when {
            requiresSchema -> CarburaDatabase.Schema.create(driver)
            existingVersion != null && existingVersion < CarburaDatabase.Schema.version ->
                CarburaDatabase.Schema.migrate(driver, existingVersion, CarburaDatabase.Schema.version)
            existingVersion != null && existingVersion > CarburaDatabase.Schema.version ->
                error("Desktop database schema is newer than this Carbura build")
        }
        driver.execute(null, "PRAGMA user_version = ${CarburaDatabase.Schema.version}", 0)
        return driver
    } catch (error: Exception) {
        driver.close()
        throw error
    }
}

private fun desktopSchemaVersion(databaseUrl: String): Long =
    DriverManager.getConnection(databaseUrl).use { connection ->
        val recordedVersion = connection.queryLong("PRAGMA user_version")
        if (recordedVersion > 0) return@use recordedVersion
        when {
            connection.hasTable("activeFamilyScope") -> 7
            connection.hasColumn("maintenanceRecords", "maintenanceTypeLabel") -> 6
            connection.hasColumn("vehicles", "nextItvDate") -> 5
            connection.hasTable("desiredNotificationOutbox") -> 4
            connection.hasTable("reminders") -> 3
            connection.hasColumn("vehicles", "pendingSync") -> 2
            connection.hasTable("vehicles") -> 1
            else -> error("Cannot identify existing Desktop database schema")
        }
    }

private fun Connection.queryLong(sql: String): Long =
    createStatement().use { statement ->
        statement.executeQuery(sql).use { result ->
            check(result.next())
            result.getLong(1)
        }
    }

private fun Connection.hasTable(table: String): Boolean =
    prepareStatement("SELECT EXISTS(SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?)").use { statement ->
        statement.setString(1, table)
        statement.executeQuery().use { result -> result.next() && result.getBoolean(1) }
    }

private fun Connection.hasColumn(
    table: String,
    column: String,
): Boolean =
    createStatement().use { statement ->
        statement.executeQuery("PRAGMA table_info($table)").use { it.containsColumn(column) }
    }

private fun ResultSet.containsColumn(column: String): Boolean {
    while (next()) {
        if (getString("name") == column) return true
    }
    return false
}

private const val DESKTOP_DATABASE_NAME = "carbura.db"
