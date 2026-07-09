package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.FamilyId
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
