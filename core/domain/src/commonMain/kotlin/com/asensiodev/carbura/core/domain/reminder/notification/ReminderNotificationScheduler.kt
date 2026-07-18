package com.asensiodev.carbura.core.domain.reminder.notification

import com.asensiodev.carbura.core.model.ReminderId

interface ReminderNotificationScheduler {
    suspend fun schedule(plan: ReminderNotificationPlan)

    suspend fun cancel(reminderId: ReminderId)
}
