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
        val typeCode: MaintenanceTypeCode,
        val customTypeLabel: String,
    ) : MaintenanceHistoryEffect

    data class MaintenanceUpdated(
        val typeCode: MaintenanceTypeCode,
        val customTypeLabel: String,
        val reminderRetained: Boolean,
    ) : MaintenanceHistoryEffect

    data class ValidationFailed(
        val message: CarburaString,
    ) : MaintenanceHistoryEffect
}
