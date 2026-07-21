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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Search
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
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.desktop.resources.Res
import com.asensiodev.carbura.desktop.resources.maintenance_add_button
import com.asensiodev.carbura.desktop.resources.maintenance_add_vehicle_description
import com.asensiodev.carbura.desktop.resources.maintenance_add_vehicle_title
import com.asensiodev.carbura.desktop.resources.maintenance_cancel_button
import com.asensiodev.carbura.desktop.resources.maintenance_clear_search_button
import com.asensiodev.carbura.desktop.resources.maintenance_clear_search_content_description
import com.asensiodev.carbura.desktop.resources.maintenance_cost_label
import com.asensiodev.carbura.desktop.resources.maintenance_cost_value
import com.asensiodev.carbura.desktop.resources.maintenance_created_message
import com.asensiodev.carbura.desktop.resources.maintenance_custom_type_label
import com.asensiodev.carbura.desktop.resources.maintenance_date_label
import com.asensiodev.carbura.desktop.resources.maintenance_delete_content_description
import com.asensiodev.carbura.desktop.resources.maintenance_delete_dialog_description
import com.asensiodev.carbura.desktop.resources.maintenance_delete_dialog_title
import com.asensiodev.carbura.desktop.resources.maintenance_delete_record_button
import com.asensiodev.carbura.desktop.resources.maintenance_deleted_message
import com.asensiodev.carbura.desktop.resources.maintenance_edit_content_description
import com.asensiodev.carbura.desktop.resources.maintenance_edit_form_title
import com.asensiodev.carbura.desktop.resources.maintenance_empty_description
import com.asensiodev.carbura.desktop.resources.maintenance_empty_title
import com.asensiodev.carbura.desktop.resources.maintenance_form_description
import com.asensiodev.carbura.desktop.resources.maintenance_form_title
import com.asensiodev.carbura.desktop.resources.maintenance_future_reminder_confirm
import com.asensiodev.carbura.desktop.resources.maintenance_future_reminder_description
import com.asensiodev.carbura.desktop.resources.maintenance_future_reminder_save_only
import com.asensiodev.carbura.desktop.resources.maintenance_future_reminder_title
import com.asensiodev.carbura.desktop.resources.maintenance_header_history_for
import com.asensiodev.carbura.desktop.resources.maintenance_header_kicker
import com.asensiodev.carbura.desktop.resources.maintenance_header_select_vehicle
import com.asensiodev.carbura.desktop.resources.maintenance_header_subtitle
import com.asensiodev.carbura.desktop.resources.maintenance_history_load_error_description
import com.asensiodev.carbura.desktop.resources.maintenance_history_load_error_title
import com.asensiodev.carbura.desktop.resources.maintenance_loading_history
import com.asensiodev.carbura.desktop.resources.maintenance_loading_vehicles
import com.asensiodev.carbura.desktop.resources.maintenance_next_due_label
import com.asensiodev.carbura.desktop.resources.maintenance_next_due_value
import com.asensiodev.carbura.desktop.resources.maintenance_no_search_results_description
import com.asensiodev.carbura.desktop.resources.maintenance_no_search_results_title
import com.asensiodev.carbura.desktop.resources.maintenance_notes_label
import com.asensiodev.carbura.desktop.resources.maintenance_odometer_label
import com.asensiodev.carbura.desktop.resources.maintenance_odometer_value
import com.asensiodev.carbura.desktop.resources.maintenance_performed_value
import com.asensiodev.carbura.desktop.resources.maintenance_persistence_error
import com.asensiodev.carbura.desktop.resources.maintenance_record_save_error
import com.asensiodev.carbura.desktop.resources.maintenance_reminder_created_message
import com.asensiodev.carbura.desktop.resources.maintenance_reminders_unavailable
import com.asensiodev.carbura.desktop.resources.maintenance_retry_button
import com.asensiodev.carbura.desktop.resources.maintenance_save_button
import com.asensiodev.carbura.desktop.resources.maintenance_search_label
import com.asensiodev.carbura.desktop.resources.maintenance_search_placeholder
import com.asensiodev.carbura.desktop.resources.maintenance_select_vehicle_description
import com.asensiodev.carbura.desktop.resources.maintenance_select_vehicle_title
import com.asensiodev.carbura.desktop.resources.maintenance_type_custom
import com.asensiodev.carbura.desktop.resources.maintenance_type_general_review
import com.asensiodev.carbura.desktop.resources.maintenance_type_insurance
import com.asensiodev.carbura.desktop.resources.maintenance_type_itv
import com.asensiodev.carbura.desktop.resources.maintenance_type_label
import com.asensiodev.carbura.desktop.resources.maintenance_type_oil_change
import com.asensiodev.carbura.desktop.resources.maintenance_type_repair
import com.asensiodev.carbura.desktop.resources.maintenance_type_tires
import com.asensiodev.carbura.desktop.resources.maintenance_update_button
import com.asensiodev.carbura.desktop.resources.maintenance_updated_message
import com.asensiodev.carbura.desktop.resources.maintenance_validation_blank_type
import com.asensiodev.carbura.desktop.resources.maintenance_validation_generic
import com.asensiodev.carbura.desktop.resources.maintenance_validation_invalid_date
import com.asensiodev.carbura.desktop.resources.maintenance_validation_negative_cost
import com.asensiodev.carbura.desktop.resources.maintenance_validation_negative_odometer
import com.asensiodev.carbura.desktop.resources.maintenance_vehicles_load_error_description
import com.asensiodev.carbura.desktop.resources.maintenance_vehicles_load_error_title
import com.asensiodev.carbura.desktop.resources.maintenance_workshop_label
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageLoadState
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewEvent
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewViewModel
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryEffect
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryEvent
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryUiState
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryViewModel
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceLoadState
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceMutation
import org.jetbrains.compose.resources.stringResource
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
                resolveMaintenanceVehicleSelection(
                    currentVehicleId = selectedVehicleId,
                    initialVehicleId = initialVehicleId,
                    availableVehicleIds = overviewState.vehicles.mapTo(mutableSetOf()) { it.id },
                )
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
                GarageLoadState.Loading ->
                    MaintenanceMessagePanel(
                        stringResource(Res.string.maintenance_loading_vehicles),
                        showProgress = true,
                    )
                GarageLoadState.Error ->
                    MaintenanceMessagePanel(
                        title = stringResource(Res.string.maintenance_vehicles_load_error_title),
                        detail = stringResource(Res.string.maintenance_vehicles_load_error_description),
                        actionLabel = stringResource(Res.string.maintenance_retry_button),
                        onAction = { overviewViewModel.onEvent(GarageOverviewEvent.Retry) },
                    )
                GarageLoadState.Loaded -> {
                    if (overviewState.vehicles.isEmpty()) {
                        MaintenanceMessagePanel(
                            title = stringResource(Res.string.maintenance_add_vehicle_title),
                            detail = stringResource(Res.string.maintenance_add_vehicle_description),
                            actionLabel = stringResource(Res.string.maintenance_add_button),
                            onAction = onNavigateToGarage,
                        )
                    } else {
                        VehicleSelector(
                            vehicles = overviewState.vehicles,
                            selectedVehicleId = selectedVehicleId,
                            onSelected = { selectedVehicleId = it },
                        )
                        val vehicleId = selectedVehicleId
                        if (vehicleId == null) {
                            MaintenanceMessagePanel(
                                title = stringResource(Res.string.maintenance_select_vehicle_title),
                                detail = stringResource(Res.string.maintenance_select_vehicle_description),
                            )
                        } else {
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

internal fun resolveMaintenanceVehicleSelection(
    currentVehicleId: VehicleId?,
    initialVehicleId: VehicleId?,
    availableVehicleIds: Set<VehicleId>,
): VehicleId? =
    currentVehicleId?.takeIf(availableVehicleIds::contains)
        ?: initialVehicleId?.takeIf(availableVehicleIds::contains)

@Composable
private fun MaintenanceHeader(
    selectedVehicle: Vehicle?,
    compact: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(Res.string.maintenance_header_kicker),
                color = Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.6.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.maintenance_header_subtitle), style = MaterialTheme.typography.displaySmall, color = Ink)
            Spacer(Modifier.height(6.dp))
            Text(
                selectedVehicle?.let { stringResource(Res.string.maintenance_header_history_for, it.name) }
                    ?: stringResource(Res.string.maintenance_header_select_vehicle),
                color = Muted,
            )
        }
        if (!compact) {
            Surface(color = PaleBlue, shape = RoundedCornerShape(14.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = Blue, modifier = Modifier.size(18.dp))
                    Text(stringResource(Res.string.maintenance_reminders_unavailable), color = Muted, fontSize = 12.sp)
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
    val typeNames = MaintenanceTypeCode.entries.associateWith { it.localizedDisplayName() }
    val createdMessage = stringResource(Res.string.maintenance_created_message)
    val reminderCreatedMessage = stringResource(Res.string.maintenance_reminder_created_message)
    val deletedMessage = stringResource(Res.string.maintenance_deleted_message)
    val updatedMessage = stringResource(Res.string.maintenance_updated_message)

    LaunchedEffect(viewModel) {
        viewModel.onEvent(MaintenanceHistoryEvent.Started)
        viewModel.effects.collect { effect ->
            when (effect) {
                is MaintenanceHistoryEffect.MaintenanceCreated -> {
                    showForm = false
                    val type = effect.typeCode.displayName(typeNames, effect.customTypeLabel)
                    snackbarHostState.showSnackbar(
                        if (effect.reminderCreated) reminderCreatedMessage.format(type) else createdMessage.format(type),
                    )
                }
                is MaintenanceHistoryEffect.MaintenanceDeleted -> {
                    snackbarHostState.showSnackbar(deletedMessage.format(effect.typeCode.displayName(typeNames, effect.customTypeLabel)))
                }
                is MaintenanceHistoryEffect.MaintenanceUpdated -> {
                    showForm = false
                    snackbarHostState.showSnackbar(updatedMessage.format(effect.typeCode.displayName(typeNames, effect.customTypeLabel)))
                }
                is MaintenanceHistoryEffect.ValidationFailed -> Unit
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.loadState == MaintenanceLoadState.Content) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.records.isNotEmpty()) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onEvent(MaintenanceHistoryEvent.SearchQueryChanged(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(Res.string.maintenance_search_label)) },
                        placeholder = { Text(stringResource(Res.string.maintenance_search_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon =
                            if (state.searchQuery.isNotEmpty()) {
                                {
                                    IconButton(onClick = { viewModel.onEvent(MaintenanceHistoryEvent.SearchCleared) }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(Res.string.maintenance_clear_search_content_description),
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                        singleLine = true,
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Button(onClick = { showForm = true }, enabled = state.activeMutation == null) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.maintenance_form_title))
                }
            }
        }
        if (state.persistenceError) {
            Surface(color = Color(0xFFFFE9E7), shape = RoundedCornerShape(14.dp)) {
                Text(
                    stringResource(Res.string.maintenance_persistence_error),
                    color = Color(0xFF8A2D27),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                )
            }
        }
        MaintenanceHistoryBody(
            state = state,
            onRetry = { viewModel.onEvent(MaintenanceHistoryEvent.Retry) },
            onCreate = { showForm = true },
            onClearSearch = { viewModel.onEvent(MaintenanceHistoryEvent.SearchCleared) },
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
            title = { Text(stringResource(Res.string.maintenance_delete_dialog_title)) },
            text = { Text(stringResource(Res.string.maintenance_delete_dialog_description)) },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeletion = null
                        viewModel.onEvent(MaintenanceHistoryEvent.DeleteMaintenance(record.id))
                    },
                ) {
                    Text(stringResource(Res.string.maintenance_delete_record_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletion = null }) { Text(stringResource(Res.string.maintenance_cancel_button)) }
            },
        )
    }
}

@Composable
private fun MaintenanceHistoryBody(
    state: MaintenanceHistoryUiState,
    onRetry: () -> Unit,
    onCreate: () -> Unit,
    onClearSearch: () -> Unit,
    onEdit: (MaintenanceRecord) -> Unit,
    onDelete: (MaintenanceRecord) -> Unit,
) {
    when (state.loadState) {
        MaintenanceLoadState.Loading -> MaintenanceMessagePanel(stringResource(Res.string.maintenance_loading_history), showProgress = true)
        MaintenanceLoadState.Error ->
            MaintenanceMessagePanel(
                title = stringResource(Res.string.maintenance_history_load_error_title),
                detail = stringResource(Res.string.maintenance_history_load_error_description),
                actionLabel = stringResource(Res.string.maintenance_retry_button),
                onAction = onRetry,
            )
        MaintenanceLoadState.Content -> {
            if (state.isEmpty) {
                MaintenanceMessagePanel(
                    title = stringResource(Res.string.maintenance_empty_title),
                    detail = stringResource(Res.string.maintenance_empty_description),
                    actionLabel = stringResource(Res.string.maintenance_form_title),
                    onAction = onCreate,
                )
            } else if (state.hasNoMatchingRecords) {
                MaintenanceMessagePanel(
                    title = stringResource(Res.string.maintenance_no_search_results_title),
                    detail = stringResource(Res.string.maintenance_no_search_results_description),
                    actionLabel = stringResource(Res.string.maintenance_clear_search_button),
                    onAction = onClearSearch,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.visibleRecords, key = { it.id.value }) { record ->
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
    val displayTypeName = record.localizedDisplayTypeName()
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
                Text(displayTypeName, color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.maintenance_performed_value, record.performedOn.iso8601),
                    color = Blue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                )
                val details = record.localizedDetails()
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
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.maintenance_edit_content_description, displayTypeName),
                        tint = Blue,
                    )
                }
                IconButton(onClick = onDelete, enabled = actionsEnabled) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.maintenance_delete_content_description, displayTypeName),
                        tint = Muted,
                    )
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
    val dialogTitle =
        stringResource(
            if (state.isEditing) Res.string.maintenance_edit_form_title else Res.string.maintenance_form_title,
        )
    DesktopFormDialog(
        title = dialogTitle,
        onDismissRequest = onDismiss,
        dismissEnabled = state.activeMutation == null,
        actions = {
            TextButton(onClick = onDismiss, enabled = state.activeMutation == null) {
                Text(stringResource(Res.string.maintenance_cancel_button))
            }
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
                Text(
                    stringResource(
                        if (state.isEditing) Res.string.maintenance_update_button else Res.string.maintenance_save_button,
                    ),
                )
            }
        },
    ) {
        Text(stringResource(Res.string.maintenance_form_description), color = Muted)
        Text(stringResource(Res.string.maintenance_type_label), color = Ink, fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MaintenanceTypeCode.entries.forEach { type ->
                FilterChip(
                    selected = state.maintenanceTypeCode == type,
                    onClick = { onEvent(MaintenanceHistoryEvent.TypeSelected(type)) },
                    label = { Text(type.localizedDisplayName()) },
                )
            }
        }
        if (state.maintenanceTypeCode == MaintenanceTypeCode.Custom) {
            OutlinedTextField(
                value = state.customTypeLabel,
                onValueChange = { onEvent(MaintenanceHistoryEvent.CustomTypeLabelChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.maintenance_custom_type_label)) },
                singleLine = true,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.performedOn,
                onValueChange = { onEvent(MaintenanceHistoryEvent.PerformedOnChanged(it)) },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(Res.string.maintenance_date_label)) },
                singleLine = true,
            )
            OutlinedTextField(
                value = state.odometerKm,
                onValueChange = { onEvent(MaintenanceHistoryEvent.OdometerChanged(it)) },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(Res.string.maintenance_odometer_label)) },
                singleLine = true,
            )
        }
        if (state.supportsNextDueDate) {
            OutlinedTextField(
                value = state.nextDueDate,
                onValueChange = { onEvent(MaintenanceHistoryEvent.NextDueDateChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.maintenance_next_due_label)) },
                singleLine = true,
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = state.cost,
                onValueChange = { onEvent(MaintenanceHistoryEvent.CostChanged(it)) },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(Res.string.maintenance_cost_label)) },
                singleLine = true,
            )
            OutlinedTextField(
                value = state.workshop,
                onValueChange = { onEvent(MaintenanceHistoryEvent.WorkshopChanged(it)) },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(Res.string.maintenance_workshop_label)) },
                singleLine = true,
            )
        }
        OutlinedTextField(
            value = state.notes,
            onValueChange = { onEvent(MaintenanceHistoryEvent.NotesChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.maintenance_notes_label)) },
            minLines = 2,
            maxLines = 3,
        )
        state.validationError?.let { Text(it.localizedMaintenanceMessage(), color = MaterialTheme.colorScheme.error) }
        if (state.persistenceError) {
            Text(stringResource(Res.string.maintenance_record_save_error), color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun FutureMaintenanceDialog(onEvent: (MaintenanceHistoryEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(MaintenanceHistoryEvent.DismissFutureReminderOffer) },
        title = { Text(stringResource(Res.string.maintenance_future_reminder_title)) },
        text = {
            Text(stringResource(Res.string.maintenance_future_reminder_description))
        },
        confirmButton = {
            Button(onClick = { onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder) }) {
                Text(stringResource(Res.string.maintenance_future_reminder_confirm))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { onEvent(MaintenanceHistoryEvent.DismissFutureReminderOffer) }) {
                    Text(stringResource(Res.string.maintenance_cancel_button))
                }
                TextButton(onClick = { onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceOnly) }) {
                    Text(stringResource(Res.string.maintenance_future_reminder_save_only))
                }
            }
        },
    )
}

@Composable
private fun MaintenanceTypeCode.localizedDisplayName(): String =
    when (this) {
        MaintenanceTypeCode.Itv -> stringResource(Res.string.maintenance_type_itv)
        MaintenanceTypeCode.Insurance -> stringResource(Res.string.maintenance_type_insurance)
        MaintenanceTypeCode.OilChange -> stringResource(Res.string.maintenance_type_oil_change)
        MaintenanceTypeCode.Tires -> stringResource(Res.string.maintenance_type_tires)
        MaintenanceTypeCode.GeneralReview -> stringResource(Res.string.maintenance_type_general_review)
        MaintenanceTypeCode.Repair -> stringResource(Res.string.maintenance_type_repair)
        MaintenanceTypeCode.Custom -> stringResource(Res.string.maintenance_type_custom)
    }

private fun MaintenanceTypeCode.displayName(
    typeNames: Map<MaintenanceTypeCode, String>,
    customLabel: String = "",
): String = if (this == MaintenanceTypeCode.Custom) customLabel.ifBlank { typeNames.getValue(this) } else typeNames.getValue(this)

@Composable
private fun MaintenanceRecord.localizedDisplayTypeName(): String {
    val typeNames = MaintenanceTypeCode.entries.associateWith { it.localizedDisplayName() }
    return maintenanceTypeCode?.displayName(typeNames, maintenanceTypeLabel.orEmpty())
        ?: maintenanceTypeId.value
            .removePrefix("type-")
            .replace('-', ' ')
            .replaceFirstChar(Char::uppercase)
}

@Composable
private fun MaintenanceRecord.localizedDetails(): List<String> =
    listOfNotNull(
        odometerKm?.let { stringResource(Res.string.maintenance_odometer_value, it) },
        costCents?.let {
            stringResource(
                Res.string.maintenance_cost_value,
                String.format(Locale.ROOT, "%.2f", it / 100.0),
                currency,
            )
        },
        workshop?.takeIf(String::isNotBlank),
        nextDueDate?.let { stringResource(Res.string.maintenance_next_due_value, it.iso8601) },
    )

@Composable
private fun CarburaString.localizedMaintenanceMessage(): String =
    stringResource(
        when (this) {
            CarburaString.ValidationBlankMaintenanceType -> Res.string.maintenance_validation_blank_type
            CarburaString.ValidationInvalidMaintenanceDate -> Res.string.maintenance_validation_invalid_date
            CarburaString.ValidationNegativeMaintenanceOdometer -> Res.string.maintenance_validation_negative_odometer
            CarburaString.ValidationNegativeMaintenanceCost -> Res.string.maintenance_validation_negative_cost
            else -> Res.string.maintenance_validation_generic
        },
    )
