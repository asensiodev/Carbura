package com.asensiodev.carbura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.asensiodev.carbura.app.shared.CarburaRoute
import com.asensiodev.carbura.core.designsystem.CarburaTheme
import com.asensiodev.carbura.feature.garage.presentation.GarageRoute
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarburaTheme {
                CarburaApp()
            }
        }
    }
}

@Composable
private fun CarburaApp() {
    var route: CarburaRoute by remember { mutableStateOf(CarburaRoute.Garage) }

    when (val currentRoute = route) {
        CarburaRoute.Garage -> GarageRoute(
            onVehicleSelected = { vehicleId ->
                route = CarburaRoute.VehicleDetail(vehicleId)
            },
        )

        is CarburaRoute.VehicleDetail -> MaintenanceHistoryRoute(
            vehicleId = currentRoute.vehicleId,
            onBack = { route = CarburaRoute.Garage },
        )

        is CarburaRoute.CreateMaintenance,
        CarburaRoute.Reminders,
        -> GarageRoute()
    }
}

@Preview
@Composable
private fun CarburaAppPreview() {
    CarburaTheme {
        CarburaApp()
    }
}
