package com.asensiodev.carbura.feature.garage.data

import com.asensiodev.carbura.core.domain.VehicleRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle

class InMemoryVehicleRepository : VehicleRepository {
    private val vehicles = mutableListOf<Vehicle>()

    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> =
        vehicles.filter { it.familyId == familyId }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        vehicles += vehicle
    }
}
