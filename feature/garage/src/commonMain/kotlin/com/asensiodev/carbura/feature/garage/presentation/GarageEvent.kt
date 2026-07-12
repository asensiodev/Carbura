package com.asensiodev.carbura.feature.garage.presentation

import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType

sealed interface GarageEvent {
    data object Started : GarageEvent

    data class NameChanged(
        val value: String,
    ) : GarageEvent

    data class OdometerChanged(
        val value: String,
    ) : GarageEvent

    data class TypeSelected(
        val value: VehicleType,
    ) : GarageEvent

    data class VehicleSelected(
        val vehicleId: VehicleId,
    ) : GarageEvent

    data class DeleteVehicleConfirmed(
        val vehicleId: VehicleId,
    ) : GarageEvent

    data object SubmitVehicle : GarageEvent
}
