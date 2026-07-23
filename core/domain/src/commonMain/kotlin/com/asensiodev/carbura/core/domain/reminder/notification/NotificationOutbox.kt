package com.asensiodev.carbura.core.domain.reminder.notification

import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.ReminderId

interface NotificationOutbox {
    suspend fun pending(scope: ActiveFamilyScope): List<DesiredNotification>

    suspend fun acknowledge(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
        revision: NotificationRevision,
    )
}
