package com.asensiodev.carbura.core.domain.reminder.repository

import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

interface ReminderRepository {
    suspend fun getPendingReminders(familyId: FamilyId): List<Reminder>

    suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder>

    suspend fun saveReminder(reminder: Reminder)

    suspend fun markReminderCompleted(reminderId: ReminderId)

    suspend fun deleteReminder(reminderId: ReminderId)
}
