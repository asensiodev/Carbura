package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.VehicleId

internal class FakeMaintenanceRecordRepository : MaintenanceRecordRepository {
    val savedRecords = mutableListOf<MaintenanceRecord>()
    var failDeletes = false

    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) {
        savedRecords.removeAll { it.id == record.id }
        savedRecords += record
    }

    override suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord> =
        savedRecords.filter { it.vehicleId == vehicleId }

    override suspend fun deleteMaintenanceRecord(recordId: MaintenanceRecordId) {
        if (failDeletes) error("maintenance delete failed")
        savedRecords.removeAll { it.id == recordId }
    }
}
