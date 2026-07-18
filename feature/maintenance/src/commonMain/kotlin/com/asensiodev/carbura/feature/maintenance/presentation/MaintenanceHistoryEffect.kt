package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.stringresources.CarburaString

sealed interface MaintenanceHistoryEffect {
    data class MaintenanceCreated(
        val typeCode: MaintenanceTypeCode,
        val customTypeLabel: String,
        val reminderCreated: Boolean,
    ) : MaintenanceHistoryEffect

    data class MaintenanceDeleted(
        val type: String,
    ) : MaintenanceHistoryEffect

    data class ValidationFailed(
        val message: CarburaString,
    ) : MaintenanceHistoryEffect
}
