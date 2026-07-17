package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.stringresources.CarburaString

data class MaintenanceHistoryUiState(
    val vehicle: Vehicle? = null,
    val records: List<MaintenanceRecord> = emptyList(),
    val type: String = "",
    val performedOn: String,
    val odometerKm: String = "0",
    val cost: String = "",
    val workshop: String = "",
    val notes: String = "",
    val loadState: MaintenanceLoadState = MaintenanceLoadState.Loading,
    val validationError: CarburaString? = null,
    val persistenceError: Boolean = false,
    val activeMutation: MaintenanceMutation? = null,
) {
    val isEmpty: Boolean = records.isEmpty() && loadState == MaintenanceLoadState.Content

    val isSaving: Boolean = activeMutation == MaintenanceMutation.Saving
}

enum class MaintenanceLoadState {
    Loading,
    Content,
    Error,
}

sealed interface MaintenanceMutation {
    data object Saving : MaintenanceMutation

    data class Deleting(
        val recordId: MaintenanceRecordId,
    ) : MaintenanceMutation
}
