package com.asensiodev.carbura.feature.maintenance.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaintenanceHistoryUiStateTest {
    @Test
    fun loadingStateIsNotEmptyState() {
        assertFalse(MaintenanceHistoryUiState(isLoading = true).isEmpty)
    }

    @Test
    fun emptyStateRequiresNoRecordsAndNotLoading() {
        assertTrue(MaintenanceHistoryUiState(isLoading = false).isEmpty)
    }
}
