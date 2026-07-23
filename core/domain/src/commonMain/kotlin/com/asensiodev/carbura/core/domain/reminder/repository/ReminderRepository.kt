package com.asensiodev.carbura.core.domain.reminder.repository

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

interface ReminderRepository {
    suspend fun getPendingReminders(scope: ActiveFamilyScope): List<Reminder>

    suspend fun getRemindersByVehicle(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ): List<Reminder>

    suspend fun getActiveReminder(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ): Reminder? = null

    suspend fun saveReminder(
        scope: ActiveFamilyScope,
        reminder: Reminder,
    )

    suspend fun saveReminderWithNotification(
        scope: ActiveFamilyScope,
        reminder: Reminder,
        notificationPlan: ReminderNotificationPlan?,
    ) = saveReminder(scope, reminder)

    suspend fun markReminderCompleted(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    )

    suspend fun markReminderCompletedWithNotification(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) = markReminderCompleted(scope, reminderId)

    suspend fun deleteReminder(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    )

    suspend fun deleteReminderWithNotification(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) = deleteReminder(scope, reminderId)
}
