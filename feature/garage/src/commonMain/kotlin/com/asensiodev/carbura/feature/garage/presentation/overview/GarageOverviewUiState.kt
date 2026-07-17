package com.asensiodev.carbura.feature.garage.presentation.overview

import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

data class GarageOverviewUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val loadState: GarageLoadState = GarageLoadState.Loading,
    val deletingVehicleId: VehicleId? = null,
    val deleteError: Boolean = false,
) {
    val isEmpty: Boolean = vehicles.isEmpty() && loadState == GarageLoadState.Loaded
}

enum class GarageLoadState { Loading, Loaded, Error }
