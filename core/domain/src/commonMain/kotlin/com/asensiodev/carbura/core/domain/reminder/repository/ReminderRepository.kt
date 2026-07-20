package com.asensiodev.carbura.core.domain.reminder.repository

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

interface ReminderRepository {
    suspend fun getPendingReminders(familyId: FamilyId): List<Reminder>

    suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder>

    suspend fun getActiveReminder(reminderId: ReminderId): Reminder? = null

    suspend fun saveReminder(reminder: Reminder)

    suspend fun saveReminderWithNotification(
        reminder: Reminder,
        notificationPlan: ReminderNotificationPlan?,
    ) {
        saveReminder(reminder)
    }

    suspend fun markReminderCompleted(reminderId: ReminderId)

    suspend fun markReminderCompletedWithNotification(reminderId: ReminderId) {
        markReminderCompleted(reminderId)
    }

    suspend fun deleteReminder(reminderId: ReminderId)

    suspend fun deleteReminderWithNotification(reminderId: ReminderId) {
        deleteReminder(reminderId)
    }
}
