package com.asensiodev.carbura.feature.garage.presentation.overview

import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

data class GarageOverviewUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val searchQuery: String = "",
    val loadState: GarageLoadState = GarageLoadState.Loading,
    val deletingVehicleId: VehicleId? = null,
    val deleteError: Boolean = false,
) {
    val isEmpty: Boolean = vehicles.isEmpty() && loadState == GarageLoadState.Loaded

    val visibleVehicles: List<Vehicle>
        get() {
            val query = searchQuery.trim()
            return if (query.isEmpty()) vehicles else vehicles.filter { it.matchesSearch(query) }
        }

    val hasNoMatchingVehicles: Boolean = vehicles.isNotEmpty() && searchQuery.isNotBlank() && visibleVehicles.isEmpty()
}

private fun Vehicle.matchesSearch(query: String): Boolean =
    name.contains(query, ignoreCase = true) ||
        licensePlate?.contains(query, ignoreCase = true) == true ||
        type.name.contains(query, ignoreCase = true)

enum class GarageLoadState { Loading, Loaded, Error }
