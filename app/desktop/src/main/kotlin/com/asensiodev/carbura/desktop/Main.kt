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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.asensiodev.carbura.feature.garage.di.garageModule
import com.asensiodev.carbura.feature.maintenance.di.maintenanceModule
import com.asensiodev.carbura.feature.reminders.di.remindersModule
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
        Window(
            onCloseRequest = {
                stopKoin()
                exitApplication()
            },
            state = windowState,
            title = "Carbura",
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
            onSelected = { destination = it },
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
                Text("CARBURA", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            }
        }
        Spacer(Modifier.height(48.dp))
        DesktopDestination.entries.forEach { item ->
            NavigationRailItem(
                selected = selected == item,
                onClick = { onSelected(item) },
                icon = {
                    Icon(
                        imageVector = destinationIcon(item),
                        contentDescription = item.label,
                        tint = if (selected == item) Navy else Color(0xFFBCD0E8),
                    )
                },
                label =
                    if (compact) {
                        null
                    } else {
                        { Text(item.label, color = Color.White) }
                    },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        if (!compact) {
            Text("DESKTOP PREVIEW", color = Color(0xFF9EB7D5), fontSize = 11.sp, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(6.dp))
            Text("macOS + Windows", color = Color.White, fontSize = 13.sp)
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
    if (destination == DesktopDestination.Garage) {
        GarageWorkspace(
            compact = compact,
            onOpenMaintenance = onOpenMaintenance,
            modifier = modifier,
        )
        return
    }
    if (destination == DesktopDestination.Maintenance) {
        MaintenanceWorkspace(
            compact = compact,
            initialVehicleId = selectedMaintenanceVehicleId,
            onNavigateToGarage = { onNavigate(DesktopDestination.Garage) },
            modifier = modifier,
        )
        return
    }
    if (destination == DesktopDestination.Reminders) {
        RemindersWorkspace(
            compact = compact,
            onNavigateToGarage = { onNavigate(DesktopDestination.Garage) },
            modifier = modifier,
        )
        return
    }
    Column(
        modifier = modifier.fillMaxHeight().padding(if (compact) 32.dp else 56.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(destination.eyebrow, color = Blue, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.6.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    destination.headline,
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink,
                    lineHeight = 46.sp,
                    modifier = Modifier.widthIn(max = 720.dp),
                )
            }
            Box(modifier = Modifier.size(12.dp).background(Success, CircleShape))
        }

        Text(
            "Carbura Desktop is running on this Mac.",
            style = MaterialTheme.typography.bodyLarge,
            color = Muted,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            StatusCard(
                icon = Icons.Default.CheckCircle,
                title = "Shared core ready",
                detail = "Models, SQLDelight and domain rules compile for JVM Desktop.",
                modifier = Modifier.weight(1f),
            )
            if (!compact) {
                StatusCard(
                    icon = Icons.Default.Storage,
                    title = "Local-first foundation",
                    detail = "Desktop data is stored in Carbura's application-data directory.",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        AvailabilityPanel(destination)
    }
}

@Composable
private fun StatusCard(
    icon: ImageVector,
    title: String,
    detail: String,
    modifier: Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(modifier = Modifier.size(42.dp).background(PaleBlue, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Blue)
            }
            Text(title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(detail, color = Muted, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun AvailabilityPanel(destination: DesktopDestination) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PaleBlue),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(26.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(48.dp).background(Color.White, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(destinationIcon(destination), contentDescription = null, tint = Navy)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("${destination.label} workspace", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(5.dp))
                Text(destination.description, color = Muted, lineHeight = 21.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("NEXT IN MIGRATION", color = Blue, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                Text("UI extraction", color = Ink, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun destinationIcon(destination: DesktopDestination): ImageVector =
    when (destination) {
        DesktopDestination.Garage -> Icons.Default.DirectionsCar
        DesktopDestination.Reminders -> Icons.Default.Notifications
        DesktopDestination.Maintenance -> Icons.Default.Build
        DesktopDestination.Account -> Icons.Default.Person
    }
