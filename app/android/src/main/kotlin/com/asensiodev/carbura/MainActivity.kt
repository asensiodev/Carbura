package com.asensiodev.carbura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.asensiodev.carbura.app.shared.CarburaRoute
import com.asensiodev.carbura.core.designsystem.CarburaTheme
import com.asensiodev.carbura.core.designsystem.Spacings
import com.asensiodev.carbura.feature.garage.presentation.GarageRoute
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryRoute
import com.asensiodev.carbura.feature.onboarding.presentation.OnboardingEffect
import com.asensiodev.carbura.feature.onboarding.presentation.OnboardingEvent
import com.asensiodev.carbura.feature.onboarding.presentation.OnboardingRoute
import com.asensiodev.carbura.feature.onboarding.presentation.OnboardingViewModel
import org.koin.core.context.GlobalContext

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
    val onboardingViewModel = rememberOnboardingViewModel()
    val onboardingState by onboardingViewModel.uiState.collectAsState()

    LaunchedEffect(onboardingViewModel) {
        onboardingViewModel.onEvent(OnboardingEvent.Started)
        onboardingViewModel.effects.collect { effect ->
            when (effect) {
                OnboardingEffect.NavigateToGarage -> {
                    while (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                }

                OnboardingEffect.NavigateToLogin -> {
                    while (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                }
            }
        }
    }

    if (onboardingState.isInitializing) {
        CarburaLoadingScreen()
        return
    }

    if (!onboardingState.isAuthenticated) {
        OnboardingRoute(viewModel = onboardingViewModel)
        return
    }

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
                        onSignOut = {
                            onboardingViewModel.onEvent(OnboardingEvent.SignOutClicked)
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

@Composable
private fun rememberOnboardingViewModel(): OnboardingViewModel = remember {
    GlobalContext.get().get()
}

@Composable
private fun CarburaLoadingScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(strokeWidth = Spacings.spacing4)
        }
    }
}
