package com.asensiodev.carbura.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.asensiodev.carbura.core.designsystem.Spacings
import com.asensiodev.carbura.featureonboarding.R

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel,
) {
    val state by viewModel.uiState.collectAsState()

    OnboardingScreen(
        state = state,
        onGoogleSignIn = { viewModel.onEvent(OnboardingEvent.GoogleSignInClicked) },
        modifier = modifier,
    )
}

@Composable
private fun OnboardingScreen(
    state: OnboardingUiState,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacings.spacing24),
            contentAlignment = Alignment.Center,
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacings.spacing24),
                    verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_title),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    Text(
                        text = stringResource(R.string.onboarding_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (state.errorMessage != null) {
                        LoginError(errorMessage = state.errorMessage)
                    }

                    Button(
                        onClick = onGoogleSignIn,
                        enabled = state.canSubmitLogin,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.onboarding_google_button))
                    }

                    if (state.isLoading) {
                        LoadingMessage(text = stringResource(R.string.onboarding_loading))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginError(errorMessage: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.spacing8)) {
        Text(
            text = stringResource(R.string.onboarding_error_title),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.onboarding_missing_profile_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingMessage(text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(Spacings.spacing12))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
