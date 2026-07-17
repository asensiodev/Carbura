package com.asensiodev.carbura.feature.maintenance.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaintenanceHistoryUiStateTest {
    @Test
    fun loadingStateIsNotEmptyState() {
        assertFalse(MaintenanceHistoryUiState(performedOn = TODAY).isEmpty)
    }

    @Test
    fun emptyStateRequiresNoRecordsAndNotLoading() {
        assertTrue(MaintenanceHistoryUiState(performedOn = TODAY, loadState = MaintenanceLoadState.Content).isEmpty)
    }

    private companion object {
        const val TODAY = "2026-07-17"
    }
}
