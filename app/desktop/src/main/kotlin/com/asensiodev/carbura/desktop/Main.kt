package com.asensiodev.carbura.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
import com.asensiodev.carbura.core.data.dataModule
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.desktop.resources.Res
import com.asensiodev.carbura.desktop.resources.shell_app_name
import com.asensiodev.carbura.desktop.resources.shell_brand
import com.asensiodev.carbura.desktop.resources.shell_desktop_preview
import com.asensiodev.carbura.desktop.resources.shell_supported_systems
import com.asensiodev.carbura.feature.garage.di.garageModule
import com.asensiodev.carbura.feature.maintenance.di.maintenanceModule
import com.asensiodev.carbura.feature.reminders.di.remindersModule
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

internal val Canvas = Color(0xFFF2F7FD)
internal val Ink = Color(0xFF142238)
internal val Navy = Color(0xFF17345A)
internal val Blue = Color(0xFF2867B2)
internal val PaleBlue = Color(0xFFDCEBFA)
internal val Muted = Color(0xFF607086)
internal val Line = Color(0xFFD8E3F0)
internal val Success = Color(0xFF2F7666)

fun main() {
    startKoin { modules(dataModule, garageModule, maintenanceModule, remindersModule, desktopLocalModeModule) }
    application {
        val windowState = rememberWindowState(size = DpSize(1180.dp, 760.dp))
        val appName = stringResource(Res.string.shell_app_name)
        Window(
            onCloseRequest = {
                stopKoin()
                exitApplication()
            },
            state = windowState,
            title = appName,
        ) {
            CarburaDesktopApp(windowWidthDp = with(LocalDensity.current) { window.width.toDp().value })
        }
    }
}

@Composable
@Preview
private fun CarburaDesktopApp(windowWidthDp: Float = 1180f) {
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
            DesktopShell(compact = usesCompactNavigation(windowWidthDp))
        }
    }
}

@Composable
private fun DesktopShell(compact: Boolean) {
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
        if (!compact) {
            Text(stringResource(Res.string.shell_desktop_preview), color = Color(0xFF9EB7D5), fontSize = 11.sp, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(Res.string.shell_supported_systems), color = Color.White, fontSize = 13.sp)
        }
    }
}

@Composable
private fun DestinationContent(
    destination: DesktopDestination,
    compact: Boolean,
    selectedMaintenanceVehicleId: VehicleId?,
    onNavigate: (DesktopDestination) -> Unit,
    onOpenMaintenance: (VehicleId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (destination) {
        DesktopDestination.Garage ->
            GarageWorkspace(
                compact = compact,
                onOpenMaintenance = onOpenMaintenance,
                modifier = modifier,
            )
        DesktopDestination.Reminders ->
            RemindersWorkspace(
                compact = compact,
                onNavigateToGarage = { onNavigate(DesktopDestination.Garage) },
                modifier = modifier,
            )
        DesktopDestination.Maintenance ->
            MaintenanceWorkspace(
                compact = compact,
                initialVehicleId = selectedMaintenanceVehicleId,
                onNavigateToGarage = { onNavigate(DesktopDestination.Garage) },
                modifier = modifier,
            )
        DesktopDestination.Account -> AccountWorkspace(compact = compact, modifier = modifier)
    }
}

private fun destinationIcon(destination: DesktopDestination): ImageVector =
    when (destination) {
        DesktopDestination.Garage -> Icons.Default.DirectionsCar
        DesktopDestination.Reminders -> Icons.Default.Notifications
        DesktopDestination.Maintenance -> Icons.Default.Build
        DesktopDestination.Account -> Icons.Default.Person
    }
