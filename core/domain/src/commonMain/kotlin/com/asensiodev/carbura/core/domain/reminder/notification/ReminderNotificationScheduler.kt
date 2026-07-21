package com.asensiodev.carbura.core.domain.reminder.notification

import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId

interface ReminderNotificationScheduler {
    suspend fun schedule(reminder: Reminder)
    suspend fun cancel(reminderId: ReminderId)
}
