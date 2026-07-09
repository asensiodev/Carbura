package com.asensiodev.carbura.feature.garage.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GarageUiStateTest {
    @Test
    fun loadingStateIsNotEmptyState() {
        assertFalse(GarageUiState(isLoading = true).isEmpty)
    }

    @Test
    fun emptyStateRequiresNoVehiclesAndNotLoading() {
        assertTrue(GarageUiState(isLoading = false).isEmpty)
    }
}
