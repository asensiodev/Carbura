package com.asensiodev.carbura.feature.garage.presentation.overview

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GarageOverviewUiStateTest {
    @Test
    fun emptyStateOnlyAppearsAfterSuccessfulEmptyLoad() {
        assertFalse(GarageOverviewUiState(loadState = GarageLoadState.Loading).isEmpty)
        assertFalse(GarageOverviewUiState(loadState = GarageLoadState.Error).isEmpty)
        assertTrue(GarageOverviewUiState(loadState = GarageLoadState.Loaded).isEmpty)
    }
}
