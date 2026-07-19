package com.asensiodev.carbura.core.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.auth.AccountLocalDataCleaner
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.NoOpNotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.NoOpReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutbox
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxProcessor
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
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
        single<NotificationOutbox> { SqlDelightNotificationOutbox(get()) }
        single { NotificationOutboxProcessor(get(), get()) }
        single<NotificationOutboxRecovery> { NoOpNotificationOutboxRecovery }
        single<AccountLocalDataCleaner> { SqlDelightAccountLocalDataCleaner(get(), get(), get(), get()) }
        single<VehicleRepository> { LocalVehicleRepository(get(), get()) }
        single<MaintenanceRecordRepository> { LocalMaintenanceRecordRepository(get(), get()) }
        single<ReminderRepository> { LocalReminderRepository(get(), get()) }
        single<RemoteUserProfileGateway> { SupabaseUserProfileGateway(get()) }
        single<LocalSyncDataSource> { SqlDelightLocalSyncDataSource(get(), get()) }
        single<RemoteSyncDataSource> { SupabaseSyncDataSource(get()) }
        single<SyncManager> { LocalFirstSyncManager(get(), get(), get(), get(), get()) }
    }
