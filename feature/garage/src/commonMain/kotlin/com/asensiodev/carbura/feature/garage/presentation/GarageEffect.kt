package com.asensiodev.carbura.feature.garage.presentation

sealed interface GarageEffect {
    data class VehicleCreated(val vehicleName: String) : GarageEffect
    data class ValidationFailed(val message: String) : GarageEffect
}
