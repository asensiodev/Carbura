package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId

interface ReminderNotificationScheduler {
    suspend fun schedule(reminder: Reminder)
    suspend fun cancel(reminderId: ReminderId)
}

object NoOpReminderNotificationScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(reminder: Reminder) = Unit
    override suspend fun cancel(reminderId: ReminderId) = Unit
}
