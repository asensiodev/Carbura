package com.asensiodev.carbura.feature.garage.presentation

import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.stringresources.CarburaString

data class GarageUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val name: String = "",
    val odometerKm: String = "0",
    val errorMessage: CarburaString? = null,
    val isLoading: Boolean = false,
) {
    val isEmpty: Boolean = vehicles.isEmpty()
}
