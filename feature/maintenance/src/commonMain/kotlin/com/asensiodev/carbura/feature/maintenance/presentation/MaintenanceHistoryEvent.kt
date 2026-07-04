package com.asensiodev.carbura.feature.maintenance.presentation

sealed interface MaintenanceHistoryEvent {
    data object Started : MaintenanceHistoryEvent
    data class TypeChanged(val value: String) : MaintenanceHistoryEvent
    data class PerformedOnChanged(val value: String) : MaintenanceHistoryEvent
    data class OdometerChanged(val value: String) : MaintenanceHistoryEvent
    data class CostChanged(val value: String) : MaintenanceHistoryEvent
    data class WorkshopChanged(val value: String) : MaintenanceHistoryEvent
    data class NotesChanged(val value: String) : MaintenanceHistoryEvent
    data object SubmitMaintenance : MaintenanceHistoryEvent
}
