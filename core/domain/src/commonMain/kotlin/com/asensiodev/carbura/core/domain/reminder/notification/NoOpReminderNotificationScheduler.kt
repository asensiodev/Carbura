package com.asensiodev.carbura.core.domain.reminder.notification

import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId

object NoOpReminderNotificationScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(reminder: Reminder) = Unit
    override suspend fun cancel(reminderId: ReminderId) = Unit
}
