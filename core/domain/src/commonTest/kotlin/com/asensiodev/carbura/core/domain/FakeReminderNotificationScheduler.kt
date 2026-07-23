package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId

internal class FakeReminderNotificationScheduler : ReminderNotificationScheduler {
    val scheduledReminderIds = mutableListOf<String>()
    val cancelledReminderIds = mutableListOf<String>()
    val scheduledReminders = mutableListOf<Reminder>()
    val scheduledPlans = mutableListOf<ReminderNotificationPlan>()
    var failSchedules = false
    var failCancels = false

    override suspend fun schedule(
        scope: ActiveFamilyScope,
        plan: ReminderNotificationPlan,
    ) {
        if (failSchedules) error("schedule failed")
        scheduledReminderIds += plan.reminder.id.value
        scheduledReminders += plan.reminder
        scheduledPlans += plan
    }

    override suspend fun cancel(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) {
        if (failCancels) error("cancel failed")
        cancelledReminderIds += reminderId.value
    }
}
