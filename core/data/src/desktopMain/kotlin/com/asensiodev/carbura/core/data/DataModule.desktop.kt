package com.asensiodev.carbura.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.NoOpReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.RemoteUserProfileGateway
import com.asensiodev.carbura.core.domain.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.ReminderRepository
import com.asensiodev.carbura.core.domain.SyncManager
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
    single<ReminderNotificationScheduler> { NoOpReminderNotificationScheduler }
    single<VehicleRepository> { LocalVehicleRepository(get()) }
    single<MaintenanceRecordRepository> { LocalMaintenanceRecordRepository(get()) }
    single<ReminderRepository> { LocalReminderRepository(get()) }
    single<RemoteUserProfileGateway> { SupabaseUserProfileGateway(get()) }
    single<LocalSyncDataSource> { SqlDelightLocalSyncDataSource(get()) }
    single<RemoteSyncDataSource> { SupabaseSyncDataSource(get()) }
    single<SyncManager> { LocalFirstSyncManager(get(), get(), get(), get()) }
}
