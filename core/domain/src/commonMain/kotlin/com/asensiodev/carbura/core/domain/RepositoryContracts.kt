package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle

interface VehicleRepository {
    suspend fun observeVehicles(familyId: FamilyId): List<Vehicle>
}
