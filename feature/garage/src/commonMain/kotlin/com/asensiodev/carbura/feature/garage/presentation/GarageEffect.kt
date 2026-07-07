package com.asensiodev.carbura.feature.garage.presentation

import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.stringresources.CarburaString

sealed interface GarageEffect {
    data class VehicleCreated(val vehicleName: String) : GarageEffect
    data class VehicleDeleted(val vehicleName: String) : GarageEffect
    data class ValidationFailed(val message: CarburaString) : GarageEffect
    data class NavigateToVehicleHistory(val vehicleId: VehicleId) : GarageEffect
}
