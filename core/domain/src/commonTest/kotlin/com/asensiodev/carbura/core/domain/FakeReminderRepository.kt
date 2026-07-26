package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

internal class FakeReminderRepository : ReminderRepository {
    val savedReminders = mutableListOf<Reminder>()
    val deletedReminderIds = mutableListOf<ReminderId>()
    val notificationMutations = mutableListOf<ReminderNotificationMutation>()
    var failDeletes = false

    override suspend fun getPendingReminders(scope: ActiveFamilyScope): List<Reminder> =
        savedReminders.filter { it.familyId == scope.familyId && !it.isCompleted }

    override suspend fun getRemindersByVehicle(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ): List<Reminder> = savedReminders.filter { it.familyId == scope.familyId && it.vehicleId == vehicleId }

    override suspend fun getActiveReminder(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ): Reminder? = savedReminders.firstOrNull { it.familyId == scope.familyId && it.id == reminderId && !it.isCompleted }

    override suspend fun saveReminder(
        scope: ActiveFamilyScope,
        reminder: Reminder,
    ) {
        savedReminders.removeAll { it.id == reminder.id }
        savedReminders += reminder
    }

    override suspend fun markReminderCompleted(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) {
        val index = savedReminders.indexOfFirst { it.id == reminderId }
        if (index >= 0) {
            savedReminders[index] = savedReminders[index].copy(isCompleted = true)
        }
    }

    override suspend fun saveReminderWithNotification(
        scope: ActiveFamilyScope,
        reminder: Reminder,
        notificationPlan: ReminderNotificationPlan?,
    ) {
        saveReminder(scope, reminder)
        notificationMutations += ReminderNotificationMutation.Upsert(reminder, notificationPlan)
    }

    override suspend fun markReminderCompletedWithNotification(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) {
        markReminderCompleted(scope, reminderId)
        notificationMutations += ReminderNotificationMutation.Delete(reminderId)
    }

    override suspend fun deleteReminder(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) {
        if (failDeletes) error("reminder delete failed")
        deletedReminderIds += reminderId
        savedReminders.removeAll { it.id == reminderId }
    }

    override suspend fun deleteReminderWithNotification(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) {
        deleteReminder(scope, reminderId)
        notificationMutations += ReminderNotificationMutation.Delete(reminderId)
    }
}
