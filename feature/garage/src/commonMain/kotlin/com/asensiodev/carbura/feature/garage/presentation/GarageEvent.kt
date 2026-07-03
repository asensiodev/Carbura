package com.asensiodev.carbura.feature.garage.presentation

sealed interface GarageEvent {
    data object Started : GarageEvent
    data class NameChanged(val value: String) : GarageEvent
    data class OdometerChanged(val value: String) : GarageEvent
    data object SubmitVehicle : GarageEvent
}
