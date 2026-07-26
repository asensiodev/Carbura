package com.asensiodev.carbura.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asensiodev.carbura.core.data.desktopDataDirectory
import com.asensiodev.carbura.core.data.desktopDatabasePath
import com.asensiodev.carbura.core.domain.sync.LocalDataCounts
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import com.asensiodev.carbura.desktop.resources.Res
import com.asensiodev.carbura.desktop.resources.account_action_failed
import com.asensiodev.carbura.desktop.resources.account_action_unsupported
import com.asensiodev.carbura.desktop.resources.account_application_data_label
import com.asensiodev.carbura.desktop.resources.account_authenticated_title
import com.asensiodev.carbura.desktop.resources.account_cancel
import com.asensiodev.carbura.desktop.resources.account_data_folder_action
import com.asensiodev.carbura.desktop.resources.account_data_folder_action_name
import com.asensiodev.carbura.desktop.resources.account_database_label
import com.asensiodev.carbura.desktop.resources.account_delete_button
import com.asensiodev.carbura.desktop.resources.account_delete_cancel
import com.asensiodev.carbura.desktop.resources.account_delete_confirm
import com.asensiodev.carbura.desktop.resources.account_delete_dialog_description
import com.asensiodev.carbura.desktop.resources.account_delete_dialog_title
import com.asensiodev.carbura.desktop.resources.account_deleting_button
import com.asensiodev.carbura.desktop.resources.account_deletion_description
import com.asensiodev.carbura.desktop.resources.account_deletion_title
import com.asensiodev.carbura.desktop.resources.account_excluded_description
import com.asensiodev.carbura.desktop.resources.account_excluded_title
import com.asensiodev.carbura.desktop.resources.account_family_value
import com.asensiodev.carbura.desktop.resources.account_header_description
import com.asensiodev.carbura.desktop.resources.account_header_eyebrow
import com.asensiodev.carbura.desktop.resources.account_header_title
import com.asensiodev.carbura.desktop.resources.account_last_sync
import com.asensiodev.carbura.desktop.resources.account_local_mode_available
import com.asensiodev.carbura.desktop.resources.account_local_mode_description
import com.asensiodev.carbura.desktop.resources.account_local_mode_title
import com.asensiodev.carbura.desktop.resources.account_never_synced
import com.asensiodev.carbura.desktop.resources.account_project_action
import com.asensiodev.carbura.desktop.resources.account_project_action_name
import com.asensiodev.carbura.desktop.resources.account_project_description
import com.asensiodev.carbura.desktop.resources.account_project_title
import com.asensiodev.carbura.desktop.resources.account_retry
import com.asensiodev.carbura.desktop.resources.account_sign_in
import com.asensiodev.carbura.desktop.resources.account_sign_out
import com.asensiodev.carbura.desktop.resources.account_sign_out_description
import com.asensiodev.carbura.desktop.resources.account_sign_out_title
import com.asensiodev.carbura.desktop.resources.account_storage_description
import com.asensiodev.carbura.desktop.resources.account_storage_hide_details
import com.asensiodev.carbura.desktop.resources.account_storage_show_details
import com.asensiodev.carbura.desktop.resources.account_storage_title
import com.asensiodev.carbura.desktop.resources.account_sync_now
import com.asensiodev.carbura.desktop.resources.account_syncing
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import java.net.URI
import java.nio.file.Path

internal val CARBURA_PROJECT_URI: URI = URI("https://github.com/asensiodev/Carbura")
internal val AccountCardsStackThreshold = 760.dp

internal fun useStackedAccountCards(availableWidth: Dp): Boolean = availableWidth < AccountCardsStackThreshold

internal class AccountDeletionConfirmation {
    var isVisible by mutableStateOf(false)
        private set

    fun request() {
        isVisible = true
    }

    fun cancel() {
        isVisible = false
    }

    fun confirm(onDeleteAccount: () -> Unit) {
        if (!isVisible) return
        isVisible = false
        onDeleteAccount()
    }
}

internal class AccountStorageDetails {
    var isVisible by mutableStateOf(false)
        private set

    fun toggle() {
        isVisible = !isVisible
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AccountWorkspace(
    compact: Boolean,
    startupState: DesktopStartupState,
    syncStatus: SyncStatus,
    excludedLocalData: LocalDataCounts?,
    isDeletingAccount: Boolean,
    onSignIn: () -> Unit,
    onSyncNow: () -> Unit,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
    dataDirectory: Path = desktopDataDirectory(),
    databasePath: Path = desktopDatabasePath(dataDirectory),
    platformActions: DesktopPlatformActions = AwtDesktopPlatformActions,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var confirmSignOut by remember { mutableStateOf(false) }
    val deletionConfirmation = remember { AccountDeletionConfirmation() }
    val account = startupState.accountForWorkspace()
    val unsupportedActionMessage = stringResource(Res.string.account_action_unsupported)
    val failedActionMessage = stringResource(Res.string.account_action_failed)
    val projectActionName = stringResource(Res.string.account_project_action_name)
    val reportFailure: (String, DesktopActionResult) -> Unit = { action, result ->
        if (result.shouldReportFailure()) {
            val message =
                when (result) {
                    DesktopActionResult.Unsupported -> unsupportedActionMessage.format(action)
                    DesktopActionResult.Failed -> failedActionMessage.format(action)
                    DesktopActionResult.Success -> error("A successful action must not report a failure")
                }
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
                    .verticalScroll(rememberScrollState())
                    .padding(contentPadding)
                    .padding(if (compact) 28.dp else 48.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Text(
                stringResource(Res.string.account_header_eyebrow),
                color = Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.6.sp,
            )
            Text(stringResource(Res.string.account_header_title), style = MaterialTheme.typography.displaySmall, color = Ink)
            Text(
                stringResource(Res.string.account_header_description),
                color = Muted,
                style = MaterialTheme.typography.bodyLarge,
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val accountCard: @Composable (Modifier) -> Unit = { cardModifier ->
                    if (account == null) {
                        LocalModeCard(startupState, onSignIn, onRetry, cardModifier)
                    } else {
                        AuthenticatedAccountCard(
                            account,
                            syncStatus,
                            startupState,
                            onSyncNow,
                            onRetry,
                            { confirmSignOut = true },
                            isDeletingAccount,
                            cardModifier,
                        )
                    }
                }
                if (useStackedAccountCards(maxWidth)) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        accountCard(Modifier.fillMaxWidth())
                        StorageCard(dataDirectory, databasePath, platformActions, reportFailure, Modifier.fillMaxWidth())
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        accountCard(Modifier.weight(1f).fillMaxHeight())
                        StorageCard(
                            dataDirectory,
                            databasePath,
                            platformActions,
                            reportFailure,
                            Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }

            excludedLocalData?.let { counts ->
                Surface(color = Color(0xFFFFF3D8), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(Res.string.account_excluded_title), color = Ink, fontWeight = FontWeight.Bold)
                        Text(stringResource(Res.string.account_excluded_description, counts.total), color = Muted)
                    }
                }
            }

            if (account != null) {
                AccountDeletionCard(
                    isDeletingAccount = isDeletingAccount,
                    onRequestDeletion = deletionConfirmation::request,
                )
            }

            Surface(color = PaleBlue, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    WorkspaceIcon(Icons.AutoMirrored.Filled.OpenInNew)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.account_project_title), color = Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(Res.string.account_project_description), color = Muted)
                    }
                    OutlinedButton(
                        onClick = {
                            reportFailure(
                                projectActionName,
                                openAccountProject(platformActions),
                            )
                        },
                    ) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.account_project_action))
                    }
                }
            }
        }
    }
    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text(stringResource(Res.string.account_sign_out_title)) },
            text = { Text(stringResource(Res.string.account_sign_out_description)) },
            confirmButton = {
                Button(
                    onClick = {
                        confirmSignOut = false
                        onSignOut()
                    },
                    enabled = !isDeletingAccount,
                ) {
                    Text(stringResource(Res.string.account_sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmSignOut = false }) { Text(stringResource(Res.string.account_cancel)) }
            },
        )
    }
    if (deletionConfirmation.isVisible) {
        AlertDialog(
            onDismissRequest = { if (!isDeletingAccount) deletionConfirmation.cancel() },
            title = { Text(stringResource(Res.string.account_delete_dialog_title)) },
            text = { Text(stringResource(Res.string.account_delete_dialog_description)) },
            confirmButton = {
                Button(
                    onClick = {
                        deletionConfirmation.confirm(onDeleteAccount)
                    },
                    enabled = !isDeletingAccount,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(Res.string.account_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = deletionConfirmation::cancel,
                    enabled = !isDeletingAccount,
                ) {
                    Text(stringResource(Res.string.account_delete_cancel))
                }
            },
        )
    }
}

@Composable
private fun LocalModeCard(
    startupState: DesktopStartupState,
    onSignIn: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier,
) {
    AccountCard(modifier) {
        WorkspaceIcon(Icons.Default.CloudOff)
        Text(stringResource(Res.string.account_local_mode_title), color = Ink, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        Text(stringResource(Res.string.account_local_mode_description), color = Muted)
        Surface(color = Color(0xFFE6F3EF), shape = RoundedCornerShape(12.dp)) {
            Text(
                stringResource(Res.string.account_local_mode_available),
                color = Success,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                fontWeight = FontWeight.SemiBold,
            )
        }
        (startupState as? DesktopStartupState.LocalMode)?.authError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        if (startupState is DesktopStartupState.RecoverableFailure) {
            Text(startupState.message, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry) { Text(stringResource(Res.string.account_retry)) }
        }
        Button(onClick = onSignIn) { Text(stringResource(Res.string.account_sign_in)) }
    }
}

@Composable
private fun AuthenticatedAccountCard(
    account: DesktopAccount,
    syncStatus: SyncStatus,
    startupState: DesktopStartupState,
    onSyncNow: () -> Unit,
    onRetry: () -> Unit,
    onSignOut: () -> Unit,
    isDeletingAccount: Boolean,
    modifier: Modifier,
) {
    AccountCard(modifier) {
        Text(stringResource(Res.string.account_authenticated_title), color = Ink, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        Text(account.displayName, color = Ink)
        account.email?.let { Text(it, color = Muted) }
        Text(stringResource(Res.string.account_family_value, account.familyName ?: account.familyId.value), color = Muted)
        Text(
            if (syncStatus.isSyncing) {
                stringResource(Res.string.account_syncing)
            } else {
                syncStatus.lastSyncedAtMillis?.let { stringResource(Res.string.account_last_sync, formatDesktopTimestamp(it)) }
                    ?: stringResource(Res.string.account_never_synced)
            },
            color = if (syncStatus.isSyncing) Blue else Muted,
        )
        (startupState as? DesktopStartupState.RecoverableFailure)?.let {
            Text(it.message, color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onRetry, enabled = !isDeletingAccount) { Text(stringResource(Res.string.account_retry)) }
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(onClick = onSyncNow, enabled = !syncStatus.isSyncing && !isDeletingAccount) {
                Text(stringResource(Res.string.account_sync_now))
            }
            OutlinedButton(onClick = onSignOut, enabled = !isDeletingAccount) { Text(stringResource(Res.string.account_sign_out)) }
        }
    }
}

@Composable
private fun AccountDeletionCard(
    isDeletingAccount: Boolean,
    onRequestDeletion: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                stringResource(Res.string.account_deletion_title),
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Text(
                stringResource(Res.string.account_deletion_description),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            OutlinedButton(
                onClick = onRequestDeletion,
                enabled = !isDeletingAccount,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(
                    stringResource(
                        if (isDeletingAccount) Res.string.account_deleting_button else Res.string.account_delete_button,
                    ),
                )
            }
        }
    }
}

private fun DesktopStartupState.accountForWorkspace(): DesktopAccount? =
    when (this) {
        is DesktopStartupState.Authenticated -> account
        is DesktopStartupState.InitialSync -> account
        is DesktopStartupState.RecoverableFailure -> account
        else -> null
    }

@Composable
private fun StorageCard(
    dataDirectory: Path,
    databasePath: Path,
    platformActions: DesktopPlatformActions,
    reportFailure: (String, DesktopActionResult) -> Unit,
    modifier: Modifier,
) {
    val dataFolderActionName = stringResource(Res.string.account_data_folder_action_name)
    val details = remember { AccountStorageDetails() }
    AccountCard(modifier) {
        WorkspaceIcon(Icons.Default.Storage)
        Text(stringResource(Res.string.account_storage_title), color = Ink, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        Text(
            stringResource(Res.string.account_storage_description),
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    reportFailure(
                        dataFolderActionName,
                        openAccountDataDirectory(platformActions, dataDirectory),
                    )
                },
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.account_data_folder_action))
            }
            TextButton(onClick = details::toggle) {
                Text(
                    stringResource(
                        if (details.isVisible) Res.string.account_storage_hide_details else Res.string.account_storage_show_details,
                    ),
                )
            }
        }
        if (details.isVisible) {
            PathLabel(stringResource(Res.string.account_application_data_label), dataDirectory)
            PathLabel(stringResource(Res.string.account_database_label), databasePath)
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
