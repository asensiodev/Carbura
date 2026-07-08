package com.asensiodev.carbura.feature.maintenance.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.asensiodev.carbura.core.designsystem.Spacings
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featuremaintenance.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

@Composable
fun MaintenanceHistoryRoute(
    vehicleId: String,
    familyId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MaintenanceHistoryViewModel = rememberMaintenanceHistoryViewModel(vehicleId, familyId),
) {
    val uiState by viewModel.uiState.collectAsState()
    var effectMessage by remember { mutableStateOf<CarburaString?>(null) }
    var effectMessageArg by remember { mutableStateOf<String?>(null) }
    var maintenanceCreatedSignal by remember { mutableStateOf(0) }
    var maintenanceSuccessSignal by remember { mutableStateOf(0) }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(MaintenanceHistoryEvent.Started)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MaintenanceHistoryEffect.MaintenanceCreated -> {
                    effectMessage = CarburaString.MaintenanceCreatedMessage
                    effectMessageArg = effect.type
                    maintenanceCreatedSignal += 1
                    maintenanceSuccessSignal += 1
                }

                is MaintenanceHistoryEffect.MaintenanceDeleted -> {
                    effectMessage = CarburaString.MaintenanceDeletedMessage
                    effectMessageArg = effect.type
                    maintenanceSuccessSignal += 1
                }

                is MaintenanceHistoryEffect.ValidationFailed -> {
                    effectMessage = effect.message
                    effectMessageArg = null
                }
            }
        }
    }

    val resolvedEffectMessage = effectMessage?.let { message ->
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
        onTypeChange = { viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged(it)) },
        onPerformedOnChange = { viewModel.onEvent(MaintenanceHistoryEvent.PerformedOnChanged(it)) },
        onOdometerChange = { viewModel.onEvent(MaintenanceHistoryEvent.OdometerChanged(it)) },
        onCostChange = { viewModel.onEvent(MaintenanceHistoryEvent.CostChanged(it)) },
        onWorkshopChange = { viewModel.onEvent(MaintenanceHistoryEvent.WorkshopChanged(it)) },
        onNotesChange = { viewModel.onEvent(MaintenanceHistoryEvent.NotesChanged(it)) },
        onSubmitMaintenance = { viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance) },
        onDeleteMaintenance = { viewModel.onEvent(MaintenanceHistoryEvent.DeleteMaintenance(it.id)) },
        modifier = modifier,
    )
}

@Composable
private fun rememberMaintenanceHistoryViewModel(
    vehicleId: String,
    familyId: String,
): MaintenanceHistoryViewModel = remember(vehicleId, familyId) {
    GlobalContext.get().get { parametersOf(VehicleId(vehicleId), FamilyId(familyId)) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaintenanceHistoryScreen(
    state: MaintenanceHistoryUiState,
    effectMessage: String?,
    maintenanceCreatedSignal: Int,
    maintenanceSuccessSignal: Int,
    onBack: () -> Unit,
    onTypeChange: (String) -> Unit,
    onPerformedOnChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onWorkshopChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmitMaintenance: () -> Unit,
    onDeleteMaintenance: (MaintenanceRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMaintenanceSheet by remember { mutableStateOf(false) }
    var recordPendingDeletion by remember { mutableStateOf<MaintenanceRecord?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(maintenanceCreatedSignal) {
        if (maintenanceCreatedSignal > 0) {
            showMaintenanceSheet = false
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
                    title = { Text(stringResource(R.string.maintenance_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.maintenance_back_button),
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(Spacings.spacing24),
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
            ) {
                item {
                    MaintenanceHeader(onAddMaintenance = { showMaintenanceSheet = true })
                }

                item {
                    Text(
                        text = stringResource(R.string.maintenance_history_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }

                if (state.isEmpty) {
                    item {
                        EmptyHistoryCard(onAddMaintenance = { showMaintenanceSheet = true })
                    }
                } else {
                    items(state.records) { record ->
                        MaintenanceRecordCard(
                            record = record,
                            onDeleteMaintenance = { recordPendingDeletion = record },
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .safeDrawingPadding()
                .padding(horizontal = Spacings.spacing16),
        )
    }

    if (showMaintenanceSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMaintenanceSheet = false },
            sheetState = sheetState,
        ) {
            MaintenanceForm(
                state = state,
                onTypeChange = onTypeChange,
                onPerformedOnChange = onPerformedOnChange,
                onOdometerChange = onOdometerChange,
                onCostChange = onCostChange,
                onWorkshopChange = onWorkshopChange,
                onNotesChange = onNotesChange,
                onSubmitMaintenance = onSubmitMaintenance,
                modifier = Modifier.padding(
                    start = Spacings.spacing24,
                    end = Spacings.spacing24,
                    bottom = Spacings.spacing24,
                ),
            )
        }
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
private fun MaintenanceHeader(onAddMaintenance: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.maintenance_subtitle),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onAddMaintenance) {
            Text(stringResource(R.string.add_maintenance_button))
        }
    }
}

@Composable
private fun MaintenanceForm(
    state: MaintenanceHistoryUiState,
    onTypeChange: (String) -> Unit,
    onPerformedOnChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onWorkshopChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmitMaintenance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
    ) {
        Text(
            text = stringResource(R.string.maintenance_form_title),
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = state.type,
            onValueChange = onTypeChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.maintenance_type_label)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true,
        )
        MaintenanceDatePickerField(
            value = state.performedOn,
            onValueChange = onPerformedOnChange,
        )
        OutlinedTextField(
            value = state.odometerKm,
            onValueChange = onOdometerChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.maintenance_odometer_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.cost,
            onValueChange = onCostChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.maintenance_cost_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.workshop,
            onValueChange = onWorkshopChange,
            modifier = Modifier.fillMaxWidth(),
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
        if (state.errorMessage != null) {
            Text(
                text = stringResource(state.errorMessage.maintenanceStringRes()),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Button(
            onClick = onSubmitMaintenance,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save_maintenance_button))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaintenanceDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = value.toUtcMillisOrNull())

    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Text(
            text = stringResource(R.string.maintenance_date_label),
            style = MaterialTheme.typography.labelLarge,
        )
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(value)
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
private fun EmptyHistoryCard(onAddMaintenance: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Text(
                text = stringResource(R.string.empty_maintenance_history_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.empty_maintenance_history_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    onDeleteMaintenance: (MaintenanceRecord) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = record.displayType(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.performedOn.iso8601,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    IconButton(onClick = { onDeleteMaintenance(record) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_maintenance_content_description),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            record.odometerKm?.let { odometer ->
                Text("$odometer km", style = MaterialTheme.typography.bodyMedium)
            }
            record.costCents?.let { costCents ->
                Text("${costCents / 100}.${(costCents % 100).toString().padStart(2, '0')} ${record.currency}")
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
        }
    }
}

private fun String.toUtcMillisOrNull(): Long? = runCatching {
    LocalDate.parse(this).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
}.getOrNull()

private fun Long.toIsoDate(): String = Instant
    .ofEpochMilli(this)
    .atZone(ZoneOffset.UTC)
    .toLocalDate()
    .toString()
