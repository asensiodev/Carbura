package com.asensiodev.carbura.feature.garage.presentation

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featuregarage.R
import org.koin.core.context.GlobalContext

@Composable
fun GarageRoute(
    modifier: Modifier = Modifier,
    onVehicleSelected: (String) -> Unit = {},
    viewModel: GarageViewModel = rememberGarageViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var effectMessage by remember { mutableStateOf<CarburaString?>(null) }
    var effectMessageArg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(GarageEvent.Started)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GarageEffect.VehicleCreated -> {
                    effectMessage = CarburaString.VehicleCreatedMessage
                    effectMessageArg = effect.vehicleName
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

    val resolvedEffectMessage = effectMessage?.let { message ->
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
        onNameChange = { value -> viewModel.onEvent(GarageEvent.NameChanged(value)) },
        onOdometerChange = { value -> viewModel.onEvent(GarageEvent.OdometerChanged(value)) },
        onCreateVehicle = { viewModel.onEvent(GarageEvent.SubmitVehicle) },
        onSelectVehicle = { vehicle -> viewModel.onEvent(GarageEvent.VehicleSelected(vehicle.id)) },
        modifier = modifier,
    )
}

@Composable
private fun rememberGarageViewModel(): GarageViewModel = remember {
    GlobalContext.get().get()
}

@Composable
private fun GarageScreen(
    state: GarageUiState,
    effectMessage: String?,
    onNameChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onCreateVehicle: () -> Unit,
    onSelectVehicle: (Vehicle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacings.spacing24),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
        ) {
            item {
                GarageHeader()
            }

            item {
                VehicleForm(
                    name = state.name,
                    odometer = state.odometerKm,
                    errorMessage = state.errorMessage,
                    effectMessage = effectMessage,
                    onNameChange = onNameChange,
                    onOdometerChange = onOdometerChange,
                    onCreateVehicle = onCreateVehicle,
                )
            }

            if (state.isEmpty) {
                item { EmptyGarageCard() }
            } else {
                items(state.vehicles) { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        onSelectVehicle = onSelectVehicle,
                    )
                }
            }
        }
    }
}

@Composable
private fun GarageHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Text(
            text = stringResource(R.string.garage_title),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = stringResource(R.string.garage_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VehicleForm(
    name: String,
    odometer: String,
    errorMessage: CarburaString?,
    effectMessage: String?,
    onNameChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onCreateVehicle: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            Text(
                text = stringResource(R.string.vehicle_form_title),
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.vehicle_name_label)) },
                singleLine = true,
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
            if (effectMessage != null && errorMessage == null) {
                Text(
                    text = effectMessage,
                    color = MaterialTheme.colorScheme.primary,
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
private fun EmptyGarageCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Spacings.spacing16)) {
            Text(
                text = stringResource(R.string.empty_garage_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(Spacings.spacing8))
            Text(
                text = stringResource(R.string.empty_garage_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VehicleCard(
    vehicle: Vehicle,
    onSelectVehicle: (Vehicle) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
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
                        text = vehicle.type.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${vehicle.currentOdometerKm} km",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            OutlinedButton(onClick = { onSelectVehicle(vehicle) }) {
                Text(stringResource(R.string.view_history_button))
            }
        }
    }
}
