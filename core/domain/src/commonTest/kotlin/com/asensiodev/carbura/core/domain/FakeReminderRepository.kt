package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

internal class FakeReminderRepository : ReminderRepository {
    val savedReminders = mutableListOf<Reminder>()

    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> =
        savedReminders.filter { it.familyId == familyId && !it.isCompleted }

    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> = savedReminders.filter { it.vehicleId == vehicleId }

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

    override suspend fun deleteReminder(reminderId: ReminderId) {
        savedReminders.removeAll { it.id == reminderId }
    }
}
