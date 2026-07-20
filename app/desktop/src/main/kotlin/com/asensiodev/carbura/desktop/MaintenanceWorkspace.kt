package com.asensiodev.carbura.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageLoadState
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewEvent
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewViewModel
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryEffect
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryEvent
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryUiState
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryViewModel
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceLoadState
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceMutation
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import java.util.Locale

@Composable
internal fun MaintenanceWorkspace(
    compact: Boolean,
    initialVehicleId: VehicleId?,
    onNavigateToGarage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val familyId = remember { GlobalContext.get().get<FamilyId>() }
    val overviewViewModel =
        remember {
            GlobalContext.get().get<GarageOverviewViewModel> { parametersOf(familyId) }
        }
    val overviewState by overviewViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedVehicleId by remember(initialVehicleId) { mutableStateOf(initialVehicleId) }

    LaunchedEffect(overviewViewModel) {
        overviewViewModel.onEvent(GarageOverviewEvent.Started)
    }
    LaunchedEffect(overviewState.vehicles, overviewState.loadState, initialVehicleId) {
        if (overviewState.loadState == GarageLoadState.Loaded) {
            selectedVehicleId =
                selectedVehicleId?.takeIf { id -> overviewState.vehicles.any { it.id == id } }
                    ?: initialVehicleId?.takeIf { id -> overviewState.vehicles.any { it.id == id } }
                    ?: overviewState.vehicles.firstOrNull()?.id
        }
    }

    Box(modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(if (compact) 28.dp else 48.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            MaintenanceHeader(
                selectedVehicle = overviewState.vehicles.firstOrNull { it.id == selectedVehicleId },
                compact = compact,
            )
            when (overviewState.loadState) {
                GarageLoadState.Loading -> MaintenanceMessagePanel("Loading vehicles...", showProgress = true)
                GarageLoadState.Error ->
                    MaintenanceMessagePanel(
                        title = "Vehicles could not be loaded",
                        detail = "Your local data is unchanged. Try opening Maintenance again.",
                        actionLabel = "Retry",
                        onAction = { overviewViewModel.onEvent(GarageOverviewEvent.Retry) },
                    )
                GarageLoadState.Loaded -> {
                    if (overviewState.vehicles.isEmpty()) {
                        MaintenanceMessagePanel(
                            title = "Add a vehicle first",
                            detail = "Maintenance history needs a vehicle to organize its records.",
                            actionLabel = "Go to Garage",
                            onAction = onNavigateToGarage,
                        )
                    } else {
                        VehicleSelector(
                            vehicles = overviewState.vehicles,
                            selectedVehicleId = selectedVehicleId,
                            onSelected = { selectedVehicleId = it },
                        )
                        selectedVehicleId?.let { vehicleId ->
                            MaintenanceVehicleContent(
                                vehicleId = vehicleId,
                                familyId = familyId,
                                snackbarHostState = snackbarHostState,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp))
    }
}

@Composable
private fun MaintenanceHeader(
    selectedVehicle: Vehicle?,
    compact: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Column(modifier = Modifier.weight(1f)) {
            Text("SERVICE LOG", color = Blue, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.6.sp)
            Spacer(Modifier.height(8.dp))
            Text("Every service, in one timeline.", style = MaterialTheme.typography.displaySmall, color = Ink)
            Spacer(Modifier.height(6.dp))
            Text(selectedVehicle?.let { "History for ${it.name}" } ?: "Choose a vehicle", color = Muted)
        }
        if (!compact) {
            Surface(color = PaleBlue, shape = RoundedCornerShape(14.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = Blue, modifier = Modifier.size(18.dp))
                    Text("Reminders stored locally; native alerts unavailable", color = Muted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun VehicleSelector(
    vehicles: List<Vehicle>,
    selectedVehicleId: VehicleId?,
    onSelected: (VehicleId) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        vehicles.forEach { vehicle ->
            FilterChip(
                selected = vehicle.id == selectedVehicleId,
                onClick = { onSelected(vehicle.id) },
                label = { Text(vehicle.name) },
            )
        }
    }
}

@Composable
private fun MaintenanceVehicleContent(
    vehicleId: VehicleId,
    familyId: FamilyId,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val viewModel =
        remember(vehicleId, familyId) {
            GlobalContext.get().get<MaintenanceHistoryViewModel> { parametersOf(vehicleId, familyId) }
        }
    val state by viewModel.uiState.collectAsState()
    var showForm by remember(vehicleId) { mutableStateOf(false) }
    var pendingDeletion by remember(vehicleId) { mutableStateOf<MaintenanceRecord?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(MaintenanceHistoryEvent.Started)
        viewModel.effects.collect { effect ->
            when (effect) {
                is MaintenanceHistoryEffect.MaintenanceCreated -> {
                    showForm = false
                    val type = effect.typeCode.displayName(effect.customTypeLabel)
                    val reminder = if (effect.reminderCreated) " Reminder stored in Carbura." else ""
                    snackbarHostState.showSnackbar("$type added.$reminder")
                }
                is MaintenanceHistoryEffect.MaintenanceDeleted -> {
                    snackbarHostState.showSnackbar("${effect.typeCode.displayName(effect.customTypeLabel)} deleted")
                }
                is MaintenanceHistoryEffect.MaintenanceUpdated -> {
                    showForm = false
                    snackbarHostState.showSnackbar("${effect.typeCode.displayName(effect.customTypeLabel)} updated")
                }
                is MaintenanceHistoryEffect.ValidationFailed -> Unit
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.loadState == MaintenanceLoadState.Content) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = { showForm = true }, enabled = state.activeMutation == null) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add maintenance")
                }
            }
        }
        if (state.persistenceError) {
            Surface(color = Color(0xFFFFE9E7), shape = RoundedCornerShape(14.dp)) {
                Text(
                    "The maintenance action could not be completed. Please try again.",
                    color = Color(0xFF8A2D27),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                )
            }
        }
        MaintenanceHistoryBody(
            state = state,
            onRetry = { viewModel.onEvent(MaintenanceHistoryEvent.Retry) },
            onCreate = { showForm = true },
            onEdit = {
                viewModel.onEvent(MaintenanceHistoryEvent.EditMaintenance(it.id))
                showForm = true
            },
            onDelete = { pendingDeletion = it },
        )
    }

    if (showForm) {
        MaintenanceFormDialog(
            state = state,
            onEvent = viewModel::onEvent,
            onDismiss = {
                if (state.activeMutation == null && !state.showFutureReminderOffer) {
                    if (state.isEditing) viewModel.onEvent(MaintenanceHistoryEvent.CancelMaintenanceEdit)
                    showForm = false
                }
            },
        )
    }
    if (state.showFutureReminderOffer) {
        FutureMaintenanceDialog(onEvent = viewModel::onEvent)
    }
    pendingDeletion?.let { record ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            title = { Text("Delete maintenance record?") },
            text = { Text("The record and reminders generated from it will be removed from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeletion = null
                        viewModel.onEvent(MaintenanceHistoryEvent.DeleteMaintenance(record.id))
                    },
                ) {
                    Text("Delete record")
                }
            },
            dismissButton = { TextButton(onClick = { pendingDeletion = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun MaintenanceHistoryBody(
    state: MaintenanceHistoryUiState,
    onRetry: () -> Unit,
    onCreate: () -> Unit,
    onEdit: (MaintenanceRecord) -> Unit,
    onDelete: (MaintenanceRecord) -> Unit,
) {
    when (state.loadState) {
        MaintenanceLoadState.Loading -> MaintenanceMessagePanel("Loading maintenance history...", showProgress = true)
        MaintenanceLoadState.Error ->
            MaintenanceMessagePanel(
                title = "History could not be loaded",
                detail = "Your local records are unchanged.",
                actionLabel = "Retry",
                onAction = onRetry,
            )
        MaintenanceLoadState.Content -> {
            if (state.isEmpty) {
                MaintenanceMessagePanel(
                    title = "No maintenance recorded yet",
                    detail = "Add the first service, inspection, or repair to start this vehicle's timeline.",
                    actionLabel = "Add maintenance",
                    onAction = onCreate,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.records, key = { it.id.value }) { record ->
                        MaintenanceRecordCard(
                            record = record,
                            isDeleting = state.activeMutation == MaintenanceMutation.Deleting(record.id),
                            actionsEnabled = state.activeMutation == null,
                            onEdit = { onEdit(record) },
                            onDelete = { onDelete(record) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceRecordCard(
    record: MaintenanceRecord,
    isDeleting: Boolean,
    actionsEnabled: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(46.dp).background(PaleBlue, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = Blue)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(record.displayTypeName(), color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(4.dp))
                Text("Performed ${record.performedOn.iso8601}", color = Blue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                val details = record.details()
                if (details.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(details.joinToString("  |  "), color = Muted, fontSize = 13.sp)
                }
                record.notes?.takeIf(String::isNotBlank)?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = Muted, fontSize = 13.sp)
                }
            }
            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onEdit, enabled = actionsEnabled) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit ${record.displayTypeName()}", tint = Blue)
                }
                IconButton(onClick = onDelete, enabled = actionsEnabled) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete ${record.displayTypeName()}", tint = Muted)
                }
            }
        }
    }
}

@Composable
private fun MaintenanceMessagePanel(
    title: String,
    detail: String? = null,
    showProgress: Boolean = false,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PaleBlue),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (showProgress) CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
            Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            detail?.let { Text(it, color = Muted) }
            if (actionLabel != null && onAction != null) {
                OutlinedButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
private fun MaintenanceFormDialog(
    state: MaintenanceHistoryUiState,
    onEvent: (MaintenanceHistoryEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    DialogWindow(onCloseRequest = onDismiss, title = "Add maintenance") {
        Surface(
            modifier = Modifier.width(680.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    if (state.isEditing) "Edit maintenance" else "Add maintenance",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                )
                Text("Record the work exactly once; Carbura handles related reminders atomically.", color = Muted)
                Text("Maintenance type", color = Ink, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MaintenanceTypeCode.entries.forEach { type ->
                        FilterChip(
                            selected = state.maintenanceTypeCode == type,
                            onClick = { onEvent(MaintenanceHistoryEvent.TypeSelected(type)) },
                            label = { Text(type.displayName()) },
                        )
                    }
                }
                if (state.maintenanceTypeCode == MaintenanceTypeCode.Custom) {
                    OutlinedTextField(
                        value = state.customTypeLabel,
                        onValueChange = { onEvent(MaintenanceHistoryEvent.CustomTypeLabelChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Custom type") },
                        singleLine = true,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.performedOn,
                        onValueChange = { onEvent(MaintenanceHistoryEvent.PerformedOnChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Performed date (YYYY-MM-DD)") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.odometerKm,
                        onValueChange = { onEvent(MaintenanceHistoryEvent.OdometerChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Odometer (km)") },
                        singleLine = true,
                    )
                }
                if (state.supportsNextDueDate) {
                    OutlinedTextField(
                        value = state.nextDueDate,
                        onValueChange = { onEvent(MaintenanceHistoryEvent.NextDueDateChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Next due date (optional, YYYY-MM-DD)") },
                        singleLine = true,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.cost,
                        onValueChange = { onEvent(MaintenanceHistoryEvent.CostChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Cost in EUR (optional)") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.workshop,
                        onValueChange = { onEvent(MaintenanceHistoryEvent.WorkshopChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Workshop (optional)") },
                        singleLine = true,
                    )
                }
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { onEvent(MaintenanceHistoryEvent.NotesChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes (optional)") },
                    minLines = 2,
                    maxLines = 3,
                )
                state.validationError?.let { Text(it.desktopMessage(), color = MaterialTheme.colorScheme.error) }
                if (state.persistenceError) {
                    Text("The record could not be saved. Your entries are still here.", color = MaterialTheme.colorScheme.error)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = state.activeMutation == null) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onEvent(
                                if (state.isEditing) {
                                    MaintenanceHistoryEvent.SubmitMaintenanceEdit
                                } else {
                                    MaintenanceHistoryEvent.SubmitMaintenance
                                },
                            )
                        },
                        enabled = state.activeMutation == null,
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (state.isEditing) "Save changes" else "Save record")
                    }
                }
            }
        }
    }
}

@Composable
private fun FutureMaintenanceDialog(onEvent: (MaintenanceHistoryEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(MaintenanceHistoryEvent.DismissFutureReminderOffer) },
        title = { Text("Future maintenance date") },
        text = {
            Text(
                "Would you like Carbura to store a reminder for this future service? " +
                    "Native macOS and Windows alerts are not enabled in this version.",
            )
        },
        confirmButton = {
            Button(onClick = { onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder) }) {
                Text("Save with reminder")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onEvent(MaintenanceHistoryEvent.DismissFutureReminderOffer) }) { Text("Cancel") }
                TextButton(onClick = { onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceOnly) }) { Text("Save only") }
            }
        },
    )
}

private fun MaintenanceTypeCode.displayName(customLabel: String = ""): String =
    when (this) {
        MaintenanceTypeCode.Itv -> "ITV"
        MaintenanceTypeCode.Insurance -> "Insurance"
        MaintenanceTypeCode.OilChange -> "Oil change"
        MaintenanceTypeCode.Tires -> "Tires"
        MaintenanceTypeCode.GeneralReview -> "General review"
        MaintenanceTypeCode.Repair -> "Repair"
        MaintenanceTypeCode.Custom -> customLabel.ifBlank { "Custom" }
    }

private fun MaintenanceRecord.displayTypeName(): String =
    maintenanceTypeCode?.displayName(maintenanceTypeLabel.orEmpty())
        ?: maintenanceTypeId.value
            .removePrefix("type-")
            .replace('-', ' ')
            .replaceFirstChar(Char::uppercase)

private fun MaintenanceRecord.details(): List<String> =
    listOfNotNull(
        odometerKm?.let { "%,d km".format(it) },
        costCents?.let { String.format(Locale.ROOT, "%.2f %s", it / 100.0, currency) },
        workshop?.takeIf(String::isNotBlank),
        nextDueDate?.let { "Next due ${it.iso8601}" },
    )
