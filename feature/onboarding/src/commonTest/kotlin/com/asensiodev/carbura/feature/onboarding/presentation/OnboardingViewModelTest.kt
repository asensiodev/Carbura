package com.asensiodev.carbura.feature.onboarding.presentation

import app.cash.turbine.test
import com.asensiodev.carbura.core.auth.AuthGateway
import com.asensiodev.carbura.core.auth.AuthSession
import com.asensiodev.carbura.core.auth.AuthUser
import com.asensiodev.carbura.core.data.RemoteUserProfile
import com.asensiodev.carbura.core.data.RemoteUserProfileGateway
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @Test
    fun startupWithoutSessionShowsUnauthenticatedState() = runTest {
        val viewModel = onboardingViewModel(authGateway = FakeAuthGateway(currentSession = null))

        viewModel.onEvent(OnboardingEvent.Started)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isInitializing)
        assertFalse(state.isAuthenticated)
        assertTrue(state.canSubmitLogin)
    }

    @Test
    fun startupWithSessionShowsAuthenticatedState() = runTest {
        val viewModel = onboardingViewModel(authGateway = FakeAuthGateway(currentSession = authSession()))

        viewModel.onEvent(OnboardingEvent.Started)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isInitializing)
        assertTrue(state.isAuthenticated)
        assertEquals("Angela", state.displayName)
    }

    @Test
    fun googleLoginEmitsLoadingThenNavigateWhenProfileExists() = runTest {
        val signInGate = CompletableDeferred<Unit>()
        val viewModel = onboardingViewModel(
            authGateway = FakeAuthGateway(signInSession = authSession(), signInGate = signInGate),
            remoteUserProfileGateway = FakeRemoteUserProfileGateway(remoteProfile()),
        )

        viewModel.effects.test {
            viewModel.onEvent(OnboardingEvent.GoogleSignInClicked)
            runCurrent()
            assertTrue(viewModel.uiState.value.isLoading)
            signInGate.complete(Unit)
            advanceUntilIdle()

            assertIs<OnboardingEffect.NavigateToGarage>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isAuthenticated)
        assertEquals("Angela Remote", state.displayName)
    }

    @Test
    fun googleLoginFailureShowsError() = runTest {
        val viewModel = onboardingViewModel(
            authGateway = FakeAuthGateway(signInError = IllegalStateException("Missing config")),
        )

        viewModel.onEvent(OnboardingEvent.GoogleSignInClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isAuthenticated)
        assertEquals("Missing config", state.errorMessage)
    }

    @Test
    fun googleLoginWithoutRemoteProfileShowsRecoverableError() = runTest {
        val viewModel = onboardingViewModel(
            authGateway = FakeAuthGateway(signInSession = authSession()),
            remoteUserProfileGateway = FakeRemoteUserProfileGateway(profile = null),
        )

        viewModel.onEvent(OnboardingEvent.GoogleSignInClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isAuthenticated)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun signOutReturnsToLoginAndEmitsEffect() = runTest {
        val viewModel = onboardingViewModel(authGateway = FakeAuthGateway(currentSession = authSession()))

        viewModel.onEvent(OnboardingEvent.Started)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAuthenticated)

        viewModel.effects.test {
            viewModel.onEvent(OnboardingEvent.SignOutClicked)
            advanceUntilIdle()

            assertIs<OnboardingEffect.NavigateToLogin>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isAuthenticated)
    }

    private fun TestScope.onboardingViewModel(
        authGateway: AuthGateway = FakeAuthGateway(),
        remoteUserProfileGateway: RemoteUserProfileGateway = FakeRemoteUserProfileGateway(remoteProfile()),
    ): OnboardingViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return OnboardingViewModel(
            authGateway = authGateway,
            remoteUserProfileGateway = remoteUserProfileGateway,
            dispatchers = TestDispatcherProvider(
                io = dispatcher,
                default = dispatcher,
                main = dispatcher,
            ),
            coroutineScope = this,
        )
    }

    private fun authSession(): AuthSession = AuthSession(
        accessToken = "access-token",
        user = AuthUser(
            id = "user-1",
            email = "angela@example.com",
            displayName = "Angela",
        ),
    )

    private fun remoteProfile(): RemoteUserProfile = RemoteUserProfile(
        userId = UserId("user-1"),
        familyId = FamilyId("family-1"),
        displayName = "Angela Remote",
        email = "angela@example.com",
    )
}

private class FakeAuthGateway(
    private val currentSession: AuthSession? = null,
    private val signInSession: AuthSession = currentSession ?: AuthSession(
        accessToken = "access-token",
        user = AuthUser("user-1", "angela@example.com", "Angela"),
    ),
    private val signInError: Throwable? = null,
    private val signInGate: CompletableDeferred<Unit>? = null,
) : AuthGateway {
    override suspend fun currentSession(): AuthSession? = currentSession

    override suspend fun signInWithGoogle(): AuthSession {
        signInGate?.await()
        signInError?.let { throw it }
        return signInSession
    }

    override suspend fun signOut() = Unit
}

private class FakeRemoteUserProfileGateway(
    private val profile: RemoteUserProfile?,
) : RemoteUserProfileGateway {
    override suspend fun getProfileForUser(userId: UserId): RemoteUserProfile? = profile
}
