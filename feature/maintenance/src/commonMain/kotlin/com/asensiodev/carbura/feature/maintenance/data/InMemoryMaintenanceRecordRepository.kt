package com.asensiodev.carbura.feature.maintenance.data

import com.asensiodev.carbura.core.domain.MaintenanceRecordRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.VehicleId

class InMemoryMaintenanceRecordRepository : MaintenanceRecordRepository {
    private val records = mutableListOf<MaintenanceRecord>()

    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) {
        records += record
    }

    override suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord> =
        records.filter { it.vehicleId == vehicleId }
}
