package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway

internal class NotificationOutboxReconciler(
    private val database: CarburaDatabase,
    private val familyScope: ActiveFamilyScopeGateway = SqlDelightActiveFamilyScopeGateway(database),
) {
    private val outbox = SqlDelightNotificationOutbox(database)

    fun reconcileExistingReminders(scope: ActiveFamilyScope) {
        familyScope.requireCurrent(scope)
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries.selectAllReminders(scope.familyId.value).executeAsList().forEach { row ->
                if (database.carburaDatabaseQueries.selectNotificationRevision(scope.familyId.value, row.id).executeAsOneOrNull() != null) {
                    return@forEach
                }
                val reminder = row.toReminder()
                if (row.deletedAt == null && row.isCompleted == 0L && reminder.dueDate != null) {
                    outbox.recordSchedule(scope, manualReminderNotificationPlan(reminder))
                } else {
                    outbox.recordCancel(scope, ReminderId(row.id))
                }
            }
        }
    }
}
