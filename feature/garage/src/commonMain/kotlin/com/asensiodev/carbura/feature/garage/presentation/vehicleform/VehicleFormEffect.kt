package com.asensiodev.carbura.feature.garage.presentation.vehicleform

import com.asensiodev.carbura.core.stringresources.CarburaString

sealed interface VehicleFormEffect {
    data class VehicleCreated(
        val vehicleName: String,
    ) : VehicleFormEffect

    data class VehicleUpdated(
        val vehicleName: String,
    ) : VehicleFormEffect

    data class ValidationFailed(
        val message: CarburaString,
    ) : VehicleFormEffect
}
