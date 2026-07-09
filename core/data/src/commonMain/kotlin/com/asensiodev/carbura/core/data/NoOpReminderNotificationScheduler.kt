package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.domain.ReminderNotificationScheduler
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId

internal class NoOpReminderNotificationScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(reminder: Reminder) = Unit
    override suspend fun cancel(reminderId: ReminderId) = Unit
}
