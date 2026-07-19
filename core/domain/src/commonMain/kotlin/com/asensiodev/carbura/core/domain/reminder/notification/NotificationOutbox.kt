package com.asensiodev.carbura.core.domain.reminder.notification

import com.asensiodev.carbura.core.model.ReminderId

interface NotificationOutbox {
    suspend fun pending(): List<DesiredNotification>

    suspend fun acknowledge(
        reminderId: ReminderId,
        revision: NotificationRevision,
    )
}
