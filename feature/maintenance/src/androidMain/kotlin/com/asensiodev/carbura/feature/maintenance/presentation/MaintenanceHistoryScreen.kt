package com.asensiodev.carbura.feature.maintenance.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.carbura.core.designsystem.Spacings
import com.asensiodev.carbura.core.designsystem.SwipeToDeleteContainer
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featuremaintenance.R
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

@Composable
fun MaintenanceHistoryRoute(
    vehicleId: String,
    familyId: String,
    onBack: () -> Unit,
    refreshSignal: Long = 0L,
    modifier: Modifier = Modifier,
    viewModel: MaintenanceHistoryViewModel = rememberMaintenanceHistoryViewModel(vehicleId, familyId),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var effectMessage by remember { mutableStateOf<CarburaString?>(null) }
    var effectMessageArg by remember { mutableStateOf<String?>(null) }
    var createdFeedback by remember { mutableStateOf<MaintenanceHistoryEffect.MaintenanceCreated?>(null) }
    var maintenanceCreatedSignal by remember { mutableStateOf(0) }
    var maintenanceSuccessSignal by remember { mutableStateOf(0) }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(MaintenanceHistoryEvent.Started)
    }

    LaunchedEffect(viewModel, refreshSignal) {
        if (refreshSignal > 0L) viewModel.onEvent(MaintenanceHistoryEvent.Refresh)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MaintenanceHistoryEffect.MaintenanceCreated -> {
                    effectMessage = null
                    effectMessageArg = null
                    createdFeedback = effect
                    maintenanceCreatedSignal += 1
                    maintenanceSuccessSignal += 1
                }

                is MaintenanceHistoryEffect.MaintenanceDeleted -> {
                    createdFeedback = null
                    effectMessage = CarburaString.MaintenanceDeletedMessage
                    effectMessageArg = effect.type
                    maintenanceSuccessSignal += 1
                }

                is MaintenanceHistoryEffect.ValidationFailed -> {
                    createdFeedback = null
                    effectMessage = effect.message
                    effectMessageArg = null
                }
            }
        }
    }

    val resolvedEffectMessage =
        createdFeedback?.let { feedback ->
            stringResource(
                if (feedback.reminderCreated) {
                    R.string.maintenance_and_reminder_created_message
                } else {
                    R.string.maintenance_created_message
                },
                feedback.typeCode.localizedLabel(feedback.customTypeLabel),
            )
        } ?: effectMessage?.let { message ->
            val arg = effectMessageArg
            if (arg == null) {
                stringResource(message.maintenanceStringRes())
            } else {
                stringResource(message.maintenanceStringRes(), arg)
            }
        }

    MaintenanceHistoryScreen(
        state = uiState,
        effectMessage = resolvedEffectMessage,
        maintenanceCreatedSignal = maintenanceCreatedSignal,
        maintenanceSuccessSignal = maintenanceSuccessSignal,
        onBack = onBack,
        onTypeSelected = { viewModel.onEvent(MaintenanceHistoryEvent.TypeSelected(it)) },
        onCustomTypeLabelChange = { viewModel.onEvent(MaintenanceHistoryEvent.CustomTypeLabelChanged(it)) },
        onPerformedOnChange = { viewModel.onEvent(MaintenanceHistoryEvent.PerformedOnChanged(it)) },
        onNextDueDateChange = { viewModel.onEvent(MaintenanceHistoryEvent.NextDueDateChanged(it)) },
        onOdometerChange = { viewModel.onEvent(MaintenanceHistoryEvent.OdometerChanged(it)) },
        onCostChange = { viewModel.onEvent(MaintenanceHistoryEvent.CostChanged(it)) },
        onWorkshopChange = { viewModel.onEvent(MaintenanceHistoryEvent.WorkshopChanged(it)) },
        onNotesChange = { viewModel.onEvent(MaintenanceHistoryEvent.NotesChanged(it)) },
        onSubmitMaintenance = { viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance) },
        onDeleteMaintenance = { viewModel.onEvent(MaintenanceHistoryEvent.DeleteMaintenance(it.id)) },
        onRetry = { viewModel.onEvent(MaintenanceHistoryEvent.Retry) },
        modifier = modifier,
    )
}

@Composable
private fun rememberMaintenanceHistoryViewModel(
    vehicleId: String,
    familyId: String,
): MaintenanceHistoryViewModel =
    remember(vehicleId, familyId) {
        GlobalContext.get().get { parametersOf(VehicleId(vehicleId), FamilyId(familyId)) }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaintenanceHistoryScreen(
    state: MaintenanceHistoryUiState,
    effectMessage: String?,
    maintenanceCreatedSignal: Int,
    maintenanceSuccessSignal: Int,
    onBack: () -> Unit,
    onTypeSelected: (MaintenanceTypeCode) -> Unit,
    onCustomTypeLabelChange: (String) -> Unit,
    onPerformedOnChange: (String) -> Unit,
    onNextDueDateChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onWorkshopChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmitMaintenance: () -> Unit,
    onDeleteMaintenance: (MaintenanceRecord) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMaintenanceForm by remember { mutableStateOf(false) }
    var recordPendingDeletion by remember { mutableStateOf<MaintenanceRecord?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val isCompact = LocalConfiguration.current.screenWidthDp < 600
    val canAddMaintenance = state.vehicle != null

    LaunchedEffect(maintenanceCreatedSignal) {
        if (maintenanceCreatedSignal > 0) {
            showMaintenanceForm = false
        }
    }

    LaunchedEffect(maintenanceSuccessSignal) {
        if (maintenanceSuccessSignal > 0 && effectMessage != null) {
            snackbarHostState.showSnackbar(effectMessage)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.maintenance_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.maintenance_back_button),
                            )
                        }
                    },
                    actions = {
                        if (!isCompact && canAddMaintenance) {
                            TextButton(onClick = { showMaintenanceForm = true }) {
                                Text(stringResource(R.string.add_maintenance_fab))
                            }
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                )
            },
            floatingActionButton = {
                if (isCompact && canAddMaintenance) {
                    ExtendedFloatingActionButton(
                        onClick = { showMaintenanceForm = true },
                        modifier = Modifier.testTag("add_maintenance_fab"),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                            )
                        },
                        text = { Text(stringResource(R.string.add_maintenance_fab)) },
                    )
                }
            },
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .widthIn(max = 720.dp),
                    contentPadding =
                        PaddingValues(
                            start = Spacings.spacing24,
                            top = Spacings.spacing24,
                            end = Spacings.spacing24,
                            bottom = if (isCompact && canAddMaintenance) 104.dp else Spacings.spacing24,
                        ),
                    verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
                ) {
                    state.vehicle?.let { vehicle ->
                        item { VehicleContextCard(vehicle) }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.maintenance_history_title),
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }

                    if (state.persistenceError) {
                        item {
                            val message = stringResource(R.string.maintenance_persistence_error)
                            Text(
                                text = message,
                                modifier =
                                    Modifier.semantics {
                                        liveRegion = LiveRegionMode.Assertive
                                        error(message)
                                    },
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    when (state.loadState) {
                        MaintenanceLoadState.Loading ->
                            item {
                                LoadingStateCard(message = stringResource(R.string.maintenance_loading_message))
                            }
                        MaintenanceLoadState.Error -> item { LoadErrorCard(onRetry) }
                        MaintenanceLoadState.Content -> {
                            if (state.isEmpty) {
                                item { EmptyHistoryCard(onAddMaintenance = { showMaintenanceForm = true }) }
                            } else {
                                items(state.records, key = { it.id.value }) { record ->
                                    MaintenanceRecordCard(
                                        record = record,
                                        isDeleting = state.activeMutation == MaintenanceMutation.Deleting(record.id),
                                        actionsEnabled = state.activeMutation == null,
                                        onDeleteMaintenance = { recordPendingDeletion = record },
                                    )
                                }
                            }
                        }
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

    if (showMaintenanceForm) {
        FullScreenMaintenanceForm(
            state = state,
            onDismissRequest = { if (state.activeMutation == null) showMaintenanceForm = false },
            onTypeSelected = onTypeSelected,
            onCustomTypeLabelChange = onCustomTypeLabelChange,
            onPerformedOnChange = onPerformedOnChange,
            onNextDueDateChange = onNextDueDateChange,
            onOdometerChange = onOdometerChange,
            onCostChange = onCostChange,
            onWorkshopChange = onWorkshopChange,
            onNotesChange = onNotesChange,
            onSubmitMaintenance = onSubmitMaintenance,
        )
    }

    recordPendingDeletion?.let { record ->
        val type = record.displayType()
        AlertDialog(
            onDismissRequest = { recordPendingDeletion = null },
            title = { Text(stringResource(R.string.delete_maintenance_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
                    Text(stringResource(R.string.delete_maintenance_dialog_description))
                    Text(
                        text = type,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = state.activeMutation == null,
                    onClick = {
                        recordPendingDeletion = null
                        onDeleteMaintenance(record)
                    },
                ) {
                    Text(stringResource(R.string.delete_maintenance_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { recordPendingDeletion = null }) {
                    Text(stringResource(R.string.delete_maintenance_cancel_button))
                }
            },
        )
    }
}

@Composable
private fun LoadingStateCard(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(Spacings.spacing16),
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator()
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenMaintenanceForm(
    state: MaintenanceHistoryUiState,
    onDismissRequest: () -> Unit,
    onTypeSelected: (MaintenanceTypeCode) -> Unit,
    onCustomTypeLabelChange: (String) -> Unit,
    onPerformedOnChange: (String) -> Unit,
    onNextDueDateChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onWorkshopChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmitMaintenance: () -> Unit,
) {
    BackHandler(onBack = onDismissRequest)
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    Surface(
        modifier = Modifier.fillMaxSize().testTag("full_screen_maintenance_form"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.maintenance_form_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onDismissRequest,
                            enabled = state.activeMutation == null,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.close_maintenance_form),
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
                            onClick = onSubmitMaintenance,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(Spacings.spacing16)
                                    .testTag("save_maintenance_button"),
                            enabled = state.activeMutation == null,
                        ) {
                            Text(
                                if (state.isSaving) {
                                    stringResource(R.string.maintenance_saving_status)
                                } else {
                                    stringResource(R.string.save_maintenance_button)
                                },
                            )
                        }
                    }
                }
            },
        ) { contentPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(contentPadding).imePadding(),
                contentAlignment = Alignment.TopCenter,
            ) {
                MaintenanceForm(
                    state = state,
                    onTypeSelected = onTypeSelected,
                    onCustomTypeLabelChange = onCustomTypeLabelChange,
                    onPerformedOnChange = onPerformedOnChange,
                    onNextDueDateChange = onNextDueDateChange,
                    onOdometerChange = onOdometerChange,
                    onCostChange = onCostChange,
                    onWorkshopChange = onWorkshopChange,
                    onNotesChange = onNotesChange,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .widthIn(max = 720.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaintenanceForm(
    state: MaintenanceHistoryUiState,
    onTypeSelected: (MaintenanceTypeCode) -> Unit,
    onCustomTypeLabelChange: (String) -> Unit,
    onPerformedOnChange: (String) -> Unit,
    onNextDueDateChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onWorkshopChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val customTypeErrorRequester = remember { BringIntoViewRequester() }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(state.validationError) {
        if (state.validationError == CarburaString.ValidationBlankMaintenanceType) {
            customTypeErrorRequester.bringIntoView()
        }
    }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = Spacings.spacing24,
                    top = Spacings.spacing8,
                    end = Spacings.spacing24,
                    bottom = Spacings.spacing24,
                ),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        ExposedDropdownMenuBox(
            expanded = typeMenuExpanded,
            onExpandedChange = { typeMenuExpanded = it },
        ) {
            OutlinedTextField(
                value = state.maintenanceTypeCode.localizedLabel(),
                onValueChange = {},
                modifier =
                    Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                        .testTag("maintenance_type_dropdown"),
                readOnly = true,
                label = { Text(stringResource(R.string.maintenance_type_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = typeMenuExpanded,
                onDismissRequest = { typeMenuExpanded = false },
            ) {
                MaintenanceTypeCode.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.localizedLabel()) },
                        onClick = {
                            onTypeSelected(type)
                            typeMenuExpanded = false
                        },
                    )
                }
            }
        }
        if (state.maintenanceTypeCode == MaintenanceTypeCode.Custom) {
            val typeError = state.validationError == CarburaString.ValidationBlankMaintenanceType
            OutlinedTextField(
                value = state.customTypeLabel,
                onValueChange = onCustomTypeLabelChange,
                modifier = Modifier.fillMaxWidth().bringIntoViewRequester(customTypeErrorRequester),
                label = { Text(stringResource(R.string.maintenance_custom_type_label)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                isError = typeError,
                supportingText = if (typeError) validationSupportingText(state.validationError) else null,
                singleLine = true,
            )
        }
        MaintenanceDatePickerField(
            value = state.performedOn,
            onValueChange = onPerformedOnChange,
            error = state.validationError == CarburaString.ValidationInvalidMaintenanceDate,
            label = stringResource(R.string.maintenance_date_label),
        )
        if (state.supportsNextDueDate) {
            Text(
                text = stringResource(R.string.maintenance_next_date_explanation),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            MaintenanceDatePickerField(
                value = state.nextDueDate,
                onValueChange = onNextDueDateChange,
                error = state.validationError == CarburaString.ValidationInvalidMaintenanceDate,
                label =
                    stringResource(
                        if (state.maintenanceTypeCode == MaintenanceTypeCode.Itv) {
                            R.string.maintenance_next_itv_date_label
                        } else {
                            R.string.maintenance_next_insurance_date_label
                        },
                    ),
                optional = true,
            )
        }
        OutlinedTextField(
            value = state.odometerKm,
            onValueChange = onOdometerChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.maintenance_odometer_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = state.validationError == CarburaString.ValidationNegativeMaintenanceOdometer,
            supportingText =
                if (state.validationError == CarburaString.ValidationNegativeMaintenanceOdometer) {
                    validationSupportingText(state.validationError)
                } else {
                    null
                },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.cost,
            onValueChange = onCostChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.maintenance_cost_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = state.validationError == CarburaString.ValidationNegativeMaintenanceCost,
            supportingText =
                if (state.validationError == CarburaString.ValidationNegativeMaintenanceCost) {
                    validationSupportingText(state.validationError)
                } else {
                    null
                },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.workshop,
            onValueChange = onWorkshopChange,
            modifier = Modifier.fillMaxWidth().testTag("maintenance_workshop_input"),
            label = { Text(stringResource(R.string.maintenance_workshop_label)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.maintenance_notes_label)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            minLines = 2,
        )
        if (state.validationError == CarburaString.ValidationGeneric) {
            Text(
                text = stringResource(state.validationError.maintenanceStringRes()),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (state.persistenceError) {
            Text(
                text = stringResource(R.string.maintenance_persistence_error),
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaintenanceDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    error: Boolean,
    label: String,
    optional: Boolean = false,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = value.toUtcMillisOrNull())

    val errorMessage = stringResource(R.string.validation_invalid_maintenance_date)
    Column(
        modifier = Modifier.semantics { if (error) error(errorMessage) },
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(value.ifBlank { stringResource(R.string.maintenance_select_date) })
        }
        if (optional && value.isNotBlank()) {
            TextButton(onClick = { onValueChange("") }) {
                Text(stringResource(R.string.maintenance_clear_date))
            }
        }
        if (error) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onValueChange(it.toIsoDate()) }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun MaintenanceTypeCode.localizedLabel(customLabel: String = ""): String =
    when (this) {
        MaintenanceTypeCode.Itv -> stringResource(R.string.maintenance_type_itv)
        MaintenanceTypeCode.Insurance -> stringResource(R.string.maintenance_type_insurance)
        MaintenanceTypeCode.OilChange -> stringResource(R.string.maintenance_type_oil_change)
        MaintenanceTypeCode.Tires -> stringResource(R.string.maintenance_type_tires)
        MaintenanceTypeCode.GeneralReview -> stringResource(R.string.maintenance_type_general_review)
        MaintenanceTypeCode.Repair -> stringResource(R.string.maintenance_type_repair)
        MaintenanceTypeCode.Custom -> customLabel.ifBlank { stringResource(R.string.maintenance_type_custom) }
    }

@Composable
private fun EmptyHistoryCard(onAddMaintenance: () -> Unit) {
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
                text = stringResource(R.string.empty_maintenance_history_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.empty_maintenance_history_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.empty_maintenance_history_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Button(onClick = onAddMaintenance) {
                Text(stringResource(R.string.add_maintenance_button))
            }
        }
    }
}

@Composable
private fun MaintenanceRecordCard(
    record: MaintenanceRecord,
    isDeleting: Boolean,
    actionsEnabled: Boolean,
    onDeleteMaintenance: (MaintenanceRecord) -> Unit,
) {
    val displayType = record.displayType()
    SwipeToDeleteContainer(
        actionLabel = stringResource(R.string.delete_maintenance_confirm_button),
        accessibilityLabel = stringResource(R.string.delete_maintenance_content_description, displayType),
        enabled = actionsEnabled && !isDeleting,
        onDeleteRequest = { onDeleteMaintenance(record) },
        modifier = Modifier.fillMaxWidth().testTag("maintenance_card_${record.id.value}"),
    ) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacings.spacing16),
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
            ) {
                Text(
                    text = displayType,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = record.performedOn.localizedDate(),
                    style = MaterialTheme.typography.bodyMedium,
                )
                record.odometerKm?.let { odometer ->
                    Text(stringResource(R.string.maintenance_odometer_value, odometer), style = MaterialTheme.typography.bodyMedium)
                }
                record.costCents?.let { costCents ->
                    Text(costCents.localizedCost(record.currency))
                }
                record.workshop?.let { workshop ->
                    Text(workshop, style = MaterialTheme.typography.bodyMedium)
                }
                record.notes?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (isDeleting) {
                    Text(
                        text = stringResource(R.string.maintenance_deleting_status),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleContextCard(vehicle: Vehicle) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing4),
        ) {
            Text(vehicle.name, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineSmall)
            val description = listOfNotNull(vehicle.brand, vehicle.model).joinToString(" ")
            if (description.isNotBlank()) Text(description, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(R.string.maintenance_vehicle_summary, vehicle.type.localizedLabel(), vehicle.currentOdometerKm),
                style = MaterialTheme.typography.bodyMedium,
            )
            vehicle.licensePlate?.let { Text(stringResource(R.string.maintenance_vehicle_plate, it)) }
        }
    }
}

@Composable
private fun VehicleType.localizedLabel(): String =
    stringResource(
        when (this) {
            VehicleType.Car -> R.string.vehicle_type_car
            VehicleType.Motorcycle -> R.string.vehicle_type_motorcycle
            VehicleType.Van -> R.string.vehicle_type_van
            VehicleType.Other -> R.string.vehicle_type_other
        },
    )

@Composable
private fun LoadErrorCard(onRetry: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacings.spacing24),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Text(stringResource(R.string.maintenance_load_error_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.maintenance_load_error_description))
            Button(onClick = onRetry) { Text(stringResource(R.string.maintenance_retry_button)) }
        }
    }
}

@Composable
private fun validationSupportingText(error: CarburaString?): (@Composable () -> Unit)? =
    error?.let { { Text(stringResource(it.maintenanceStringRes())) } }

internal fun com.asensiodev.carbura.core.model.CalendarDate.localizedDate(locale: Locale = Locale.getDefault()): String =
    LocalDate.parse(iso8601).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))

internal fun Int.localizedCost(
    currencyCode: String,
    locale: Locale = Locale.getDefault(),
): String =
    NumberFormat
        .getCurrencyInstance(locale)
        .apply {
            currency = Currency.getInstance(currencyCode)
        }.format(this / 100.0)

private fun String.toUtcMillisOrNull(): Long? =
    runCatching {
        LocalDate
            .parse(this)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    }.getOrNull()

private fun Long.toIsoDate(): String =
    Instant
        .ofEpochMilli(this)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toString()
