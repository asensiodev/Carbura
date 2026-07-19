package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.auth.AccountLocalDataCleaner
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.ReminderId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.withLock

internal class SqlDelightAccountLocalDataCleaner(
    private val database: CarburaDatabase,
    private val notificationScheduler: ReminderNotificationScheduler,
    private val operationLock: SyncOperationLock = SyncOperationLock(),
) : AccountLocalDataCleaner {
    override suspend fun clear(familyId: FamilyId) {
        operationLock.mutex.withLock {
            val reminderIds =
                database.carburaDatabaseQueries
                    .selectReminderIdsByFamilyForAccountCleanup(familyId.value)
                    .executeAsList()
                    .map(::ReminderId)
            var cancellation: CancellationException? = null

            reminderIds.forEach { reminderId ->
                if (cancellation != null) return@forEach
                try {
                    notificationScheduler.cancel(reminderId)
                } catch (error: CancellationException) {
                    cancellation = error
                } catch (_: Exception) {
                    // Local rows still need removal after remote account deletion.
                }
            }

            database.carburaDatabaseQueries.transaction {
                database.carburaDatabaseQueries.deleteRemindersByFamilyImmediately(familyId.value)
                database.carburaDatabaseQueries.deleteMaintenanceRecordsByFamilyImmediately(familyId.value)
                database.carburaDatabaseQueries.deleteVehiclesByFamilyImmediately(familyId.value)
            }
            cancellation?.let { throw it }
        }
    }
}
