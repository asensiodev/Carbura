package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.model.MaintenanceRecordId

sealed interface MaintenanceHistoryEvent {
    data object Started : MaintenanceHistoryEvent

    data object Retry : MaintenanceHistoryEvent

    data object Refresh : MaintenanceHistoryEvent

    data class TypeChanged(
        val value: String,
    ) : MaintenanceHistoryEvent

    data class PerformedOnChanged(
        val value: String,
    ) : MaintenanceHistoryEvent

    data class OdometerChanged(
        val value: String,
    ) : MaintenanceHistoryEvent

    data class CostChanged(
        val value: String,
    ) : MaintenanceHistoryEvent

    data class WorkshopChanged(
        val value: String,
    ) : MaintenanceHistoryEvent

    data class NotesChanged(
        val value: String,
    ) : MaintenanceHistoryEvent

    data object SubmitMaintenance : MaintenanceHistoryEvent

    data class DeleteMaintenance(
        val recordId: MaintenanceRecordId,
    ) : MaintenanceHistoryEvent
}
