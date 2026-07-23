package com.asensiodev.carbura.core.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.auth.AccountLocalDataCleaner
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutbox
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxProcessor
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.sync.LocalDataAdoptionGateway
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataModule: Module =
    module {
        single<DispatcherProvider> { DefaultDispatcherProvider() }
        single<SqlDriver> {
            AndroidSqliteDriver(
                schema = CarburaDatabase.Schema,
                context = get<Context>(),
                name = "carbura.db",
            )
        }
        single { CarburaDatabase(get()) }
        single { SyncOperationLock() }
        single<ActiveFamilyScopeGateway> { SqlDelightActiveFamilyScopeGateway(get()) }
        single<LocalDataAdoptionGateway> { SqlDelightLocalDataAdoptionGateway(get()) }
        single<ReminderNotificationScheduler> { AndroidReminderNotificationScheduler(get()) }
        single<NotificationOutbox> { SqlDelightNotificationOutbox(get()) }
        single { NotificationOutboxProcessor(get(), get()) }
        single { NotificationOutboxReconciler(get()) }
        single<NotificationOutboxRecovery> { AndroidNotificationOutboxRecovery(get(), get(), get(), get(), get()) }
        single<AccountLocalDataCleaner> { SqlDelightAccountLocalDataCleaner(get(), get(), get(), get()) }
        single<VehicleRepository> { LocalVehicleRepository(get(), get()) }
        single<MaintenanceRecordRepository> { LocalMaintenanceRecordRepository(get(), get()) }
        single<ReminderRepository> { LocalReminderRepository(get(), get()) }
        single<RemoteUserProfileGateway> { SupabaseUserProfileGateway(get()) }
        single<LocalSyncDataSource> { SqlDelightLocalSyncDataSource(get(), get()) }
        single<RemoteSyncDataSource> { SupabaseSyncDataSource(get()) }
        single<SyncManager> { LocalFirstSyncManager(get(), get(), get(), get(), get(), get()) }
    }
