package com.asensiodev.carbura.core.domain.reminder.notification

import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId

enum class DesiredNotificationAction {
    Schedule,
    Cancel,
}

@JvmInline
value class NotificationRevision(
    val value: Long,
)

data class DesiredNotificationPayload(
    val reminder: Reminder,
    val alerts: List<ReminderAlert>,
) {
    fun toPlan(revision: NotificationRevision): ReminderNotificationPlan = ReminderNotificationPlan(reminder, alerts, revision)
}

data class DesiredNotification(
    val reminderId: ReminderId,
    val action: DesiredNotificationAction,
    val payload: DesiredNotificationPayload?,
    val revision: NotificationRevision,
) {
    init {
        require((action == DesiredNotificationAction.Schedule) == (payload != null)) {
            "Schedule actions require a payload and cancel actions must not have one"
        }
    }
}

sealed interface ReminderNotificationMutation {
    data class Upsert(
        val reminder: Reminder,
        val notificationPlan: ReminderNotificationPlan?,
    ) : ReminderNotificationMutation

    data class Delete(
        val reminderId: ReminderId,
    ) : ReminderNotificationMutation
}

sealed interface NotificationOutboxDrainResult {
    data object Drained : NotificationOutboxDrainResult

    data object RetryableWorkRemaining : NotificationOutboxDrainResult

    data object PermissionDenied : NotificationOutboxDrainResult

    data object NonRetryableWorkRemaining : NotificationOutboxDrainResult
}
