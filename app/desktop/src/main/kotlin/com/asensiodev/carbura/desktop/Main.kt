package com.asensiodev.carbura.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.asensiodev.carbura.core.auth.SupabaseSettings
import com.asensiodev.carbura.core.auth.authModule
import com.asensiodev.carbura.core.data.dataModule
import com.asensiodev.carbura.core.domain.auth.AccountLocalDataCleaner
import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway
import com.asensiodev.carbura.core.domain.sync.LocalDataAdoptionGateway
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.desktop.resources.Res
import com.asensiodev.carbura.desktop.resources.shell_app_name
import com.asensiodev.carbura.desktop.resources.shell_brand
import com.asensiodev.carbura.desktop.resources.startup_authenticating
import com.asensiodev.carbura.desktop.resources.startup_cancel
import com.asensiodev.carbura.desktop.resources.startup_import_action
import com.asensiodev.carbura.desktop.resources.startup_import_description
import com.asensiodev.carbura.desktop.resources.startup_import_summary
import com.asensiodev.carbura.desktop.resources.startup_import_title
import com.asensiodev.carbura.desktop.resources.startup_initial_sync
import com.asensiodev.carbura.desktop.resources.startup_local_action
import com.asensiodev.carbura.desktop.resources.startup_profile
import com.asensiodev.carbura.desktop.resources.startup_restoring
import com.asensiodev.carbura.desktop.resources.startup_retry
import com.asensiodev.carbura.desktop.resources.startup_sign_in
import com.asensiodev.carbura.desktop.resources.startup_use_account_action
import com.asensiodev.carbura.feature.garage.di.garageModule
import com.asensiodev.carbura.feature.maintenance.di.maintenanceModule
import com.asensiodev.carbura.feature.reminders.di.remindersModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

internal val Canvas = Color(0xFFF2F7FD)
internal val Ink = Color(0xFF142238)
internal val Navy = Color(0xFF17345A)
internal val Blue = Color(0xFF2867B2)
internal val PaleBlue = Color(0xFFDCEBFA)
internal val Muted = Color(0xFF607086)
internal val Line = Color(0xFFD8E3F0)
internal val Success = Color(0xFF2F7666)

internal val SupabaseSettings.isDesktopConfigurationAvailable: Boolean
    get() = url.isNotBlank() && anonKey.isNotBlank()

internal fun desktopModules(settings: SupabaseSettings): List<Module> =
    buildList {
        add(authModule)
        add(dataModule)
        add(garageModule)
        add(maintenanceModule)
        add(remindersModule)
        add(module { single { settings } })
        if (!settings.isDesktopConfigurationAvailable) add(desktopLocalModeModule)
    }

fun main() {
    val settings =
        SupabaseSettings(
            url = DesktopPublicConfig.supabaseUrl,
            anonKey = DesktopPublicConfig.supabaseAnonKey,
        )
    val koin =
        startKoin {
            allowOverride(true)
            modules(desktopModules(settings))
        }.koin
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val controller =
        DesktopAppController(
            configurationAvailable = settings.isDesktopConfigurationAvailable,
            authGateway = { koin.get<AuthGateway>() },
            profileGateway = { koin.get<RemoteUserProfileGateway>() },
            adoptionGateway = { koin.get<LocalDataAdoptionGateway>() },
            syncManager = { koin.get<SyncManager>() },
            accountLocalDataCleaner = { koin.get<AccountLocalDataCleaner>() },
            familyScope = koin.get<ActiveFamilyScopeGateway>(),
            coroutineScope = appScope,
        )
    application {
        val windowState = rememberWindowState(size = DpSize(1180.dp, 760.dp))
        val appName = stringResource(Res.string.shell_app_name)
        Window(
            onCloseRequest = {
                appScope.cancel()
                stopKoin()
                exitApplication()
            },
            state = windowState,
            title = appName,
        ) {
            DisposableEffect(window, controller) {
                val listener =
                    object : WindowFocusListener {
                        override fun windowGainedFocus(event: WindowEvent?) = controller.onForeground()

                        override fun windowLostFocus(event: WindowEvent?) = Unit
                    }
                window.addWindowFocusListener(listener)
                onDispose { window.removeWindowFocusListener(listener) }
            }
            CarburaDesktopApp(
                controller = controller,
                windowWidthDp = with(LocalDensity.current) { window.width.toDp().value },
            )
        }
    }
}

@Composable
private fun CarburaDesktopApp(
    controller: DesktopAppController,
    windowWidthDp: Float = 1180f,
) {
    val colors =
        lightColorScheme(
            primary = Blue,
            onPrimary = Color.White,
            background = Canvas,
            onBackground = Ink,
            surface = Color.White,
            onSurface = Ink,
            outline = Line,
        )
    MaterialTheme(
        colorScheme = colors,
        typography =
            MaterialTheme.typography.copy(
                displaySmall =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                    ),
                headlineMedium =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                    ),
                bodyLarge = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
            ),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
            val startupState by controller.state.collectAsState()
            val syncStatus by controller.syncStatus.collectAsState()
            val contentRevision by controller.contentRevision.collectAsState()
            val excludedLocalData by controller.excludedLocalData.collectAsState()
            LaunchedEffect(controller) { controller.start() }
            val periodicAccount = startupState.accountOrNull()
            LaunchedEffect(periodicAccount) {
                if (periodicAccount == null) return@LaunchedEffect
                while (true) {
                    delay(DESKTOP_PERIODIC_SYNC_MILLIS)
                    controller.onPeriodicTick()
                }
            }
            DesktopStartupContent(
                state = startupState,
                syncStatus = syncStatus,
                excludedLocalData = excludedLocalData,
                contentRevision = contentRevision,
                compact = usesCompactNavigation(windowWidthDp),
                controller = controller,
            )
        }
    }
}

@Composable
private fun DesktopStartupContent(
    state: DesktopStartupState,
    syncStatus: com.asensiodev.carbura.core.domain.sync.SyncStatus,
    excludedLocalData: com.asensiodev.carbura.core.domain.sync.LocalDataCounts?,
    contentRevision: Long,
    compact: Boolean,
    controller: DesktopAppController,
) {
    when (state) {
        DesktopStartupState.Restoring -> StartupProgress(stringResource(Res.string.startup_restoring))
        DesktopStartupState.Authenticating -> StartupProgress(stringResource(Res.string.startup_authenticating))
        DesktopStartupState.ResolvingProfile -> StartupProgress(stringResource(Res.string.startup_profile))
        is DesktopStartupState.InitialSync -> StartupProgress(stringResource(Res.string.startup_initial_sync))
        is DesktopStartupState.AwaitingImportDecision ->
            LocalDataDecisionContent(
                state = state,
                onImport = controller::importAndMerge,
                onUseAccount = controller::useAccountData,
                onCancel = controller::cancelImportDecision,
            )
        is DesktopStartupState.LocalMode ->
            DesktopShell(
                compact = compact,
                familyId = FamilyId("local-family"),
                startupState = state,
                syncStatus = syncStatus,
                excludedLocalData = excludedLocalData,
                contentRevision = contentRevision,
                controller = controller,
            )
        is DesktopStartupState.Authenticated ->
            DesktopShell(
                compact = compact,
                familyId = state.account.familyId,
                startupState = state,
                syncStatus = syncStatus,
                excludedLocalData = excludedLocalData,
                contentRevision = contentRevision,
                controller = controller,
            )
        is DesktopStartupState.RecoverableFailure -> {
            val account = state.account
            if (account != null) {
                DesktopShell(
                    compact = compact,
                    familyId = account.familyId,
                    startupState = state,
                    syncStatus = syncStatus,
                    excludedLocalData = excludedLocalData,
                    contentRevision = contentRevision,
                    controller = controller,
                )
            } else {
                StartupFailure(
                    message = state.message,
                    onRetry = controller::retry,
                    onLocalMode = controller::enterLocalMode,
                    onSignIn = controller::signIn,
                )
            }
        }
    }
}

@Composable
private fun StartupProgress(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(18.dp))
        Text(message, color = Ink, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun LocalDataDecisionContent(
    state: DesktopStartupState.AwaitingImportDecision,
    onImport: () -> Unit,
    onUseAccount: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(color = Color.White, shape = RoundedCornerShape(24.dp), modifier = Modifier.width(680.dp)) {
            Column(modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(Res.string.startup_import_title), style = MaterialTheme.typography.headlineMedium, color = Ink)
                Text(stringResource(Res.string.startup_import_description), color = Muted)
                Text(
                    stringResource(
                        Res.string.startup_import_summary,
                        state.snapshot.counts.vehicles,
                        state.snapshot.counts.maintenanceRecords,
                        state.snapshot.counts.reminders,
                    ),
                    color = Blue,
                    fontWeight = FontWeight.SemiBold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onImport) { Text(stringResource(Res.string.startup_import_action)) }
                    OutlinedButton(onClick = onUseAccount) { Text(stringResource(Res.string.startup_use_account_action)) }
                    OutlinedButton(onClick = onCancel) { Text(stringResource(Res.string.startup_cancel)) }
                }
            }
        }
    }
}

@Composable
private fun StartupFailure(
    message: String,
    onRetry: () -> Unit,
    onLocalMode: () -> Unit,
    onSignIn: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onRetry) { Text(stringResource(Res.string.startup_retry)) }
            OutlinedButton(onClick = onSignIn) { Text(stringResource(Res.string.startup_sign_in)) }
            OutlinedButton(onClick = onLocalMode) { Text(stringResource(Res.string.startup_local_action)) }
        }
    }
}

@Composable
internal fun DesktopShell(
    compact: Boolean,
    familyId: FamilyId,
    startupState: DesktopStartupState,
    syncStatus: com.asensiodev.carbura.core.domain.sync.SyncStatus,
    excludedLocalData: com.asensiodev.carbura.core.domain.sync.LocalDataCounts?,
    contentRevision: Long,
    controller: DesktopAppController,
) {
    var destination by remember { mutableStateOf(DesktopDestination.Garage) }
    var selectedMaintenanceVehicleId by remember { mutableStateOf<VehicleId?>(null) }
    Row(modifier = Modifier.fillMaxSize()) {
        DesktopNavigation(
            compact = compact,
            selected = destination,
            onSelected = {
                selectedMaintenanceVehicleId = maintenanceVehicleAfterSidebarNavigation(it, selectedMaintenanceVehicleId)
                destination = it
            },
        )
        DestinationContent(
            destination = destination,
            compact = compact,
            familyId = familyId,
            startupState = startupState,
            syncStatus = syncStatus,
            excludedLocalData = excludedLocalData,
            contentRevision = contentRevision,
            controller = controller,
            selectedMaintenanceVehicleId = selectedMaintenanceVehicleId,
            onNavigate = { destination = it },
            onOpenMaintenance = { vehicleId ->
                selectedMaintenanceVehicleId = vehicleId
                destination = DesktopDestination.Maintenance
            },
            modifier = Modifier.weight(1f),
        )
    }
}

internal fun maintenanceVehicleAfterSidebarNavigation(
    destination: DesktopDestination,
    currentVehicleId: VehicleId?,
): VehicleId? = if (destination == DesktopDestination.Maintenance) null else currentVehicleId

@Composable
private fun DesktopNavigation(
    compact: Boolean,
    selected: DesktopDestination,
    onSelected: (DesktopDestination) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .width(if (compact) 92.dp else 238.dp)
                .background(Navy)
                .padding(horizontal = if (compact) 12.dp else 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (compact) Arrangement.Center else Arrangement.Start,
        ) {
            Box(
                modifier = Modifier.size(42.dp).background(Color.White, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Navy)
            }
            if (!compact) {
                Spacer(Modifier.width(12.dp))
                Text(stringResource(Res.string.shell_brand), color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            }
        }
        Spacer(Modifier.height(48.dp))
        DesktopDestination.entries.forEach { item ->
            val label = stringResource(item.label)
            NavigationRailItem(
                selected = selected == item,
                onClick = { onSelected(item) },
                icon = {
                    Icon(
                        imageVector = destinationIcon(item),
                        contentDescription = label,
                        tint = if (selected == item) Navy else Color(0xFFBCD0E8),
                    )
                },
                label =
                    if (compact) {
                        null
                    } else {
                        { Text(label, color = Color.White) }
                    },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun DestinationContent(
    destination: DesktopDestination,
    compact: Boolean,
    familyId: FamilyId,
    startupState: DesktopStartupState,
    syncStatus: com.asensiodev.carbura.core.domain.sync.SyncStatus,
    excludedLocalData: com.asensiodev.carbura.core.domain.sync.LocalDataCounts?,
    contentRevision: Long,
    controller: DesktopAppController,
    selectedMaintenanceVehicleId: VehicleId?,
    onNavigate: (DesktopDestination) -> Unit,
    onOpenMaintenance: (VehicleId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (destination) {
        DesktopDestination.Garage ->
            GarageWorkspace(
                compact = compact,
                familyId = familyId,
                refreshGeneration = contentRevision,
                onOpenMaintenance = onOpenMaintenance,
                modifier = modifier,
            )
        DesktopDestination.Reminders ->
            RemindersWorkspace(
                compact = compact,
                familyId = familyId,
                refreshGeneration = contentRevision,
                onNavigateToGarage = { onNavigate(DesktopDestination.Garage) },
                modifier = modifier,
            )
        DesktopDestination.Maintenance ->
            MaintenanceWorkspace(
                compact = compact,
                familyId = familyId,
                refreshGeneration = contentRevision,
                initialVehicleId = selectedMaintenanceVehicleId,
                onNavigateToGarage = { onNavigate(DesktopDestination.Garage) },
                modifier = modifier,
            )
        DesktopDestination.Account -> {
            val isDeletingAccount by controller.isDeletingAccount.collectAsState()
            AccountWorkspace(
                compact = compact,
                startupState = startupState,
                syncStatus = syncStatus,
                excludedLocalData = excludedLocalData,
                isDeletingAccount = isDeletingAccount,
                onSignIn = controller::signIn,
                onSyncNow = controller::syncNow,
                onRetry = controller::retry,
                onSignOut = controller::signOut,
                onDeleteAccount = controller::deleteAccount,
                modifier = modifier,
            )
        }
    }
}

private fun DesktopStartupState.accountOrNull(): DesktopAccount? =
    when (this) {
        is DesktopStartupState.Authenticated -> account
        is DesktopStartupState.InitialSync -> account
        is DesktopStartupState.RecoverableFailure -> account
        else -> null
    }

private const val DESKTOP_PERIODIC_SYNC_MILLIS = 5 * 60 * 1_000L

private fun destinationIcon(destination: DesktopDestination): ImageVector =
    when (destination) {
        DesktopDestination.Garage -> Icons.Default.DirectionsCar
        DesktopDestination.Reminders -> Icons.Default.Notifications
        DesktopDestination.Maintenance -> Icons.Default.Build
        DesktopDestination.Account -> Icons.Default.Person
    }
