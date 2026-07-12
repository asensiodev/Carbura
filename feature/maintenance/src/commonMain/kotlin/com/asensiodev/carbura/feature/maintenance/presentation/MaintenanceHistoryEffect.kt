package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.stringresources.CarburaString

sealed interface MaintenanceHistoryEffect {
    data class MaintenanceCreated(
        val type: String,
    ) : MaintenanceHistoryEffect

    data class MaintenanceDeleted(
        val type: String,
    ) : MaintenanceHistoryEffect

    data class ValidationFailed(
        val message: CarburaString,
    ) : MaintenanceHistoryEffect
}
