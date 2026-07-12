package com.asensiodev.carbura.feature.garage.presentation

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featuregarage.R
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

@Composable
fun GarageRoute(
    familyId: String,
    modifier: Modifier = Modifier,
    onVehicleSelected: (String) -> Unit = {},
    viewModel: GarageViewModel = rememberGarageViewModel(familyId),
) {
    val uiState by viewModel.uiState.collectAsState()
    var effectMessage by remember { mutableStateOf<CarburaString?>(null) }
    var effectMessageArg by remember { mutableStateOf<String?>(null) }
    var vehicleCreatedSignal by remember { mutableStateOf(0) }
    var vehicleSuccessSignal by remember { mutableStateOf(0) }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(GarageEvent.Started)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GarageEffect.VehicleCreated -> {
                    effectMessage = CarburaString.VehicleCreatedMessage
                    effectMessageArg = effect.vehicleName
                    vehicleCreatedSignal += 1
                    vehicleSuccessSignal += 1
                }

                is GarageEffect.VehicleDeleted -> {
                    effectMessage = CarburaString.VehicleDeletedMessage
                    effectMessageArg = effect.vehicleName
                    vehicleSuccessSignal += 1
                }

                is GarageEffect.VehicleUpdated -> {
                    effectMessage = CarburaString.VehicleUpdatedMessage
                    effectMessageArg = effect.vehicleName
                    vehicleSuccessSignal += 1
                }

                is GarageEffect.ValidationFailed -> {
                    effectMessage = effect.message
                    effectMessageArg = null
                }

                is GarageEffect.NavigateToVehicleHistory -> {
                    onVehicleSelected(effect.vehicleId.value)
                }
            }
        }
    }

    val resolvedEffectMessage =
        effectMessage?.let { message ->
            val arg = effectMessageArg
            if (arg == null) {
                stringResource(message.garageStringRes())
            } else {
                stringResource(message.garageStringRes(), arg)
            }
        }

    GarageScreen(
        state = uiState,
        effectMessage = resolvedEffectMessage,
        vehicleCreatedSignal = vehicleCreatedSignal,
        vehicleSuccessSignal = vehicleSuccessSignal,
        onNameChange = { value -> viewModel.onEvent(GarageEvent.NameChanged(value)) },
        onOdometerChange = { value -> viewModel.onEvent(GarageEvent.OdometerChanged(value)) },
        onTypeSelected = { value -> viewModel.onEvent(GarageEvent.TypeSelected(value)) },
        onCreateVehicle = { viewModel.onEvent(GarageEvent.SubmitVehicle) },
        onSelectVehicle = { vehicle -> viewModel.onEvent(GarageEvent.VehicleSelected(vehicle.id)) },
        onDeleteVehicle = { vehicle -> viewModel.onEvent(GarageEvent.DeleteVehicleConfirmed(vehicle.id)) },
        onEditVehicle = { vehicle -> viewModel.onEvent(GarageEvent.EditVehicleRequested(vehicle.id)) },
        onQuickOdometerUpdate = { vehicle -> viewModel.onEvent(GarageEvent.QuickOdometerUpdateRequested(vehicle.id)) },
        onEditNameChange = { viewModel.onEvent(GarageEvent.EditNameChanged(it)) },
        onEditLicensePlateChange = { viewModel.onEvent(GarageEvent.EditLicensePlateChanged(it)) },
        onEditOdometerChange = { viewModel.onEvent(GarageEvent.EditOdometerChanged(it)) },
        onEditTypeSelected = { viewModel.onEvent(GarageEvent.EditTypeSelected(it)) },
        onSubmitEdit = { viewModel.onEvent(GarageEvent.SubmitVehicleEdit) },
        onDismissEdit = { viewModel.onEvent(GarageEvent.DismissVehicleEdit) },
        onConfirmOdometerDecrease = { viewModel.onEvent(GarageEvent.ConfirmOdometerDecrease) },
        onCancelOdometerDecrease = { viewModel.onEvent(GarageEvent.CancelOdometerDecrease) },
        modifier = modifier,
    )
}

@Composable
private fun rememberGarageViewModel(familyId: String): GarageViewModel =
    remember(familyId) {
        GlobalContext.get().get { parametersOf(FamilyId(familyId)) }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GarageScreen(
    state: GarageUiState,
    effectMessage: String?,
    vehicleCreatedSignal: Int,
    vehicleSuccessSignal: Int,
    onNameChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onTypeSelected: (VehicleType) -> Unit,
    onCreateVehicle: () -> Unit,
    onSelectVehicle: (Vehicle) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit,
    onEditVehicle: (Vehicle) -> Unit,
    onQuickOdometerUpdate: (Vehicle) -> Unit,
    onEditNameChange: (String) -> Unit,
    onEditLicensePlateChange: (String) -> Unit,
    onEditOdometerChange: (String) -> Unit,
    onEditTypeSelected: (VehicleType) -> Unit,
    onSubmitEdit: () -> Unit,
    onDismissEdit: () -> Unit,
    onConfirmOdometerDecrease: () -> Unit,
    onCancelOdometerDecrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showVehicleSheet by remember { mutableStateOf(false) }
    var vehiclePendingDeletion by remember { mutableStateOf<Vehicle?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vehicleCreatedSignal) {
        if (vehicleCreatedSignal > 0) {
            showVehicleSheet = false
        }
    }

    LaunchedEffect(vehicleSuccessSignal) {
        if (vehicleSuccessSignal > 0 && effectMessage != null) {
            snackbarHostState.showSnackbar(effectMessage)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
        ) { _ ->
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                contentPadding = PaddingValues(Spacings.spacing24),
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
            ) {
                item {
                    GarageHeader(
                        showAddVehicleAction = !state.isEmpty,
                        onAddVehicle = { showVehicleSheet = true },
                    )
                }

                if (state.isLoading) {
                    item {
                        LoadingStateCard(message = stringResource(R.string.garage_loading_message))
                    }
                } else if (state.isEmpty) {
                    item {
                        EmptyGarageCard(
                            onAddVehicle = { showVehicleSheet = true },
                        )
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(R.string.vehicle_list_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    items(state.vehicles) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            onSelectVehicle = onSelectVehicle,
                            onDeleteVehicle = { vehiclePendingDeletion = vehicle },
                            onEditVehicle = onEditVehicle,
                            onQuickOdometerUpdate = onQuickOdometerUpdate,
                        )
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

    if (showVehicleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVehicleSheet = false },
            sheetState = sheetState,
        ) {
            VehicleForm(
                title =
                    if (state.isEmpty) {
                        stringResource(R.string.vehicle_form_title_first)
                    } else {
                        stringResource(R.string.vehicle_form_title_next)
                    },
                name = state.name,
                odometer = state.odometerKm,
                selectedType = state.selectedType,
                errorMessage = state.errorMessage,
                onNameChange = onNameChange,
                onOdometerChange = onOdometerChange,
                onTypeSelected = onTypeSelected,
                onCreateVehicle = onCreateVehicle,
                modifier =
                    Modifier.padding(
                        start = Spacings.spacing24,
                        end = Spacings.spacing24,
                        bottom = Spacings.spacing24,
                    ),
            )
        }
    }

    vehiclePendingDeletion?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { vehiclePendingDeletion = null },
            title = { Text(stringResource(R.string.delete_vehicle_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
                    Text(stringResource(R.string.delete_vehicle_dialog_description))
                    Text(
                        text = vehicle.name,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vehiclePendingDeletion = null
                        onDeleteVehicle(vehicle)
                    },
                ) {
                    Text(stringResource(R.string.delete_vehicle_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { vehiclePendingDeletion = null }) {
                    Text(stringResource(R.string.delete_vehicle_cancel_button))
                }
            },
        )
    }

    if (state.editingVehicleId != null && state.odometerDecreaseConfirmation == null) {
        AlertDialog(
            onDismissRequest = onDismissEdit,
            title = {
                Text(
                    stringResource(
                        if (state.editMode == VehicleEditMode.Full) {
                            R.string.edit_vehicle_title
                        } else {
                            R.string.update_odometer_title
                        },
                    ),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing12)) {
                    if (state.editMode == VehicleEditMode.Full) {
                        OutlinedTextField(
                            value = state.editName,
                            onValueChange = onEditNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.vehicle_name_label)) },
                            singleLine = true,
                        )
                        VehicleTypeSelector(state.editType, onEditTypeSelected)
                        OutlinedTextField(
                            value = state.editLicensePlate,
                            onValueChange = onEditLicensePlateChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.license_plate_label)) },
                            singleLine = true,
                        )
                    }
                    OutlinedTextField(
                        value = state.editOdometerKm,
                        onValueChange = onEditOdometerChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.current_odometer_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    state.editErrorMessage?.let {
                        Text(
                            text = stringResource(it.garageStringRes()),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onSubmitEdit) {
                    Text(stringResource(R.string.save_changes_button))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissEdit) {
                    Text(stringResource(R.string.cancel_button))
                }
            },
        )
    }

    state.odometerDecreaseConfirmation?.let { confirmation ->
        AlertDialog(
            onDismissRequest = onCancelOdometerDecrease,
            title = { Text(stringResource(R.string.odometer_decrease_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.odometer_decrease_message,
                        confirmation.currentOdometerKm,
                        confirmation.proposedOdometerKm,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmOdometerDecrease) {
                    Text(stringResource(R.string.confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelOdometerDecrease) {
                    Text(stringResource(R.string.cancel_button))
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
private fun GarageHeader(
    showAddVehicleAction: Boolean,
    onAddVehicle: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.garage_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            if (showAddVehicleAction) {
                Button(onClick = onAddVehicle) {
                    Text(stringResource(R.string.add_vehicle_button))
                }
            }
        }
    }
}

@Composable
private fun VehicleForm(
    title: String,
    name: String,
    odometer: String,
    selectedType: VehicleType,
    errorMessage: CarburaString?,
    onNameChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onTypeSelected: (VehicleType) -> Unit,
    onCreateVehicle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.vehicle_name_label)) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
            )
            VehicleTypeSelector(
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
            )
            OutlinedTextField(
                value = odometer,
                onValueChange = onOdometerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.current_odometer_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            if (errorMessage != null) {
                Text(
                    text = stringResource(errorMessage.garageStringRes()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = onCreateVehicle,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save_vehicle_button))
            }
        }
    }
}

@Composable
private fun VehicleTypeSelector(
    selectedType: VehicleType,
    onTypeSelected: (VehicleType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Text(
            text = stringResource(R.string.vehicle_type_label),
            style = MaterialTheme.typography.titleSmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
            VehicleTypeButton(
                label = stringResource(R.string.vehicle_type_car),
                selected = selectedType == VehicleType.Car,
                onClick = { onTypeSelected(VehicleType.Car) },
            )
            VehicleTypeButton(
                label = stringResource(R.string.vehicle_type_motorcycle),
                selected = selectedType == VehicleType.Motorcycle,
                onClick = { onTypeSelected(VehicleType.Motorcycle) },
            )
        }
    }
}

@Composable
private fun VehicleTypeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
    }
}

@Composable
private fun EmptyGarageCard(onAddVehicle: () -> Unit) {
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
                text = stringResource(R.string.empty_garage_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.empty_garage_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(R.string.empty_garage_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Button(onClick = onAddVehicle) {
                Text(stringResource(R.string.add_first_vehicle_button))
            }
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onSelectVehicle: (Vehicle) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit,
    onEditVehicle: (Vehicle) -> Unit,
    onQuickOdometerUpdate: (Vehicle) -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = vehicle.type.label(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
                    Text(
                        text = "${vehicle.currentOdometerKm} km",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    IconButton(onClick = { onDeleteVehicle(vehicle) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete_vehicle_content_description),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                    IconButton(onClick = { onEditVehicle(vehicle) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_vehicle_content_description),
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
                OutlinedButton(onClick = { onSelectVehicle(vehicle) }) {
                    Text(stringResource(R.string.view_history_button))
                }
                TextButton(onClick = { onQuickOdometerUpdate(vehicle) }) {
                    Text(stringResource(R.string.update_odometer_button))
                }
            }
        }
    }
}

@Composable
private fun VehicleType.label(): String =
    when (this) {
        VehicleType.Car -> stringResource(R.string.vehicle_type_car)
        VehicleType.Motorcycle -> stringResource(R.string.vehicle_type_motorcycle)
        VehicleType.Van,
        VehicleType.Other,
        -> name
    }
