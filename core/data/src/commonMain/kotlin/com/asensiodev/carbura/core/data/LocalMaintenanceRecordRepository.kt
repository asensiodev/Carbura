package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.VehicleId

class LocalMaintenanceRecordRepository(
    private val database: CarburaDatabase,
) : MaintenanceRecordRepository {
    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.upsertMaintenanceRecord(
            id = record.id.value,
            familyId = record.familyId.value,
            vehicleId = record.vehicleId.value,
            maintenanceTypeId = record.maintenanceTypeId.value,
            maintenanceTypeCode = record.maintenanceTypeCode?.name,
            performedOn = record.performedOn.iso8601,
            odometerKm = record.odometerKm?.toLong(),
            costCents = record.costCents?.toLong(),
            currency = record.currency,
            workshop = record.workshop,
            notes = record.notes,
            nextDueDate = record.nextDueDate?.iso8601,
            updatedAt = now,
            pendingSync = 1,
            deletedAt = null,
        )
    }

    override suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord> =
        database.carburaDatabaseQueries
            .selectMaintenanceRecordsByVehicle(vehicleId.value)
            .executeAsList()
            .map { it.toMaintenanceRecord() }

    override suspend fun deleteMaintenanceRecord(recordId: MaintenanceRecordId) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.deleteMaintenanceRecord(
            deletedAt = now,
            updatedAt = now,
            id = recordId.value,
        )
    }
}
