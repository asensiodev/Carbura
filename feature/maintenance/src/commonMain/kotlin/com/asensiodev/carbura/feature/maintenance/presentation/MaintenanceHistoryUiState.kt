package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.stringresources.CarburaString

data class MaintenanceHistoryUiState(
    val vehicle: Vehicle? = null,
    val records: List<MaintenanceRecord> = emptyList(),
    val searchQuery: String = "",
    val maintenanceTypeCode: MaintenanceTypeCode = MaintenanceTypeCode.Itv,
    val customTypeLabel: String = "",
    val performedOn: String,
    val nextDueDate: String = "",
    val odometerKm: String = "0",
    val cost: String = "",
    val workshop: String = "",
    val notes: String = "",
    val loadState: MaintenanceLoadState = MaintenanceLoadState.Loading,
    val validationError: CarburaString? = null,
    val persistenceError: Boolean = false,
    val activeMutation: MaintenanceMutation? = null,
    val showFutureReminderOffer: Boolean = false,
    val editingRecordId: MaintenanceRecordId? = null,
) {
    val isEmpty: Boolean = records.isEmpty() && loadState == MaintenanceLoadState.Content

    val visibleRecords: List<MaintenanceRecord>
        get() {
            val query = searchQuery.trim()
            return if (query.isEmpty()) records else records.filter { it.matchesSearch(query) }
        }

    val hasNoMatchingRecords: Boolean
        get() = records.isNotEmpty() && searchQuery.isNotBlank() && visibleRecords.isEmpty()

    val isSaving: Boolean = activeMutation == MaintenanceMutation.Saving || activeMutation is MaintenanceMutation.Updating

    val isEditing: Boolean = editingRecordId != null

    val supportsNextDueDate: Boolean =
        maintenanceTypeCode == MaintenanceTypeCode.Itv || maintenanceTypeCode == MaintenanceTypeCode.Insurance
}

private fun MaintenanceRecord.matchesSearch(query: String): Boolean =
    listOfNotNull(
        maintenanceTypeCode?.searchLabel(),
        maintenanceTypeLabel,
        maintenanceTypeId.value,
        workshop,
        notes,
        performedOn.iso8601,
        nextDueDate?.iso8601,
    ).any { it.contains(query, ignoreCase = true) }

private fun MaintenanceTypeCode.searchLabel(): String =
    when (this) {
        MaintenanceTypeCode.Itv -> "ITV"
        MaintenanceTypeCode.Insurance -> "Insurance"
        MaintenanceTypeCode.OilChange -> "Oil change"
        MaintenanceTypeCode.Tires -> "Tires"
        MaintenanceTypeCode.GeneralReview -> "General review"
        MaintenanceTypeCode.Repair -> "Repair"
        MaintenanceTypeCode.Custom -> "Custom"
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

    data class Updating(
        val recordId: MaintenanceRecordId,
    ) : MaintenanceMutation
}
