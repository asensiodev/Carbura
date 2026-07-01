package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

interface VehicleRepository {
    suspend fun observeVehicles(familyId: FamilyId): List<Vehicle>
    suspend fun saveVehicle(vehicle: Vehicle)
}

interface MaintenanceRecordRepository {
    suspend fun saveMaintenanceRecord(record: MaintenanceRecord)
    suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord>
}

interface ReminderRepository {
    suspend fun saveReminder(reminder: Reminder)
}
