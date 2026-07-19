package com.asensiodev.carbura.feature.reminders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.reminder.usecase.CompleteReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.DeleteReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.GetPendingRemindersUseCase
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.stringresources.CarburaString
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
import kotlin.random.Random

class RemindersViewModel(
    private val familyId: FamilyId,
    private val vehicleRepository: VehicleRepository,
    private val dispatchers: DispatcherProvider,
    private val createReminderUseCase: CreateReminderUseCase,
    private val getPendingRemindersUseCase: GetPendingRemindersUseCase,
    private val completeReminderUseCase: CompleteReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val syncManager: SyncManager? = null,
    private val nextReminderId: () -> ReminderId = ::randomReminderId,
    private val coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RemindersUiState())
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    private val _effects = Channel<RemindersEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<RemindersEffect> = _effects.receiveAsFlow()
    private var isLoadInProgress = false

    private val scope: CoroutineScope
        get() = coroutineScope ?: viewModelScope

    fun onEvent(event: RemindersEvent) {
        when (event) {
            RemindersEvent.Started -> launchLoad(showLoading = true)
            RemindersEvent.Retry -> launchLoad(showLoading = true)
            RemindersEvent.Refresh -> launchLoad(showLoading = false)
            RemindersEvent.GarageRequested -> scope.launch { _effects.send(RemindersEffect.NavigateToGarage) }
            is RemindersEvent.TitleChanged -> updateForm { it.copy(title = event.value, errorMessage = null, hasPersistenceError = false) }
            is RemindersEvent.VehicleSelected ->
                updateForm { it.copy(selectedVehicleId = event.vehicleId, errorMessage = null, hasPersistenceError = false) }
            is RemindersEvent.VehicleFilterToggled -> toggleVehicleFilter(event.vehicleId)
            RemindersEvent.VehicleFiltersCleared -> _uiState.update { it.copy(selectedFilterVehicleIds = emptySet()) }
            is RemindersEvent.DueDateChanged ->
                updateForm { it.copy(dueDate = event.value, errorMessage = null, hasPersistenceError = false) }
            is RemindersEvent.DueOdometerChanged ->
                updateForm { it.copy(dueOdometerKm = event.value, errorMessage = null, hasPersistenceError = false) }
            RemindersEvent.SubmitReminder -> scope.launch { createReminder() }
            is RemindersEvent.CompleteReminder -> scope.launch { completeReminder(event.reminderId) }
            is RemindersEvent.DeleteReminder -> scope.launch { deleteReminder(event.reminderId) }
        }
    }

    private fun launchLoad(showLoading: Boolean) {
        scope.launch { loadReminders(showLoading) }.invokeOnCompletion { cause ->
            if (cause is CancellationException) {
                isLoadInProgress = false
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun updateForm(transform: (RemindersUiState) -> RemindersUiState) {
        _uiState.update(transform)
    }

    private suspend fun loadReminders(showLoading: Boolean) {
        if (isLoadInProgress) return
        isLoadInProgress = true
        _uiState.update { it.copy(isLoading = showLoading, hasLoadError = false) }
        try {
            val vehicles = withContext(dispatchers.io) { vehicleRepository.observeVehicles(familyId) }
            val reminders = withContext(dispatchers.io) { getPendingRemindersUseCase(familyId) }
            val availableVehicleIds = vehicles.mapTo(mutableSetOf()) { it.id }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasLoadError = false,
                    vehicles = vehicles,
                    reminders = reminders,
                    selectedFilterVehicleIds = it.selectedFilterVehicleIds.intersect(availableVehicleIds),
                    selectedVehicleId =
                        it.selectedVehicleId?.takeIf { id -> vehicles.any { vehicle -> vehicle.id == id } }
                            ?: vehicles.firstOrNull()?.id,
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            _uiState.update { it.copy(hasLoadError = true) }
        } finally {
            isLoadInProgress = false
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun toggleVehicleFilter(vehicleId: VehicleId) {
        _uiState.update { state ->
            val selectedIds = state.selectedFilterVehicleIds
            if (state.vehicles.none { it.id == vehicleId }) {
                return@update state.copy(selectedFilterVehicleIds = selectedIds - vehicleId)
            }
            state.copy(
                selectedFilterVehicleIds =
                    if (vehicleId in selectedIds) {
                        selectedIds - vehicleId
                    } else {
                        selectedIds + vehicleId
                    },
            )
        }
    }

    private suspend fun createReminder() {
        val state = _uiState.value
        if (state.activeAction != null) return
        val vehicleId = state.selectedVehicleId
        if (vehicleId == null) {
            emitValidation(CarburaString.ValidationMissingReminderVehicle)
            return
        }

        val dueDate =
            state.dueDate.trim().ifBlank { null }?.let { value ->
                value.toCalendarDateOrNull() ?: run {
                    emitValidation(CarburaString.ValidationInvalidReminderDate)
                    return
                }
            }
        val dueOdometer =
            state.dueOdometerKm
                .trim()
                .ifBlank { null }
                ?.toIntOrNull()

        val reminder =
            Reminder(
                id = nextReminderId(),
                familyId = familyId,
                vehicleId = vehicleId,
                maintenanceTypeId = null,
                title = state.title.trim(),
                dueDate = dueDate,
                dueOdometerKm = dueOdometer,
            )

        _uiState.update { it.copy(activeAction = ReminderAction.Create, hasPersistenceError = false) }
        try {
            when (val result = withContext(dispatchers.io) { createReminderUseCase(reminder) }) {
                is DomainResult.Success -> refreshAfterCreate(result.value.title)
                is DomainResult.ValidationError -> emitValidation(result.reason.toRemindersMessage())
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            _uiState.update { it.copy(hasPersistenceError = true) }
        } finally {
            _uiState.update { it.copy(activeAction = null) }
        }
    }

    private suspend fun refreshAfterCreate(title: String) {
        val reminders = withContext(dispatchers.io) { getPendingRemindersUseCase(familyId) }
        _uiState.update {
            it.copy(
                reminders = reminders,
                title = "",
                dueDate = "",
                dueOdometerKm = "",
                errorMessage = null,
            )
        }
        _effects.send(RemindersEffect.ReminderCreated(title))
        syncAfterMutation()
    }

    private suspend fun completeReminder(reminderId: ReminderId) {
        if (_uiState.value.activeAction != null) return
        val title =
            _uiState.value.reminders
                .firstOrNull { it.id == reminderId }
                ?.title
                .orEmpty()
        _uiState.update { it.copy(activeAction = ReminderAction.Complete(reminderId), hasPersistenceError = false) }
        try {
            withContext(dispatchers.io) { completeReminderUseCase(reminderId) }
            val reminders = withContext(dispatchers.io) { getPendingRemindersUseCase(familyId) }
            _uiState.update { it.copy(reminders = reminders, errorMessage = null) }
            _effects.send(RemindersEffect.ReminderCompleted(title))
            syncAfterMutation()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            _uiState.update { it.copy(hasPersistenceError = true) }
        } finally {
            _uiState.update { it.copy(activeAction = null) }
        }
    }

    private suspend fun deleteReminder(reminderId: ReminderId) {
        if (_uiState.value.activeAction != null) return
        val title =
            _uiState.value.reminders
                .firstOrNull { it.id == reminderId }
                ?.title
                .orEmpty()
        _uiState.update { it.copy(activeAction = ReminderAction.Delete(reminderId), hasPersistenceError = false) }
        try {
            withContext(dispatchers.io) { deleteReminderUseCase(reminderId) }
            val reminders = withContext(dispatchers.io) { getPendingRemindersUseCase(familyId) }
            _uiState.update { it.copy(reminders = reminders, errorMessage = null) }
            _effects.send(RemindersEffect.ReminderDeleted(title))
            syncAfterMutation()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            _uiState.update { it.copy(hasPersistenceError = true) }
        } finally {
            _uiState.update { it.copy(activeAction = null) }
        }
    }

    private fun syncAfterMutation() {
        scope.launch { syncManager?.syncNow() }
    }

    private suspend fun emitValidation(message: CarburaString) {
        _uiState.update { it.copy(errorMessage = message) }
        _effects.send(RemindersEffect.ValidationFailed(message))
    }
}

private fun randomReminderId(): ReminderId = ReminderId("reminder-${Random.nextInt(1, Int.MAX_VALUE)}")

private fun String.toCalendarDateOrNull(): CalendarDate? =
    try {
        CalendarDate(this)
    } catch (_: IllegalArgumentException) {
        null
    }
