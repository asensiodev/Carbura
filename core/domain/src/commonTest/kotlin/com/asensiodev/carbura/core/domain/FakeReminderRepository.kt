package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

internal class FakeReminderRepository : ReminderRepository {
    val savedReminders = mutableListOf<Reminder>()
    val deletedReminderIds = mutableListOf<ReminderId>()
    val notificationMutations = mutableListOf<ReminderNotificationMutation>()
    var failDeletes = false

    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> =
        savedReminders.filter { it.familyId == familyId && !it.isCompleted }

    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> = savedReminders.filter { it.vehicleId == vehicleId }

    override suspend fun getActiveReminder(reminderId: ReminderId): Reminder? =
        savedReminders.firstOrNull { it.id == reminderId && !it.isCompleted }

    override suspend fun saveReminder(reminder: Reminder) {
        savedReminders.removeAll { it.id == reminder.id }
        savedReminders += reminder
    }

    override suspend fun markReminderCompleted(reminderId: ReminderId) {
        val index = savedReminders.indexOfFirst { it.id == reminderId }
        if (index >= 0) {
            savedReminders[index] = savedReminders[index].copy(isCompleted = true)
        }
    }

    override suspend fun saveReminderWithNotification(
        reminder: Reminder,
        notificationPlan: ReminderNotificationPlan?,
    ) {
        saveReminder(reminder)
        notificationMutations += ReminderNotificationMutation.Upsert(reminder, notificationPlan)
    }

    override suspend fun markReminderCompletedWithNotification(reminderId: ReminderId) {
        markReminderCompleted(reminderId)
        notificationMutations += ReminderNotificationMutation.Delete(reminderId)
    }

    override suspend fun deleteReminder(reminderId: ReminderId) {
        if (failDeletes) error("reminder delete failed")
        deletedReminderIds += reminderId
        savedReminders.removeAll { it.id == reminderId }
    }

    override suspend fun deleteReminderWithNotification(reminderId: ReminderId) {
        deleteReminder(reminderId)
        notificationMutations += ReminderNotificationMutation.Delete(reminderId)
    }
}
