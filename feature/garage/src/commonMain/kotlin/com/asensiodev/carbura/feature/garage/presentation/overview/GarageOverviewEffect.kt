package com.asensiodev.carbura.feature.garage.presentation.overview

import com.asensiodev.carbura.core.model.VehicleId

sealed interface GarageOverviewEffect {
    data class VehicleDeleted(
        val vehicleName: String,
    ) : GarageOverviewEffect

    data class NavigateToVehicleHistory(
        val vehicleId: VehicleId,
    ) : GarageOverviewEffect
}
