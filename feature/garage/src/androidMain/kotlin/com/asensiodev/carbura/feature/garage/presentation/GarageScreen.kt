package com.asensiodev.carbura.feature.garage.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.carbura.core.designsystem.ConstrainedScreen
import com.asensiodev.carbura.core.designsystem.LoadingState
import com.asensiodev.carbura.core.designsystem.RetryState
import com.asensiodev.carbura.core.designsystem.Spacings
import com.asensiodev.carbura.core.designsystem.SwipeToDeleteContainer
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageLoadState
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewEffect
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewEvent
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewUiState
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewViewModel
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleEditMode
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormEffect
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormEvent
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormMutation
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormUiState
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormViewModel
import com.asensiodev.carbura.featuregarage.R
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

@Composable
fun GarageRoute(
    familyId: String,
    refreshSignal: Long = 0L,
    modifier: Modifier = Modifier,
    onVehicleSelected: (String) -> Unit = {},
    overviewViewModel: GarageOverviewViewModel = rememberGarageOverviewViewModel(familyId),
    vehicleFormViewModel: VehicleFormViewModel = rememberVehicleFormViewModel(familyId),
) {
    val overviewState by overviewViewModel.uiState.collectAsStateWithLifecycle()
    val formState by vehicleFormViewModel.uiState.collectAsStateWithLifecycle()
    var effectMessage by remember { mutableStateOf<CarburaString?>(null) }
    var effectMessageArg by remember { mutableStateOf<String?>(null) }
    var vehicleCreatedSignal by remember { mutableStateOf(0) }
    var vehicleSuccessSignal by remember { mutableStateOf(0) }

    LaunchedEffect(overviewViewModel) {
        overviewViewModel.onEvent(GarageOverviewEvent.Started)
    }

    LaunchedEffect(overviewViewModel, refreshSignal) {
        if (refreshSignal > 0L) overviewViewModel.onEvent(GarageOverviewEvent.Refresh)
    }

    LaunchedEffect(overviewViewModel) {
        overviewViewModel.effects.collect { effect ->
            when (effect) {
                is GarageOverviewEffect.VehicleDeleted -> {
                    effectMessage = CarburaString.VehicleDeletedMessage
                    effectMessageArg = effect.vehicleName
                    vehicleSuccessSignal += 1
                }
                is GarageOverviewEffect.NavigateToVehicleHistory -> {
                    onVehicleSelected(effect.vehicleId.value)
                }
            }
        }
    }

    LaunchedEffect(vehicleFormViewModel, overviewViewModel) {
        vehicleFormViewModel.effects.collect { effect ->
            when (effect) {
                is VehicleFormEffect.VehicleCreated -> {
                    effectMessage = CarburaString.VehicleCreatedMessage
                    effectMessageArg = effect.vehicleName
                    vehicleCreatedSignal += 1
                    vehicleSuccessSignal += 1
                    overviewViewModel.onEvent(GarageOverviewEvent.Refresh)
                }
                is VehicleFormEffect.VehicleUpdated -> {
                    effectMessage = CarburaString.VehicleUpdatedMessage
                    effectMessageArg = effect.vehicleName
                    vehicleSuccessSignal += 1
                    overviewViewModel.onEvent(GarageOverviewEvent.Refresh)
                }
                is VehicleFormEffect.ValidationFailed -> {
                    effectMessage = effect.message
                    effectMessageArg = null
                }
            }
        }
    }

    val resolvedEffectMessage =
        effectMessage?.let { message ->
            val arg = effectMessageArg
            if (arg == null) {
                stringResource(message.garageStringRes())
            } else {
                stringResource(message.garageStringRes(), arg)
            }
        }

    GarageScreen(
        overviewState = overviewState,
        formState = formState,
        effectMessage = resolvedEffectMessage,
        vehicleCreatedSignal = vehicleCreatedSignal,
        vehicleSuccessSignal = vehicleSuccessSignal,
        onNameChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.NameChanged(it)) },
        onOdometerChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.OdometerChanged(it)) },
        onTypeSelected = { vehicleFormViewModel.onEvent(VehicleFormEvent.TypeSelected(it)) },
        onNextItvDateChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.NextItvDateChanged(it)) },
        onInsuranceRenewalDateChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.InsuranceRenewalDateChanged(it)) },
        onNextServiceOdometerChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.NextServiceOdometerChanged(it)) },
        onCreateVehicle = { vehicleFormViewModel.onEvent(VehicleFormEvent.SubmitVehicle) },
        onSelectVehicle = { overviewViewModel.onEvent(GarageOverviewEvent.VehicleSelected(it.id)) },
        onDeleteVehicle = { overviewViewModel.onEvent(GarageOverviewEvent.DeleteVehicleConfirmed(it.id)) },
        onEditVehicle = { vehicleFormViewModel.onEvent(VehicleFormEvent.EditVehicleRequested(it)) },
        onQuickOdometerUpdate = { vehicleFormViewModel.onEvent(VehicleFormEvent.QuickOdometerUpdateRequested(it)) },
        onEditNameChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.EditNameChanged(it)) },
        onEditLicensePlateChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.EditLicensePlateChanged(it)) },
        onEditOdometerChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.EditOdometerChanged(it)) },
        onEditTypeSelected = { vehicleFormViewModel.onEvent(VehicleFormEvent.EditTypeSelected(it)) },
        onEditNextItvDateChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.EditNextItvDateChanged(it)) },
        onEditInsuranceRenewalDateChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.EditInsuranceRenewalDateChanged(it)) },
        onEditNextServiceOdometerChange = { vehicleFormViewModel.onEvent(VehicleFormEvent.EditNextServiceOdometerChanged(it)) },
        onSubmitEdit = { vehicleFormViewModel.onEvent(VehicleFormEvent.SubmitVehicleEdit) },
        onDismissEdit = { vehicleFormViewModel.onEvent(VehicleFormEvent.DismissVehicleEdit) },
        onConfirmOdometerDecrease = { vehicleFormViewModel.onEvent(VehicleFormEvent.ConfirmOdometerDecrease) },
        onCancelOdometerDecrease = { vehicleFormViewModel.onEvent(VehicleFormEvent.CancelOdometerDecrease) },
        onConfirmReminderSuggestions = { vehicleFormViewModel.onEvent(VehicleFormEvent.ConfirmReminderSuggestions) },
        onDeclineReminderSuggestions = { vehicleFormViewModel.onEvent(VehicleFormEvent.DeclineReminderSuggestions) },
        onRetry = { overviewViewModel.onEvent(GarageOverviewEvent.Retry) },
        modifier = modifier,
    )
}

@Composable
private fun rememberGarageOverviewViewModel(familyId: String): GarageOverviewViewModel =
    remember(familyId) {
        GlobalContext.get().get { parametersOf(FamilyId(familyId)) }
    }

@Composable
private fun rememberVehicleFormViewModel(familyId: String): VehicleFormViewModel =
    remember(familyId) {
        GlobalContext.get().get { parametersOf(FamilyId(familyId)) }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GarageScreen(
    overviewState: GarageOverviewUiState,
    formState: VehicleFormUiState,
    effectMessage: String?,
    vehicleCreatedSignal: Int,
    vehicleSuccessSignal: Int,
    onNameChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onTypeSelected: (VehicleType) -> Unit,
    onNextItvDateChange: (String) -> Unit,
    onInsuranceRenewalDateChange: (String) -> Unit,
    onNextServiceOdometerChange: (String) -> Unit,
    onCreateVehicle: () -> Unit,
    onSelectVehicle: (Vehicle) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit,
    onEditVehicle: (Vehicle) -> Unit,
    onQuickOdometerUpdate: (Vehicle) -> Unit,
    onEditNameChange: (String) -> Unit,
    onEditLicensePlateChange: (String) -> Unit,
    onEditOdometerChange: (String) -> Unit,
    onEditTypeSelected: (VehicleType) -> Unit,
    onEditNextItvDateChange: (String) -> Unit,
    onEditInsuranceRenewalDateChange: (String) -> Unit,
    onEditNextServiceOdometerChange: (String) -> Unit,
    onSubmitEdit: () -> Unit,
    onDismissEdit: () -> Unit,
    onConfirmOdometerDecrease: () -> Unit,
    onCancelOdometerDecrease: () -> Unit,
    onConfirmReminderSuggestions: () -> Unit,
    onDeclineReminderSuggestions: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showVehicleSheet by remember { mutableStateOf(false) }
    var vehiclePendingDeletion by remember { mutableStateOf<Vehicle?>(null) }
    var showDiscardEditConfirmation by remember(formState.editingVehicleId) { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }
    val isCompact = LocalConfiguration.current.screenWidthDp < 600
    val showCompactAddAction =
        isCompact && overviewState.loadState == GarageLoadState.Loaded && !overviewState.isEmpty
    val showExpandedAddAction =
        !isCompact && overviewState.loadState == GarageLoadState.Loaded && !overviewState.isEmpty
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(vehicleCreatedSignal) {
        if (vehicleCreatedSignal > 0) {
            showVehicleSheet = false
        }
    }

    LaunchedEffect(vehicleSuccessSignal) {
        if (vehicleSuccessSignal > 0 && effectMessage != null) {
            snackbarHostState.showSnackbar(effectMessage)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                MediumTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.garage_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    actions = {
                        if (showExpandedAddAction) {
                            TextButton(onClick = { showVehicleSheet = true }) {
                                Text(stringResource(R.string.add_vehicle_button))
                            }
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                if (showCompactAddAction) {
                    val addVehicleLabel = stringResource(R.string.add_vehicle_fab)
                    ExtendedFloatingActionButton(
                        onClick = { showVehicleSheet = true },
                        modifier =
                            Modifier
                                .semantics { contentDescription = addVehicleLabel }
                                .testTag("garage_add_vehicle_fab"),
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                            )
                        },
                        text = { Text(addVehicleLabel) },
                    )
                }
            },
        ) { innerPadding ->
            ConstrainedScreen(modifier = Modifier.padding(innerPadding)) {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .testTag("garage_vehicle_list"),
                    contentPadding =
                        PaddingValues(
                            top = Spacings.spacing24,
                            bottom = if (showCompactAddAction) 112.dp else Spacings.spacing24,
                        ),
                    verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
                ) {
                    when (overviewState.loadState) {
                        GarageLoadState.Loading -> {
                            item {
                                LoadingState(
                                    message = stringResource(R.string.garage_loading_message),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp, max = 400.dp),
                                )
                            }
                        }
                        GarageLoadState.Error -> {
                            item {
                                RetryState(
                                    title = stringResource(R.string.garage_load_error_title),
                                    description = stringResource(R.string.garage_load_error_description),
                                    retryLabel = stringResource(R.string.retry_button),
                                    onRetry = onRetry,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp, max = 400.dp),
                                )
                            }
                        }
                        GarageLoadState.Loaded ->
                            if (overviewState.isEmpty) {
                                item {
                                    EmptyGarageCard(
                                        onAddVehicle = { showVehicleSheet = true },
                                    )
                                }
                            } else {
                                item {
                                    Text(
                                        text = stringResource(R.string.vehicle_list_title),
                                        modifier = Modifier.semantics { heading() },
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                }
                                items(overviewState.vehicles) { vehicle ->
                                    VehicleCard(
                                        vehicle = vehicle,
                                        actionsEnabled =
                                            overviewState.deletingVehicleId == null && formState.activeMutation == null,
                                        deleting = overviewState.deletingVehicleId == vehicle.id,
                                        onSelectVehicle = onSelectVehicle,
                                        onDeleteVehicle = { vehiclePendingDeletion = vehicle },
                                        onEditVehicle = onEditVehicle,
                                        onQuickOdometerUpdate = onQuickOdometerUpdate,
                                    )
                                }
                            }
                    }
                    if (overviewState.deleteError) {
                        item { PersistenceErrorText(stringResource(R.string.vehicle_delete_error)) }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .safeDrawingPadding()
                    .padding(horizontal = Spacings.spacing16),
        )
    }

    if (showVehicleSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                if (formState.activeMutation != VehicleFormMutation.Creating) showVehicleSheet = false
            },
            sheetState = sheetState,
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                VehicleForm(
                    title =
                        if (overviewState.isEmpty) {
                            stringResource(R.string.vehicle_form_title_first)
                        } else {
                            stringResource(R.string.vehicle_form_title_next)
                        },
                    name = formState.name,
                    odometer = formState.odometerKm,
                    selectedType = formState.selectedType,
                    nextItvDate = formState.nextItvDate,
                    insuranceRenewalDate = formState.insuranceRenewalDate,
                    nextServiceOdometer = formState.nextServiceOdometerKm,
                    errorMessage = formState.createValidationError,
                    persistenceError = formState.persistenceError,
                    isSaving = formState.activeMutation == VehicleFormMutation.Creating,
                    onNameChange = onNameChange,
                    onOdometerChange = onOdometerChange,
                    onTypeSelected = onTypeSelected,
                    onNextItvDateChange = onNextItvDateChange,
                    onInsuranceRenewalDateChange = onInsuranceRenewalDateChange,
                    onNextServiceOdometerChange = onNextServiceOdometerChange,
                    onCreateVehicle = onCreateVehicle,
                    modifier =
                        Modifier
                            .widthIn(max = 720.dp)
                            .padding(
                                start = Spacings.spacing24,
                                end = Spacings.spacing24,
                                bottom = Spacings.spacing24,
                            ),
                )
            }
        }
    }

    vehiclePendingDeletion?.let { vehicle ->
        VehicleDeleteDialog(
            vehicle = vehicle,
            onConfirm = {
                vehiclePendingDeletion = null
                onDeleteVehicle(vehicle)
            },
            onDismiss = { vehiclePendingDeletion = null },
        )
    }

    if (formState.editingVehicleId != null && formState.odometerDecreaseConfirmation == null) {
        val isSavingEdit = formState.activeMutation is VehicleFormMutation.Updating
        val requestFullEditDismiss = {
            if (!isSavingEdit) {
                if (formState.isEditDirty) showDiscardEditConfirmation = true else onDismissEdit()
            }
        }
        when {
            formState.editMode == VehicleEditMode.Full && isCompact ->
                FullScreenVehicleEditor(
                    state = formState,
                    isSaving = isSavingEdit,
                    onDismissRequest = requestFullEditDismiss,
                    onNameChange = onEditNameChange,
                    onLicensePlateChange = onEditLicensePlateChange,
                    onOdometerChange = onEditOdometerChange,
                    onTypeSelected = onEditTypeSelected,
                    onNextItvDateChange = onEditNextItvDateChange,
                    onInsuranceRenewalDateChange = onEditInsuranceRenewalDateChange,
                    onNextServiceOdometerChange = onEditNextServiceOdometerChange,
                    onSubmit = onSubmitEdit,
                )
            else ->
                VehicleEditDialog(
                    state = formState,
                    fullEdit = formState.editMode == VehicleEditMode.Full,
                    isSaving = isSavingEdit,
                    onDismissRequest =
                        if (formState.editMode == VehicleEditMode.Full) requestFullEditDismiss else onDismissEdit,
                    onNameChange = onEditNameChange,
                    onLicensePlateChange = onEditLicensePlateChange,
                    onOdometerChange = onEditOdometerChange,
                    onTypeSelected = onEditTypeSelected,
                    onNextItvDateChange = onEditNextItvDateChange,
                    onInsuranceRenewalDateChange = onEditInsuranceRenewalDateChange,
                    onNextServiceOdometerChange = onEditNextServiceOdometerChange,
                    onSubmit = onSubmitEdit,
                )
        }
    }

    if (showDiscardEditConfirmation) {
        AlertDialog(
            onDismissRequest = { showDiscardEditConfirmation = false },
            title = { Text(stringResource(R.string.discard_vehicle_changes_title)) },
            text = { Text(stringResource(R.string.discard_vehicle_changes_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardEditConfirmation = false
                    onDismissEdit()
                }) {
                    Text(stringResource(R.string.discard_changes_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardEditConfirmation = false }) {
                    Text(stringResource(R.string.keep_editing_button))
                }
            },
        )
    }

    formState.odometerDecreaseConfirmation?.let { confirmation ->
        AlertDialog(
            onDismissRequest = onCancelOdometerDecrease,
            title = { Text(stringResource(R.string.odometer_decrease_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.odometer_decrease_message,
                        confirmation.currentOdometerKm,
                        confirmation.proposedOdometerKm,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmOdometerDecrease) {
                    Text(stringResource(R.string.confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelOdometerDecrease) {
                    Text(stringResource(R.string.cancel_button))
                }
            },
        )
    }

    if (formState.reminderConfirmationMode != null) {
        AlertDialog(
            onDismissRequest = onDeclineReminderSuggestions,
            title = { Text(stringResource(R.string.reminder_suggestions_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
                    Text(stringResource(R.string.reminder_suggestions_description))
                    formState.reminderSuggestions.forEach { suggestion ->
                        Text("- ${suggestion.reminder.title}")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmReminderSuggestions) {
                    Text(stringResource(R.string.create_reminders_button))
                }
            },
            dismissButton = {
                TextButton(onClick = onDeclineReminderSuggestions) {
                    Text(stringResource(R.string.save_without_reminders_button))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenVehicleEditor(
    state: VehicleFormUiState,
    isSaving: Boolean,
    onDismissRequest: () -> Unit,
    onNameChange: (String) -> Unit,
    onLicensePlateChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onTypeSelected: (VehicleType) -> Unit,
    onNextItvDateChange: (String) -> Unit,
    onInsuranceRenewalDateChange: (String) -> Unit,
    onNextServiceOdometerChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    BackHandler(onBack = onDismissRequest)
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    Surface(
        modifier = Modifier.fillMaxSize().testTag("full_screen_vehicle_editor"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(stringResource(R.string.edit_vehicle_title))
                            Text(
                                text = state.editName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest, enabled = !isSaving) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.close_vehicle_editor),
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                )
            },
            bottomBar = {
                if (!imeVisible) {
                    Surface(shadowElevation = 8.dp) {
                        Button(
                            onClick = onSubmit,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(Spacings.spacing16)
                                    .testTag("full_screen_vehicle_save"),
                            enabled = !isSaving,
                        ) {
                            Text(stringResource(if (isSaving) R.string.saving_vehicle else R.string.save_changes_button))
                        }
                    }
                }
            },
        ) { contentPadding ->
            ConstrainedScreen(
                modifier = Modifier.padding(contentPadding),
                contentPadding = PaddingValues(vertical = Spacings.spacing16),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
                ) {
                    Text(
                        text = stringResource(R.string.vehicle_details_section_title),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleLarge,
                    )
                    VehicleEditFields(
                        state = state,
                        fullEdit = true,
                        onNameChange = onNameChange,
                        onLicensePlateChange = onLicensePlateChange,
                        onOdometerChange = onOdometerChange,
                        onTypeSelected = onTypeSelected,
                        onNextItvDateChange = onNextItvDateChange,
                        onInsuranceRenewalDateChange = onInsuranceRenewalDateChange,
                        onNextServiceOdometerChange = onNextServiceOdometerChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleEditDialog(
    state: VehicleFormUiState,
    fullEdit: Boolean,
    isSaving: Boolean,
    onDismissRequest: () -> Unit,
    onNameChange: (String) -> Unit,
    onLicensePlateChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onTypeSelected: (VehicleType) -> Unit,
    onNextItvDateChange: (String) -> Unit,
    onInsuranceRenewalDateChange: (String) -> Unit,
    onNextServiceOdometerChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(if (fullEdit) R.string.edit_vehicle_title else R.string.update_odometer_title))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).imePadding(),
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
            ) {
                VehicleEditFields(
                    state = state,
                    fullEdit = fullEdit,
                    onNameChange = onNameChange,
                    onLicensePlateChange = onLicensePlateChange,
                    onOdometerChange = onOdometerChange,
                    onTypeSelected = onTypeSelected,
                    onNextItvDateChange = onNextItvDateChange,
                    onInsuranceRenewalDateChange = onInsuranceRenewalDateChange,
                    onNextServiceOdometerChange = onNextServiceOdometerChange,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSubmit, enabled = !isSaving) {
                Text(stringResource(if (isSaving) R.string.saving_vehicle else R.string.save_changes_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, enabled = !isSaving) {
                Text(stringResource(R.string.cancel_button))
            }
        },
    )
}

@Composable
private fun VehicleEditFields(
    state: VehicleFormUiState,
    fullEdit: Boolean,
    onNameChange: (String) -> Unit,
    onLicensePlateChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onTypeSelected: (VehicleType) -> Unit,
    onNextItvDateChange: (String) -> Unit,
    onInsuranceRenewalDateChange: (String) -> Unit,
    onNextServiceOdometerChange: (String) -> Unit,
) {
    if (fullEdit) {
        val nameError = state.editValidationError == CarburaString.ValidationBlankVehicleName
        OutlinedTextField(
            value = state.editName,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth().testTag("vehicle_edit_name"),
            label = { Text(stringResource(R.string.vehicle_name_label)) },
            isError = nameError,
            supportingText = if (nameError) ({ ValidationErrorText(state.editValidationError) }) else null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true,
        )
        VehicleTypeSelector(state.editType, onTypeSelected)
        OutlinedTextField(
            value = state.editLicensePlate,
            onValueChange = onLicensePlateChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.license_plate_label)) },
            singleLine = true,
        )
        OptionalDatePickerField(
            label = stringResource(R.string.next_itv_date_label),
            value = state.editNextItvDate,
            onValueChange = onNextItvDateChange,
        )
        OptionalDatePickerField(
            label = stringResource(R.string.insurance_renewal_date_label),
            value = state.editInsuranceRenewalDate,
            onValueChange = onInsuranceRenewalDateChange,
        )
        OutlinedTextField(
            value = state.editNextServiceOdometerKm,
            onValueChange = onNextServiceOdometerChange,
            modifier = Modifier.fillMaxWidth().testTag("vehicle_edit_next_service_odometer"),
            label = { Text(stringResource(R.string.next_service_odometer_label)) },
            isError =
                state.editValidationError == CarburaString.ValidationNegativeVehicleOdometer &&
                    state.editNextServiceOdometerKm.isInvalidOdometerInput(),
            supportingText =
                if (
                    state.editValidationError == CarburaString.ValidationNegativeVehicleOdometer &&
                    state.editNextServiceOdometerKm.isInvalidOdometerInput()
                ) {
                    { ValidationErrorText(state.editValidationError) }
                } else {
                    null
                },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
    }
    OutlinedTextField(
        value = state.editOdometerKm,
        onValueChange = onOdometerChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.current_odometer_label)) },
        isError =
            state.editValidationError == CarburaString.ValidationNegativeVehicleOdometer &&
                !state.editNextServiceOdometerKm.isInvalidOdometerInput(),
        supportingText =
            if (
                state.editValidationError == CarburaString.ValidationNegativeVehicleOdometer &&
                !state.editNextServiceOdometerKm.isInvalidOdometerInput()
            ) {
                { ValidationErrorText(state.editValidationError) }
            } else {
                null
            },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
    )
    if (state.persistenceError) {
        PersistenceErrorText(stringResource(R.string.vehicle_save_error))
    }
}

@Composable
internal fun VehicleDeleteDialog(
    vehicle: Vehicle,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_vehicle_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
                Text(stringResource(R.string.delete_vehicle_dialog_description))
                Text(vehicle.name, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete_vehicle_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.delete_vehicle_cancel_button))
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionalDatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = value.toUtcMillisOrNull())
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(value.ifBlank { stringResource(R.string.select_date_button) })
            }
            if (value.isNotBlank()) {
                TextButton(onClick = { onValueChange("") }) {
                    Text(stringResource(R.string.clear_date_button))
                }
            }
        }
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onValueChange(it.toIsoDate()) }
                    showDatePicker = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun ValidationErrorText(errorMessage: CarburaString?) {
    if (errorMessage == null) return
    val message = stringResource(errorMessage.garageStringRes())
    Text(
        text = message,
        modifier =
            Modifier.semantics {
                error(message)
                liveRegion = LiveRegionMode.Assertive
            },
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun PersistenceErrorText(message: String) {
    Text(
        text = message,
        modifier =
            Modifier.semantics {
                error(message)
                liveRegion = LiveRegionMode.Assertive
            },
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
internal fun VehicleForm(
    title: String,
    name: String,
    odometer: String,
    selectedType: VehicleType,
    nextItvDate: String,
    insuranceRenewalDate: String,
    nextServiceOdometer: String,
    errorMessage: CarburaString?,
    persistenceError: Boolean,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onTypeSelected: (VehicleType) -> Unit,
    onNextItvDateChange: (String) -> Unit,
    onInsuranceRenewalDateChange: (String) -> Unit,
    onNextServiceOdometerChange: (String) -> Unit,
    onCreateVehicle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
            )
            val nameError = errorMessage == CarburaString.ValidationBlankVehicleName
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.vehicle_name_label)) },
                isError = nameError,
                supportingText =
                    if (nameError) {
                        { ValidationErrorText(errorMessage) }
                    } else {
                        null
                    },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
            )
            OptionalDatePickerField(
                label = stringResource(R.string.next_itv_date_label),
                value = nextItvDate,
                onValueChange = onNextItvDateChange,
            )
            OptionalDatePickerField(
                label = stringResource(R.string.insurance_renewal_date_label),
                value = insuranceRenewalDate,
                onValueChange = onInsuranceRenewalDateChange,
            )
            OutlinedTextField(
                value = nextServiceOdometer,
                onValueChange = onNextServiceOdometerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.next_service_odometer_label)) },
                isError =
                    errorMessage == CarburaString.ValidationNegativeVehicleOdometer &&
                        nextServiceOdometer.isInvalidOdometerInput(),
                supportingText =
                    if (
                        errorMessage == CarburaString.ValidationNegativeVehicleOdometer &&
                        nextServiceOdometer.isInvalidOdometerInput()
                    ) {
                        { ValidationErrorText(errorMessage) }
                    } else {
                        null
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            VehicleTypeSelector(
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
            )
            OutlinedTextField(
                value = odometer,
                onValueChange = onOdometerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.current_odometer_label)) },
                isError =
                    errorMessage == CarburaString.ValidationNegativeVehicleOdometer &&
                        !nextServiceOdometer.isInvalidOdometerInput(),
                supportingText =
                    if (
                        errorMessage == CarburaString.ValidationNegativeVehicleOdometer &&
                        !nextServiceOdometer.isInvalidOdometerInput()
                    ) {
                        { ValidationErrorText(errorMessage) }
                    } else {
                        null
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            if (errorMessage != null && !nameError && errorMessage != CarburaString.ValidationNegativeVehicleOdometer) {
                ValidationErrorText(errorMessage)
            }
            if (persistenceError) {
                PersistenceErrorText(stringResource(R.string.vehicle_save_error))
            }
            Button(
                onClick = onCreateVehicle,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
            ) {
                Text(stringResource(if (isSaving) R.string.saving_vehicle else R.string.save_vehicle_button))
            }
        }
    }
}

@Composable
internal fun VehicleTypeSelector(
    selectedType: VehicleType,
    onTypeSelected: (VehicleType) -> Unit,
) {
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
    ) {
        Text(
            text = stringResource(R.string.vehicle_type_label),
            style = MaterialTheme.typography.titleSmall,
        )
        VehicleType.entries.forEach { type ->
            val selected = selectedType == type
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selected,
                            onClick = { onTypeSelected(type) },
                            role = Role.RadioButton,
                        ).padding(vertical = Spacings.spacing8),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
            ) {
                RadioButton(selected = selected, onClick = null)
                Text(type.label())
            }
        }
    }
}

@Composable
private fun EmptyGarageCard(onAddVehicle: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(Spacings.spacing24),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Text(
                text = stringResource(R.string.empty_garage_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.empty_garage_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.empty_garage_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Button(onClick = onAddVehicle) {
                Text(stringResource(R.string.add_first_vehicle_button))
            }
        }
    }
}

@Composable
internal fun VehicleCard(
    vehicle: Vehicle,
    actionsEnabled: Boolean,
    deleting: Boolean,
    onSelectVehicle: (Vehicle) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit,
    onEditVehicle: (Vehicle) -> Unit,
    onQuickOdometerUpdate: (Vehicle) -> Unit,
) {
    SwipeToDeleteContainer(
        actionLabel = stringResource(R.string.delete_vehicle_confirm_button),
        accessibilityLabel = stringResource(R.string.delete_vehicle_content_description, vehicle.name),
        enabled = actionsEnabled && !deleting,
        onDeleteRequest = { onDeleteVehicle(vehicle) },
        modifier = Modifier.fillMaxWidth().testTag("vehicle_card_${vehicle.id.value}"),
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacings.spacing16),
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Spacings.spacing4),
                    ) {
                        Text(
                            text = vehicle.name,
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = vehicle.type.label(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        vehicle.licensePlate?.takeIf { it.isNotBlank() }?.let { plate ->
                            Text(
                                text = plate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    IconButton(
                        onClick = { onEditVehicle(vehicle) },
                        modifier = Modifier.size(48.dp),
                        enabled = actionsEnabled,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_vehicle_content_description, vehicle.name),
                        )
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacings.spacing16, vertical = Spacings.spacing12),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.odometer_summary_label),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.odometer_summary_value, vehicle.currentOdometerKm),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Button(
                    onClick = { onSelectVehicle(vehicle) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = actionsEnabled,
                ) {
                    Text(stringResource(R.string.view_history_button))
                }
                OutlinedButton(
                    onClick = { onQuickOdometerUpdate(vehicle) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = actionsEnabled,
                ) {
                    Text(stringResource(R.string.update_odometer_button))
                }
                if (deleting) Text(stringResource(R.string.deleting_vehicle))
            }
        }
    }
}

@Composable
private fun VehicleType.label(): String =
    when (this) {
        VehicleType.Car -> stringResource(R.string.vehicle_type_car)
        VehicleType.Motorcycle -> stringResource(R.string.vehicle_type_motorcycle)
        VehicleType.Van -> stringResource(R.string.vehicle_type_van)
        VehicleType.Other -> stringResource(R.string.vehicle_type_other)
    }

private fun String.toUtcMillisOrNull(): Long? =
    try {
        LocalDate
            .parse(this)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }

private fun Long.toIsoDate(): String =
    Instant
        .ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toString()

private fun String.isInvalidOdometerInput(): Boolean = isNotBlank() && (toIntOrNull()?.let { it < 0 } != false)
