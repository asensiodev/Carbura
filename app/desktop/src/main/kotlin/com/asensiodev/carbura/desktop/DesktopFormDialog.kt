package com.asensiodev.carbura.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

internal val DesktopFormDialogMaxWidth = 640.dp
internal val DesktopFormDialogMargin = 24.dp
internal val DesktopPairedFieldsThreshold = 480.dp

internal fun desktopFormDialogMaxHeight(availableHeight: Dp): Dp = (availableHeight - DesktopFormDialogMargin * 2).coerceAtLeast(0.dp)

internal fun useStackedDesktopFields(availableWidth: Dp): Boolean = availableWidth < DesktopPairedFieldsThreshold

@Composable
internal fun DesktopPairedFields(
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (useStackedDesktopFields(maxWidth)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                first(Modifier.fillMaxWidth())
                second(Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                first(Modifier.weight(1f))
                second(Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DesktopFormDialog(
    title: String,
    onDismissRequest: () -> Unit,
    dismissEnabled: Boolean,
    actions: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = { if (dismissEnabled) onDismissRequest() },
        properties =
            DialogProperties(
                dismissOnBackPress = dismissEnabled,
                dismissOnClickOutside = dismissEnabled,
                usePlatformDefaultWidth = false,
            ),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val availableHeight = maxHeight
            Box(
                modifier = Modifier.fillMaxSize().padding(DesktopFormDialogMargin),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier =
                        Modifier
                            .widthIn(max = DesktopFormDialogMaxWidth)
                            .fillMaxWidth()
                            .heightIn(max = desktopFormDialogMaxHeight(availableHeight)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 12.dp,
                ) {
                    Column {
                        Text(
                            text = title,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 22.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Ink,
                        )
                        HorizontalDivider(color = Line)
                        Column(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            content = content,
                        )
                        HorizontalDivider(color = Line)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 18.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            actions()
                        }
                    }
                }
            }
        }
    }
}
