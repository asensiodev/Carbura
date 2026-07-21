package com.asensiodev.carbura.feature.maintenance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordInput
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.DeleteMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.GetVehicleHistoryUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.UpdateMaintenanceRecordInput
import com.asensiodev.carbura.core.domain.maintenance.usecase.UpdateMaintenanceRecordResult
import com.asensiodev.carbura.core.domain.maintenance.usecase.UpdateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreatePlannedMaintenanceReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.RemovePlannedMaintenanceReminderUseCase
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.stringresources.CarburaString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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
    private val createMaintenanceWithReminderFromInputUseCase: CreateMaintenanceWithReminderFromInputUseCase,
    private val createPlannedMaintenanceReminderUseCase: CreatePlannedMaintenanceReminderUseCase,
    private val removePlannedMaintenanceReminderUseCase: RemovePlannedMaintenanceReminderUseCase,
    private val getVehicleHistoryUseCase: GetVehicleHistoryUseCase,
    private val deleteMaintenanceRecordUseCase: DeleteMaintenanceRecordUseCase,
    private val updateMaintenanceRecordUseCase: UpdateMaintenanceRecordUseCase? = null,
    private val vehicleRepository: VehicleRepository,
    private val syncManager: SyncManager? = null,
    private val nextRecordId: () -> MaintenanceRecordId = ::randomMaintenanceRecordId,
    private val localDateProvider: LocalDateProvider = LocalDateProvider(::deviceLocalDate),
    private val coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private var pendingRecordId: MaintenanceRecordId? = null
    private val _uiState = MutableStateFlow(MaintenanceHistoryUiState(performedOn = localDateProvider.currentDate().iso8601))
    val uiState: StateFlow<MaintenanceHistoryUiState> = _uiState.asStateFlow()

    private val _effects = Channel<MaintenanceHistoryEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<MaintenanceHistoryEffect> = _effects.receiveAsFlow()
    private var loadJob: Job? = null
    private var loadRevision = 0L
    private var settledLoadState = MaintenanceLoadState.Content

    private val scope: CoroutineScope
        get() = coroutineScope ?: viewModelScope

    fun onEvent(event: MaintenanceHistoryEvent) {
        when (event) {
            MaintenanceHistoryEvent.Started,
            MaintenanceHistoryEvent.Retry,
            -> launchLoad(showLoading = true)
            MaintenanceHistoryEvent.Refresh -> launchLoad(showLoading = false)
            is MaintenanceHistoryEvent.SearchQueryChanged ->
                _uiState.update { it.copy(searchQuery = event.value) }
            MaintenanceHistoryEvent.SearchCleared ->
                _uiState.update { it.copy(searchQuery = "") }
            MaintenanceHistoryEvent.SubmitMaintenance -> submitMaintenance()
            is MaintenanceHistoryEvent.EditMaintenance -> startEditing(event.recordId)
            MaintenanceHistoryEvent.CancelMaintenanceEdit -> clearEditState()
            MaintenanceHistoryEvent.SubmitMaintenanceEdit -> scope.launch { updateMaintenance() }
            MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder -> saveFutureMaintenance(createReminder = true)
            MaintenanceHistoryEvent.SaveFutureMaintenanceOnly -> saveFutureMaintenance(createReminder = false)
            MaintenanceHistoryEvent.DismissFutureReminderOffer ->
                _uiState.update { it.copy(showFutureReminderOffer = false) }
            is MaintenanceHistoryEvent.DeleteMaintenance -> scope.launch { deleteMaintenance(event.recordId) }
            else -> handleFormEvent(event)
        }
    }

    private fun handleFormEvent(event: MaintenanceHistoryEvent) {
        when (event) {
            is MaintenanceHistoryEvent.TypeSelected ->
                updateForm {
                    it.copy(
                        maintenanceTypeCode = event.value,
                        customTypeLabel = if (event.value == MaintenanceTypeCode.Custom) it.customTypeLabel else "",
                        nextDueDate =
                            if (event.value == MaintenanceTypeCode.Itv || event.value == MaintenanceTypeCode.Insurance) {
                                it.nextDueDate
                            } else {
                                ""
                            },
                        validationError = null,
                    )
                }
            is MaintenanceHistoryEvent.CustomTypeLabelChanged ->
                updateForm { it.copy(customTypeLabel = event.value, validationError = null) }
            is MaintenanceHistoryEvent.PerformedOnChanged -> updateForm { it.copy(performedOn = event.value, validationError = null) }
            is MaintenanceHistoryEvent.NextDueDateChanged -> updateForm { it.copy(nextDueDate = event.value, validationError = null) }
            is MaintenanceHistoryEvent.OdometerChanged -> updateForm { it.copy(odometerKm = event.value, validationError = null) }
            is MaintenanceHistoryEvent.CostChanged -> updateForm { it.copy(cost = event.value, validationError = null) }
            is MaintenanceHistoryEvent.WorkshopChanged -> updateForm { it.copy(workshop = event.value, validationError = null) }
            is MaintenanceHistoryEvent.NotesChanged -> updateForm { it.copy(notes = event.value, validationError = null) }
            else -> Unit
        }
    }

    private fun updateForm(transform: (MaintenanceHistoryUiState) -> MaintenanceHistoryUiState) {
        _uiState.update(transform)
    }

    private fun startEditing(recordId: MaintenanceRecordId) {
        if (_uiState.value.activeMutation != null) return
        val record = _uiState.value.records.firstOrNull { it.id == recordId } ?: return
        val typeCode = record.maintenanceTypeCode ?: MaintenanceTypeCode.Custom
        _uiState.update {
            it.copy(
                maintenanceTypeCode = typeCode,
                customTypeLabel = if (typeCode == MaintenanceTypeCode.Custom) record.displayType() else "",
                performedOn = record.performedOn.iso8601,
                nextDueDate = record.nextDueDate?.iso8601.orEmpty(),
                odometerKm = record.odometerKm?.toString().orEmpty(),
                cost = record.costCents.toEditableCost(),
                workshop = record.workshop.orEmpty(),
                notes = record.notes.orEmpty(),
                validationError = null,
                persistenceError = false,
                editingRecordId = record.id,
            )
        }
    }

    private fun clearEditState() {
        _uiState.update { it.resetForm(localDateProvider.currentDate()) }
    }

    private fun launchLoad(showLoading: Boolean) {
        val revision = ++loadRevision
        loadJob?.cancel()
        loadJob = scope.launch { loadHistory(showLoading, revision) }
        loadJob?.invokeOnCompletion { cause ->
            if (cause is CancellationException && showLoading && revision == loadRevision) {
                _uiState.update { it.copy(loadState = settledLoadState) }
            }
        }
    }

    private suspend fun loadHistory(
        showLoading: Boolean,
        revision: Long,
    ) {
        if (showLoading) _uiState.update { it.copy(loadState = MaintenanceLoadState.Loading) }
        try {
            val vehicle =
                withContext(dispatchers.io) {
                    vehicleRepository.observeVehicles(familyId).firstOrNull { it.id == vehicleId }
                } ?: error("Selected vehicle is unavailable")
            if (revision != loadRevision) return
            _uiState.update { it.copy(vehicle = vehicle) }
            val records = withContext(dispatchers.io) { getVehicleHistoryUseCase(vehicleId) }
            if (revision != loadRevision) return
            _uiState.update {
                it.copy(
                    records = records,
                    loadState = MaintenanceLoadState.Content,
                )
            }
            settledLoadState = MaintenanceLoadState.Content
        } catch (error: CancellationException) {
            if (showLoading && revision == loadRevision) {
                _uiState.update { it.copy(loadState = settledLoadState) }
            }
            throw error
        } catch (_: Throwable) {
            if (showLoading && revision == loadRevision) {
                _uiState.update { it.copy(loadState = MaintenanceLoadState.Error) }
                settledLoadState = MaintenanceLoadState.Error
            }
        }
    }

    private fun submitMaintenance() {
        if (_uiState.value.activeMutation != null) return
        val performedOn = _uiState.value.performedOn.toCalendarDateOrNull()
        if (performedOn != null && performedOn > localDateProvider.currentDate()) {
            _uiState.update { it.copy(showFutureReminderOffer = true) }
        } else {
            scope.launch { createMaintenance(plannedReminderChoice = null) }
        }
    }

    private fun saveFutureMaintenance(createReminder: Boolean) {
        if (!_uiState.value.showFutureReminderOffer || _uiState.value.activeMutation != null) return
        _uiState.update { it.copy(showFutureReminderOffer = false) }
        scope.launch { createMaintenance(plannedReminderChoice = createReminder) }
    }

    private suspend fun createMaintenance(plannedReminderChoice: Boolean?) {
        if (_uiState.value.activeMutation != null) return
        _uiState.update {
            it.copy(
                activeMutation = MaintenanceMutation.Saving,
                persistenceError = false,
            )
        }
        val state = _uiState.value
        val input =
            CreateMaintenanceRecordInput(
                id = pendingRecordId ?: nextRecordId().also { pendingRecordId = it },
                familyId = familyId,
                vehicleId = vehicleId,
                performedOn = state.performedOn,
                odometerKm = state.odometerKm,
                cost = state.cost,
                workshop = state.workshop,
                notes = state.notes,
                maintenanceTypeCode = state.maintenanceTypeCode,
                customTypeLabel = state.customTypeLabel,
                nextDueDate = state.nextDueDate,
            )

        try {
            when (val result = withContext(dispatchers.io) { createMaintenanceWithReminderFromInputUseCase(input) }) {
                is DomainResult.Success -> {
                    val plannedReminder =
                        when (plannedReminderChoice) {
                            true -> withContext(dispatchers.io) { createPlannedMaintenanceReminderUseCase(result.value.record) }
                            false,
                            null,
                            -> {
                                withContext(dispatchers.io) { removePlannedMaintenanceReminderUseCase(result.value.record) }
                                null
                            }
                        }
                    val records = withContext(dispatchers.io) { getVehicleHistoryUseCase(vehicleId) }
                    _uiState.update {
                        it.copy(
                            records = records,
                            maintenanceTypeCode = MaintenanceTypeCode.Itv,
                            customTypeLabel = "",
                            performedOn = localDateProvider.currentDate().iso8601,
                            nextDueDate = "",
                            odometerKm = "0",
                            cost = "",
                            workshop = "",
                            notes = "",
                            validationError = null,
                        )
                    }
                    pendingRecordId = null
                    _effects.send(
                        MaintenanceHistoryEffect.MaintenanceCreated(
                            typeCode = input.maintenanceTypeCode,
                            customTypeLabel = input.customTypeLabel.orEmpty().trim(),
                            reminderCreated = result.value.generatedReminder != null || plannedReminder != null,
                        ),
                    )
                    syncAfterMutation()
                }

                is DomainResult.ValidationError -> {
                    emitValidation(result.reason.toMaintenanceMessage())
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            _uiState.update { it.copy(persistenceError = true) }
        } finally {
            _uiState.update { it.copy(activeMutation = null) }
        }
    }

    private suspend fun updateMaintenance() {
        val state = _uiState.value
        val recordId = state.editingRecordId ?: return
        if (state.activeMutation != null) return
        val mutation = MaintenanceMutation.Updating(recordId)
        _uiState.update { it.copy(activeMutation = mutation, persistenceError = false) }
        try {
            val input =
                UpdateMaintenanceRecordInput(
                    recordId = recordId,
                    expectedFamilyId = familyId,
                    expectedVehicleId = vehicleId,
                    maintenanceTypeCode = state.maintenanceTypeCode,
                    customTypeLabel = state.customTypeLabel,
                    performedOn = state.performedOn,
                    odometerKm = state.odometerKm,
                    cost = state.cost,
                    workshop = state.workshop,
                    notes = state.notes,
                    nextDueDate = state.nextDueDate,
                    currentDate = localDateProvider.currentDate(),
                )
            when (val result = withContext(dispatchers.io) { requireNotNull(updateMaintenanceRecordUseCase)(input) }) {
                is UpdateMaintenanceRecordResult.Success -> {
                    val records = withContext(dispatchers.io) { getVehicleHistoryUseCase(vehicleId) }
                    _uiState.update { it.resetForm(localDateProvider.currentDate()).copy(records = records) }
                    _effects.send(
                        MaintenanceHistoryEffect.MaintenanceUpdated(
                            typeCode = result.record.maintenanceTypeCode ?: MaintenanceTypeCode.Custom,
                            customTypeLabel = result.record.displayType(),
                            reminderRetained = result.reminderRetained,
                        ),
                    )
                    syncAfterMutation()
                }
                is UpdateMaintenanceRecordResult.ValidationError -> emitValidation(result.reason.toMaintenanceMessage())
                UpdateMaintenanceRecordResult.NotFound -> {
                    val records = withContext(dispatchers.io) { getVehicleHistoryUseCase(vehicleId) }
                    _uiState.update {
                        it.resetForm(localDateProvider.currentDate()).copy(records = records, persistenceError = true)
                    }
                }
            }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            _uiState.update { it.copy(persistenceError = true) }
        } finally {
            _uiState.update { if (it.activeMutation == mutation) it.copy(activeMutation = null) else it }
        }
    }

    private suspend fun emitValidation(message: CarburaString) {
        _uiState.update { it.copy(validationError = message) }
        _effects.send(MaintenanceHistoryEffect.ValidationFailed(message))
    }

    private suspend fun deleteMaintenance(recordId: MaintenanceRecordId) {
        if (_uiState.value.activeMutation != null) return
        _uiState.update {
            it.copy(
                activeMutation = MaintenanceMutation.Deleting(recordId),
                persistenceError = false,
            )
        }
        val record =
            _uiState.value.records
                .firstOrNull { it.id == recordId }
        try {
            withContext(dispatchers.io) { deleteMaintenanceRecordUseCase(recordId) }
            val records = withContext(dispatchers.io) { getVehicleHistoryUseCase(vehicleId) }
            _uiState.update { it.copy(records = records, validationError = null) }
            record?.let {
                _effects.send(
                    MaintenanceHistoryEffect.MaintenanceDeleted(
                        typeCode = it.maintenanceTypeCode ?: MaintenanceTypeCode.Custom,
                        customTypeLabel = it.displayType(),
                    ),
                )
            }
            syncAfterMutation()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            _uiState.update { it.copy(persistenceError = true) }
        } finally {
            _uiState.update { it.copy(activeMutation = null) }
        }
    }

    private fun syncAfterMutation() {
        scope.launch { syncManager?.syncNow() }
    }
}

internal fun MaintenanceRecord.displayType(): String =
    maintenanceTypeLabel?.takeIf(String::isNotBlank)
        ?: maintenanceTypeId.value
            .removePrefix("type-")
            .replace('-', ' ')
            .replaceFirstChar(Char::uppercase)

private fun Int?.toEditableCost(): String {
    val cents = this ?: return ""
    return "${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
}

private fun MaintenanceHistoryUiState.resetForm(currentDate: CalendarDate): MaintenanceHistoryUiState =
    copy(
        maintenanceTypeCode = MaintenanceTypeCode.Itv,
        customTypeLabel = "",
        performedOn = currentDate.iso8601,
        nextDueDate = "",
        odometerKm = "0",
        cost = "",
        workshop = "",
        notes = "",
        validationError = null,
        persistenceError = false,
        showFutureReminderOffer = false,
        editingRecordId = null,
    )

private fun randomMaintenanceRecordId(): MaintenanceRecordId = MaintenanceRecordId("maintenance-${Random.nextInt(1, Int.MAX_VALUE)}")

private fun String.toCalendarDateOrNull(): CalendarDate? =
    try {
        CalendarDate(this)
    } catch (_: IllegalArgumentException) {
        null
    }
