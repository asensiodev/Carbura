package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotification
import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotificationAction
import com.asensiodev.carbura.core.domain.reminder.notification.DesiredNotificationPayload
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutbox
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationRevision
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway

internal class SqlDelightNotificationOutbox(
    private val database: CarburaDatabase,
    private val payloadCodec: DesiredNotificationPayloadCodec = DesiredNotificationPayloadCodec(),
    private val familyScope: ActiveFamilyScopeGateway = SqlDelightActiveFamilyScopeGateway(database),
) : NotificationOutbox {
    override suspend fun pending(scope: ActiveFamilyScope): List<DesiredNotification> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectDesiredNotifications(scope.familyId.value).executeAsList().map { row ->
            val action = DesiredNotificationAction.valueOf(row.action)
            DesiredNotification(
                reminderId = ReminderId(row.reminderId),
                action = action,
                payload = row.payload?.let(payloadCodec::decode),
                revision = NotificationRevision(row.revision),
            )
        }

    override suspend fun acknowledge(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
        revision: NotificationRevision,
    ) {
        familyScope.requireCurrent(scope)
        database.carburaDatabaseQueries.acknowledgeDesiredNotification(scope.familyId.value, reminderId.value, revision.value)
    }

    fun recordSchedule(scope: ActiveFamilyScope, plan: ReminderNotificationPlan) {
        replace(
            scope = scope,
            reminderId = plan.reminder.id,
            action = DesiredNotificationAction.Schedule,
            payload = payloadCodec.encode(DesiredNotificationPayload(plan.reminder, plan.alerts)),
        )
    }

    fun recordCancel(scope: ActiveFamilyScope, reminderId: ReminderId) {
        replace(scope, reminderId, DesiredNotificationAction.Cancel, payload = null)
    }

    private fun replace(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
        action: DesiredNotificationAction,
        payload: String?,
    ) {
        database.carburaDatabaseQueries.transaction {
            familyScope.requireCurrent(scope)
            val current = database.carburaDatabaseQueries.selectDesiredNotificationById(scope.familyId.value, reminderId.value).executeAsOneOrNull()
            if (current?.action == action.name && current.payload == payload) return@transaction
            val revision =
                database.carburaDatabaseQueries
                    .selectNotificationRevision(scope.familyId.value, reminderId.value)
                    .executeAsOneOrNull()
                    ?.plus(1L)
                    ?: 1L
            database.carburaDatabaseQueries.replaceNotificationRevision(scope.familyId.value, reminderId.value, revision)
            database.carburaDatabaseQueries.replaceDesiredNotification(
                familyId = scope.familyId.value,
                reminderId = reminderId.value,
                action = action.name,
                payload = payload,
                revision = revision,
            )
        }
    }
}
