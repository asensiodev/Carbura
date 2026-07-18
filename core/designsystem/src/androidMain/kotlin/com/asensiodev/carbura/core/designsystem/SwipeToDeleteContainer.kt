package com.asensiodev.carbura.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import kotlin.math.abs

@Composable
fun SwipeToDeleteContainer(
    actionLabel: String,
    accessibilityLabel: String,
    enabled: Boolean,
    onDeleteRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val rowWidthPx = remember { mutableIntStateOf(0) }
    lateinit var dismissState: SwipeToDismissBoxState
    dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                val draggedFarEnough = abs(dismissState.requireOffset()) >= rowWidthPx.intValue * 0.6f
                if (enabled && value == SwipeToDismissBoxValue.EndToStart && draggedFarEnough) {
                    onDeleteRequest()
                }
                false
            },
            positionalThreshold = { distance -> distance * 0.6f },
        )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = Spacings.spacing24)
                        .clearAndSetSemantics {},
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = actionLabel,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        modifier =
            modifier
                .onSizeChanged { rowWidthPx.intValue = it.width }
                .clip(MaterialTheme.shapes.medium)
                .semantics {
                    if (enabled) {
                        customActions =
                            listOf(
                                CustomAccessibilityAction(accessibilityLabel) {
                                    onDeleteRequest()
                                    true
                                },
                            )
                    }
                },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = enabled,
        content = { content() },
    )
}
