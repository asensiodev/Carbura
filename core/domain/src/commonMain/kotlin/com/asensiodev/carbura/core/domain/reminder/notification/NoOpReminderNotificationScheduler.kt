package com.asensiodev.carbura.core.domain.reminder.notification

import com.asensiodev.carbura.core.model.ReminderId

object NoOpReminderNotificationScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(plan: ReminderNotificationPlan) = Unit

    override suspend fun cancel(reminderId: ReminderId) = Unit
}
