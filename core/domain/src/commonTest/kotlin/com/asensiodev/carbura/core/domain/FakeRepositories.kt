package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

internal class FakeVehicleRepository : VehicleRepository {
    val savedVehicles = mutableListOf<Vehicle>()

    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> =
        savedVehicles.filter { it.familyId == familyId }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        savedVehicles += vehicle
    }

    override suspend fun deleteVehicle(vehicleId: VehicleId) {
        savedVehicles.removeAll { it.id == vehicleId }
    }
}

internal class FakeMaintenanceRecordRepository : MaintenanceRecordRepository {
    val savedRecords = mutableListOf<MaintenanceRecord>()

    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) {
        savedRecords += record
    }

    override suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord> =
        savedRecords.filter { it.vehicleId == vehicleId }

    override suspend fun deleteMaintenanceRecord(recordId: MaintenanceRecordId) {
        savedRecords.removeAll { it.id == recordId }
    }
}

internal class FakeReminderRepository : ReminderRepository {
    val savedReminders = mutableListOf<Reminder>()

    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> =
        savedReminders.filter { it.familyId == familyId && !it.isCompleted }

    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> =
        savedReminders.filter { it.vehicleId == vehicleId }

    override suspend fun saveReminder(reminder: Reminder) {
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

internal class FakeReminderNotificationScheduler : ReminderNotificationScheduler {
    val scheduledReminderIds = mutableListOf<String>()
    val cancelledReminderIds = mutableListOf<String>()

    override suspend fun schedule(reminder: Reminder) {
        scheduledReminderIds += reminder.id.value
    }

    override suspend fun cancel(reminderId: ReminderId) {
        cancelledReminderIds += reminderId.value
    }
}
