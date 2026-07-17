package com.asensiodev.carbura.feature.reminders.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemindersUiStateTest {
    @Test
    fun loadingStateIsNotEmptyState() {
        assertFalse(RemindersUiState(isLoading = true).isEmpty)
    }

    @Test
    fun emptyStateRequiresNoRemindersAndNotLoading() {
        assertTrue(RemindersUiState(isLoading = false).isEmpty)
    }

    @Test
    fun loadFailureIsNeitherEmptyNorNoVehicles() {
        val state = RemindersUiState(isLoading = false, hasLoadError = true)

        assertFalse(state.isEmpty)
        assertFalse(state.hasNoVehicles)
    }

    @Test
    fun loadedStateWithoutVehiclesExposesPrerequisite() {
        assertTrue(RemindersUiState(isLoading = false).hasNoVehicles)
    }
}
