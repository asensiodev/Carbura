package com.asensiodev.carbura.feature.maintenance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.domain.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.GetVehicleHistoryUseCase
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.stringresources.CarburaString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MaintenanceHistoryViewModel(
    private val vehicleId: VehicleId,
    private val familyId: FamilyId,
    private val dispatchers: DispatcherProvider,
    private val createMaintenanceRecordUseCase: CreateMaintenanceRecordUseCase,
    private val getVehicleHistoryUseCase: GetVehicleHistoryUseCase,
    private val nextRecordId: () -> MaintenanceRecordId = ::randomMaintenanceRecordId,
    private val coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MaintenanceHistoryUiState())
    val uiState: StateFlow<MaintenanceHistoryUiState> = _uiState.asStateFlow()

    private val _effects = Channel<MaintenanceHistoryEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<MaintenanceHistoryEffect> = _effects.receiveAsFlow()

    private val scope: CoroutineScope
        get() = coroutineScope ?: viewModelScope

    fun onEvent(event: MaintenanceHistoryEvent) {
        when (event) {
            MaintenanceHistoryEvent.Started -> scope.launch { loadHistory() }
            is MaintenanceHistoryEvent.TypeChanged -> updateForm { it.copy(type = event.value, errorMessage = null) }
            is MaintenanceHistoryEvent.PerformedOnChanged -> updateForm { it.copy(performedOn = event.value, errorMessage = null) }
            is MaintenanceHistoryEvent.OdometerChanged -> updateForm { it.copy(odometerKm = event.value, errorMessage = null) }
            is MaintenanceHistoryEvent.CostChanged -> updateForm { it.copy(cost = event.value, errorMessage = null) }
            is MaintenanceHistoryEvent.WorkshopChanged -> updateForm { it.copy(workshop = event.value, errorMessage = null) }
            is MaintenanceHistoryEvent.NotesChanged -> updateForm { it.copy(notes = event.value, errorMessage = null) }
            MaintenanceHistoryEvent.SubmitMaintenance -> scope.launch { createMaintenance() }
        }
    }

    private fun updateForm(transform: (MaintenanceHistoryUiState) -> MaintenanceHistoryUiState) {
        _uiState.update(transform)
    }

    private suspend fun loadHistory() {
        _uiState.update { it.copy(isLoading = true) }
        val records = withContext(dispatchers.io) { getVehicleHistoryUseCase(vehicleId) }
        _uiState.update { it.copy(records = records, isLoading = false) }
    }

    private suspend fun createMaintenance() {
        val state = _uiState.value
        val type = state.type.trim()
        if (type.isBlank()) {
            emitValidation(CarburaString.ValidationBlankMaintenanceType)
            return
        }

        val performedOn = runCatching { CalendarDate(state.performedOn.trim()) }.getOrNull()
        if (performedOn == null) {
            emitValidation(CarburaString.ValidationInvalidMaintenanceDate)
            return
        }

        val odometerKm = state.odometerKm.toIntOrNull() ?: -1
        val costCents = state.cost.toCostCentsOrNull()
        val record = MaintenanceRecord(
            id = nextRecordId(),
            familyId = familyId,
            vehicleId = vehicleId,
            maintenanceTypeId = MaintenanceTypeId("type-${type.lowercase().replace(' ', '-')}"),
            maintenanceTypeCode = MaintenanceTypeCode.Custom,
            performedOn = performedOn,
            odometerKm = odometerKm,
            costCents = costCents,
            workshop = state.workshop.trim().ifBlank { null },
            notes = state.notes.trim().ifBlank { null },
        )

        when (val result = withContext(dispatchers.io) { createMaintenanceRecordUseCase(record) }) {
            is DomainResult.Success -> {
                val records = withContext(dispatchers.io) { getVehicleHistoryUseCase(vehicleId) }
                _uiState.update {
                    it.copy(
                        records = records,
                        type = "",
                        odometerKm = "0",
                        cost = "",
                        workshop = "",
                        notes = "",
                        errorMessage = null,
                    )
                }
                _effects.send(MaintenanceHistoryEffect.MaintenanceCreated(type))
            }

            is DomainResult.ValidationError -> {
                emitValidation(result.reason.toMaintenanceMessage())
            }
        }
    }

    private suspend fun emitValidation(message: CarburaString) {
        _uiState.update { it.copy(errorMessage = message) }
        _effects.send(MaintenanceHistoryEffect.ValidationFailed(message))
    }
}

private fun String.toCostCentsOrNull(): Int? {
    val trimmed = trim()
    if (trimmed.isBlank()) return null
    return trimmed.replace(',', '.').toDoubleOrNull()?.let { (it * 100).toInt() }
}

private fun randomMaintenanceRecordId(): MaintenanceRecordId =
    MaintenanceRecordId("maintenance-${Random.nextInt(1, Int.MAX_VALUE)}")
