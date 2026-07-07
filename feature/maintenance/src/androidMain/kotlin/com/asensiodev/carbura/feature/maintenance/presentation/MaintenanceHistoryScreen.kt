package com.asensiodev.carbura.feature.maintenance.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.asensiodev.carbura.core.designsystem.Spacings
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featuremaintenance.R
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

@Composable
fun MaintenanceHistoryRoute(
    vehicleId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MaintenanceHistoryViewModel = rememberMaintenanceHistoryViewModel(vehicleId),
) {
    val uiState by viewModel.uiState.collectAsState()
    var effectMessage by remember { mutableStateOf<CarburaString?>(null) }
    var effectMessageArg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(MaintenanceHistoryEvent.Started)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MaintenanceHistoryEffect.MaintenanceCreated -> {
                    effectMessage = CarburaString.MaintenanceCreatedMessage
                    effectMessageArg = effect.type
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
        onBack = onBack,
        onTypeChange = { viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged(it)) },
        onPerformedOnChange = { viewModel.onEvent(MaintenanceHistoryEvent.PerformedOnChanged(it)) },
        onOdometerChange = { viewModel.onEvent(MaintenanceHistoryEvent.OdometerChanged(it)) },
        onCostChange = { viewModel.onEvent(MaintenanceHistoryEvent.CostChanged(it)) },
        onWorkshopChange = { viewModel.onEvent(MaintenanceHistoryEvent.WorkshopChanged(it)) },
        onNotesChange = { viewModel.onEvent(MaintenanceHistoryEvent.NotesChanged(it)) },
        onSubmitMaintenance = { viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance) },
        modifier = modifier,
    )
}

@Composable
private fun rememberMaintenanceHistoryViewModel(vehicleId: String): MaintenanceHistoryViewModel = remember(vehicleId) {
    GlobalContext.get().get { parametersOf(VehicleId(vehicleId)) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaintenanceHistoryScreen(
    state: MaintenanceHistoryUiState,
    effectMessage: String?,
    onBack: () -> Unit,
    onTypeChange: (String) -> Unit,
    onPerformedOnChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onWorkshopChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmitMaintenance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                MaintenanceHeader()
            }

            item {
                MaintenanceForm(
                    state = state,
                    effectMessage = effectMessage,
                    onTypeChange = onTypeChange,
                    onPerformedOnChange = onPerformedOnChange,
                    onOdometerChange = onOdometerChange,
                    onCostChange = onCostChange,
                    onWorkshopChange = onWorkshopChange,
                    onNotesChange = onNotesChange,
                    onSubmitMaintenance = onSubmitMaintenance,
                )
            }

            item {
                Text(
                    text = stringResource(R.string.maintenance_history_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            if (state.isEmpty) {
                item { EmptyHistoryCard() }
            } else {
                items(state.records) { record ->
                    MaintenanceRecordCard(record = record)
                }
            }
        }
    }
}

@Composable
private fun MaintenanceHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Text(
            text = stringResource(R.string.maintenance_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MaintenanceForm(
    state: MaintenanceHistoryUiState,
    effectMessage: String?,
    onTypeChange: (String) -> Unit,
    onPerformedOnChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onCostChange: (String) -> Unit,
    onWorkshopChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmitMaintenance: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
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
                singleLine = true,
            )
            OutlinedTextField(
                value = state.performedOn,
                onValueChange = onPerformedOnChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.maintenance_date_label)) },
                singleLine = true,
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
                singleLine = true,
            )
            OutlinedTextField(
                value = state.notes,
                onValueChange = onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.maintenance_notes_label)) },
                minLines = 2,
            )
            if (state.errorMessage != null) {
                Text(
                    text = stringResource(state.errorMessage.maintenanceStringRes()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (effectMessage != null && state.errorMessage == null) {
                Text(
                    text = effectMessage,
                    color = MaterialTheme.colorScheme.primary,
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
}

@Composable
private fun EmptyHistoryCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacings.spacing16)) {
            Text(
                text = stringResource(R.string.empty_maintenance_history_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(Spacings.spacing8))
            Text(
                text = stringResource(R.string.empty_maintenance_history_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MaintenanceRecordCard(record: MaintenanceRecord) {
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
                    text = record.maintenanceTypeId.value.removePrefix("type-").replace('-', ' '),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = record.performedOn.iso8601,
                    style = MaterialTheme.typography.bodyMedium,
                )
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
