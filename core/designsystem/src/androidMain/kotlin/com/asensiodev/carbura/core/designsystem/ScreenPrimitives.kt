package com.asensiodev.carbura.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val ExpandedWidth = 600.dp
private val MaximumContentWidth = 840.dp

@Composable
fun ConstrainedScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val horizontalPadding = if (maxWidth < ExpandedWidth) Spacings.spacing24 else Spacings.spacing48

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = horizontalPadding)
                    .widthIn(max = MaximumContentWidth)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
fun LoadingState(
    message: String? = null,
    modifier: Modifier = Modifier,
) {
    StatePresentation(modifier = modifier) {
        CircularProgressIndicator()
        if (message != null) {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    StatePresentation(modifier = modifier) {
        StateCopy(title = title, description = description)
        action?.invoke()
    }
}

@Composable
fun RetryState(
    title: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    StatePresentation(
        modifier =
            modifier.semantics {
                liveRegion = LiveRegionMode.Polite
                error(description ?: title)
            },
    ) {
        StateCopy(title = title, description = description)
        Button(onClick = onRetry) {
            Text(retryLabel)
        }
    }
}

@Composable
private fun StatePresentation(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacings.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(
                Spacings.spacing16,
                Alignment.CenterVertically,
            ),
    ) {
        content()
    }
}

@Composable
private fun StateCopy(
    title: String,
    description: String?,
) {
    Text(
        text = title,
        modifier = Modifier.semantics { heading() },
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
    )
    if (description != null) {
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
