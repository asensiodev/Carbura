package com.asensiodev.carbura.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import java.nio.file.Files
import java.nio.file.Path

internal fun desktopDataDirectory(
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

internal fun createDesktopSqlDriver(dataDirectory: Path = desktopDataDirectory()): SqlDriver {
    Files.createDirectories(dataDirectory)
    val databaseFile = dataDirectory.resolve(DESKTOP_DATABASE_NAME)
    val requiresSchema = Files.notExists(databaseFile)
    return JdbcSqliteDriver("jdbc:sqlite:${databaseFile.toAbsolutePath()}").also { driver ->
        if (requiresSchema) CarburaDatabase.Schema.create(driver)
    }
}

private const val DESKTOP_DATABASE_NAME = "carbura.db"
