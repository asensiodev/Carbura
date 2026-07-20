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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.feature.reminders.presentation.ReminderAction
import com.asensiodev.carbura.feature.reminders.presentation.RemindersEffect
import com.asensiodev.carbura.feature.reminders.presentation.RemindersEvent
import com.asensiodev.carbura.feature.reminders.presentation.RemindersUiState
import com.asensiodev.carbura.feature.reminders.presentation.RemindersViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

@Composable
internal fun RemindersWorkspace(
    compact: Boolean,
    onNavigateToGarage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel =
        remember {
            val familyId = GlobalContext.get().get<FamilyId>()
            GlobalContext.get().get<RemindersViewModel> { parametersOf(familyId) }
        }
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateForm by remember { mutableStateOf(false) }
    var pendingDeletion by remember { mutableStateOf<Reminder?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(RemindersEvent.Started)
        viewModel.effects.collect { effect ->
            reminderNavigationDestination(effect)?.let {
                onNavigateToGarage()
                return@collect
            }
            val feedback =
                when (effect) {
                    is RemindersEffect.ReminderCreated -> {
                        showCreateForm = false
                        "${effect.title} added to Reminders"
                    }
                    is RemindersEffect.ReminderCompleted -> "${effect.title} completed"
                    is RemindersEffect.ReminderDeleted -> "${effect.title} deleted"
                    is RemindersEffect.ValidationFailed,
                    RemindersEffect.NavigateToGarage,
                    -> null
                }
            feedback?.let { snackbarHostState.showSnackbar(it) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxHeight(),
        containerColor = Canvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(if (compact) 28.dp else 48.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            RemindersHeader(
                reminderCount = state.reminders.size,
                compact = compact,
                canCreate = !state.isLoading && !state.hasLoadError && !state.hasNoVehicles,
                onCreate = { showCreateForm = true },
            )
            NotificationAvailabilityPanel(compact)
            if (state.vehicles.isNotEmpty() && !state.hasLoadError) {
                ReminderFilters(
                    vehicles = state.vehicles,
                    selectedVehicleIds = state.selectedFilterVehicleIds,
                    onToggle = { viewModel.onEvent(RemindersEvent.VehicleFilterToggled(it)) },
                    onClear = { viewModel.onEvent(RemindersEvent.VehicleFiltersCleared) },
                )
            }
            RemindersBody(
                state = state,
                onRetry = { viewModel.onEvent(RemindersEvent.Retry) },
                onNavigateToGarage = { viewModel.onEvent(RemindersEvent.GarageRequested) },
                onCreate = { showCreateForm = true },
                onClearFilters = { viewModel.onEvent(RemindersEvent.VehicleFiltersCleared) },
                onComplete = { viewModel.onEvent(RemindersEvent.CompleteReminder(it.id)) },
                onDelete = { pendingDeletion = it },
            )
        }
    }

    if (showCreateForm) {
        ReminderFormDialog(
            state = state,
            onEvent = viewModel::onEvent,
            onDismiss = { if (state.activeAction != ReminderAction.Create) showCreateForm = false },
        )
    }

    pendingDeletion?.let { reminder ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            title = { Text("Delete reminder?") },
            text = { Text("\"${reminder.title}\" will be removed from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeletion = null
                        viewModel.onEvent(RemindersEvent.DeleteReminder(reminder.id))
                    },
                ) {
                    Text("Delete reminder")
                }
            },
            dismissButton = { TextButton(onClick = { pendingDeletion = null }) { Text("Cancel") } },
        )
    }
}

internal fun reminderNavigationDestination(effect: RemindersEffect): DesktopDestination? =
    if (effect == RemindersEffect.NavigateToGarage) DesktopDestination.Garage else null

@Composable
private fun RemindersHeader(
    reminderCount: Int,
    compact: Boolean,
    canCreate: Boolean,
    onCreate: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("UP NEXT", color = Blue, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.6.sp)
            Spacer(Modifier.height(8.dp))
            Text("Nothing important slips by.", style = MaterialTheme.typography.displaySmall, color = Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                if (reminderCount == 1) "1 pending reminder" else "$reminderCount pending reminders",
                color = Muted,
            )
        }
        Spacer(Modifier.width(20.dp))
        Button(onClick = onCreate, enabled = canCreate) {
            Icon(Icons.Default.Add, contentDescription = null)
            if (!compact) {
                Spacer(Modifier.width(8.dp))
                Text("Add reminder")
            }
        }
    }
}

@Composable
private fun NotificationAvailabilityPanel(compact: Boolean) {
    Surface(color = PaleBlue, shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = Blue)
            Column(modifier = Modifier.weight(1f)) {
                Text("Stored locally in Carbura", color = Ink, fontWeight = FontWeight.SemiBold)
                if (!compact) {
                    Text("Native macOS and Windows alerts are not enabled in this version.", color = Muted, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ReminderFilters(
    vehicles: List<Vehicle>,
    selectedVehicleIds: Set<VehicleId>,
    onToggle: (VehicleId) -> Unit,
    onClear: () -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedVehicleIds.isEmpty(),
            onClick = onClear,
            label = { Text("All vehicles") },
        )
        vehicles.forEach { vehicle ->
            FilterChip(
                selected = vehicle.id in selectedVehicleIds,
                onClick = { onToggle(vehicle.id) },
                label = { Text(vehicle.name) },
            )
        }
    }
}

@Composable
private fun RemindersBody(
    state: RemindersUiState,
    onRetry: () -> Unit,
    onNavigateToGarage: () -> Unit,
    onCreate: () -> Unit,
    onClearFilters: () -> Unit,
    onComplete: (Reminder) -> Unit,
    onDelete: (Reminder) -> Unit,
) {
    when {
        state.isLoading -> ReminderMessagePanel("Loading reminders...", showProgress = true)
        state.hasLoadError ->
            ReminderMessagePanel(
                title = "Reminders could not be loaded",
                detail = "Your local data is unchanged. Try opening it again.",
                actionLabel = "Retry",
                onAction = onRetry,
            )
        state.hasNoVehicles ->
            ReminderMessagePanel(
                title = "Add a vehicle first",
                detail = "Reminders need a vehicle so their due date or mileage has context.",
                actionLabel = "Go to Garage",
                onAction = onNavigateToGarage,
            )
        state.hasNoMatchingReminders ->
            ReminderMessagePanel(
                title = "No reminders for these vehicles",
                detail = "Choose another vehicle or return to the complete list.",
                actionLabel = "Clear filters",
                onAction = onClearFilters,
            )
        state.isEmpty ->
            ReminderMessagePanel(
                title = "Your agenda is clear",
                detail = "Create a date or mileage reminder for the next important task.",
                actionLabel = "Add a reminder",
                onAction = onCreate,
            )
        else -> {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state.hasPersistenceError) {
                    Surface(color = Color(0xFFFFE9E7), shape = RoundedCornerShape(14.dp)) {
                        Text(
                            "The reminder action could not be completed. Please try again.",
                            color = Color(0xFF8A2D27),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        )
                    }
                }
                val vehicleNames = state.vehicles.associate { it.id to it.name }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.visibleReminders, key = { it.id.value }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            vehicleName = vehicleNames[reminder.vehicleId] ?: "Unavailable vehicle",
                            activeAction = state.activeAction,
                            onComplete = { onComplete(reminder) },
                            onDelete = { onDelete(reminder) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    vehicleName: String,
    activeAction: ReminderAction?,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    val isCompleting = activeAction == ReminderAction.Complete(reminder.id)
    val isDeleting = activeAction == ReminderAction.Delete(reminder.id)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(46.dp).background(PaleBlue, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = Blue)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reminder.title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(4.dp))
                Text(vehicleName, color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(3.dp))
                Text(reminder.dueDescription(), color = Blue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            if (isCompleting || isDeleting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                OutlinedButton(onClick = onComplete, enabled = activeAction == null) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Complete")
                }
                IconButton(onClick = onDelete, enabled = activeAction == null) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete ${reminder.title}", tint = Muted)
                }
            }
        }
    }
}

@Composable
private fun ReminderMessagePanel(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderFormDialog(
    state: RemindersUiState,
    onEvent: (RemindersEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val isSaving = state.activeAction == ReminderAction.Create
    var vehicleMenuExpanded by remember { mutableStateOf(false) }
    val selectedVehicle = state.vehicles.firstOrNull { it.id == state.selectedVehicleId }
    DialogWindow(onCloseRequest = { if (!isSaving) onDismiss() }, title = "Add reminder") {
        Surface(
            modifier = Modifier.width(580.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("Add a reminder", style = MaterialTheme.typography.headlineMedium, color = Ink)
                Text("Use a date, target mileage, or both.", color = Muted)
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { onEvent(RemindersEvent.TitleChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Reminder title") },
                    singleLine = true,
                )
                ExposedDropdownMenuBox(
                    expanded = vehicleMenuExpanded,
                    onExpandedChange = { vehicleMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedVehicle?.name.orEmpty(),
                        onValueChange = {},
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        readOnly = true,
                        label = { Text("Vehicle") },
                        placeholder = { Text("Select a vehicle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleMenuExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )
                    ExposedDropdownMenu(
                        expanded = vehicleMenuExpanded,
                        onDismissRequest = { vehicleMenuExpanded = false },
                    ) {
                        state.vehicles.forEach { vehicle ->
                            DropdownMenuItem(
                                text = { Text(vehicle.name) },
                                onClick = {
                                    onEvent(RemindersEvent.VehicleSelected(vehicle.id))
                                    vehicleMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.dueDate,
                        onValueChange = { onEvent(RemindersEvent.DueDateChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Due date (YYYY-MM-DD)") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.dueOdometerKm,
                        onValueChange = { onEvent(RemindersEvent.DueOdometerChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Target odometer (km)") },
                        singleLine = true,
                    )
                }
                state.errorMessage?.let { Text(it.desktopMessage(), color = MaterialTheme.colorScheme.error) }
                if (state.hasPersistenceError) {
                    Text("The reminder could not be saved. Your entries are still here.", color = MaterialTheme.colorScheme.error)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onEvent(RemindersEvent.SubmitReminder) }, enabled = !isSaving) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Add reminder")
                    }
                }
            }
        }
    }
}

private fun Reminder.dueDescription(): String {
    val targets =
        listOfNotNull(
            dueDate?.iso8601?.let { "Due $it" },
            dueOdometerKm?.let { "%,d km".format(it) },
        )
    return targets.joinToString("  |  ").ifBlank { "No due target" }
}
