package com.asensiodev.carbura.feature.garage.presentation.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.domain.vehicle.usecase.DeleteVehicleUseCase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import kotlinx.coroutines.CancellationException
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

class GarageOverviewViewModel(
    private val familyId: FamilyId,
    private val vehicleRepository: VehicleRepository,
    private val dispatchers: DispatcherProvider,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    private val syncManager: SyncManager? = null,
    private val coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GarageOverviewUiState())
    val uiState: StateFlow<GarageOverviewUiState> = _uiState.asStateFlow()

    private val _effects = Channel<GarageOverviewEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<GarageOverviewEffect> = _effects.receiveAsFlow()
    private var loadInProgress = false

    private val scope: CoroutineScope
        get() = coroutineScope ?: viewModelScope

    fun onEvent(event: GarageOverviewEvent) {
        when (event) {
            GarageOverviewEvent.Started,
            GarageOverviewEvent.Retry,
            -> loadVehicles(showLoading = true)
            GarageOverviewEvent.Refresh -> loadVehicles(showLoading = false)
            is GarageOverviewEvent.VehicleSelected ->
                scope.launch {
                    _effects.send(GarageOverviewEffect.NavigateToVehicleHistory(event.vehicleId))
                }
            is GarageOverviewEvent.DeleteVehicleConfirmed -> deleteVehicle(event.vehicleId)
        }
    }

    private fun loadVehicles(showLoading: Boolean) {
        scope.launch {
            if (loadInProgress) return@launch
            loadInProgress = true
            val previousLoadState = _uiState.value.loadState
            var loadSettled = false
            if (showLoading) _uiState.update { it.copy(loadState = GarageLoadState.Loading) }
            try {
                val vehicles = withContext(dispatchers.io) { vehicleRepository.observeVehicles(familyId) }
                _uiState.update {
                    it.copy(
                        vehicles = mergeVehiclesPreservingOrder(it.vehicles, vehicles),
                        loadState = GarageLoadState.Loaded,
                    )
                }
                loadSettled = true
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                if (showLoading) _uiState.update { it.copy(loadState = GarageLoadState.Error) }
                loadSettled = true
            } finally {
                if (showLoading && !loadSettled) {
                    _uiState.update { it.copy(loadState = previousLoadState) }
                }
                loadInProgress = false
            }
        }
    }

    private fun deleteVehicle(vehicleId: VehicleId) {
        scope.launch {
            if (_uiState.value.deletingVehicleId != null) return@launch
            _uiState.update { it.copy(deletingVehicleId = vehicleId, deleteError = false) }
            val vehicleName =
                _uiState.value.vehicles
                    .firstOrNull { it.id == vehicleId }
                    ?.name
                    .orEmpty()
            try {
                withContext(dispatchers.io) { deleteVehicleUseCase(vehicleId) }
                _uiState.update {
                    it.copy(
                        vehicles = it.vehicles.filterNot { vehicle -> vehicle.id == vehicleId },
                        deleteError = false,
                    )
                }
                _effects.send(GarageOverviewEffect.VehicleDeleted(vehicleName))
                scope.launch { syncManager?.syncNow() }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { it.copy(deleteError = true) }
            } finally {
                _uiState.update {
                    if (it.deletingVehicleId == vehicleId) it.copy(deletingVehicleId = null) else it
                }
            }
        }
    }
}

private fun mergeVehiclesPreservingOrder(
    current: List<Vehicle>,
    refreshed: List<Vehicle>,
): List<Vehicle> {
    if (current.isEmpty()) return refreshed
    val refreshedById = refreshed.associateBy { it.id }
    val currentIds = current.mapTo(mutableSetOf()) { it.id }
    return current.mapNotNull { refreshedById[it.id] } + refreshed.filterNot { it.id in currentIds }
}
