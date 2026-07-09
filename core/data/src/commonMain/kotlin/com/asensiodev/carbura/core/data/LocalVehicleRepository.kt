package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.data.local.Vehicles
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType

class LocalVehicleRepository(
    private val database: CarburaDatabase,
) : VehicleRepository {
    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> =
        database.carburaDatabaseQueries
            .selectVehiclesByFamily(familyId.value)
            .executeAsList()
            .map { it.toVehicle() }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.upsertVehicle(
            id = vehicle.id.value,
            familyId = vehicle.familyId.value,
            name = vehicle.name,
            type = vehicle.type.name,
            brand = vehicle.brand,
            model = vehicle.model,
            licensePlate = vehicle.licensePlate,
            currentOdometerKm = vehicle.currentOdometerKm.toLong(),
            updatedAt = now,
            pendingSync = 1,
            deletedAt = null,
        )
    }

    override suspend fun deleteVehicle(vehicleId: VehicleId) {
        val now = currentTimeMillis()
        database.carburaDatabaseQueries.transaction {
            database.carburaDatabaseQueries.deleteMaintenanceRecordsByVehicle(
                deletedAt = now,
                updatedAt = now,
                vehicleId = vehicleId.value,
            )
            database.carburaDatabaseQueries.deleteRemindersByVehicle(
                deletedAt = now,
                updatedAt = now,
                vehicleId = vehicleId.value,
            )
            database.carburaDatabaseQueries.deleteVehicle(
                deletedAt = now,
                updatedAt = now,
                id = vehicleId.value,
            )
        }
    }
}

private fun Vehicles.toVehicle(): Vehicle = Vehicle(
    id = VehicleId(id),
    familyId = FamilyId(familyId),
    name = name,
    type = VehicleType.valueOf(type),
    brand = brand,
    model = model,
    licensePlate = licensePlate,
    currentOdometerKm = currentOdometerKm.toInt(),
)
