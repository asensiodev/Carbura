package com.asensiodev.carbura.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.VehicleRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataModule: Module = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<SqlDriver> {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { driver ->
            CarburaDatabase.Schema.create(driver)
        }
    }
    single { CarburaDatabase(get()) }
    single<VehicleRepository> { LocalVehicleRepository(get()) }
    single<MaintenanceRecordRepository> { LocalMaintenanceRecordRepository(get()) }
    single<RemoteUserProfileGateway> { SupabaseUserProfileGateway(get()) }
}
