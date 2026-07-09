package com.asensiodev.carbura.feature.maintenance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordInput
import com.asensiodev.carbura.core.domain.maintenance.usecase.DeleteMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.GetVehicleHistoryUseCase
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
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
    private val createMaintenanceRecordFromInputUseCase: CreateMaintenanceRecordFromInputUseCase,
    private val getVehicleHistoryUseCase: GetVehicleHistoryUseCase,
    private val deleteMaintenanceRecordUseCase: DeleteMaintenanceRecordUseCase,
    private val syncManager: SyncManager? = null,
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
            is MaintenanceHistoryEvent.DeleteMaintenance -> scope.launch { deleteMaintenance(event.recordId) }
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
        val input = CreateMaintenanceRecordInput(
            id = nextRecordId(),
            familyId = familyId,
            vehicleId = vehicleId,
            type = state.type,
            performedOn = state.performedOn,
            odometerKm = state.odometerKm,
            cost = state.cost,
            workshop = state.workshop,
            notes = state.notes,
        )

        when (val result = withContext(dispatchers.io) { createMaintenanceRecordFromInputUseCase(input) }) {
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
                _effects.send(MaintenanceHistoryEffect.MaintenanceCreated(input.type.trim()))
                syncAfterMutation()
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

    private suspend fun deleteMaintenance(recordId: MaintenanceRecordId) {
        val type = _uiState.value.records.firstOrNull { it.id == recordId }?.displayType().orEmpty()
        withContext(dispatchers.io) { deleteMaintenanceRecordUseCase(recordId) }
        val records = withContext(dispatchers.io) { getVehicleHistoryUseCase(vehicleId) }
        _uiState.update { it.copy(records = records, errorMessage = null) }
        _effects.send(MaintenanceHistoryEffect.MaintenanceDeleted(type))
        syncAfterMutation()
    }

    private fun syncAfterMutation() {
        scope.launch { syncManager?.syncNow() }
    }
}

internal fun MaintenanceRecord.displayType(): String = maintenanceTypeId.value.removePrefix("type-").replace('-', ' ')

private fun randomMaintenanceRecordId(): MaintenanceRecordId =
    MaintenanceRecordId("maintenance-${Random.nextInt(1, Int.MAX_VALUE)}")
