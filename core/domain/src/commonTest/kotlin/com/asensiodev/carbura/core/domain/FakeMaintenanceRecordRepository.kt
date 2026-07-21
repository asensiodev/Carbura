package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.VehicleId

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
