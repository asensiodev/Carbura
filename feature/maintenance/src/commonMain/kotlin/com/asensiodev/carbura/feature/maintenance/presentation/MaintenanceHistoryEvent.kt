package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode

sealed interface MaintenanceHistoryEvent {
    data object Started : MaintenanceHistoryEvent

    data object Retry : MaintenanceHistoryEvent

    data object Refresh : MaintenanceHistoryEvent

    data class TypeSelected(
        val value: MaintenanceTypeCode,
    ) : MaintenanceHistoryEvent

    data class CustomTypeLabelChanged(
        val value: String,
    ) : MaintenanceHistoryEvent

    data class PerformedOnChanged(
        val value: String,
    ) : MaintenanceHistoryEvent

    data class NextDueDateChanged(
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

    data class EditMaintenance(
        val recordId: MaintenanceRecordId,
    ) : MaintenanceHistoryEvent

    data object CancelMaintenanceEdit : MaintenanceHistoryEvent

    data object SubmitMaintenanceEdit : MaintenanceHistoryEvent

    data object SaveFutureMaintenanceWithReminder : MaintenanceHistoryEvent

    data object SaveFutureMaintenanceOnly : MaintenanceHistoryEvent

    data object DismissFutureReminderOffer : MaintenanceHistoryEvent

    data class DeleteMaintenance(
        val recordId: MaintenanceRecordId,
    ) : MaintenanceHistoryEvent
}
