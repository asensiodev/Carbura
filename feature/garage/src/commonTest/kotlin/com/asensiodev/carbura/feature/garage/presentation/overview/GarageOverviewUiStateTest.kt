package com.asensiodev.carbura.feature.garage.presentation.overview

import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GarageOverviewUiStateTest {
    @Test
    fun emptyStateOnlyAppearsAfterSuccessfulEmptyLoad() {
        assertFalse(GarageOverviewUiState(loadState = GarageLoadState.Loading).isEmpty)
        assertFalse(GarageOverviewUiState(loadState = GarageLoadState.Error).isEmpty)
        assertTrue(GarageOverviewUiState(loadState = GarageLoadState.Loaded).isEmpty)
    }

    @Test
    fun blankSearchPreservesVehicleOrder() {
        val vehicles = listOf(vehicle("van", "Work van", VehicleType.Van), vehicle("car", "Family car", VehicleType.Car))

        assertEquals(vehicles, GarageOverviewUiState(vehicles = vehicles, searchQuery = "  ").visibleVehicles)
    }

    @Test
    fun searchMatchesNamePlateAndTypeCaseInsensitively() {
        val van = vehicle("van", "Work van", VehicleType.Van, "1234 ABC")
        val bike = vehicle("bike", "Weekend ride", VehicleType.Motorcycle, "MOTO 9")
        val vehicles = listOf(van, bike)

        assertEquals(listOf(van), GarageOverviewUiState(vehicles = vehicles, searchQuery = "WORK").visibleVehicles)
        assertEquals(listOf(van), GarageOverviewUiState(vehicles = vehicles, searchQuery = "abc").visibleVehicles)
        assertEquals(listOf(bike), GarageOverviewUiState(vehicles = vehicles, searchQuery = "motor").visibleVehicles)
    }

    @Test
    fun noMatchDoesNotBecomeEmptyGarage() {
        val state =
            GarageOverviewUiState(
                vehicles = listOf(vehicle("car", "Family car", VehicleType.Car)),
                searchQuery = "van",
                loadState = GarageLoadState.Loaded,
            )

        assertFalse(state.isEmpty)
        assertTrue(state.hasNoMatchingVehicles)
        assertEquals(emptyList(), state.visibleVehicles)
    }

    private fun vehicle(
        id: String,
        name: String,
        type: VehicleType,
        licensePlate: String? = null,
    ) = Vehicle(
        id = VehicleId(id),
        familyId = FamilyId("family"),
        name = name,
        type = type,
        licensePlate = licensePlate,
        currentOdometerKm = 0,
    )
}
