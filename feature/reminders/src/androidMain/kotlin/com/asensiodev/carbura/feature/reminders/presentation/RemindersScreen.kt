package com.asensiodev.carbura.feature.reminders.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.carbura.core.designsystem.Spacings
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featurereminders.R
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun RemindersRoute(
    familyId: String,
    onNavigateToGarage: () -> Unit = {},
    refreshSignal: Long = 0L,
    modifier: Modifier = Modifier,
    viewModel: RemindersViewModel = rememberRemindersViewModel(familyId),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var effectMessage by remember { mutableStateOf<CarburaString?>(null) }
    var effectMessageArg by remember { mutableStateOf<String?>(null) }
    var reminderCreatedSignal by remember { mutableStateOf(0) }
    var reminderSuccessSignal by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val activity = context.findActivity()
    val permissionPreferences = remember(context) { context.getSharedPreferences(PERMISSION_PREFERENCES, Context.MODE_PRIVATE) }
    var notificationPermissionState by remember {
        mutableStateOf(resolveNotificationPermissionState(context, activity, permissionPreferences.getBoolean(PERMISSION_REQUESTED, false)))
    }
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            notificationPermissionState =
                resolveNotificationPermissionState(context, activity, permissionPreferences.getBoolean(PERMISSION_REQUESTED, false))
        }
    val lifecycleOwner = activity as? LifecycleOwner

    DisposableEffect(lifecycleOwner, activity) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    notificationPermissionState =
                        resolveNotificationPermissionState(context, activity, permissionPreferences.getBoolean(PERMISSION_REQUESTED, false))
                }
            }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(RemindersEvent.Started)
    }

    LaunchedEffect(viewModel, refreshSignal) {
        if (refreshSignal > 0L) viewModel.onEvent(RemindersEvent.Refresh)
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

                RemindersEffect.NavigateToGarage -> onNavigateToGarage()
            }
        }
    }

    val resolvedEffectMessage =
        effectMessage?.let { message ->
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
        onRetry = { viewModel.onEvent(RemindersEvent.Retry) },
        onNavigateToGarage = { viewModel.onEvent(RemindersEvent.GarageRequested) },
        notificationPermissionState = notificationPermissionState,
        onRequestNotificationPermission = {
            permissionPreferences.edit().putBoolean(PERMISSION_REQUESTED, true).apply()
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        },
        onOpenNotificationSettings = { context.openNotificationSettings() },
        modifier = modifier,
    )
}

@Composable
private fun rememberRemindersViewModel(familyId: String): RemindersViewModel =
    remember(familyId) {
        GlobalContext.get().get { parametersOf(FamilyId(familyId)) }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RemindersScreen(
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
    onRetry: () -> Unit,
    onNavigateToGarage: () -> Unit,
    notificationPermissionState: NotificationPermissionState,
    onRequestNotificationPermission: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
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

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val showAddReminderAction = !state.isLoading && !state.hasLoadError && !state.hasNoVehicles && !state.isEmpty
        val useCompactAddAction = maxWidth < 600.dp
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                if (showAddReminderAction && useCompactAddAction) {
                    ExtendedFloatingActionButton(
                        onClick = { showReminderSheet = true },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                            )
                        },
                        text = { Text(stringResource(R.string.add_reminder_button)) },
                        modifier = Modifier.testTag("add_reminder_fab"),
                    )
                }
            },
        ) { _ ->
            Box(
                modifier = Modifier.fillMaxSize().statusBarsPadding(),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                            .testTag("reminders_content"),
                    contentPadding =
                        PaddingValues(
                            start = Spacings.spacing24,
                            top = Spacings.spacing24,
                            end = Spacings.spacing24,
                            bottom = if (showAddReminderAction && useCompactAddAction) 104.dp else Spacings.spacing24,
                        ),
                    verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
                ) {
                    item {
                        RemindersHeader(
                            showAddReminderAction = showAddReminderAction && !useCompactAddAction,
                            onAddReminder = { showReminderSheet = true },
                        )
                    }
                    if (notificationPermissionState != NotificationPermissionState.Granted) {
                        item {
                            NotificationPermissionCard(
                                isPermanentlyDenied = notificationPermissionState == NotificationPermissionState.PermanentlyDenied,
                                onAction =
                                    if (notificationPermissionState == NotificationPermissionState.PermanentlyDenied) {
                                        onOpenNotificationSettings
                                    } else {
                                        onRequestNotificationPermission
                                    },
                            )
                        }
                    }
                    if (state.hasPersistenceError) {
                        item { PersistenceErrorCard() }
                    }
                    if (state.hasNoVehicles) {
                        item { NoVehiclesCard(onNavigateToGarage = onNavigateToGarage) }
                    }
                    item {
                        Text(
                            text = stringResource(R.string.pending_reminders_title),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                    if (state.isLoading) {
                        item {
                            LoadingStateCard(message = stringResource(R.string.reminders_loading_message))
                        }
                    } else if (state.hasLoadError) {
                        item { LoadErrorCard(onRetry = onRetry) }
                    } else if (state.isEmpty && state.hasNoVehicles) {
                        Unit
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
                                vehicleName =
                                    state.vehicles.firstOrNull { it.id == reminder.vehicleId }?.name
                                        ?: stringResource(R.string.unavailable_vehicle_name),
                                activeAction = state.activeAction,
                                onCompleteReminder = onCompleteReminder,
                                onDeleteReminder = { reminderPendingDeletion = reminder },
                            )
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
                modifier =
                    Modifier
                        .padding(
                            start = Spacings.spacing24,
                            end = Spacings.spacing24,
                            bottom = Spacings.spacing24,
                        ).navigationBarsPadding()
                        .imePadding(),
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
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics {
                    stateDescription = message
                    liveRegion = LiveRegionMode.Polite
                },
    ) {
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
private fun LoadErrorCard(onRetry: () -> Unit) {
    val errorMessage = stringResource(R.string.reminders_load_error_title)
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacings.spacing24),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    Modifier.semantics {
                        heading()
                        error(errorMessage)
                        liveRegion = LiveRegionMode.Assertive
                    },
            )
            Text(stringResource(R.string.reminders_load_error_description))
            Button(onClick = onRetry) {
                Text(stringResource(R.string.retry_button))
            }
        }
    }
}

@Composable
private fun PersistenceErrorCard() {
    val errorMessage = stringResource(R.string.reminder_mutation_error)
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .semantics {
                    error(errorMessage)
                    liveRegion = LiveRegionMode.Assertive
                },
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Text(
            text = errorMessage,
            modifier = Modifier.padding(Spacings.spacing16),
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun NoVehiclesCard(onNavigateToGarage: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(Spacings.spacing24),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Text(
                text = stringResource(R.string.no_vehicles_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.no_vehicles_description),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Button(onClick = onNavigateToGarage) {
                Text(stringResource(R.string.go_to_garage_button))
            }
        }
    }
}

@Composable
private fun RemindersHeader(
    showAddReminderAction: Boolean,
    onAddReminder: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Text(
            text = stringResource(R.string.reminders_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.semantics { heading() },
        )
        if (showAddReminderAction) {
            Button(onClick = onAddReminder) {
                Text(stringResource(R.string.add_reminder_button))
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
            modifier =
                Modifier
                    .padding(Spacings.spacing16)
                    .verticalScroll(rememberScrollState()),
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
                val validationMessage = stringResource(state.errorMessage.remindersStringRes())
                Text(
                    text = validationMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier =
                        Modifier.semantics {
                            error(validationMessage)
                            liveRegion = LiveRegionMode.Assertive
                        },
                )
            }
            if (state.hasPersistenceError) {
                val saveErrorMessage = stringResource(R.string.reminder_save_error)
                Text(
                    text = saveErrorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier =
                        Modifier.semantics {
                            error(saveErrorMessage)
                            liveRegion = LiveRegionMode.Assertive
                        },
                )
            }
            Button(
                onClick = onSubmitReminder,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.activeAction == null,
            ) {
                Text(
                    stringResource(
                        if (state.activeAction == ReminderAction.Create) {
                            R.string.saving_reminder_button
                        } else {
                            R.string.save_reminder_button
                        },
                    ),
                )
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
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing4),
        ) {
            items(vehicles, key = { it.id.value }) { vehicle ->
                val isSelected = vehicle.id.value == selectedVehicleId
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onVehicleSelected(vehicle) },
                            ).padding(vertical = Spacings.spacing8),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                    )
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPermissionCard(
    isPermanentlyDenied: Boolean,
    onAction: () -> Unit,
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
    ) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        ) {
            Text(
                text = stringResource(R.string.notification_permission_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text =
                    stringResource(
                        if (isPermanentlyDenied) {
                            R.string.notification_permission_settings_description
                        } else {
                            R.string.notification_permission_description
                        },
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Button(onClick = onAction) {
                Text(
                    stringResource(
                        if (isPermanentlyDenied) {
                            R.string.notification_permission_settings_action
                        } else {
                            R.string.notification_permission_action
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun EmptyRemindersCard(onAddReminder: () -> Unit) {
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
                text = stringResource(R.string.empty_reminders_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.empty_reminders_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.empty_reminders_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
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
    activeAction: ReminderAction?,
    onCompleteReminder: (Reminder) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
) {
    val completeDescription = stringResource(R.string.complete_reminder_content_description, reminder.title)
    val deleteDescription = stringResource(R.string.delete_reminder_content_description, reminder.title)
    val busyDescription = stringResource(R.string.reminder_action_in_progress)
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag("reminder_card_${reminder.id.value}")
                .semantics {
                    if (activeAction?.reminderId == reminder.id) {
                        stateDescription = busyDescription
                    }
                },
    ) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        ) {
            Text(reminder.title, style = MaterialTheme.typography.titleMedium)
            Text(vehicleName, style = MaterialTheme.typography.bodyMedium)
            reminder.dueDate?.let {
                Text(it.iso8601, style = MaterialTheme.typography.bodyMedium)
            }
            reminder.dueOdometerKm?.let {
                Text("$it km", style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedButton(
                onClick = { onCompleteReminder(reminder) },
                enabled = activeAction == null,
                modifier = Modifier.semantics { contentDescription = completeDescription },
            ) {
                Text(stringResource(R.string.complete_reminder_button))
            }
            IconButton(
                onClick = { onDeleteReminder(reminder) },
                enabled = activeAction == null,
                modifier = Modifier.semantics { contentDescription = deleteDescription },
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private val ReminderAction.reminderId
    get() =
        when (this) {
            ReminderAction.Create -> null
            is ReminderAction.Complete -> reminderId
            is ReminderAction.Delete -> reminderId
        }

internal enum class NotificationPermissionState {
    Granted,
    Requestable,
    PermanentlyDenied,
}

private fun resolveNotificationPermissionState(
    context: Context,
    activity: Activity?,
    permissionWasRequested: Boolean,
): NotificationPermissionState {
    if (
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    ) {
        return NotificationPermissionState.Granted
    }
    return if (
        permissionWasRequested &&
        activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) != true
    ) {
        NotificationPermissionState.PermanentlyDenied
    } else {
        NotificationPermissionState.Requestable
    }
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

private fun Context.openNotificationSettings() {
    startActivity(
        Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

private const val PERMISSION_PREFERENCES = "reminder_notification_permission"
private const val PERMISSION_REQUESTED = "requested"

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
