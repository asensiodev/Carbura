package com.asensiodev.carbura.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asensiodev.carbura.core.auth.SupabaseSettings
import com.asensiodev.carbura.core.designsystem.Spacings
import com.asensiodev.carbura.featureonboarding.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val googleClientId =
        remember {
            GlobalContext.get().get<SupabaseSettings>().googleClientId
        }

    OnboardingScreen(
        state = state,
        onGoogleSignIn = {
            scope.launch {
                viewModel.onEvent(OnboardingEvent.GoogleCredentialRequestStarted)
                try {
                    if (googleClientId.isBlank()) {
                        viewModel.onEvent(
                            OnboardingEvent.GoogleSignInError(
                                "GOOGLE_CLIENT_ID is not configured in local.properties",
                            ),
                        )
                        return@launch
                    }
                    val googleIdOption =
                        GetSignInWithGoogleOption
                            .Builder(googleClientId)
                            .build()
                    val request =
                        GetCredentialRequest
                            .Builder()
                            .addCredentialOption(googleIdOption)
                            .build()
                    val result = credentialManager.getCredential(context, request)
                    val credential = result.credential
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential
                                .createFrom(credential.data)
                        viewModel.onEvent(
                            OnboardingEvent.GoogleIdTokenReceived(googleIdTokenCredential.idToken),
                        )
                    }
                } catch (e: NoCredentialException) {
                    viewModel.onEvent(
                        OnboardingEvent.GoogleSignInError(
                            e.message ?: "No Google credential is available on this device.",
                        ),
                    )
                } catch (e: Exception) {
                    viewModel.onEvent(OnboardingEvent.GoogleSignInError(e.diagnostic()))
                }
            }
        },
        modifier = modifier,
    )
}

private fun Exception.diagnostic(): String = "${this::class.simpleName ?: "Credential error"}: ${message.orEmpty()}"

@Composable
internal fun OnboardingScreen(
    state: OnboardingUiState,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacings.spacing24, vertical = Spacings.spacing16),
            contentAlignment = Alignment.TopCenter,
        ) {
            Card(
                modifier =
                    Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxWidth()
                        .testTag("onboarding_content"),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(Spacings.spacing24),
                    verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_title),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = stringResource(R.string.onboarding_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (state.error != null) {
                        LoginError(errorMessage = stringResource(state.error.stringResource()))
                    }

                    Button(
                        onClick = onGoogleSignIn,
                        enabled = state.canSubmitLogin,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(
                                if (state.error == null) {
                                    R.string.onboarding_google_button
                                } else {
                                    R.string.onboarding_google_retry_button
                                },
                            ),
                        )
                    }

                    if (state.isInitializing || state.isLoading) {
                        LoadingMessage(
                            text =
                                stringResource(
                                    if (state.isInitializing) {
                                        R.string.onboarding_initializing
                                    } else {
                                        R.string.onboarding_loading
                                    },
                                ),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginError(errorMessage: String) {
    Column(
        modifier =
            Modifier.semantics {
                liveRegion = LiveRegionMode.Assertive
                error(errorMessage)
            },
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
    ) {
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
    }
}

@Composable
private fun LoadingMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.semantics {
                liveRegion = LiveRegionMode.Polite
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                stateDescription = text
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(Spacings.spacing12))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun OnboardingError.stringResource(): Int =
    when (this) {
        OnboardingError.SessionUnavailable -> R.string.onboarding_error_session
        OnboardingError.ProfileUnavailable -> R.string.onboarding_error_profile
        OnboardingError.SignInFailed -> R.string.onboarding_error_sign_in
        OnboardingError.ProfileCreationFailed -> R.string.onboarding_error_profile_creation
        OnboardingError.SignOutFailed -> R.string.onboarding_error_sign_out
    }
