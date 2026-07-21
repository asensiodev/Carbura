package com.asensiodev.carbura.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asensiodev.carbura.core.data.desktopDataDirectory
import com.asensiodev.carbura.core.data.desktopDatabasePath
import kotlinx.coroutines.launch
import java.net.URI
import java.nio.file.Path

internal val CARBURA_PROJECT_URI: URI = URI("https://github.com/asensiodev/Carbura")

@Composable
internal fun AccountWorkspace(
    compact: Boolean,
    modifier: Modifier = Modifier,
    dataDirectory: Path = desktopDataDirectory(),
    databasePath: Path = desktopDatabasePath(dataDirectory),
    platformActions: DesktopPlatformActions = AwtDesktopPlatformActions,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val reportFailure: (String, DesktopActionResult) -> Unit = { action, result ->
        desktopActionFailureMessage(action, result)?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxHeight(),
        containerColor = Canvas,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .padding(contentPadding)
                    .padding(if (compact) 28.dp else 48.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Text("LOCAL ACCOUNT", color = Blue, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.6.sp)
            Text("Your data, on this device.", style = MaterialTheme.typography.displaySmall, color = Ink)
            Text(
                "Carbura Desktop currently works without sign-in. Vehicles, maintenance and reminders stay in local storage.",
                color = Muted,
                style = MaterialTheme.typography.bodyLarge,
            )

            if (compact) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LocalModeCard(Modifier.fillMaxWidth())
                    StorageCard(dataDirectory, databasePath, platformActions, reportFailure, Modifier.fillMaxWidth())
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    LocalModeCard(Modifier.weight(0.85f))
                    StorageCard(dataDirectory, databasePath, platformActions, reportFailure, Modifier.weight(1.15f))
                }
            }

            Surface(color = PaleBlue, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    WorkspaceIcon(Icons.AutoMirrored.Filled.OpenInNew)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Carbura Desktop", color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("View the source project, documentation and release history.", color = Muted)
                    }
                    OutlinedButton(
                        onClick = { reportFailure("Project website", openAccountProject(platformActions)) },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open project")
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalModeCard(modifier: Modifier) {
    AccountCard(modifier) {
        WorkspaceIcon(Icons.Default.CloudOff)
        Text("Local mode", color = Ink, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        Text("No account is connected and cloud synchronization is not active on Desktop.", color = Muted)
        Surface(color = Color(0xFFE6F3EF), shape = RoundedCornerShape(12.dp)) {
            Text(
                "Your local workflows remain fully available.",
                color = Success,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun StorageCard(
    dataDirectory: Path,
    databasePath: Path,
    platformActions: DesktopPlatformActions,
    reportFailure: (String, DesktopActionResult) -> Unit,
    modifier: Modifier,
) {
    AccountCard(modifier) {
        WorkspaceIcon(Icons.Default.Storage)
        Text("Local storage", color = Ink, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        PathLabel("Application data", dataDirectory)
        PathLabel("Database", databasePath)
        Text(
            "Carbura manages this database. Close the app before making an external backup.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        Button(
            onClick = { reportFailure("Data folder", openAccountDataDirectory(platformActions, dataDirectory)) },
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Open data folder")
        }
    }
}

@Composable
private fun PathLabel(
    label: String,
    path: Path,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label.uppercase(), color = Blue, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        Text(path.toAbsolutePath().toString(), color = Ink, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AccountCard(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            content = content,
        )
    }
}

internal fun openAccountDataDirectory(
    platformActions: DesktopPlatformActions,
    dataDirectory: Path,
): DesktopActionResult = platformActions.openDirectory(dataDirectory)

internal fun openAccountProject(platformActions: DesktopPlatformActions): DesktopActionResult = platformActions.browse(CARBURA_PROJECT_URI)

@Composable
private fun WorkspaceIcon(icon: ImageVector) {
    Box(
        modifier = Modifier.size(44.dp).background(PaleBlue, RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Blue)
    }
}
