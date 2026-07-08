package com.asensiodev.carbura

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
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
import com.asensiodev.carbura.feature.reminders.presentation.RemindersRoute
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
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

    CarburaMainScaffold(
        currentRoute = backStack.lastOrNull() as? CarburaRoute ?: CarburaRoute.Garage,
        onGarageSelected = {
            while (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        onRemindersSelected = {
            while (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
            if (backStack.lastOrNull() != CarburaRoute.Reminders) {
                backStack.add(CarburaRoute.Reminders)
            }
        },
        onUserSelected = {
            while (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
            if (backStack.lastOrNull() != CarburaRoute.User) {
                backStack.add(CarburaRoute.User)
            }
        },
    ) { contentPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val navPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            top = Spacings.spacing0,
            end = contentPadding.calculateEndPadding(layoutDirection),
            bottom = contentPadding.calculateBottomPadding(),
        )
        NavDisplay(
            modifier = Modifier.padding(navPadding),
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

                    CarburaRoute.Reminders -> NavEntry(route) {
                        RemindersRoute()
                    }

                    CarburaRoute.User -> NavEntry(route) {
                        UserRoute(
                            displayName = onboardingState.displayName,
                            email = onboardingState.email,
                            familyName = onboardingState.familyName,
                            onSignOut = {
                                onboardingViewModel.onEvent(OnboardingEvent.SignOutClicked)
                            },
                        )
                    }

                    is CarburaRoute.CreateMaintenance -> NavEntry(route) { GarageRoute() }
                }
            },
        )
    }
}

@Composable
private fun CarburaMainScaffold(
    currentRoute: CarburaRoute,
    onGarageSelected: () -> Unit,
    onRemindersSelected: () -> Unit,
    onUserSelected: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val showBottomBar = currentRoute == CarburaRoute.Garage ||
        currentRoute == CarburaRoute.Reminders ||
        currentRoute == CarburaRoute.User
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = Spacings.spacing4,
                ) {
                    NavigationBarItem(
                        selected = currentRoute == CarburaRoute.Garage,
                        onClick = onGarageSelected,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(R.string.tab_garage)) },
                    )
                    NavigationBarItem(
                        selected = currentRoute == CarburaRoute.Reminders,
                        onClick = onRemindersSelected,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(R.string.tab_reminders)) },
                    )
                    NavigationBarItem(
                        selected = currentRoute == CarburaRoute.User,
                        onClick = onUserSelected,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.AccountCircle,
                                contentDescription = null,
                            )
                        },
                        label = { Text(stringResource(R.string.tab_user)) },
                    )
                }
            }
        },
        content = content,
    )
}

@Composable
private fun UserRoute(
    displayName: String?,
    email: String?,
    familyName: String?,
    onSignOut: () -> Unit,
) {
    val resolvedDisplayName = displayName.cleanUserText() ?: stringResource(R.string.user_profile_fallback_name)
    val resolvedFamilyName = familyName.cleanUserText() ?: stringResource(R.string.user_family_fallback_name)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacings.spacing24),
            verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
        ) {
            Text(
                text = stringResource(R.string.user_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(R.string.user_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Spacings.spacing16),
                    verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
                ) {
                    Text(
                        text = stringResource(R.string.user_profile_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = resolvedDisplayName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (email != null) {
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Spacings.spacing16),
                    verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
                ) {
                    Text(
                        text = stringResource(R.string.user_family_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = resolvedFamilyName,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = stringResource(R.string.user_family_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.user_family_deferred_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Spacings.spacing16),
                    verticalArrangement = Arrangement.spacedBy(Spacings.spacing12),
                ) {
                    Text(
                        text = stringResource(R.string.user_session_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Button(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.user_sign_out_button))
                    }
                }
            }
        }
    }
}

private fun String?.cleanUserText(): String? = this
    ?.trim()
    ?.trim('"')
    ?.replace("\"", "")
    ?.takeIf { it.isNotBlank() }

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
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(strokeWidth = Spacings.spacing4)
        }
    }
}
