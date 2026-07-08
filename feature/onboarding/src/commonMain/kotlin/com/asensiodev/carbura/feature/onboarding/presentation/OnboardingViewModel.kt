package com.asensiodev.carbura.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asensiodev.carbura.core.auth.AuthGateway
import com.asensiodev.carbura.core.auth.AuthSession
import com.asensiodev.carbura.core.data.RemoteUserProfile
import com.asensiodev.carbura.core.data.RemoteUserProfileGateway
import com.asensiodev.carbura.core.domain.DispatcherProvider
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import kotlinx.coroutines.CoroutineScope
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
    private val dispatchers: DispatcherProvider,
    private val coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OnboardingEffect>(capacity = Channel.BUFFERED)
    val effects: Flow<OnboardingEffect> = _effects.receiveAsFlow()

    private val scope: CoroutineScope
        get() = coroutineScope ?: viewModelScope

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.Started -> scope.launch { loadCurrentSession() }
            OnboardingEvent.GoogleSignInClicked -> scope.launch { signInWithGoogle() }
            is OnboardingEvent.GoogleIdTokenReceived -> scope.launch { signInWithGoogle(event.idToken) }
            is OnboardingEvent.GoogleSignInError -> _uiState.update {
                it.copy(isLoading = false, errorMessage = event.message)
            }
            OnboardingEvent.SignOutClicked -> scope.launch { signOut() }
            OnboardingEvent.ErrorDismissed -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private suspend fun loadCurrentSession() {
        _uiState.update { it.copy(isInitializing = true, errorMessage = null) }
        val session = runCatching {
            withContext(dispatchers.io) { authGateway.currentSession() }
        }.getOrElse { error ->
            _uiState.update {
                it.copy(
                    isInitializing = false,
                    isAuthenticated = false,
                    errorMessage = error.message ?: "Unable to check current session.",
                )
            }
            return
        }

        if (session == null) {
            _uiState.update {
                it.copy(
                    isInitializing = false,
                    isAuthenticated = false,
                    displayName = null,
                    email = null,
                    familyId = null,
                    familyName = null,
                    errorMessage = null,
                )
            }
        } else {
            val profileResult = runCatching {
                withContext(dispatchers.io) {
                    remoteUserProfileGateway.getProfileForUser(UserId(session.user.id))
                }
            }
            profileResult.onSuccess { profile ->
                if (profile == null) {
                    ensureProfileAndNavigate(session, isInitializing = true)
                } else {
                    applyAuthenticatedProfile(
                        profile = profile,
                        isInitializing = false,
                        isLoading = false,
                    )
                }
            }.onFailure {
                applyAuthenticatedSessionFallback(session)
            }
        }
    }

    private suspend fun signInWithGoogle() {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = runCatching {
            val session = withContext(dispatchers.io) { authGateway.signInWithGoogle() }
            val profile = withContext(dispatchers.io) {
                remoteUserProfileGateway.getProfileForUser(UserId(session.user.id))
            }
            session to profile
        }

        result.onSuccess { (session, profile) ->
            if (profile == null) {
                ensureProfileAndNavigate(session)
            } else {
                applyAuthenticatedProfile(profile = profile)
                _effects.send(OnboardingEffect.NavigateToGarage)
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = error.message ?: "Unable to sign in.",
                )
            }
        }
    }

    private suspend fun signInWithGoogle(idToken: String) {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val result = runCatching {
            val session = withContext(dispatchers.io) { authGateway.signInWithGoogle(idToken) }
            val profile = withContext(dispatchers.io) {
                remoteUserProfileGateway.getProfileForUser(UserId(session.user.id))
            }
            session to profile
        }

        result.onSuccess { (session, profile) ->
            if (profile == null) {
                ensureProfileAndNavigate(session)
            } else {
                applyAuthenticatedProfile(profile = profile)
                _effects.send(OnboardingEffect.NavigateToGarage)
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = error.message ?: "Unable to sign in.",
                )
            }
        }
    }

    private suspend fun ensureProfileAndNavigate(
        session: AuthSession,
        isInitializing: Boolean = false,
    ) {
        withContext(dispatchers.io) {
            runCatching {
                remoteUserProfileGateway.ensureProfile(
                    displayName = session.displayName ?: "Usuario",
                    email = session.user.email,
                )
            }
        }.onSuccess { profile ->
            applyAuthenticatedProfile(
                profile = profile,
                isInitializing = false,
                isLoading = false,
            )
            if (!isInitializing) {
                _effects.send(OnboardingEffect.NavigateToGarage)
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isInitializing = false,
                    isLoading = false,
                    isAuthenticated = false,
                    errorMessage = error.message ?: "Unable to create profile.",
                )
            }
        }
    }

    private fun applyAuthenticatedProfile(
        profile: RemoteUserProfile,
        isInitializing: Boolean = false,
        isLoading: Boolean = false,
    ) {
        _uiState.update {
            it.copy(
                isInitializing = isInitializing,
                isLoading = isLoading,
                isAuthenticated = true,
                displayName = profile.displayName,
                email = profile.email,
                familyId = profile.familyId.value,
                familyName = profile.familyName,
                errorMessage = null,
            )
        }
    }

    private fun applyAuthenticatedSessionFallback(session: AuthSession) {
        _uiState.update {
            it.copy(
                isInitializing = false,
                isLoading = false,
                isAuthenticated = true,
                displayName = session.displayName,
                email = session.user.email,
                familyId = null,
                familyName = null,
                errorMessage = null,
            )
        }
    }

    private suspend fun signOut() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        runCatching {
            withContext(dispatchers.io) { authGateway.signOut() }
        }.onSuccess {
            _uiState.update {
                OnboardingUiState(
                    isInitializing = false,
                    isLoading = false,
                )
            }
            _effects.send(OnboardingEffect.NavigateToLogin)
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Unable to sign out.",
                )
            }
        }
    }
}

private val AuthSession.displayName: String?
    get() = user.displayName ?: user.email
