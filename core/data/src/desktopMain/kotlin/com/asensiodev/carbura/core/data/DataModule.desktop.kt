package com.asensiodev.carbura.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.auth.AccountLocalDataCleaner
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.NoOpReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataModule: Module =
    module {
        single<DispatcherProvider> { DefaultDispatcherProvider() }
        single<SqlDriver> {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { driver ->
                CarburaDatabase.Schema.create(driver)
            }
        }
        single { CarburaDatabase(get()) }
        single { SyncOperationLock() }
        single<ReminderNotificationScheduler> { NoOpReminderNotificationScheduler }
        single<AccountLocalDataCleaner> { SqlDelightAccountLocalDataCleaner(get(), get(), get()) }
        single<VehicleRepository> { LocalVehicleRepository(get()) }
        single<MaintenanceRecordRepository> { LocalMaintenanceRecordRepository(get()) }
        single<ReminderRepository> { LocalReminderRepository(get()) }
        single<RemoteUserProfileGateway> { SupabaseUserProfileGateway(get()) }
        single<LocalSyncDataSource> { SqlDelightLocalSyncDataSource(get()) }
        single<RemoteSyncDataSource> { SupabaseSyncDataSource(get()) }
        single<SyncManager> { LocalFirstSyncManager(get(), get(), get(), get(), get()) }
    }
