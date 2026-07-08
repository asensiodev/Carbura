package com.asensiodev.carbura.feature.garage.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.domain.CreateVehicleUseCase
import com.asensiodev.carbura.core.domain.DeleteVehicleUseCase
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SyncManager
import com.asensiodev.carbura.core.domain.VehicleRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
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
    private val deleteVehicleUseCase: DeleteVehicleUseCase = DeleteVehicleUseCase(vehicleRepository),
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
            is GarageEvent.NameChanged -> _uiState.update {
                it.copy(name = event.value, errorMessage = null)
            }

            is GarageEvent.OdometerChanged -> _uiState.update {
                it.copy(odometerKm = event.value, errorMessage = null)
            }

            is GarageEvent.TypeSelected -> _uiState.update {
                it.copy(selectedType = event.value, errorMessage = null)
            }

            GarageEvent.Started -> scope.launch { loadVehicles() }
            GarageEvent.SubmitVehicle -> scope.launch { createVehicle() }
            is GarageEvent.DeleteVehicleConfirmed -> scope.launch { deleteVehicle(event.vehicleId) }
            is GarageEvent.VehicleSelected -> scope.launch {
                _effects.send(GarageEffect.NavigateToVehicleHistory(event.vehicleId))
            }
        }
    }

    private suspend fun loadVehicles() {
        _uiState.update {
            it.copy(isLoading = true)
        }
        val vehicles = withContext(dispatchers.io) {
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
        val vehicle = Vehicle(
            id = nextVehicleId(),
            familyId = familyId,
            name = state.name.trim(),
            type = state.selectedType,
            currentOdometerKm = odometerKm,
        )

        when (val result = withContext(dispatchers.io) { createVehicleUseCase(vehicle) }) {
            is DomainResult.Success -> {
                val vehicles = withContext(dispatchers.io) {
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
        val vehicleName = _uiState.value.vehicles.firstOrNull { it.id == vehicleId }?.name.orEmpty()
        withContext(dispatchers.io) { deleteVehicleUseCase(vehicleId) }
        val vehicles = withContext(dispatchers.io) {
            vehicleRepository.observeVehicles(familyId)
        }
        _uiState.update { it.copy(vehicles = vehicles) }
        _effects.send(GarageEffect.VehicleDeleted(vehicleName))
        syncAfterMutation()
    }

    private fun syncAfterMutation() {
        scope.launch { syncManager?.syncNow() }
    }
}

private fun randomVehicleId(): VehicleId = VehicleId("vehicle-${Random.nextInt(1, Int.MAX_VALUE)}")
