package com.asensiodev.carbura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
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
    val backStack = rememberNavBackStack(CarburaRoute.Garage)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = { route ->
            when (val carburaRoute = route as CarburaRoute) {
                CarburaRoute.Garage -> NavEntry(route) {
                    GarageRoute(
                        onVehicleSelected = { vehicleId ->
                            backStack.add(CarburaRoute.VehicleDetail(vehicleId))
                        },
                    )
                }

                is CarburaRoute.VehicleDetail -> NavEntry(route) {
                    MaintenanceHistoryRoute(
                        vehicleId = carburaRoute.vehicleId,
                        onBack = {
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            }
                        },
                    )
                }

                is CarburaRoute.CreateMaintenance,
                CarburaRoute.Reminders,
                -> NavEntry(route) { GarageRoute() }
            }
        },
    )
}

@Preview
@Composable
private fun CarburaAppPreview() {
    CarburaTheme {
        CarburaApp()
    }
}
