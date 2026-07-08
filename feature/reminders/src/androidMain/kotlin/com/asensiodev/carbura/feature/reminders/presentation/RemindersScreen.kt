package com.asensiodev.carbura.feature.reminders.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featurereminders.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

@Composable
fun RemindersRoute(
    familyId: String,
    modifier: Modifier = Modifier,
    viewModel: RemindersViewModel = rememberRemindersViewModel(familyId),
) {
    val uiState by viewModel.uiState.collectAsState()
    var effectMessage by remember { mutableStateOf<CarburaString?>(null) }
    var effectMessageArg by remember { mutableStateOf<String?>(null) }
    var reminderCreatedSignal by remember { mutableStateOf(0) }
    var reminderSuccessSignal by remember { mutableStateOf(0) }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(RemindersEvent.Started)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is RemindersEffect.ReminderCreated -> {
                    effectMessage = CarburaString.ReminderCreatedMessage
                    effectMessageArg = effect.title
                    reminderCreatedSignal += 1
                    reminderSuccessSignal += 1
                }

                is RemindersEffect.ReminderCompleted -> {
                    effectMessage = CarburaString.ReminderCompletedMessage
                    effectMessageArg = effect.title
                    reminderSuccessSignal += 1
                }

                is RemindersEffect.ReminderDeleted -> {
                    effectMessage = CarburaString.ReminderDeletedMessage
                    effectMessageArg = effect.title
                    reminderSuccessSignal += 1
                }

                is RemindersEffect.ValidationFailed -> {
                    effectMessage = effect.message
                    effectMessageArg = null
                }
            }
        }
    }

    val resolvedEffectMessage = effectMessage?.let { message ->
        val arg = effectMessageArg
        if (arg == null) {
            stringResource(message.remindersStringRes())
        } else {
            stringResource(message.remindersStringRes(), arg)
        }
    }

    RemindersScreen(
        state = uiState,
        effectMessage = resolvedEffectMessage,
        reminderCreatedSignal = reminderCreatedSignal,
        reminderSuccessSignal = reminderSuccessSignal,
        onTitleChange = { viewModel.onEvent(RemindersEvent.TitleChanged(it)) },
        onVehicleSelected = { viewModel.onEvent(RemindersEvent.VehicleSelected(it.id)) },
        onDueDateChange = { viewModel.onEvent(RemindersEvent.DueDateChanged(it)) },
        onDueOdometerChange = { viewModel.onEvent(RemindersEvent.DueOdometerChanged(it)) },
        onSubmitReminder = { viewModel.onEvent(RemindersEvent.SubmitReminder) },
        onCompleteReminder = { viewModel.onEvent(RemindersEvent.CompleteReminder(it.id)) },
        onDeleteReminder = { viewModel.onEvent(RemindersEvent.DeleteReminder(it.id)) },
        modifier = modifier,
    )
}

@Composable
private fun rememberRemindersViewModel(familyId: String): RemindersViewModel = remember(familyId) {
    GlobalContext.get().get { parametersOf(FamilyId(familyId)) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemindersScreen(
    state: RemindersUiState,
    effectMessage: String?,
    reminderCreatedSignal: Int,
    reminderSuccessSignal: Int,
    onTitleChange: (String) -> Unit,
    onVehicleSelected: (Vehicle) -> Unit,
    onDueDateChange: (String) -> Unit,
    onDueOdometerChange: (String) -> Unit,
    onSubmitReminder: () -> Unit,
    onCompleteReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showReminderSheet by remember { mutableStateOf(false) }
    var reminderPendingDeletion by remember { mutableStateOf<Reminder?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(reminderCreatedSignal) {
        if (reminderCreatedSignal > 0) {
            showReminderSheet = false
        }
    }

    LaunchedEffect(reminderSuccessSignal) {
        if (reminderSuccessSignal > 0 && effectMessage != null) {
            snackbarHostState.showSnackbar(effectMessage)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
        ) { _ ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(Spacings.spacing24),
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
            ) {
                item {
                    RemindersHeader(
                        showAddReminderAction = !state.isEmpty,
                        onAddReminder = { showReminderSheet = true },
                    )
                }
                item {
                    Text(
                        text = stringResource(R.string.pending_reminders_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (state.isLoading) {
                    item {
                        LoadingStateCard(message = stringResource(R.string.reminders_loading_message))
                    }
                } else if (state.isEmpty) {
                    item {
                        EmptyRemindersCard(
                            onAddReminder = { showReminderSheet = true },
                        )
                    }
                } else {
                    items(state.reminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            vehicleName = state.vehicles.firstOrNull { it.id == reminder.vehicleId }?.name ?: reminder.vehicleId.value,
                            onCompleteReminder = onCompleteReminder,
                            onDeleteReminder = { reminderPendingDeletion = reminder },
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

    if (showReminderSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReminderSheet = false },
            sheetState = sheetState,
        ) {
            ReminderForm(
                state = state,
                onTitleChange = onTitleChange,
                onVehicleSelected = onVehicleSelected,
                onDueDateChange = onDueDateChange,
                onDueOdometerChange = onDueOdometerChange,
                onSubmitReminder = onSubmitReminder,
                modifier = Modifier.padding(
                    start = Spacings.spacing24,
                    end = Spacings.spacing24,
                    bottom = Spacings.spacing24,
                ),
            )
        }
    }

    reminderPendingDeletion?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderPendingDeletion = null },
            title = { Text(stringResource(R.string.delete_reminder_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
                    Text(stringResource(R.string.delete_reminder_dialog_description))
                    Text(
                        text = reminder.title,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderPendingDeletion = null
                        onDeleteReminder(reminder)
                    },
                ) {
                    Text(stringResource(R.string.delete_reminder_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderPendingDeletion = null }) {
                    Text(stringResource(R.string.delete_reminder_cancel_button))
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

@Composable
private fun RemindersHeader(
    showAddReminderAction: Boolean,
    onAddReminder: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.reminders_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            if (showAddReminderAction) {
                Button(onClick = onAddReminder) {
                    Text(stringResource(R.string.add_reminder_button))
                }
            }
        }
        Text(
            text = stringResource(R.string.reminders_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReminderForm(
    state: RemindersUiState,
    onTitleChange: (String) -> Unit,
    onVehicleSelected: (Vehicle) -> Unit,
    onDueDateChange: (String) -> Unit,
    onDueOdometerChange: (String) -> Unit,
    onSubmitReminder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Text(
                text = stringResource(R.string.reminders_form_title),
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.reminder_title_label)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true,
            )
            VehicleSelector(
                vehicles = state.vehicles,
                selectedVehicleId = state.selectedVehicleId?.value,
                onVehicleSelected = onVehicleSelected,
            )
            ReminderDatePickerField(
                value = state.dueDate,
                onValueChange = onDueDateChange,
            )
            OutlinedTextField(
                value = state.dueOdometerKm,
                onValueChange = onDueOdometerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.reminder_due_odometer_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            if (state.errorMessage != null) {
                Text(
                    text = stringResource(state.errorMessage.remindersStringRes()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = onSubmitReminder,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save_reminder_button))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = value.toUtcMillisOrNull())

    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Text(
            text = stringResource(R.string.reminder_due_date_label),
            style = MaterialTheme.typography.labelLarge,
        )
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(value.ifBlank { stringResource(R.string.select_reminder_due_date_button) })
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
private fun VehicleSelector(
    vehicles: List<Vehicle>,
    selectedVehicleId: String?,
    onVehicleSelected: (Vehicle) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Text(
            text = stringResource(R.string.select_vehicle_title),
            style = MaterialTheme.typography.titleSmall,
        )
        vehicles.forEach { vehicle ->
            val isSelected = vehicle.id.value == selectedVehicleId
            if (isSelected) {
                Button(onClick = { onVehicleSelected(vehicle) }) {
                    Text(vehicle.name)
                }
            } else {
                OutlinedButton(onClick = { onVehicleSelected(vehicle) }) {
                    Text(vehicle.name)
                }
            }
        }
    }
}

@Composable
private fun EmptyRemindersCard(
    onAddReminder: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Text(
                text = stringResource(R.string.empty_reminders_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.empty_reminders_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onAddReminder) {
                Text(stringResource(R.string.add_first_reminder_button))
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    vehicleName: String,
    onCompleteReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
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
                Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(vehicleName, style = MaterialTheme.typography.bodyMedium)
                    IconButton(onClick = { onDeleteReminder(reminder) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_reminder_content_description),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            reminder.dueDate?.let {
                Text(it.iso8601, style = MaterialTheme.typography.bodyMedium)
            }
            reminder.dueOdometerKm?.let {
                Text("$it km", style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedButton(onClick = { onCompleteReminder(reminder) }) {
                Text(stringResource(R.string.complete_reminder_button))
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
