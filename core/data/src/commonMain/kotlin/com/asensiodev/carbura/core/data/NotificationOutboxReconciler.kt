package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.model.ReminderId

internal class NotificationOutboxReconciler(
    private val database: CarburaDatabase,
) {
    private val outbox = SqlDelightNotificationOutbox(database)

    fun reconcileExistingReminders() {
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries.selectAllReminders().executeAsList().forEach { row ->
                if (database.carburaDatabaseQueries.selectNotificationRevision(row.id).executeAsOneOrNull() != null) {
                    return@forEach
                }
                val reminder = row.toReminder()
                if (row.deletedAt == null && row.isCompleted == 0L && reminder.dueDate != null) {
                    outbox.recordSchedule(manualReminderNotificationPlan(reminder))
                } else {
                    outbox.recordCancel(ReminderId(row.id))
                }
            }
        }
    }
}
