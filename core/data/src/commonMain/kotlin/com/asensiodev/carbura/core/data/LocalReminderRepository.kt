package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.NoOpNotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

class LocalReminderRepository(
    private val database: CarburaDatabase,
    private val notificationRecovery: NotificationOutboxRecovery = NoOpNotificationOutboxRecovery,
) : ReminderRepository {
    private val notificationOutbox = SqlDelightNotificationOutbox(database)
    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> =
        database.carburaDatabaseQueries
            .selectPendingRemindersByFamily(familyId.value)
            .executeAsList()
            .map { it.toReminder() }

    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> =
        database.carburaDatabaseQueries
            .selectSyncRemindersByVehicle(vehicleId.value)
            .executeAsList()
            .map { it.toReminder() }

    override suspend fun saveReminder(reminder: Reminder) {
        val now = currentTimeMillis()
        saveReminder(reminder, now)
    }

    override suspend fun saveReminderWithNotification(
        reminder: Reminder,
        notificationPlan: ReminderNotificationPlan?,
    ) {
        database.carburaDatabaseQueries.transaction {
            saveReminder(reminder, currentTimeMillis())
            if (notificationPlan != null) {
                notificationOutbox.recordSchedule(notificationPlan)
            } else {
                notificationOutbox.recordCancel(reminder.id)
            }
        }
        notificationRecovery.request()
    }

    private fun saveReminder(
        reminder: Reminder,
        now: Long,
    ) {
        database.carburaDatabaseQueries.upsertReminder(
            id = reminder.id.value,
            familyId = reminder.familyId.value,
            vehicleId = reminder.vehicleId.value,
            maintenanceTypeId = reminder.maintenanceTypeId?.value,
            title = reminder.title,
            dueDate = reminder.dueDate?.iso8601,
            dueOdometerKm = reminder.dueOdometerKm?.toLong(),
            notifyDaysBefore = reminder.notifyDaysBefore.toLong(),
            isCompleted = if (reminder.isCompleted) 1 else 0,
            updatedAt = now,
            pendingSync = 1,
            deletedAt = null,
        )
    }

    override suspend fun markReminderCompleted(reminderId: ReminderId) {
        database.carburaDatabaseQueries.markReminderCompleted(
            updatedAt = currentTimeMillis(),
            id = reminderId.value,
        )
    }

    override suspend fun markReminderCompletedWithNotification(reminderId: ReminderId) {
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries.markReminderCompleted(
                updatedAt = currentTimeMillis(),
                id = reminderId.value,
            )
            notificationOutbox.recordCancel(reminderId)
        }
        notificationRecovery.request()
    }

    override suspend fun deleteReminder(reminderId: ReminderId) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.deleteReminder(
            deletedAt = now,
            updatedAt = now,
            id = reminderId.value,
        )
    }

    override suspend fun deleteReminderWithNotification(reminderId: ReminderId) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries.deleteReminder(
                deletedAt = now,
                updatedAt = now,
                id = reminderId.value,
            )
            notificationOutbox.recordCancel(reminderId)
        }
        notificationRecovery.request()
    }
}
