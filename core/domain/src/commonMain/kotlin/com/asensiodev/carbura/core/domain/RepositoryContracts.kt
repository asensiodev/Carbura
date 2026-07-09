package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

interface VehicleRepository {
    suspend fun observeVehicles(familyId: FamilyId): List<Vehicle>
    suspend fun saveVehicle(vehicle: Vehicle)
    suspend fun deleteVehicle(vehicleId: VehicleId)
}

interface MaintenanceRecordRepository {
    suspend fun saveMaintenanceRecord(record: MaintenanceRecord)
    suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord>
    suspend fun deleteMaintenanceRecord(recordId: MaintenanceRecordId)
}

interface ReminderRepository {
    suspend fun getPendingReminders(familyId: FamilyId): List<Reminder>
    suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder>
    suspend fun saveReminder(reminder: Reminder)
    suspend fun markReminderCompleted(reminderId: ReminderId)
    suspend fun deleteReminder(reminderId: ReminderId)
}
