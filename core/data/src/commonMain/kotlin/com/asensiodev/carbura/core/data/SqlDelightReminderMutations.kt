package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.model.Reminder

internal class SqlDelightReminderMutations(
    private val database: CarburaDatabase,
    private val notificationOutbox: SqlDelightNotificationOutbox = SqlDelightNotificationOutbox(database),
) {
    fun apply(
        mutation: ReminderNotificationMutation,
        now: Long,
    ) {
        when (mutation) {
            is ReminderNotificationMutation.Upsert -> {
                upsert(mutation.reminder, now)
                mutation.notificationPlan?.let(notificationOutbox::recordSchedule)
                    ?: notificationOutbox.recordCancel(mutation.reminder.id)
            }
            is ReminderNotificationMutation.Delete -> {
                database.carburaDatabaseQueries.deleteReminder(
                    deletedAt = now,
                    updatedAt = now,
                    id = mutation.reminderId.value,
                )
                notificationOutbox.recordCancel(mutation.reminderId)
            }
        }
    }

    fun upsert(
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
}
