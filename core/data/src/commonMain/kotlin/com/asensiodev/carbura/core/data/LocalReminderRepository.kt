package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.NoOpNotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway

class LocalReminderRepository(
    private val database: CarburaDatabase,
    private val notificationRecovery: NotificationOutboxRecovery = NoOpNotificationOutboxRecovery,
    private val familyScope: ActiveFamilyScopeGateway = SqlDelightActiveFamilyScopeGateway(database),
) : ReminderRepository {
    private val notificationOutbox = SqlDelightNotificationOutbox(database)
    override suspend fun getPendingReminders(scope: ActiveFamilyScope): List<Reminder> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectPendingRemindersByFamily(scope.familyId.value)
            .executeAsList()
            .map { it.toReminder() }

    override suspend fun getRemindersByVehicle(scope: ActiveFamilyScope, vehicleId: VehicleId): List<Reminder> =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectSyncRemindersByVehicle(scope.familyId.value, vehicleId.value)
            .executeAsList()
            .map { it.toReminder() }

    override suspend fun getActiveReminder(scope: ActiveFamilyScope, reminderId: ReminderId): Reminder? =
        database.carburaDatabaseQueries.also { familyScope.requireCurrent(scope) }
            .selectActiveReminderById(scope.familyId.value, reminderId.value)
            .executeAsOneOrNull()
            ?.toReminder()

    override suspend fun saveReminder(scope: ActiveFamilyScope, reminder: Reminder) {
        requireScope(scope, reminder.familyId)
        val now = currentTimeMillis()
        saveReminder(reminder, now)
    }

    override suspend fun saveReminderWithNotification(
        scope: ActiveFamilyScope,
        reminder: Reminder,
        notificationPlan: ReminderNotificationPlan?,
    ) {
        requireScope(scope, reminder.familyId)
        database.carburaDatabaseQueries.transaction {
            saveReminder(reminder, currentTimeMillis())
            if (notificationPlan != null) {
                notificationOutbox.recordSchedule(scope, notificationPlan)
            } else {
                notificationOutbox.recordCancel(scope, reminder.id)
            }
        }
        notificationRecovery.request()
    }

    private fun saveReminder(
        reminder: Reminder,
        now: Long,
    ) {
        database.carburaDatabaseQueries.upsertReminder(
            id = reminder.id.value,
            familyId = reminder.familyId.value,
            vehicleId = reminder.vehicleId.value,
            maintenanceTypeId = reminder.maintenanceTypeId?.value,
            title = reminder.title,
            dueDate = reminder.dueDate?.iso8601,
            dueOdometerKm = reminder.dueOdometerKm?.toLong(),
            notifyDaysBefore = reminder.notifyDaysBefore.toLong(),
            isCompleted = if (reminder.isCompleted) 1 else 0,
            updatedAt = now,
            pendingSync = 1,
            deletedAt = null,
        )
    }

    override suspend fun markReminderCompleted(scope: ActiveFamilyScope, reminderId: ReminderId) {
        familyScope.requireCurrent(scope)
        database.carburaDatabaseQueries.markReminderCompleted(
            updatedAt = currentTimeMillis(),
            id = reminderId.value,
            familyId = scope.familyId.value,
        )
    }

    override suspend fun markReminderCompletedWithNotification(scope: ActiveFamilyScope, reminderId: ReminderId) {
        familyScope.requireCurrent(scope)
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries.markReminderCompleted(
                updatedAt = currentTimeMillis(),
                id = reminderId.value,
                familyId = scope.familyId.value,
            )
            notificationOutbox.recordCancel(scope, reminderId)
        }
        notificationRecovery.request()
    }

    override suspend fun deleteReminder(scope: ActiveFamilyScope, reminderId: ReminderId) {
        familyScope.requireCurrent(scope)
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.deleteReminder(
            deletedAt = now,
            updatedAt = now,
            id = reminderId.value,
            familyId = scope.familyId.value,
        )
    }

    override suspend fun deleteReminderWithNotification(scope: ActiveFamilyScope, reminderId: ReminderId) {
        familyScope.requireCurrent(scope)
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries.deleteReminder(
                deletedAt = now,
                updatedAt = now,
                id = reminderId.value,
                familyId = scope.familyId.value,
            )
            notificationOutbox.recordCancel(scope, reminderId)
        }
        notificationRecovery.request()
    }

    private fun requireScope(scope: ActiveFamilyScope, familyId: FamilyId) {
        familyScope.requireCurrent(scope)
        require(scope.familyId == familyId)
    }
}
