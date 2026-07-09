package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId

internal class FakeReminderNotificationScheduler : ReminderNotificationScheduler {
    val scheduledReminderIds = mutableListOf<String>()
    val cancelledReminderIds = mutableListOf<String>()

    override suspend fun schedule(reminder: Reminder) {
        scheduledReminderIds += reminder.id.value
    }

    override suspend fun cancel(reminderId: ReminderId) {
        cancelledReminderIds += reminderId.value
    }
}
