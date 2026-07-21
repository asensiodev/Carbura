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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.desktop.resources.Res
import com.asensiodev.carbura.desktop.resources.garage_add_vehicle_button
import com.asensiodev.carbura.desktop.resources.garage_add_vehicle_description_text
import com.asensiodev.carbura.desktop.resources.garage_cancel_button
import com.asensiodev.carbura.desktop.resources.garage_clear_search_button
import com.asensiodev.carbura.desktop.resources.garage_clear_search_description
import com.asensiodev.carbura.desktop.resources.garage_confirm_button
import com.asensiodev.carbura.desktop.resources.garage_create_reminders_button
import com.asensiodev.carbura.desktop.resources.garage_current_odometer_label
import com.asensiodev.carbura.desktop.resources.garage_delete_vehicle_confirm_button
import com.asensiodev.carbura.desktop.resources.garage_delete_vehicle_description
import com.asensiodev.carbura.desktop.resources.garage_delete_vehicle_dialog_description
import com.asensiodev.carbura.desktop.resources.garage_delete_vehicle_dialog_title
import com.asensiodev.carbura.desktop.resources.garage_desktop_notification_notice
import com.asensiodev.carbura.desktop.resources.garage_edit_vehicle_description
import com.asensiodev.carbura.desktop.resources.garage_edit_vehicle_description_text
import com.asensiodev.carbura.desktop.resources.garage_edit_vehicle_title
import com.asensiodev.carbura.desktop.resources.garage_empty_description
import com.asensiodev.carbura.desktop.resources.garage_empty_title
import com.asensiodev.carbura.desktop.resources.garage_header_label
import com.asensiodev.carbura.desktop.resources.garage_headline
import com.asensiodev.carbura.desktop.resources.garage_history_button
import com.asensiodev.carbura.desktop.resources.garage_insurance_renewal_date_label
import com.asensiodev.carbura.desktop.resources.garage_keep_current_value_button
import com.asensiodev.carbura.desktop.resources.garage_kilometers_format
import com.asensiodev.carbura.desktop.resources.garage_license_plate_label
import com.asensiodev.carbura.desktop.resources.garage_load_error_description
import com.asensiodev.carbura.desktop.resources.garage_load_error_title
import com.asensiodev.carbura.desktop.resources.garage_loading_message
import com.asensiodev.carbura.desktop.resources.garage_next_itv_date_label
import com.asensiodev.carbura.desktop.resources.garage_next_service_odometer_label
import com.asensiodev.carbura.desktop.resources.garage_no_search_results_description
import com.asensiodev.carbura.desktop.resources.garage_no_search_results_title
import com.asensiodev.carbura.desktop.resources.garage_odometer_decrease_message
import com.asensiodev.carbura.desktop.resources.garage_odometer_decrease_title
import com.asensiodev.carbura.desktop.resources.garage_odometer_save_error
import com.asensiodev.carbura.desktop.resources.garage_planning_description
import com.asensiodev.carbura.desktop.resources.garage_planning_title
import com.asensiodev.carbura.desktop.resources.garage_reminder_suggestion_item
import com.asensiodev.carbura.desktop.resources.garage_reminder_suggestions_description
import com.asensiodev.carbura.desktop.resources.garage_reminder_suggestions_title
import com.asensiodev.carbura.desktop.resources.garage_retry_button
import com.asensiodev.carbura.desktop.resources.garage_save_changes_button
import com.asensiodev.carbura.desktop.resources.garage_save_without_reminders_button
import com.asensiodev.carbura.desktop.resources.garage_search_vehicles_label
import com.asensiodev.carbura.desktop.resources.garage_search_vehicles_placeholder
import com.asensiodev.carbura.desktop.resources.garage_update_odometer_description
import com.asensiodev.carbura.desktop.resources.garage_update_odometer_help
import com.asensiodev.carbura.desktop.resources.garage_update_odometer_title
import com.asensiodev.carbura.desktop.resources.garage_validation_blank_vehicle_name
import com.asensiodev.carbura.desktop.resources.garage_validation_generic
import com.asensiodev.carbura.desktop.resources.garage_validation_negative_vehicle_odometer
import com.asensiodev.carbura.desktop.resources.garage_vehicle_count_one
import com.asensiodev.carbura.desktop.resources.garage_vehicle_count_other
import com.asensiodev.carbura.desktop.resources.garage_vehicle_created_message
import com.asensiodev.carbura.desktop.resources.garage_vehicle_delete_error
import com.asensiodev.carbura.desktop.resources.garage_vehicle_deleted_message
import com.asensiodev.carbura.desktop.resources.garage_vehicle_fallback
import com.asensiodev.carbura.desktop.resources.garage_vehicle_name_label
import com.asensiodev.carbura.desktop.resources.garage_vehicle_save_error
import com.asensiodev.carbura.desktop.resources.garage_vehicle_type_car
import com.asensiodev.carbura.desktop.resources.garage_vehicle_type_label
import com.asensiodev.carbura.desktop.resources.garage_vehicle_type_motorcycle
import com.asensiodev.carbura.desktop.resources.garage_vehicle_type_other
import com.asensiodev.carbura.desktop.resources.garage_vehicle_type_van
import com.asensiodev.carbura.desktop.resources.garage_vehicle_updated_message
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageLoadState
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewEffect
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewEvent
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewUiState
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewViewModel
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleEditMode
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormEffect
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormEvent
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormUiState
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

@Composable
internal fun GarageWorkspace(
    compact: Boolean,
    onOpenMaintenance: (VehicleId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val overviewViewModel =
        remember {
            val familyId = GlobalContext.get().get<FamilyId>()
            GlobalContext.get().get<GarageOverviewViewModel> { parametersOf(familyId) }
        }
    val formViewModel =
        remember {
            val familyId = GlobalContext.get().get<FamilyId>()
            GlobalContext.get().get<VehicleFormViewModel> { parametersOf(familyId) }
        }
    val overviewState by overviewViewModel.uiState.collectAsState()
    val formState by formViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateForm by remember { mutableStateOf(false) }
    var pendingDeletion by remember { mutableStateOf<Vehicle?>(null) }
    val vehicleFallback = stringResource(Res.string.garage_vehicle_fallback)
    val vehicleDeletedMessage = stringResource(Res.string.garage_vehicle_deleted_message)
    val vehicleCreatedMessage = stringResource(Res.string.garage_vehicle_created_message)
    val vehicleUpdatedMessage = stringResource(Res.string.garage_vehicle_updated_message)

    LaunchedEffect(overviewViewModel) {
        overviewViewModel.onEvent(GarageOverviewEvent.Started)
        overviewViewModel.effects.collect { effect ->
            when (effect) {
                is GarageOverviewEffect.VehicleDeleted -> {
                    snackbarHostState.showSnackbar(
                        vehicleDeletedMessage.format(effect.vehicleName.ifBlank { vehicleFallback }),
                    )
                }
                is GarageOverviewEffect.NavigateToVehicleHistory -> onOpenMaintenance(effect.vehicleId)
            }
        }
    }
    LaunchedEffect(formViewModel) {
        formViewModel.effects.collect { effect ->
            when (effect) {
                is VehicleFormEffect.VehicleCreated -> {
                    showCreateForm = false
                    overviewViewModel.onEvent(GarageOverviewEvent.Refresh)
                    snackbarHostState.showSnackbar(vehicleCreatedMessage.format(effect.vehicleName))
                }
                is VehicleFormEffect.VehicleUpdated -> {
                    overviewViewModel.onEvent(GarageOverviewEvent.Refresh)
                    snackbarHostState.showSnackbar(vehicleUpdatedMessage.format(effect.vehicleName))
                }
                is VehicleFormEffect.ValidationFailed -> Unit
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            GarageHeader(
                vehicleCount = overviewState.vehicles.size,
                compact = compact,
                onAddVehicle = { showCreateForm = true },
            )
            GarageBody(
                state = overviewState,
                compact = compact,
                onRetry = { overviewViewModel.onEvent(GarageOverviewEvent.Retry) },
                onSearchQueryChanged = { overviewViewModel.onEvent(GarageOverviewEvent.SearchQueryChanged(it)) },
                onClearSearch = { overviewViewModel.onEvent(GarageOverviewEvent.SearchCleared) },
                onAddVehicle = { showCreateForm = true },
                onOpenMaintenance = { overviewViewModel.onEvent(GarageOverviewEvent.VehicleSelected(it.id)) },
                onEditVehicle = { formViewModel.onEvent(VehicleFormEvent.EditVehicleRequested(it)) },
                onQuickOdometer = { formViewModel.onEvent(VehicleFormEvent.QuickOdometerUpdateRequested(it)) },
                onDeleteVehicle = { pendingDeletion = it },
            )
        }
    }

    if (showCreateForm || formState.editMode == VehicleEditMode.Full) {
        VehicleFormDialog(
            state = formState,
            isEditing = formState.editingVehicleId != null,
            onEvent = formViewModel::onEvent,
            onDismiss = {
                if (formState.editingVehicleId != null) {
                    formViewModel.onEvent(VehicleFormEvent.DismissVehicleEdit)
                } else {
                    showCreateForm = false
                }
            },
        )
    }

    if (formState.editMode == VehicleEditMode.Odometer) {
        QuickOdometerDialog(
            state = formState,
            onEvent = formViewModel::onEvent,
        )
    }

    if (formState.reminderConfirmationMode != null) {
        VehicleReminderConfirmationDialog(
            state = formState,
            onEvent = formViewModel::onEvent,
        )
    }

    pendingDeletion?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { pendingDeletion = null },
            title = { Text(stringResource(Res.string.garage_delete_vehicle_dialog_title, vehicle.name)) },
            text = { Text(stringResource(Res.string.garage_delete_vehicle_dialog_description)) },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeletion = null
                        overviewViewModel.onEvent(GarageOverviewEvent.DeleteVehicleConfirmed(vehicle.id))
                    },
                ) {
                    Text(stringResource(Res.string.garage_delete_vehicle_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletion = null }) {
                    Text(stringResource(Res.string.garage_cancel_button))
                }
            },
        )
    }

    formState.odometerDecreaseConfirmation?.let { confirmation ->
        AlertDialog(
            onDismissRequest = { formViewModel.onEvent(VehicleFormEvent.CancelOdometerDecrease) },
            title = { Text(stringResource(Res.string.garage_odometer_decrease_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.garage_odometer_decrease_message,
                        confirmation.currentOdometerKm,
                        confirmation.proposedOdometerKm,
                    ),
                )
            },
            confirmButton = {
                Button(onClick = { formViewModel.onEvent(VehicleFormEvent.ConfirmOdometerDecrease) }) {
                    Text(stringResource(Res.string.garage_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { formViewModel.onEvent(VehicleFormEvent.CancelOdometerDecrease) }) {
                    Text(stringResource(Res.string.garage_keep_current_value_button))
                }
            },
        )
    }
}

@Composable
private fun GarageHeader(
    vehicleCount: Int,
    compact: Boolean,
    onAddVehicle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(Res.string.garage_header_label),
                color = Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.6.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.garage_headline), style = MaterialTheme.typography.displaySmall, color = Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                if (vehicleCount == 1) {
                    stringResource(Res.string.garage_vehicle_count_one)
                } else {
                    stringResource(Res.string.garage_vehicle_count_other, vehicleCount)
                },
                color = Muted,
            )
        }
        Spacer(Modifier.width(20.dp))
        Button(onClick = onAddVehicle) {
            Icon(Icons.Default.Add, contentDescription = null)
            if (!compact) {
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.garage_add_vehicle_button))
            }
        }
    }
}

@Composable
private fun GarageBody(
    state: GarageOverviewUiState,
    compact: Boolean,
    onRetry: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onAddVehicle: () -> Unit,
    onOpenMaintenance: (Vehicle) -> Unit,
    onEditVehicle: (Vehicle) -> Unit,
    onQuickOdometer: (Vehicle) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit,
) {
    when (state.loadState) {
        GarageLoadState.Loading ->
            GarageMessagePanel(stringResource(Res.string.garage_loading_message), showProgress = true)
        GarageLoadState.Error ->
            GarageMessagePanel(
                title = stringResource(Res.string.garage_load_error_title),
                detail = stringResource(Res.string.garage_load_error_description),
                actionLabel = stringResource(Res.string.garage_retry_button),
                onAction = onRetry,
            )
        GarageLoadState.Loaded -> {
            if (state.isEmpty) {
                GarageMessagePanel(
                    title = stringResource(Res.string.garage_empty_title),
                    detail = stringResource(Res.string.garage_empty_description),
                    actionLabel = stringResource(Res.string.garage_add_vehicle_button),
                    onAction = onAddVehicle,
                )
            } else {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.deleteError) {
                        Surface(color = Color(0xFFFFE9E7), shape = RoundedCornerShape(14.dp)) {
                            Text(
                                stringResource(Res.string.garage_vehicle_delete_error),
                                color = Color(0xFF8A2D27),
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.garage_search_vehicles_label)) },
                        placeholder = { Text(stringResource(Res.string.garage_search_vehicles_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon =
                            if (state.searchQuery.isNotEmpty()) {
                                {
                                    IconButton(onClick = onClearSearch) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(Res.string.garage_clear_search_description),
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                        singleLine = true,
                    )
                    if (state.hasNoMatchingVehicles) {
                        GarageMessagePanel(
                            title = stringResource(Res.string.garage_no_search_results_title),
                            detail = stringResource(Res.string.garage_no_search_results_description),
                            actionLabel = stringResource(Res.string.garage_clear_search_button),
                            onAction = onClearSearch,
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.visibleVehicles, key = { it.id.value }) { vehicle ->
                                VehicleCard(
                                    vehicle = vehicle,
                                    compact = compact,
                                    isDeleting = state.deletingVehicleId == vehicle.id,
                                    onOpenMaintenance = { onOpenMaintenance(vehicle) },
                                    onEdit = { onEditVehicle(vehicle) },
                                    onQuickOdometer = { onQuickOdometer(vehicle) },
                                    onDelete = { onDeleteVehicle(vehicle) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    compact: Boolean,
    isDeleting: Boolean,
    onOpenMaintenance: () -> Unit,
    onEdit: () -> Unit,
    onQuickOdometer: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(if (compact) 18.dp else 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(PaleBlue, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Blue)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(vehicle.name, color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    listOfNotNull(vehicle.type.displayName(), vehicle.licensePlate?.takeIf(String::isNotBlank))
                        .joinToString("  |  "),
                    color = Muted,
                )
            }
            if (!compact) {
                Text(vehicle.currentOdometerKm.formatKilometers(), color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(20.dp))
            }
            if (isDeleting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                OutlinedButton(onClick = onOpenMaintenance) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
                    if (!compact) {
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(Res.string.garage_history_button))
                    }
                }
                IconButton(onClick = onQuickOdometer) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = stringResource(Res.string.garage_update_odometer_description, vehicle.name),
                        tint = Blue,
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.garage_edit_vehicle_description, vehicle.name),
                        tint = Blue,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.garage_delete_vehicle_description, vehicle.name),
                        tint = Muted,
                    )
                }
            }
        }
    }
}

internal fun maintenanceVehicleForGarageEffect(effect: GarageOverviewEffect): VehicleId? =
    (effect as? GarageOverviewEffect.NavigateToVehicleHistory)?.vehicleId

@Composable
private fun GarageMessagePanel(
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
private fun VehicleFormDialog(
    state: VehicleFormUiState,
    isEditing: Boolean,
    onEvent: (VehicleFormEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val mutationActive = state.activeMutation != null
    val validationError = if (isEditing) state.editValidationError else state.createValidationError
    val windowTitle =
        if (isEditing) {
            stringResource(Res.string.garage_edit_vehicle_title)
        } else {
            stringResource(Res.string.garage_add_vehicle_button)
        }
    DesktopFormDialog(
        title = windowTitle,
        onDismissRequest = onDismiss,
        dismissEnabled = !mutationActive,
        actions = {
            TextButton(onClick = onDismiss, enabled = !mutationActive) {
                Text(stringResource(Res.string.garage_cancel_button))
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    onEvent(if (isEditing) VehicleFormEvent.SubmitVehicleEdit else VehicleFormEvent.SubmitVehicle)
                },
                enabled = !mutationActive && (!isEditing || state.isEditDirty),
            ) {
                if (mutationActive) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    if (isEditing) {
                        stringResource(Res.string.garage_save_changes_button)
                    } else {
                        stringResource(Res.string.garage_add_vehicle_button)
                    },
                )
            }
        },
    ) {
        Text(
            if (isEditing) {
                stringResource(Res.string.garage_edit_vehicle_description_text)
            } else {
                stringResource(Res.string.garage_add_vehicle_description_text)
            },
            color = Muted,
        )
        OutlinedTextField(
            value = if (isEditing) state.editName else state.name,
            onValueChange = {
                onEvent(if (isEditing) VehicleFormEvent.EditNameChanged(it) else VehicleFormEvent.NameChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.garage_vehicle_name_label)) },
            singleLine = true,
        )
        if (isEditing) {
            OutlinedTextField(
                value = state.editLicensePlate,
                onValueChange = { onEvent(VehicleFormEvent.EditLicensePlateChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.garage_license_plate_label)) },
                singleLine = true,
            )
        }
        OutlinedTextField(
            value = if (isEditing) state.editOdometerKm else state.odometerKm,
            onValueChange = {
                onEvent(if (isEditing) VehicleFormEvent.EditOdometerChanged(it) else VehicleFormEvent.OdometerChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.garage_current_odometer_label)) },
            singleLine = true,
        )
        Text(stringResource(Res.string.garage_vehicle_type_label), color = Ink, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VehicleType.entries.forEach { type ->
                FilterChip(
                    selected = type == if (isEditing) state.editType else state.selectedType,
                    onClick = {
                        onEvent(
                            if (isEditing) {
                                VehicleFormEvent.EditTypeSelected(type)
                            } else {
                                VehicleFormEvent.TypeSelected(type)
                            },
                        )
                    },
                    label = { Text(type.displayName()) },
                )
            }
        }
        VehiclePlanningFields(
            state = state,
            isEditing = isEditing,
            enabled = !mutationActive,
            onEvent = onEvent,
        )
        validationError?.let {
            Text(it.garageMessage(), color = MaterialTheme.colorScheme.error)
        }
        if (state.persistenceError) {
            Text(stringResource(Res.string.garage_vehicle_save_error), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun VehiclePlanningFields(
    state: VehicleFormUiState,
    isEditing: Boolean,
    enabled: Boolean,
    onEvent: (VehicleFormEvent) -> Unit,
) {
    Text(stringResource(Res.string.garage_planning_title), color = Ink, fontWeight = FontWeight.SemiBold)
    Text(
        stringResource(Res.string.garage_planning_description),
        color = Muted,
        style = MaterialTheme.typography.bodySmall,
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = if (isEditing) state.editNextItvDate else state.nextItvDate,
            onValueChange = {
                onEvent(if (isEditing) VehicleFormEvent.EditNextItvDateChanged(it) else VehicleFormEvent.NextItvDateChanged(it))
            },
            modifier = Modifier.weight(1f),
            enabled = enabled,
            label = { Text(stringResource(Res.string.garage_next_itv_date_label)) },
            singleLine = true,
        )
        OutlinedTextField(
            value = if (isEditing) state.editInsuranceRenewalDate else state.insuranceRenewalDate,
            onValueChange = {
                onEvent(
                    if (isEditing) {
                        VehicleFormEvent.EditInsuranceRenewalDateChanged(it)
                    } else {
                        VehicleFormEvent.InsuranceRenewalDateChanged(it)
                    },
                )
            },
            modifier = Modifier.weight(1f),
            enabled = enabled,
            label = { Text(stringResource(Res.string.garage_insurance_renewal_date_label)) },
            singleLine = true,
        )
    }
    OutlinedTextField(
        value = if (isEditing) state.editNextServiceOdometerKm else state.nextServiceOdometerKm,
        onValueChange = {
            onEvent(
                if (isEditing) {
                    VehicleFormEvent.EditNextServiceOdometerChanged(it)
                } else {
                    VehicleFormEvent.NextServiceOdometerChanged(it)
                },
            )
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        label = { Text(stringResource(Res.string.garage_next_service_odometer_label)) },
        singleLine = true,
    )
}

@Composable
private fun QuickOdometerDialog(
    state: VehicleFormUiState,
    onEvent: (VehicleFormEvent) -> Unit,
) {
    val mutationActive = state.activeMutation != null
    AlertDialog(
        onDismissRequest = { if (!mutationActive) onEvent(VehicleFormEvent.DismissVehicleEdit) },
        title = { Text(stringResource(Res.string.garage_update_odometer_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.garage_update_odometer_help), color = Muted)
                OutlinedTextField(
                    value = state.editOdometerKm,
                    onValueChange = { onEvent(VehicleFormEvent.EditOdometerChanged(it)) },
                    enabled = !mutationActive,
                    label = { Text(stringResource(Res.string.garage_current_odometer_label)) },
                    singleLine = true,
                )
                state.editValidationError?.let { Text(it.garageMessage(), color = MaterialTheme.colorScheme.error) }
                if (state.persistenceError) {
                    Text(stringResource(Res.string.garage_odometer_save_error), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(VehicleFormEvent.SubmitVehicleEdit) },
                enabled = !mutationActive && state.isEditDirty,
            ) {
                if (mutationActive) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(Res.string.garage_update_odometer_title))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(VehicleFormEvent.DismissVehicleEdit) },
                enabled = !mutationActive,
            ) { Text(stringResource(Res.string.garage_cancel_button)) }
        },
    )
}

@Composable
private fun VehicleReminderConfirmationDialog(
    state: VehicleFormUiState,
    onEvent: (VehicleFormEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(Res.string.garage_reminder_suggestions_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(Res.string.garage_reminder_suggestions_description), color = Muted)
                state.reminderSuggestions.forEach { suggestion ->
                    val target =
                        suggestion.reminder.dueDate?.iso8601
                            ?: suggestion.reminder.dueOdometerKm
                                ?.formatKilometers()
                                .orEmpty()
                    Text(
                        stringResource(
                            Res.string.garage_reminder_suggestion_item,
                            suggestion.reminder.title,
                            target,
                        ),
                        color = Ink,
                    )
                }
                if (state.reminderSuggestions.any { it.reminder.dueDate != null }) {
                    Text(
                        stringResource(Res.string.garage_desktop_notification_notice),
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onEvent(VehicleFormEvent.ConfirmReminderSuggestions) }) {
                Text(stringResource(Res.string.garage_create_reminders_button))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(VehicleFormEvent.DeclineReminderSuggestions) }) {
                Text(stringResource(Res.string.garage_save_without_reminders_button))
            }
        },
    )
}

@Composable
private fun VehicleType.displayName(): String =
    when (this) {
        VehicleType.Car -> stringResource(Res.string.garage_vehicle_type_car)
        VehicleType.Motorcycle -> stringResource(Res.string.garage_vehicle_type_motorcycle)
        VehicleType.Van -> stringResource(Res.string.garage_vehicle_type_van)
        VehicleType.Other -> stringResource(Res.string.garage_vehicle_type_other)
    }

@Composable
private fun Int.formatKilometers(): String = stringResource(Res.string.garage_kilometers_format, this)

@Composable
private fun CarburaString.garageMessage(): String =
    when (this) {
        CarburaString.ValidationBlankVehicleName -> stringResource(Res.string.garage_validation_blank_vehicle_name)
        CarburaString.ValidationNegativeVehicleOdometer ->
            stringResource(Res.string.garage_validation_negative_vehicle_odometer)
        else -> stringResource(Res.string.garage_validation_generic)
    }
