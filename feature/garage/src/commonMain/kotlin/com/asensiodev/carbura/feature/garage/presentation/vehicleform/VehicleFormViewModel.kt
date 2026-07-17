package com.asensiodev.carbura.feature.garage.presentation.vehicleform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.reminder.usecase.DeriveVehicleReminderSuggestionsUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.SaveVehicleWithRemindersParams
import com.asensiodev.carbura.core.domain.reminder.usecase.SaveVehicleWithRemindersUseCase
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.domain.vehicle.usecase.CreateVehicleUseCase
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleParams
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleResult
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleUseCase
import com.asensiodev.carbura.core.model.CalendarDate
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

class VehicleFormViewModel(
    private val familyId: FamilyId,
    private val dispatchers: DispatcherProvider,
    vehicleRepository: VehicleRepository,
    private val createVehicleUseCase: CreateVehicleUseCase = CreateVehicleUseCase(vehicleRepository),
    private val updateVehicleUseCase: UpdateVehicleUseCase = UpdateVehicleUseCase(vehicleRepository),
    private val deriveVehicleReminderSuggestions: DeriveVehicleReminderSuggestionsUseCase =
        DeriveVehicleReminderSuggestionsUseCase(),
    private val saveVehicleWithRemindersUseCase: SaveVehicleWithRemindersUseCase? = null,
    private val syncManager: SyncManager? = null,
    private val nextVehicleId: () -> VehicleId = ::randomVehicleId,
    private val coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VehicleFormUiState())
    val uiState: StateFlow<VehicleFormUiState> = _uiState.asStateFlow()

    private val _effects = Channel<VehicleFormEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<VehicleFormEffect> = _effects.receiveAsFlow()
    private var editingVehicle: Vehicle? = null
    private var pendingReminderVehicle: Vehicle? = null

    private val scope: CoroutineScope
        get() = coroutineScope ?: viewModelScope

    fun onEvent(event: VehicleFormEvent) {
        if (!handleFieldEvent(event)) handleActionEvent(event)
    }

    private fun handleFieldEvent(event: VehicleFormEvent): Boolean {
        when (event) {
            is VehicleFormEvent.NameChanged -> updateCreate { it.copy(name = event.value) }
            is VehicleFormEvent.OdometerChanged -> updateCreate { it.copy(odometerKm = event.value) }
            is VehicleFormEvent.TypeSelected -> updateCreate { it.copy(selectedType = event.value) }
            is VehicleFormEvent.NextItvDateChanged -> updateCreate { it.copy(nextItvDate = event.value) }
            is VehicleFormEvent.InsuranceRenewalDateChanged ->
                updateCreate { it.copy(insuranceRenewalDate = event.value) }
            is VehicleFormEvent.NextServiceOdometerChanged ->
                updateCreate { it.copy(nextServiceOdometerKm = event.value) }
            is VehicleFormEvent.EditVehicleRequested -> startEditing(event.vehicle, VehicleEditMode.Full)
            is VehicleFormEvent.QuickOdometerUpdateRequested -> startEditing(event.vehicle, VehicleEditMode.Odometer)
            is VehicleFormEvent.EditNameChanged -> updateEdit { it.copy(editName = event.value) }
            is VehicleFormEvent.EditLicensePlateChanged -> updateEdit { it.copy(editLicensePlate = event.value) }
            is VehicleFormEvent.EditOdometerChanged -> updateEdit { it.copy(editOdometerKm = event.value) }
            is VehicleFormEvent.EditTypeSelected -> updateEdit { it.copy(editType = event.value) }
            is VehicleFormEvent.EditNextItvDateChanged -> updateEdit { it.copy(editNextItvDate = event.value) }
            is VehicleFormEvent.EditInsuranceRenewalDateChanged ->
                updateEdit { it.copy(editInsuranceRenewalDate = event.value) }
            is VehicleFormEvent.EditNextServiceOdometerChanged ->
                updateEdit { it.copy(editNextServiceOdometerKm = event.value) }
            else -> return false
        }
        return true
    }

    private fun handleActionEvent(event: VehicleFormEvent) {
        when (event) {
            VehicleFormEvent.SubmitVehicle -> launchMutation(VehicleFormMutation.Creating) { createVehicle() }
            VehicleFormEvent.SubmitVehicleEdit -> launchUpdate { updateVehicle() }
            VehicleFormEvent.ConfirmOdometerDecrease -> launchUpdate { updateVehicle(allowOdometerDecrease = true) }
            VehicleFormEvent.CancelOdometerDecrease ->
                _uiState.update { it.copy(odometerDecreaseConfirmation = null) }
            VehicleFormEvent.DismissVehicleEdit -> clearEditState()
            VehicleFormEvent.ConfirmReminderSuggestions -> launchPendingReminderSave(reconcile = true)
            VehicleFormEvent.DeclineReminderSuggestions -> launchPendingReminderSave(reconcile = false)
            else -> Unit
        }
    }

    private fun updateCreate(transform: (VehicleFormUiState) -> VehicleFormUiState) {
        _uiState.update { transform(it).copy(createValidationError = null, persistenceError = false) }
    }

    private fun updateEdit(transform: (VehicleFormUiState) -> VehicleFormUiState) {
        _uiState.update {
            transform(it).copy(
                editValidationError = null,
                persistenceError = false,
                odometerDecreaseConfirmation = null,
            )
        }
    }

    private fun startEditing(
        vehicle: Vehicle,
        mode: VehicleEditMode,
    ) {
        editingVehicle = vehicle
        _uiState.update {
            it.copy(
                editMode = mode,
                editingVehicleId = vehicle.id,
                editName = vehicle.name,
                editLicensePlate = vehicle.licensePlate.orEmpty(),
                editOdometerKm = vehicle.currentOdometerKm.toString(),
                editType = vehicle.type,
                editNextItvDate = vehicle.nextItvDate?.iso8601.orEmpty(),
                editInsuranceRenewalDate = vehicle.insuranceRenewalDate?.iso8601.orEmpty(),
                editNextServiceOdometerKm = vehicle.nextServiceOdometerKm?.toString().orEmpty(),
                editValidationError = null,
                persistenceError = false,
                odometerDecreaseConfirmation = null,
            )
        }
    }

    private fun clearEditState() {
        editingVehicle = null
        _uiState.update {
            it.copy(
                editMode = null,
                editingVehicleId = null,
                editName = "",
                editLicensePlate = "",
                editOdometerKm = "",
                editType = VehicleType.Car,
                editNextItvDate = "",
                editInsuranceRenewalDate = "",
                editNextServiceOdometerKm = "",
                editValidationError = null,
                persistenceError = false,
                odometerDecreaseConfirmation = null,
            )
        }
    }

    private fun launchUpdate(block: suspend () -> Unit) {
        val vehicleId = _uiState.value.editingVehicleId ?: return
        launchMutation(VehicleFormMutation.Updating(vehicleId), block)
    }

    private fun launchPendingReminderSave(reconcile: Boolean) {
        val state = _uiState.value
        val mutation =
            when (state.reminderConfirmationMode) {
                VehicleSaveMode.Create -> VehicleFormMutation.Creating
                VehicleSaveMode.Edit -> state.editingVehicleId?.let(VehicleFormMutation::Updating)
                null -> null
            } ?: return
        launchMutation(mutation) { completePendingReminderSave(reconcile) }
    }

    private fun launchMutation(
        mutation: VehicleFormMutation,
        block: suspend () -> Unit,
    ) {
        if (_uiState.value.activeMutation != null) return
        _uiState.update { it.copy(activeMutation = mutation, persistenceError = false) }
        scope.launch {
            try {
                block()
            } catch (_: Exception) {
                _uiState.update { it.copy(persistenceError = true) }
            } finally {
                _uiState.update {
                    if (it.activeMutation == mutation) it.copy(activeMutation = null) else it
                }
            }
        }
    }

    private suspend fun createVehicle() {
        val state = _uiState.value
        if (state.nextServiceOdometerKm.isInvalidOptionalOdometer()) {
            _uiState.update { it.copy(createValidationError = CarburaString.ValidationNegativeVehicleOdometer) }
            return
        }
        val vehicle =
            Vehicle(
                id = nextVehicleId(),
                familyId = familyId,
                name = state.name.trim(),
                type = state.selectedType,
                currentOdometerKm = state.odometerKm.toIntOrNull() ?: -1,
                nextItvDate = state.nextItvDate.toCalendarDateOrNull(),
                insuranceRenewalDate = state.insuranceRenewalDate.toCalendarDateOrNull(),
                nextServiceOdometerKm = state.nextServiceOdometerKm.toIntOrNull(),
            )
        if (showReminderConfirmation(vehicle, VehicleSaveMode.Create)) return
        persistCreatedVehicle(vehicle, reconcileReminders = false)
    }

    private suspend fun persistCreatedVehicle(
        vehicle: Vehicle,
        reconcileReminders: Boolean,
    ) {
        val result = withContext(dispatchers.io) { createVehicleUseCase(vehicle) }
        when (result) {
            is DomainResult.Success -> {
                _uiState.update {
                    it.copy(
                        name = "",
                        odometerKm = "0",
                        nextItvDate = "",
                        insuranceRenewalDate = "",
                        nextServiceOdometerKm = "",
                        createValidationError = null,
                        persistenceError = false,
                        reminderSuggestions = emptyList(),
                        reminderConfirmationMode = null,
                    )
                }
                _effects.send(VehicleFormEffect.VehicleCreated(result.value.name))
                if (reconcileReminders) reconcileReminders(result.value)
                syncAfterMutation()
            }
            is DomainResult.ValidationError -> showCreateValidation(result.reason.toGarageMessage())
        }
    }

    private suspend fun updateVehicle(
        allowOdometerDecrease: Boolean = false,
        skipReminderConfirmation: Boolean = false,
    ) {
        val state = _uiState.value
        if (state.editNextServiceOdometerKm.isInvalidOptionalOdometer()) {
            _uiState.update { it.copy(editValidationError = CarburaString.ValidationNegativeVehicleOdometer) }
            return
        }
        val vehicle = editingVehicle ?: return
        val params =
            UpdateVehicleParams(
                currentVehicle = vehicle,
                name = state.editName,
                type = state.editType,
                licensePlate = state.editLicensePlate,
                odometerKm = state.editOdometerKm.toIntOrNull() ?: -1,
                nextItvDate = state.editNextItvDate.toCalendarDateOrNull(),
                insuranceRenewalDate = state.editInsuranceRenewalDate.toCalendarDateOrNull(),
                nextServiceOdometerKm = state.editNextServiceOdometerKm.toIntOrNull(),
                allowOdometerDecrease = allowOdometerDecrease,
            )
        val candidate =
            vehicle.copy(
                name = params.name.trim(),
                type = params.type,
                licensePlate = params.licensePlate,
                currentOdometerKm = params.odometerKm,
                nextItvDate = params.nextItvDate,
                insuranceRenewalDate = params.insuranceRenewalDate,
                nextServiceOdometerKm = params.nextServiceOdometerKm,
            )
        val removedTarget =
            (vehicle.nextItvDate != null && candidate.nextItvDate == null) ||
                (vehicle.insuranceRenewalDate != null && candidate.insuranceRenewalDate == null) ||
                (vehicle.nextServiceOdometerKm != null && candidate.nextServiceOdometerKm == null)
        if (!allowOdometerDecrease &&
            !skipReminderConfirmation &&
            showReminderConfirmation(candidate, VehicleSaveMode.Edit, force = removedTarget)
        ) {
            return
        }

        when (val result = withContext(dispatchers.io) { updateVehicleUseCase(params) }) {
            is UpdateVehicleResult.Success -> {
                clearEditState()
                _effects.send(VehicleFormEffect.VehicleUpdated(result.vehicle.name))
                syncAfterMutation()
            }
            is UpdateVehicleResult.ValidationError -> {
                val message = result.reason.toGarageMessage()
                _uiState.update { it.copy(editValidationError = message) }
                _effects.send(VehicleFormEffect.ValidationFailed(message))
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

    private suspend fun showCreateValidation(message: CarburaString) {
        _uiState.update { it.copy(createValidationError = message) }
        _effects.send(VehicleFormEffect.ValidationFailed(message))
    }

    private fun showReminderConfirmation(
        vehicle: Vehicle,
        mode: VehicleSaveMode,
        force: Boolean = false,
    ): Boolean {
        val suggestions = deriveVehicleReminderSuggestions(vehicle)
        if (suggestions.isEmpty() && !force) return false
        pendingReminderVehicle = vehicle
        _uiState.update { it.copy(reminderSuggestions = suggestions, reminderConfirmationMode = mode) }
        return true
    }

    private suspend fun completePendingReminderSave(reconcile: Boolean) {
        val mode = _uiState.value.reminderConfirmationMode ?: return
        val pendingVehicle = pendingReminderVehicle ?: return
        pendingReminderVehicle = null
        _uiState.update { it.copy(reminderSuggestions = emptyList(), reminderConfirmationMode = null) }
        when (mode) {
            VehicleSaveMode.Create -> persistCreatedVehicle(pendingVehicle, reconcile)
            VehicleSaveMode.Edit -> {
                if (reconcile) reconcileReminders(pendingVehicle)
                updateVehicle(skipReminderConfirmation = true)
            }
        }
    }

    private suspend fun reconcileReminders(vehicle: Vehicle) {
        withContext(dispatchers.io) {
            saveVehicleWithRemindersUseCase?.invoke(SaveVehicleWithRemindersParams(vehicle, true))
        }
    }

    private fun syncAfterMutation() {
        scope.launch { syncManager?.syncNow() }
    }
}

private fun randomVehicleId(): VehicleId = VehicleId("vehicle-${Random.nextInt(1, Int.MAX_VALUE)}")

private fun String.toCalendarDateOrNull(): CalendarDate? = trim().takeIf { it.isNotEmpty() }?.let(::CalendarDate)

private fun String.isInvalidOptionalOdometer(): Boolean = isNotBlank() && (toIntOrNull()?.let { it < 0 } != false)

private fun ValidationFailure.toGarageMessage(): CarburaString =
    when (this) {
        ValidationFailure.BlankVehicleName -> CarburaString.ValidationBlankVehicleName
        ValidationFailure.NegativeVehicleOdometer,
        ValidationFailure.NegativeVehicleServiceOdometer,
        -> CarburaString.ValidationNegativeVehicleOdometer
        else -> CarburaString.ValidationGeneric
    }
