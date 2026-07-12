package com.asensiodev.carbura.feature.garage.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.domain.vehicle.usecase.CreateVehicleUseCase
import com.asensiodev.carbura.core.domain.vehicle.usecase.DeleteVehicleUseCase
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleParams
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleResult
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleUseCase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
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

class GarageViewModel(
    private val familyId: FamilyId,
    private val vehicleRepository: VehicleRepository,
    private val dispatchers: DispatcherProvider,
    private val createVehicleUseCase: CreateVehicleUseCase = CreateVehicleUseCase(vehicleRepository),
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase = UpdateVehicleUseCase(vehicleRepository),
    private val syncManager: SyncManager? = null,
    private val nextVehicleId: () -> VehicleId = ::randomVehicleId,
    private val coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GarageUiState())
    val uiState: StateFlow<GarageUiState> = _uiState.asStateFlow()

    private val _effects = Channel<GarageEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<GarageEffect> = _effects.receiveAsFlow()

    private val scope: CoroutineScope
        get() = coroutineScope ?: viewModelScope

    fun onEvent(event: GarageEvent) {
        when (event) {
            is GarageEvent.NameChanged ->
                _uiState.update {
                    it.copy(name = event.value, errorMessage = null)
                }

            is GarageEvent.OdometerChanged ->
                _uiState.update {
                    it.copy(odometerKm = event.value, errorMessage = null)
                }

            is GarageEvent.TypeSelected ->
                _uiState.update {
                    it.copy(selectedType = event.value, errorMessage = null)
                }

            is GarageEvent.EditNameChanged -> updateEditState { it.copy(editName = event.value) }
            is GarageEvent.EditLicensePlateChanged -> updateEditState { it.copy(editLicensePlate = event.value) }
            is GarageEvent.EditOdometerChanged -> updateEditState { it.copy(editOdometerKm = event.value) }
            is GarageEvent.EditTypeSelected -> updateEditState { it.copy(editType = event.value) }

            GarageEvent.Started -> scope.launch { loadVehicles() }
            GarageEvent.SubmitVehicle -> scope.launch { createVehicle() }
            GarageEvent.SubmitVehicleEdit -> scope.launch { updateVehicle() }
            GarageEvent.ConfirmOdometerDecrease -> scope.launch { updateVehicle(allowOdometerDecrease = true) }
            GarageEvent.CancelOdometerDecrease ->
                _uiState.update { it.copy(odometerDecreaseConfirmation = null) }

            GarageEvent.DismissVehicleEdit -> clearEditState()
            is GarageEvent.DeleteVehicleConfirmed -> scope.launch { deleteVehicle(event.vehicleId) }
            is GarageEvent.EditVehicleRequested -> startEditing(event.vehicleId, VehicleEditMode.Full)
            is GarageEvent.QuickOdometerUpdateRequested -> startEditing(event.vehicleId, VehicleEditMode.Odometer)
            is GarageEvent.VehicleSelected ->
                scope.launch {
                    _effects.send(GarageEffect.NavigateToVehicleHistory(event.vehicleId))
                }
        }
    }

    private fun updateEditState(transform: (GarageUiState) -> GarageUiState) {
        _uiState.update { transform(it).copy(editErrorMessage = null, odometerDecreaseConfirmation = null) }
    }

    private fun startEditing(
        vehicleId: VehicleId,
        mode: VehicleEditMode,
    ) {
        val vehicle = _uiState.value.vehicles.firstOrNull { it.id == vehicleId } ?: return
        _uiState.update {
            it.copy(
                editMode = mode,
                editingVehicleId = vehicle.id,
                editName = vehicle.name,
                editLicensePlate = vehicle.licensePlate.orEmpty(),
                editOdometerKm = vehicle.currentOdometerKm.toString(),
                editType = vehicle.type,
                editErrorMessage = null,
                odometerDecreaseConfirmation = null,
            )
        }
    }

    private fun clearEditState() {
        _uiState.update {
            it.copy(
                editMode = null,
                editingVehicleId = null,
                editName = "",
                editLicensePlate = "",
                editOdometerKm = "",
                editType = VehicleType.Car,
                editErrorMessage = null,
                odometerDecreaseConfirmation = null,
            )
        }
    }

    private suspend fun loadVehicles() {
        _uiState.update {
            it.copy(isLoading = true)
        }
        val vehicles =
            withContext(dispatchers.io) {
                vehicleRepository.observeVehicles(familyId)
            }
        _uiState.update {
            it.copy(
                vehicles = vehicles,
                isLoading = false,
            )
        }
    }

    private suspend fun createVehicle() {
        val state = _uiState.value
        val odometerKm = state.odometerKm.toIntOrNull() ?: -1
        val vehicle =
            Vehicle(
                id = nextVehicleId(),
                familyId = familyId,
                name = state.name.trim(),
                type = state.selectedType,
                currentOdometerKm = odometerKm,
            )

        when (val result = withContext(dispatchers.io) { createVehicleUseCase(vehicle) }) {
            is DomainResult.Success -> {
                val vehicles =
                    withContext(dispatchers.io) {
                        vehicleRepository.observeVehicles(familyId)
                    }
                _uiState.update {
                    it.copy(
                        vehicles = vehicles,
                        name = "",
                        odometerKm = "0",
                        errorMessage = null,
                    )
                }
                _effects.send(GarageEffect.VehicleCreated(result.value.name))
                syncAfterMutation()
            }

            is DomainResult.ValidationError -> {
                val message = result.reason.toGarageMessage()
                _uiState.update {
                    it.copy(errorMessage = message)
                }
                _effects.send(GarageEffect.ValidationFailed(message))
            }
        }
    }

    private suspend fun deleteVehicle(vehicleId: VehicleId) {
        val vehicleName =
            _uiState.value.vehicles
                .firstOrNull { it.id == vehicleId }
                ?.name
                .orEmpty()
        withContext(dispatchers.io) { deleteVehicleUseCase(vehicleId) }
        val vehicles =
            withContext(dispatchers.io) {
                vehicleRepository.observeVehicles(familyId)
            }
        _uiState.update { it.copy(vehicles = vehicles) }
        _effects.send(GarageEffect.VehicleDeleted(vehicleName))
        syncAfterMutation()
    }

    private suspend fun updateVehicle(allowOdometerDecrease: Boolean = false) {
        val state = _uiState.value
        val vehicle = state.vehicles.firstOrNull { it.id == state.editingVehicleId } ?: return
        val params =
            UpdateVehicleParams(
                currentVehicle = vehicle,
                name = state.editName,
                type = state.editType,
                licensePlate = state.editLicensePlate,
                odometerKm = state.editOdometerKm.toIntOrNull() ?: -1,
                allowOdometerDecrease = allowOdometerDecrease,
            )
        val result =
            try {
                withContext(dispatchers.io) { updateVehicleUseCase(params) }
            } catch (_: Exception) {
                _uiState.update { it.copy(editErrorMessage = CarburaString.ValidationGeneric) }
                return
            }

        when (result) {
            is UpdateVehicleResult.Success -> {
                val vehicles = withContext(dispatchers.io) { vehicleRepository.observeVehicles(familyId) }
                clearEditState()
                _uiState.update { it.copy(vehicles = vehicles) }
                _effects.send(GarageEffect.VehicleUpdated(result.vehicle.name))
                syncAfterMutation()
            }

            is UpdateVehicleResult.ValidationError -> {
                val message = result.reason.toGarageMessage()
                _uiState.update { it.copy(editErrorMessage = message) }
                _effects.send(GarageEffect.ValidationFailed(message))
            }

            is UpdateVehicleResult.OdometerDecreaseConfirmationRequired ->
                _uiState.update {
                    it.copy(
                        odometerDecreaseConfirmation =
                            OdometerDecreaseConfirmation(
                                currentOdometerKm = result.currentOdometerKm,
                                proposedOdometerKm = result.proposedOdometerKm,
                            ),
                    )
                }
        }
    }

    private fun syncAfterMutation() {
        scope.launch { syncManager?.syncNow() }
    }
}

private fun randomVehicleId(): VehicleId = VehicleId("vehicle-${Random.nextInt(1, Int.MAX_VALUE)}")
