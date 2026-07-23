package com.asensiodev.carbura.core.domain.reminder.notification

import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.ReminderId

object NoOpReminderNotificationScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(
        scope: ActiveFamilyScope,
        plan: ReminderNotificationPlan,
    ) = Unit

    override suspend fun cancel(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) = Unit
}
