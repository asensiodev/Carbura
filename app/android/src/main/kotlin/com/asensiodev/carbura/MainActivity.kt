package com.asensiodev.carbura

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.asensiodev.carbura.app.shared.CarburaRoute
import com.asensiodev.carbura.core.designsystem.CarburaTheme
import com.asensiodev.carbura.core.designsystem.Size
import com.asensiodev.carbura.core.designsystem.Spacings
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import com.asensiodev.carbura.feature.garage.presentation.GarageRoute
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryRoute
import com.asensiodev.carbura.feature.onboarding.presentation.OnboardingEffect
import com.asensiodev.carbura.feature.onboarding.presentation.OnboardingEvent
import com.asensiodev.carbura.feature.onboarding.presentation.OnboardingRoute
import com.asensiodev.carbura.feature.onboarding.presentation.OnboardingViewModel
import com.asensiodev.carbura.feature.reminders.presentation.RemindersRoute
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.koin.core.context.GlobalContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private var startRoute by mutableStateOf<String?>(null)
    private var startRouteVersion by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startRoute = intent?.getStringExtra(EXTRA_START_ROUTE)
        if (startRoute != null) startRouteVersion += 1
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            CarburaTheme {
                CarburaApp(
                    startRoute = startRoute,
                    startRouteVersion = startRouteVersion,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startRoute = intent.getStringExtra(EXTRA_START_ROUTE)
        if (startRoute != null) startRouteVersion += 1
    }
}

private const val EXTRA_START_ROUTE = "com.asensiodev.carbura.START_ROUTE"
private const val START_ROUTE_REMINDERS = "reminders"

@Composable
private fun CarburaApp(
    startRoute: String?,
    startRouteVersion: Int,
) {
    val backStack = rememberNavBackStack(CarburaRoute.Garage)
    val onboardingViewModel = rememberOnboardingViewModel()
    val syncManager = rememberSyncManager()
    val onboardingState by onboardingViewModel.uiState.collectAsStateWithLifecycle()
    val syncStatus by syncManager.status.collectAsStateWithLifecycle()
    val syncScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val lastForegroundSyncAttempt = remember { mutableLongStateOf(0L) }

    LaunchedEffect(onboardingViewModel) {
        onboardingViewModel.onEvent(OnboardingEvent.Started)
        onboardingViewModel.effects.collect { effect ->
            when (effect) {
                OnboardingEffect.NavigateToGarage -> {
                    backStack.navigateToTopLevel(CarburaRoute.Garage)
                }

                OnboardingEffect.NavigateToLogin -> {
                    backStack.resetAfterSignOut()
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

    val familyId = onboardingState.familyId
    if (familyId.isNullOrBlank()) {
        CarburaLoadingScreen()
        return
    }

    var initialSyncCompleted by remember(familyId) { mutableStateOf(false) }

    LaunchedEffect(familyId) {
        try {
            syncManager.syncNow()
        } finally {
            initialSyncCompleted = true
        }
    }

    if (!initialSyncCompleted) {
        CarburaLoadingScreen(message = stringResource(R.string.initial_sync_loading))
        return
    }

    LaunchedEffect(startRouteVersion, initialSyncCompleted) {
        if (startRoute == START_ROUTE_REMINDERS) {
            backStack.navigateToTopLevel(CarburaRoute.Reminders)
        }
    }

    LaunchedEffect(onboardingState.isAuthenticated) {
        while (onboardingState.isAuthenticated) {
            delay(IN_APP_SYNC_INTERVAL_MILLIS)
            syncManager.syncNow()
        }
    }

    DisposableEffect(lifecycleOwner, onboardingState.isAuthenticated) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START && onboardingState.isAuthenticated) {
                    val now = System.currentTimeMillis()
                    if (now - lastForegroundSyncAttempt.longValue >= FOREGROUND_SYNC_THROTTLE_MILLIS) {
                        lastForegroundSyncAttempt.longValue = now
                        syncScope.launch { syncManager.syncNow() }
                    }
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val currentRoute = backStack.lastOrNull() as? CarburaRoute ?: CarburaRoute.Garage
    var refreshSignal by remember(familyId) { mutableLongStateOf(0L) }
    var refreshDestination by remember(familyId) { mutableStateOf<CarburaRoute?>(null) }
    var observedSyncTimestamp by remember(familyId) { mutableStateOf(syncStatus.lastSyncedAtMillis) }

    LaunchedEffect(syncStatus.lastSyncedAtMillis) {
        val timestamp = syncStatus.lastSyncedAtMillis
        if (timestamp != null && timestamp != observedSyncTimestamp) {
            observedSyncTimestamp = timestamp
            refreshDestination = currentRoute
            refreshSignal += 1L
        }
    }

    CarburaMainScaffold(
        currentRoute = currentRoute,
        syncStatus = syncStatus,
        onRetrySync = { syncScope.launch { syncManager.syncNow() } },
        onSyncFailureShown = syncManager::acknowledgeFailure,
        onGarageSelected = {
            backStack.navigateToTopLevel(CarburaRoute.Garage)
        },
        onRemindersSelected = {
            backStack.navigateToTopLevel(CarburaRoute.Reminders)
        },
        onUserSelected = {
            backStack.navigateToTopLevel(CarburaRoute.User)
        },
    ) { contentPadding ->
        val layoutDirection = LocalLayoutDirection.current
        val navPadding =
            PaddingValues(
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
                    CarburaRoute.Garage ->
                        NavEntry(route) {
                            GarageRoute(
                                familyId = familyId,
                                refreshSignal = refreshSignal.takeIf { refreshDestination == carburaRoute } ?: 0L,
                                onVehicleSelected = { vehicleId ->
                                    backStack.add(CarburaRoute.VehicleDetail(vehicleId))
                                },
                            )
                        }

                    is CarburaRoute.VehicleDetail ->
                        NavEntry(route) {
                            MaintenanceHistoryRoute(
                                vehicleId = carburaRoute.vehicleId,
                                familyId = familyId,
                                refreshSignal = refreshSignal.takeIf { refreshDestination == carburaRoute } ?: 0L,
                                onBack = {
                                    if (backStack.size > 1) {
                                        backStack.removeLastOrNull()
                                    }
                                },
                            )
                        }

                    CarburaRoute.Reminders ->
                        NavEntry(route) {
                            RemindersRoute(
                                familyId = familyId,
                                refreshSignal = refreshSignal.takeIf { refreshDestination == carburaRoute } ?: 0L,
                                onNavigateToGarage = { backStack.navigateToTopLevel(CarburaRoute.Garage) },
                            )
                        }

                    CarburaRoute.User ->
                        NavEntry(route) {
                            UserRoute(
                                displayName = onboardingState.displayName,
                                email = onboardingState.email,
                                familyName = onboardingState.familyName,
                                syncStatus = syncStatus,
                                onSyncNow = { syncScope.launch { syncManager.syncNow() } },
                                onSignOut = {
                                    onboardingViewModel.onEvent(OnboardingEvent.SignOutClicked)
                                },
                            )
                        }
                }
            },
        )
    }
}

@Composable
private fun CarburaMainScaffold(
    currentRoute: CarburaRoute,
    syncStatus: SyncStatus,
    onRetrySync: () -> Unit,
    onSyncFailureShown: (Long) -> Unit,
    onGarageSelected: () -> Unit,
    onRemindersSelected: () -> Unit,
    onUserSelected: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val feedbackTracker = remember { SyncFeedbackTracker() }
    val failureMessage = stringResource(R.string.sync_failure_message)
    val retryLabel = stringResource(R.string.sync_retry_action)

    LaunchedEffect(syncStatus.failureId, syncStatus.isSyncing) {
        when (val feedback = feedbackTracker.update(syncStatus)) {
            SyncFeedbackEvent.None -> Unit
            is SyncFeedbackEvent.ShowFailure -> {
                coroutineScope {
                    val previousSnackbar = snackbarHostState.currentSnackbarData
                    previousSnackbar?.dismiss()
                    val delivery =
                        async {
                            snackbarHostState.showSnackbar(
                                message = failureMessage,
                                actionLabel = retryLabel,
                                duration = SnackbarDuration.Indefinite,
                            )
                        }
                    while (delivery.isActive) {
                        val displayedSnackbar = snackbarHostState.currentSnackbarData
                        if (displayedSnackbar != null && displayedSnackbar !== previousSnackbar) {
                            onSyncFailureShown(feedback.id)
                            break
                        }
                        yield()
                    }
                    if (delivery.await() == SnackbarResult.ActionPerformed) {
                        onRetrySync()
                    }
                }
            }
        }
    }

    LaunchedEffect(syncStatus.lastSyncedAtMillis) {
        if (!syncStatus.isSyncing && syncStatus.lastErrorMessage == null && syncStatus.lastSyncedAtMillis != null) {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }
    val showBottomBar =
        currentRoute == CarburaRoute.Garage ||
            currentRoute == CarburaRoute.Reminders ||
            currentRoute == CarburaRoute.User
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserRoute(
    displayName: String?,
    email: String?,
    familyName: String?,
    syncStatus: SyncStatus,
    onSyncNow: () -> Unit,
    onSignOut: () -> Unit,
) {
    val resolvedDisplayName = displayName.cleanUserText() ?: stringResource(R.string.user_profile_fallback_name)
    val resolvedFamilyName = familyName.cleanUserText() ?: stringResource(R.string.user_family_fallback_name)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.user_title),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = Spacings.spacing8),
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = Spacings.spacing24,
                            top = Spacings.spacing16,
                            end = Spacings.spacing24,
                            bottom = Spacings.spacing24,
                        ),
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
            ) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.user_sync_title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text =
                                    when {
                                        syncStatus.isSyncing -> stringResource(R.string.user_syncing_status)
                                        syncStatus.lastErrorMessage != null -> stringResource(R.string.user_sync_pending)
                                        syncStatus.lastSyncedAtMillis != null -> stringResource(R.string.user_sync_ready)
                                        else -> stringResource(R.string.user_sync_pending)
                                    },
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (syncStatus.isSyncing) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                        Text(
                            text = stringResource(R.string.user_sync_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text =
                                syncStatus.lastSyncedAtMillis?.let {
                                    stringResource(R.string.user_sync_last, it.formatSyncTime())
                                } ?: stringResource(R.string.user_sync_never),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        syncStatus.lastErrorMessage?.let { error ->
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = Spacings.spacing4),
                                verticalArrangement = Arrangement.spacedBy(Spacings.spacing4),
                            ) {
                                Text(
                                    text = stringResource(R.string.user_sync_error_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                Text(
                                    text = stringResource(R.string.user_sync_error_description),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = stringResource(R.string.user_sync_error_detail, error),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Button(
                            onClick = onSyncNow,
                            enabled = !syncStatus.isSyncing,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text =
                                    if (syncStatus.isSyncing) {
                                        stringResource(R.string.user_syncing_button)
                                    } else {
                                        stringResource(R.string.user_sync_now_button)
                                    },
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
}

private fun String?.cleanUserText(): String? =
    this
        ?.trim()
        ?.trim('"')
        ?.replace("\"", "")
        ?.takeIf { it.isNotBlank() }

@Composable
private fun rememberOnboardingViewModel(): OnboardingViewModel =
    remember {
        GlobalContext.get().get()
    }

@Composable
private fun rememberSyncManager(): SyncManager =
    remember {
        GlobalContext.get().get()
    }

@Composable
private fun CarburaLoadingScreen(message: String? = null) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(Spacings.spacing24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(strokeWidth = Size.size4)
            if (message != null) {
                Text(
                    text = message,
                    modifier = Modifier.padding(top = Spacings.spacing16),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun Long.formatSyncTime(): String =
    DateTimeFormatter
        .ofPattern("dd/MM HH:mm")
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(this))

private const val FOREGROUND_SYNC_THROTTLE_MILLIS = 60_000L
private const val IN_APP_SYNC_INTERVAL_MILLIS = 5 * 60_000L
