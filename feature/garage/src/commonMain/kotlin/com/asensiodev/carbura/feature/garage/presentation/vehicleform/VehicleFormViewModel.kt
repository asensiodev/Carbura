package com.asensiodev.carbura.feature.garage.presentation.vehicleform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.reminder.usecase.DeriveVehicleReminderSuggestionsUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.SaveVehicleWithRemindersParams
import com.asensiodev.carbura.core.domain.reminder.usecase.SaveVehicleWithRemindersUseCase
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.validation.NumericInputResult
import com.asensiodev.carbura.core.domain.validation.parseNonNegativeIntInput
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.domain.vehicle.usecase.CreateVehicleUseCase
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleParams
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleResult
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleUseCase
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
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

class VehicleFormViewModel(
    scope: ActiveFamilyScope,
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
    private val activeScope = scope
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
            VehicleFormEvent.ResetCreateForm -> resetCreateForm()
            VehicleFormEvent.SubmitVehicleEdit -> launchUpdate { updateVehicle() }
            VehicleFormEvent.ConfirmOdometerDecrease -> launchUpdate { updateVehicle(allowOdometerDecrease = true) }
            VehicleFormEvent.CancelOdometerDecrease ->
                _uiState.update { it.copy(odometerDecreaseConfirmation = null) }
            VehicleFormEvent.DismissVehicleEdit -> clearEditState()
            VehicleFormEvent.ConfirmReminderSuggestions -> launchPendingReminderSave(reconcile = true)
            VehicleFormEvent.DeclineReminderSuggestions -> launchPendingReminderSave(reconcile = false)
            VehicleFormEvent.DismissReminderSuggestions -> dismissReminderSuggestions()
            else -> Unit
        }
    }

    private fun updateCreate(transform: (VehicleFormUiState) -> VehicleFormUiState) {
        _uiState.update { transform(it).copy(createValidationError = null, persistenceError = false) }
    }

    private fun resetCreateForm() {
        if (_uiState.value.activeMutation != null) return
        pendingReminderVehicle = null
        _uiState.update {
            it.copy(
                name = "",
                odometerKm = "0",
                selectedType = VehicleType.Car,
                nextItvDate = "",
                insuranceRenewalDate = "",
                nextServiceOdometerKm = "",
                createValidationError = null,
                persistenceError = false,
                reminderSuggestions = emptyList(),
                reminderConfirmationMode = null,
            )
        }
    }

    private fun dismissReminderSuggestions() {
        if (_uiState.value.activeMutation != null) return
        pendingReminderVehicle = null
        _uiState.update { it.copy(reminderSuggestions = emptyList(), reminderConfirmationMode = null) }
    }

    private fun updateEdit(transform: (VehicleFormUiState) -> VehicleFormUiState) {
        _uiState.update {
            val updated = transform(it)
            updated.copy(
                editValidationError = null,
                persistenceError = false,
                odometerDecreaseConfirmation = null,
                isEditDirty = updated.differsFrom(editingVehicle),
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
                isEditDirty = false,
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
                isEditDirty = false,
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
        scope.launch {
            if (_uiState.value.activeMutation != null) return@launch
            _uiState.update { it.copy(activeMutation = mutation, persistenceError = false) }
            try {
                block()
            } catch (error: CancellationException) {
                throw error
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
        val inputs = parseVehicleInputs(state, edit = false) ?: return
        val vehicle =
            Vehicle(
                id = nextVehicleId(),
                familyId = activeScope.familyId,
                name = state.name.trim(),
                type = state.selectedType,
                currentOdometerKm = inputs.odometerKm,
                nextItvDate = inputs.nextItvDate,
                insuranceRenewalDate = inputs.insuranceRenewalDate,
                nextServiceOdometerKm = inputs.nextServiceOdometerKm,
            )
        if (showReminderConfirmation(vehicle, VehicleSaveMode.Create)) return
        persistCreatedVehicle(vehicle, reconcileReminders = false)
    }

    private suspend fun persistCreatedVehicle(
        vehicle: Vehicle,
        reconcileReminders: Boolean,
    ) {
        val result = withContext(dispatchers.io) { createVehicleUseCase(FamilyScoped(activeScope, vehicle)) }
        when (result) {
            is DomainResult.Success -> {
                if (reconcileReminders) reconcileReminders(result.value)
                _uiState.update {
                    it.copy(
                        name = "",
                        odometerKm = "0",
                        selectedType = VehicleType.Car,
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
        val inputs = parseVehicleInputs(state, edit = true) ?: return
        val vehicle = editingVehicle ?: return
        val params =
            UpdateVehicleParams(
                scope = activeScope,
                currentVehicle = vehicle,
                name = state.editName,
                type = state.editType,
                licensePlate = state.editLicensePlate,
                odometerKm = inputs.odometerKm,
                nextItvDate = inputs.nextItvDate,
                insuranceRenewalDate = inputs.insuranceRenewalDate,
                nextServiceOdometerKm = inputs.nextServiceOdometerKm,
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
        val planningTargetsChanged =
            vehicle.nextItvDate != candidate.nextItvDate ||
                vehicle.insuranceRenewalDate != candidate.insuranceRenewalDate ||
                vehicle.nextServiceOdometerKm != candidate.nextServiceOdometerKm
        val shouldConfirmReminderChanges =
            !allowOdometerDecrease && !skipReminderConfirmation && planningTargetsChanged
        if (shouldConfirmReminderChanges && showReminderConfirmation(candidate, VehicleSaveMode.Edit, force = removedTarget)) {
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

    private suspend fun parseRequiredOdometer(
        input: String,
        edit: Boolean,
    ): Int? =
        when (val result = input.parseNonNegativeIntInput()) {
            NumericInputResult.Negative -> rejectVehicleInput(CarburaString.ValidationNegativeVehicleOdometer, edit)
            NumericInputResult.Blank,
            NumericInputResult.Invalid,
            -> rejectVehicleInput(CarburaString.ValidationInvalidVehicleOdometer, edit)
            is NumericInputResult.Value -> result.value
        }

    private suspend fun parseVehicleInputs(
        state: VehicleFormUiState,
        edit: Boolean,
    ): ParsedVehicleInputs? {
        val odometerInput = if (edit) state.editOdometerKm else state.odometerKm
        val serviceInput = if (edit) state.editNextServiceOdometerKm else state.nextServiceOdometerKm
        val itvInput = if (edit) state.editNextItvDate else state.nextItvDate
        val insuranceInput = if (edit) state.editInsuranceRenewalDate else state.insuranceRenewalDate
        val odometerKm = parseRequiredOdometer(odometerInput, edit) ?: return null
        val serviceOdometerKm = parseOptionalServiceOdometer(serviceInput, edit) ?: return null
        val nextItvDate = parseOptionalDate(itvInput, CarburaString.ValidationInvalidVehicleItvDate, edit) ?: return null
        val insuranceRenewalDate =
            parseOptionalDate(insuranceInput, CarburaString.ValidationInvalidVehicleInsuranceDate, edit) ?: return null
        return ParsedVehicleInputs(
            odometerKm = odometerKm,
            nextServiceOdometerKm = serviceOdometerKm.value,
            nextItvDate = nextItvDate.value,
            insuranceRenewalDate = insuranceRenewalDate.value,
        )
    }

    private suspend fun parseOptionalServiceOdometer(
        input: String,
        edit: Boolean,
    ): ParsedOptionalInt? =
        when (val result = input.parseNonNegativeIntInput()) {
            NumericInputResult.Blank -> ParsedOptionalInt(null)
            NumericInputResult.Negative -> rejectVehicleInput(CarburaString.ValidationNegativeVehicleServiceOdometer, edit)
            NumericInputResult.Invalid -> rejectVehicleInput(CarburaString.ValidationInvalidVehicleServiceOdometer, edit)
            is NumericInputResult.Value -> ParsedOptionalInt(result.value)
        }

    private suspend fun parseOptionalDate(
        input: String,
        invalidMessage: CarburaString,
        edit: Boolean,
    ): ParsedOptionalDate? {
        val value = input.trim()
        if (value.isEmpty()) return ParsedOptionalDate(null)
        val date =
            try {
                CalendarDate(value)
            } catch (_: IllegalArgumentException) {
                return rejectVehicleInput(invalidMessage, edit)
            }
        return ParsedOptionalDate(date)
    }

    private suspend fun <T> rejectVehicleInput(
        message: CarburaString,
        edit: Boolean,
    ): T? {
        _uiState.update {
            if (edit) it.copy(editValidationError = message) else it.copy(createValidationError = message)
        }
        _effects.send(VehicleFormEffect.ValidationFailed(message))
        return null
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
            saveVehicleWithRemindersUseCase?.invoke(SaveVehicleWithRemindersParams(activeScope, vehicle, true))
        }
    }

    private fun syncAfterMutation() {
        scope.launch { syncManager?.syncNow() }
    }
}

private fun randomVehicleId(): VehicleId = VehicleId("vehicle-${Random.nextInt(1, Int.MAX_VALUE)}")

private data class ParsedOptionalInt(
    val value: Int?,
)

private data class ParsedOptionalDate(
    val value: CalendarDate?,
)

private data class ParsedVehicleInputs(
    val odometerKm: Int,
    val nextServiceOdometerKm: Int?,
    val nextItvDate: CalendarDate?,
    val insuranceRenewalDate: CalendarDate?,
)

private fun VehicleFormUiState.differsFrom(vehicle: Vehicle?): Boolean =
    vehicle != null &&
        (
            editName != vehicle.name ||
                editLicensePlate != vehicle.licensePlate.orEmpty() ||
                editOdometerKm != vehicle.currentOdometerKm.toString() ||
                editType != vehicle.type ||
                editNextItvDate != vehicle.nextItvDate?.iso8601.orEmpty() ||
                editInsuranceRenewalDate != vehicle.insuranceRenewalDate?.iso8601.orEmpty() ||
                editNextServiceOdometerKm != vehicle.nextServiceOdometerKm?.toString().orEmpty()
        )

private fun ValidationFailure.toGarageMessage(): CarburaString =
    when (this) {
        ValidationFailure.BlankVehicleName -> CarburaString.ValidationBlankVehicleName
        ValidationFailure.InvalidVehicleOdometer,
        -> CarburaString.ValidationInvalidVehicleOdometer
        ValidationFailure.InvalidVehicleServiceOdometer -> CarburaString.ValidationInvalidVehicleServiceOdometer
        ValidationFailure.NegativeVehicleOdometer,
        -> CarburaString.ValidationNegativeVehicleOdometer
        ValidationFailure.NegativeVehicleServiceOdometer -> CarburaString.ValidationNegativeVehicleServiceOdometer
        else -> CarburaString.ValidationGeneric
    }
