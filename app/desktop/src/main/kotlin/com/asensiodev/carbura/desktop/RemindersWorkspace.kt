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
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.desktop.resources.Res
import com.asensiodev.carbura.desktop.resources.reminders_add
import com.asensiodev.carbura.desktop.resources.reminders_all_vehicles_filter
import com.asensiodev.carbura.desktop.resources.reminders_cancel
import com.asensiodev.carbura.desktop.resources.reminders_clear_filters
import com.asensiodev.carbura.desktop.resources.reminders_complete
import com.asensiodev.carbura.desktop.resources.reminders_delete_content_description
import com.asensiodev.carbura.desktop.resources.reminders_delete_dialog_confirm
import com.asensiodev.carbura.desktop.resources.reminders_delete_dialog_description
import com.asensiodev.carbura.desktop.resources.reminders_delete_dialog_title
import com.asensiodev.carbura.desktop.resources.reminders_desktop_notifications_unavailable
import com.asensiodev.carbura.desktop.resources.reminders_due_date
import com.asensiodev.carbura.desktop.resources.reminders_due_date_and_odometer
import com.asensiodev.carbura.desktop.resources.reminders_due_date_label
import com.asensiodev.carbura.desktop.resources.reminders_due_odometer
import com.asensiodev.carbura.desktop.resources.reminders_due_odometer_label
import com.asensiodev.carbura.desktop.resources.reminders_empty_description
import com.asensiodev.carbura.desktop.resources.reminders_empty_title
import com.asensiodev.carbura.desktop.resources.reminders_form_description
import com.asensiodev.carbura.desktop.resources.reminders_form_title
import com.asensiodev.carbura.desktop.resources.reminders_form_window_title
import com.asensiodev.carbura.desktop.resources.reminders_go_to_garage
import com.asensiodev.carbura.desktop.resources.reminders_header_eyebrow
import com.asensiodev.carbura.desktop.resources.reminders_header_title
import com.asensiodev.carbura.desktop.resources.reminders_load_error_description
import com.asensiodev.carbura.desktop.resources.reminders_load_error_title
import com.asensiodev.carbura.desktop.resources.reminders_loading
import com.asensiodev.carbura.desktop.resources.reminders_local_storage_title
import com.asensiodev.carbura.desktop.resources.reminders_mutation_error
import com.asensiodev.carbura.desktop.resources.reminders_no_due_target
import com.asensiodev.carbura.desktop.resources.reminders_no_matches_description
import com.asensiodev.carbura.desktop.resources.reminders_no_matches_title
import com.asensiodev.carbura.desktop.resources.reminders_no_vehicles_description
import com.asensiodev.carbura.desktop.resources.reminders_no_vehicles_title
import com.asensiodev.carbura.desktop.resources.reminders_pending_multiple
import com.asensiodev.carbura.desktop.resources.reminders_pending_single
import com.asensiodev.carbura.desktop.resources.reminders_retry
import com.asensiodev.carbura.desktop.resources.reminders_save_error
import com.asensiodev.carbura.desktop.resources.reminders_snackbar_completed
import com.asensiodev.carbura.desktop.resources.reminders_snackbar_created
import com.asensiodev.carbura.desktop.resources.reminders_snackbar_deleted
import com.asensiodev.carbura.desktop.resources.reminders_title_label
import com.asensiodev.carbura.desktop.resources.reminders_unavailable_vehicle
import com.asensiodev.carbura.desktop.resources.reminders_validation_blank_title
import com.asensiodev.carbura.desktop.resources.reminders_validation_generic
import com.asensiodev.carbura.desktop.resources.reminders_validation_invalid_date
import com.asensiodev.carbura.desktop.resources.reminders_validation_missing_due_target
import com.asensiodev.carbura.desktop.resources.reminders_validation_missing_vehicle
import com.asensiodev.carbura.desktop.resources.reminders_validation_negative_due_odometer
import com.asensiodev.carbura.desktop.resources.reminders_vehicle_label
import com.asensiodev.carbura.desktop.resources.reminders_vehicle_placeholder
import com.asensiodev.carbura.feature.reminders.presentation.ReminderAction
import com.asensiodev.carbura.feature.reminders.presentation.RemindersEffect
import com.asensiodev.carbura.feature.reminders.presentation.RemindersEvent
import com.asensiodev.carbura.feature.reminders.presentation.RemindersUiState
import com.asensiodev.carbura.feature.reminders.presentation.RemindersViewModel
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import java.text.NumberFormat

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
    var feedbackSequence by remember { mutableStateOf(0L) }
    var feedbackQueue by remember { mutableStateOf(emptyList<Triple<Long, CarburaString, String>>()) }
    val currentFeedback = feedbackQueue.firstOrNull()

    val feedbackText =
        currentFeedback?.let { (_, message, title) ->
            stringResource(message.remindersStringResource(), title)
        }

    LaunchedEffect(currentFeedback?.first) {
        feedbackText?.let {
            snackbarHostState.showSnackbar(it)
            feedbackQueue = feedbackQueue.drop(1)
        }
    }

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
                        CarburaString.ReminderCreatedMessage to effect.title
                    }
                    is RemindersEffect.ReminderCompleted -> CarburaString.ReminderCompletedMessage to effect.title
                    is RemindersEffect.ReminderDeleted -> CarburaString.ReminderDeletedMessage to effect.title
                    is RemindersEffect.ValidationFailed,
                    RemindersEffect.NavigateToGarage,
                    -> null
                }
            feedback?.let { (message, title) ->
                feedbackSequence += 1
                feedbackQueue = feedbackQueue + Triple(feedbackSequence, message, title)
            }
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
            title = { Text(stringResource(Res.string.reminders_delete_dialog_title)) },
            text = { Text(stringResource(Res.string.reminders_delete_dialog_description, reminder.title)) },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeletion = null
                        viewModel.onEvent(RemindersEvent.DeleteReminder(reminder.id))
                    },
                ) {
                    Text(stringResource(Res.string.reminders_delete_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletion = null }) {
                    Text(stringResource(Res.string.reminders_cancel))
                }
            },
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
            Text(
                stringResource(Res.string.reminders_header_eyebrow),
                color = Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.6.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.reminders_header_title), style = MaterialTheme.typography.displaySmall, color = Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                if (reminderCount == 1) {
                    stringResource(Res.string.reminders_pending_single)
                } else {
                    stringResource(Res.string.reminders_pending_multiple, reminderCount)
                },
                color = Muted,
            )
        }
        Spacer(Modifier.width(20.dp))
        Button(onClick = onCreate, enabled = canCreate) {
            Icon(Icons.Default.Add, contentDescription = null)
            if (!compact) {
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.reminders_add))
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
                Text(stringResource(Res.string.reminders_local_storage_title), color = Ink, fontWeight = FontWeight.SemiBold)
                if (!compact) {
                    Text(
                        stringResource(Res.string.reminders_desktop_notifications_unavailable),
                        color = Muted,
                        fontSize = 13.sp,
                    )
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
            label = { Text(stringResource(Res.string.reminders_all_vehicles_filter)) },
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
        state.isLoading -> ReminderMessagePanel(stringResource(Res.string.reminders_loading), showProgress = true)
        state.hasLoadError ->
            ReminderMessagePanel(
                title = stringResource(Res.string.reminders_load_error_title),
                detail = stringResource(Res.string.reminders_load_error_description),
                actionLabel = stringResource(Res.string.reminders_retry),
                onAction = onRetry,
            )
        state.hasNoVehicles ->
            ReminderMessagePanel(
                title = stringResource(Res.string.reminders_no_vehicles_title),
                detail = stringResource(Res.string.reminders_no_vehicles_description),
                actionLabel = stringResource(Res.string.reminders_go_to_garage),
                onAction = onNavigateToGarage,
            )
        state.hasNoMatchingReminders ->
            ReminderMessagePanel(
                title = stringResource(Res.string.reminders_no_matches_title),
                detail = stringResource(Res.string.reminders_no_matches_description),
                actionLabel = stringResource(Res.string.reminders_clear_filters),
                onAction = onClearFilters,
            )
        state.isEmpty ->
            ReminderMessagePanel(
                title = stringResource(Res.string.reminders_empty_title),
                detail = stringResource(Res.string.reminders_empty_description),
                actionLabel = stringResource(Res.string.reminders_add),
                onAction = onCreate,
            )
        else -> {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (state.hasPersistenceError) {
                    Surface(color = Color(0xFFFFE9E7), shape = RoundedCornerShape(14.dp)) {
                        Text(
                            stringResource(Res.string.reminders_mutation_error),
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
                            vehicleName = vehicleNames[reminder.vehicleId] ?: stringResource(Res.string.reminders_unavailable_vehicle),
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
                    Text(stringResource(Res.string.reminders_complete))
                }
                IconButton(onClick = onDelete, enabled = activeAction == null) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.reminders_delete_content_description, reminder.title),
                        tint = Muted,
                    )
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
    DialogWindow(
        onCloseRequest = { if (!isSaving) onDismiss() },
        title = stringResource(Res.string.reminders_form_window_title),
    ) {
        Surface(
            modifier = Modifier.width(580.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 12.dp,
        ) {
            Column(modifier = Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(stringResource(Res.string.reminders_form_title), style = MaterialTheme.typography.headlineMedium, color = Ink)
                Text(stringResource(Res.string.reminders_form_description), color = Muted)
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { onEvent(RemindersEvent.TitleChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.reminders_title_label)) },
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
                        label = { Text(stringResource(Res.string.reminders_vehicle_label)) },
                        placeholder = { Text(stringResource(Res.string.reminders_vehicle_placeholder)) },
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
                        label = { Text(stringResource(Res.string.reminders_due_date_label)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.dueOdometerKm,
                        onValueChange = { onEvent(RemindersEvent.DueOdometerChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(Res.string.reminders_due_odometer_label)) },
                        singleLine = true,
                    )
                }
                state.errorMessage?.let { Text(it.remindersMessage(), color = MaterialTheme.colorScheme.error) }
                if (state.hasPersistenceError) {
                    Text(stringResource(Res.string.reminders_save_error), color = MaterialTheme.colorScheme.error)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(Res.string.reminders_cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onEvent(RemindersEvent.SubmitReminder) }, enabled = !isSaving) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(stringResource(Res.string.reminders_add))
                    }
                }
            }
        }
    }
}

@Composable
private fun Reminder.dueDescription(): String {
    val date = dueDate?.iso8601?.let { stringResource(Res.string.reminders_due_date, it) }
    val odometer =
        dueOdometerKm?.let {
            stringResource(Res.string.reminders_due_odometer, NumberFormat.getIntegerInstance().format(it))
        }
    return when {
        date != null && odometer != null -> stringResource(Res.string.reminders_due_date_and_odometer, date, odometer)
        date != null -> date
        odometer != null -> odometer
        else -> stringResource(Res.string.reminders_no_due_target)
    }
}

private fun CarburaString.remindersStringResource(): StringResource =
    when (this) {
        CarburaString.ReminderCreatedMessage -> Res.string.reminders_snackbar_created
        CarburaString.ReminderCompletedMessage -> Res.string.reminders_snackbar_completed
        CarburaString.ReminderDeletedMessage -> Res.string.reminders_snackbar_deleted
        CarburaString.ValidationBlankReminderTitle -> Res.string.reminders_validation_blank_title
        CarburaString.ValidationMissingReminderVehicle -> Res.string.reminders_validation_missing_vehicle
        CarburaString.ValidationMissingReminderDueTarget -> Res.string.reminders_validation_missing_due_target
        CarburaString.ValidationNegativeReminderDueOdometer -> Res.string.reminders_validation_negative_due_odometer
        CarburaString.ValidationInvalidReminderDate -> Res.string.reminders_validation_invalid_date
        else -> Res.string.reminders_validation_generic
    }

@Composable
private fun CarburaString.remindersMessage(): String = stringResource(remindersStringResource())
