package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotification
import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotificationAction
import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotificationPayload
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutbox
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationRevision
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.model.ReminderId

internal class SqlDelightNotificationOutbox(
    private val database: CarburaDatabase,
    private val payloadCodec: DesiredNotificationPayloadCodec = DesiredNotificationPayloadCodec(),
) : NotificationOutbox {
    override suspend fun pending(): List<DesiredNotification> =
        database.carburaDatabaseQueries.selectDesiredNotifications().executeAsList().map { row ->
            val action = DesiredNotificationAction.valueOf(row.action)
            DesiredNotification(
                reminderId = ReminderId(row.reminderId),
                action = action,
                payload = row.payload?.let(payloadCodec::decode),
                revision = NotificationRevision(row.revision),
            )
        }

    override suspend fun acknowledge(
        reminderId: ReminderId,
        revision: NotificationRevision,
    ) {
        database.carburaDatabaseQueries.acknowledgeDesiredNotification(reminderId.value, revision.value)
    }

    fun recordSchedule(plan: ReminderNotificationPlan) {
        replace(
            reminderId = plan.reminder.id,
            action = DesiredNotificationAction.Schedule,
            payload = payloadCodec.encode(DesiredNotificationPayload(plan.reminder, plan.alerts)),
        )
    }

    fun recordCancel(reminderId: ReminderId) {
        replace(reminderId, DesiredNotificationAction.Cancel, payload = null)
    }

    private fun replace(
        reminderId: ReminderId,
        action: DesiredNotificationAction,
        payload: String?,
    ) {
        database.carburaDatabaseQueries.transaction {
            val current = database.carburaDatabaseQueries.selectDesiredNotificationById(reminderId.value).executeAsOneOrNull()
            if (current?.action == action.name && current.payload == payload) return@transaction
            val revision =
                database.carburaDatabaseQueries
                    .selectNotificationRevision(reminderId.value)
                    .executeAsOneOrNull()
                    ?.plus(1L)
                    ?: 1L
            database.carburaDatabaseQueries.replaceNotificationRevision(reminderId.value, revision)
            database.carburaDatabaseQueries.replaceDesiredNotification(
                reminderId = reminderId.value,
                action = action.name,
                payload = payload,
                revision = revision,
            )
        }
    }
}
