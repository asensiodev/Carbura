package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.data.local.Vehicles
import com.asensiodev.carbura.core.domain.VehicleRepository
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
        database.carburaDatabaseQueries.upsertVehicle(
            id = vehicle.id.value,
            familyId = vehicle.familyId.value,
            name = vehicle.name,
            type = vehicle.type.name,
            brand = vehicle.brand,
            model = vehicle.model,
            licensePlate = vehicle.licensePlate,
            currentOdometerKm = vehicle.currentOdometerKm.toLong(),
        )
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
