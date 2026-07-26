package com.asensiodev.carbura.core.domain.reminder.notification

import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.ReminderId

interface ReminderNotificationScheduler {
    suspend fun schedule(
        scope: ActiveFamilyScope,
        plan: ReminderNotificationPlan,
    )

    suspend fun cancel(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    )
}
