package com.asensiodev.carbura.core.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.VehicleRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataModule: Module = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = CarburaDatabase.Schema,
            context = get<Context>(),
            name = "carbura.db",
        )
    }
    single { CarburaDatabase(get()) }
    single<VehicleRepository> { LocalVehicleRepository(get()) }
    single<MaintenanceRecordRepository> { LocalMaintenanceRecordRepository(get()) }
    single<RemoteUserProfileGateway> { SupabaseUserProfileGateway(get()) }
}
