package com.asensiodev.carbura.feature.garage.presentation.overview

import com.asensiodev.carbura.core.model.VehicleId

sealed interface GarageOverviewEvent {
    data object Started : GarageOverviewEvent

    data object Retry : GarageOverviewEvent

    data object Refresh : GarageOverviewEvent

    data class SearchQueryChanged(
        val value: String,
    ) : GarageOverviewEvent

    data object SearchCleared : GarageOverviewEvent

    data class VehicleSelected(
        val vehicleId: VehicleId,
    ) : GarageOverviewEvent

    data class DeleteVehicleConfirmed(
        val vehicleId: VehicleId,
    ) : GarageOverviewEvent
}
