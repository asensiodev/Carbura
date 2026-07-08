package com.asensiodev.carbura.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
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
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val googleClientId = remember {
        GlobalContext.get().get<SupabaseSettings>().googleClientId
    }

    OnboardingScreen(
        state = state,
        onGoogleSignIn = {
            scope.launch {
                try {
                    if (googleClientId.isBlank()) {
                        viewModel.onEvent(
                            OnboardingEvent.GoogleSignInError(
                                "GOOGLE_CLIENT_ID is not configured in local.properties",
                            ),
                        )
                        return@launch
                    }
                    val googleIdOption = GetSignInWithGoogleOption.Builder(googleClientId)
                        .build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    val result = credentialManager.getCredential(context, request)
                    val credential = result.credential
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        viewModel.onEvent(
                            OnboardingEvent.GoogleIdTokenReceived(googleIdTokenCredential.idToken),
                        )
                    }
                } catch (e: NoCredentialException) {
                    viewModel.onEvent(
                        OnboardingEvent.GoogleSignInError(
                            "No hay credenciales de Google disponibles en este dispositivo.",
                        ),
                    )
                } catch (e: Exception) {
                    viewModel.onEvent(OnboardingEvent.GoogleSignInError(googleSignInErrorMessage(e)))
                }
            }
        },
        modifier = modifier,
    )
}

private fun googleSignInErrorMessage(error: Exception): String {
    val rawMessage = error.message.orEmpty()
    return if (rawMessage.contains("account reauth failed", ignoreCase = true) ||
        rawMessage.contains("16", ignoreCase = true)
    ) {
        "Google no pudo reautenticar la cuenta. Revisa que el OAuth Android tenga el package " +
            "y SHA-1/SHA-256 de esta app, y que GOOGLE_CLIENT_ID sea el Web OAuth Client ID."
    } else {
        rawMessage.ifBlank { "Unable to sign in with Google." }
    }
}

@Composable
private fun OnboardingScreen(
    state: OnboardingUiState,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
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
                        LoadingMessage(
                            text = stringResource(R.string.onboarding_loading),
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
    }
}

@Composable
private fun LoadingMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
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
