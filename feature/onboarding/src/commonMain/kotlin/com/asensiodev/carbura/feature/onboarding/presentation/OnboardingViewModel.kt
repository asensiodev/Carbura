package com.asensiodev.carbura.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.domain.auth.AccountLocalDataCleaner
import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.auth.AuthSession
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway
import com.asensiodev.carbura.core.domain.user.RemoteUserProfile
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardingViewModel(
    private val authGateway: AuthGateway,
    private val remoteUserProfileGateway: RemoteUserProfileGateway,
    private val accountLocalDataCleaner: AccountLocalDataCleaner,
    private val familyScope: ActiveFamilyScopeGateway,
    private val dispatchers: DispatcherProvider,
    private val coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<OnboardingEffect> = _effects.receiveAsFlow()
    private var activeAuthOperation: AuthOperation? = null

    private val scope: CoroutineScope
        get() = coroutineScope ?: viewModelScope

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.Started ->
                scope.launch {
                    try {
                        loadCurrentSession()
                    } finally {
                        _uiState.update { it.copy(isInitializing = false) }
                    }
                }
            OnboardingEvent.GoogleSignInClicked -> launchAuthOperation(AuthOperation.DirectSignIn) { signInWithGoogle() }
            OnboardingEvent.GoogleCredentialRequestStarted -> beginCredentialRequest()
            OnboardingEvent.GoogleCredentialRequestCancelled -> finishAuthOperation(AuthOperation.CredentialSignIn)
            is OnboardingEvent.GoogleIdTokenReceived -> continueCredentialSignIn(event.idToken)
            is OnboardingEvent.GoogleSignInError ->
                if (activeAuthOperation == null || activeAuthOperation == AuthOperation.CredentialSignIn) {
                    activeAuthOperation = null
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = OnboardingError.SignInFailed,
                            errorDiagnostic = event.diagnostic,
                        )
                    }
                }
            OnboardingEvent.SignOutClicked -> launchAuthOperation(AuthOperation.SignOut) { signOut() }
            OnboardingEvent.DeleteAccountConfirmed ->
                launchAuthOperation(AuthOperation.DeleteAccount) { deleteAccount() }
        }
    }

    private fun launchAuthOperation(
        operation: AuthOperation,
        block: suspend () -> Unit,
    ) {
        if (activeAuthOperation != null) return
        activeAuthOperation = operation
        _uiState.update {
            it.copy(
                isLoading = true,
                isDeletingAccount = operation == AuthOperation.DeleteAccount,
                error = null,
                errorDiagnostic = null,
            )
        }
        scope
            .launch {
                try {
                    block()
                } finally {
                    finishAuthOperation(operation)
                }
            }.invokeOnCompletion { cause ->
                if (cause is CancellationException) finishAuthOperation(operation)
            }
    }

    private fun beginCredentialRequest() {
        if (activeAuthOperation != null) return
        activeAuthOperation = AuthOperation.CredentialSignIn
        _uiState.update { it.copy(isLoading = true, error = null, errorDiagnostic = null) }
    }

    private fun continueCredentialSignIn(idToken: String) {
        if (activeAuthOperation == null) beginCredentialRequest()
        if (activeAuthOperation != AuthOperation.CredentialSignIn) return
        scope
            .launch {
                try {
                    signInWithGoogle(idToken)
                } finally {
                    finishAuthOperation(AuthOperation.CredentialSignIn)
                }
            }.invokeOnCompletion { cause ->
                if (cause is CancellationException) finishAuthOperation(AuthOperation.CredentialSignIn)
            }
    }

    private fun finishAuthOperation(operation: AuthOperation) {
        if (activeAuthOperation != operation) return
        activeAuthOperation = null
        _uiState.update { it.copy(isLoading = false, isDeletingAccount = false) }
    }

    private suspend fun loadCurrentSession() {
        _uiState.update { it.copy(isInitializing = true, error = null, errorDiagnostic = null) }
        val session =
            try {
                withContext(dispatchers.io) { authGateway.currentSession() }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isInitializing = false,
                        isAuthenticated = false,
                        error = OnboardingError.SessionUnavailable,
                        errorDiagnostic = error.diagnostic(),
                    )
                }
                return
            }

        if (session == null) {
            familyScope.activateLocal()
            _uiState.update {
                it.copy(
                    isInitializing = false,
                    isAuthenticated = false,
                    displayName = null,
                    email = null,
                    familyId = null,
                    familyName = null,
                    error = null,
                    errorDiagnostic = null,
                )
            }
        } else {
            try {
                val profile =
                    withContext(dispatchers.io) {
                        remoteUserProfileGateway.getProfileForUser(UserId(session.user.id))
                    }
                if (profile == null) {
                    ensureProfileAndNavigate(session, isInitializing = true)
                } else {
                    applyAuthenticatedProfile(
                        profile = profile,
                        isInitializing = false,
                        isLoading = false,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                familyScope.activateLocal()
                _uiState.update {
                    it.copy(
                        isInitializing = false,
                        isLoading = false,
                        isAuthenticated = false,
                        displayName = session.displayName,
                        email = session.user.email,
                        familyId = null,
                        familyName = null,
                        error = OnboardingError.ProfileUnavailable,
                        errorDiagnostic = error.diagnostic(),
                    )
                }
            }
        }
    }

    private suspend fun signInWithGoogle() {
        try {
            val session = withContext(dispatchers.io) { authGateway.signInWithGoogle() }
            val profile =
                withContext(dispatchers.io) {
                    remoteUserProfileGateway.getProfileForUser(UserId(session.user.id))
                }
            if (profile == null) {
                ensureProfileAndNavigate(session)
            } else {
                applyAuthenticatedProfile(profile = profile)
                _effects.send(OnboardingEffect.NavigateToGarage)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    error = OnboardingError.SignInFailed,
                    errorDiagnostic = error.diagnostic(),
                )
            }
        }
    }

    private suspend fun signInWithGoogle(idToken: String) {
        try {
            val session = withContext(dispatchers.io) { authGateway.signInWithGoogle(idToken) }
            val profile =
                withContext(dispatchers.io) {
                    remoteUserProfileGateway.getProfileForUser(UserId(session.user.id))
                }
            if (profile == null) {
                ensureProfileAndNavigate(session)
            } else {
                applyAuthenticatedProfile(profile = profile)
                _effects.send(OnboardingEffect.NavigateToGarage)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    error = OnboardingError.SignInFailed,
                    errorDiagnostic = error.diagnostic(),
                )
            }
        }
    }

    private suspend fun ensureProfileAndNavigate(
        session: AuthSession,
        isInitializing: Boolean = false,
    ) {
        try {
            val profile =
                withContext(dispatchers.io) {
                    remoteUserProfileGateway.ensureProfile(
                        displayName = session.displayName ?: "Usuario",
                        email = session.user.email,
                    )
                }
            applyAuthenticatedProfile(
                profile = profile,
                isInitializing = false,
                isLoading = false,
            )
            if (!isInitializing) {
                _effects.send(OnboardingEffect.NavigateToGarage)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isInitializing = false,
                    isLoading = false,
                    isAuthenticated = false,
                    error = OnboardingError.ProfileCreationFailed,
                    errorDiagnostic = error.diagnostic(),
                )
            }
        }
    }

    private fun applyAuthenticatedProfile(
        profile: RemoteUserProfile,
        isInitializing: Boolean = false,
        isLoading: Boolean = false,
    ) {
        familyScope.activateAuthenticated(profile.userId, profile.familyId)
        _uiState.update {
            it.copy(
                isInitializing = isInitializing,
                isLoading = isLoading,
                isAuthenticated = true,
                displayName = profile.displayName,
                email = profile.email,
                familyId = profile.familyId.value,
                familyName = profile.familyName,
                error = null,
                errorDiagnostic = null,
            )
        }
    }

    private suspend fun signOut() {
        try {
            withContext(dispatchers.io) { authGateway.signOut() }
            familyScope.activateLocal()
            _uiState.update {
                OnboardingUiState(
                    isInitializing = false,
                    isLoading = false,
                )
            }
            _effects.send(OnboardingEffect.NavigateToLogin)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = OnboardingError.SignOutFailed,
                    errorDiagnostic = error.diagnostic(),
                )
            }
        }
    }

    private suspend fun deleteAccount() {
        val familyId = _uiState.value.familyId?.let(::FamilyId)
        try {
            withContext(NonCancellable + dispatchers.io) { authGateway.deleteAccount() }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            // Dispatch is irreversible and may have committed despite a lost response.
        }

        withContext(NonCancellable) {
            familyScope.activateLocal()
            if (familyId != null) {
                try {
                    withContext(dispatchers.io) { accountLocalDataCleaner.clear(familyId) }
                } catch (_: CancellationException) {
                    // Remote deletion committed; finish the terminal unauthenticated transition.
                } catch (_: Exception) {
                    // Remote deletion committed; do not leave a deleted identity in authenticated UI state.
                }
            }
            _uiState.value = OnboardingUiState(isInitializing = false, isLoading = false)
            _effects.send(OnboardingEffect.NavigateToLogin)
        }
    }
}

private enum class AuthOperation {
    DirectSignIn,
    CredentialSignIn,
    SignOut,
    DeleteAccount,
}

private val AuthSession.displayName: String?
    get() = user.displayName ?: user.email

private fun Throwable.diagnostic(): String =
    buildString {
        append(this@diagnostic::class.simpleName ?: "Authentication error")
        this@diagnostic.message?.takeIf(String::isNotBlank)?.let {
            append(": ")
            append(it)
        }
    }
