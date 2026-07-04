package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.stringresources.CarburaString

data class MaintenanceHistoryUiState(
    val records: List<MaintenanceRecord> = emptyList(),
    val type: String = "",
    val performedOn: String = "2026-07-04",
    val odometerKm: String = "0",
    val cost: String = "",
    val workshop: String = "",
    val notes: String = "",
    val isLoading: Boolean = false,
    val errorMessage: CarburaString? = null,
) {
    val isEmpty: Boolean = records.isEmpty() && !isLoading
}
