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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.asensiodev.carbura.core.model.Vehicle
import org.koin.core.context.GlobalContext

@Composable
fun GarageRoute(
    modifier: Modifier = Modifier,
    viewModel: GarageViewModel = rememberGarageViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var effectMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.onEvent(GarageEvent.Started)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            effectMessage = when (effect) {
                is GarageEffect.VehicleCreated -> "${effect.vehicleName} guardado en el garaje."
                is GarageEffect.ValidationFailed -> effect.message
            }
        }
    }

    GarageScreen(
        state = uiState,
        effectMessage = effectMessage,
        onNameChange = { value -> viewModel.onEvent(GarageEvent.NameChanged(value)) },
        onOdometerChange = { value -> viewModel.onEvent(GarageEvent.OdometerChanged(value)) },
        onCreateVehicle = { viewModel.onEvent(GarageEvent.SubmitVehicle) },
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
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    VehicleCard(vehicle = vehicle)
                }
            }
        }
    }
}

@Composable
private fun GarageHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Carbura",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Tu garaje, siempre a punto.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VehicleForm(
    name: String,
    odometer: String,
    errorMessage: String?,
    effectMessage: String?,
    onNameChange: (String) -> Unit,
    onOdometerChange: (String) -> Unit,
    onCreateVehicle: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Anade tu primer vehiculo",
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre") },
                singleLine = true,
            )
            OutlinedTextField(
                value = odometer,
                onValueChange = onOdometerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Kilometros actuales") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
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
                Text("Guardar vehiculo")
            }
        }
    }
}

@Composable
private fun EmptyGarageCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Garaje vacio",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Crea un vehiculo para empezar a registrar mantenimientos y recordatorios.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun VehicleCard(vehicle: Vehicle) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
    }
}
