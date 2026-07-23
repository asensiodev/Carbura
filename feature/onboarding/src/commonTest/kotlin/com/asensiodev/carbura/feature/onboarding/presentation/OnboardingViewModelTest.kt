package com.asensiodev.carbura.feature.onboarding.presentation

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.auth.AccountLocalDataCleaner
import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.auth.AuthSession
import com.asensiodev.carbura.core.domain.auth.AuthUser
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway
import com.asensiodev.carbura.core.domain.user.RemoteUserProfile
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @Test
    fun initialStateBlocksLoginWhileSessionInitializes() {
        val state = OnboardingUiState()

        assertTrue(state.isInitializing)
        assertFalse(state.canSubmitLogin)
    }

    @Test
    fun startupWithoutSessionShowsUnauthenticatedState() =
        runTest {
            val viewModel = onboardingViewModel(authGateway = FakeAuthGateway(currentSession = null))

            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isInitializing)
            assertFalse(state.isAuthenticated)
            assertEquals(null, state.displayName)
            assertEquals(null, state.email)
            assertEquals(null, state.familyName)
            assertTrue(state.canSubmitLogin)
        }

    @Test
    fun startupWithSessionShowsAuthenticatedProfileState() =
        runTest {
            val viewModel = onboardingViewModel(authGateway = FakeAuthGateway(currentSession = authSession()))

            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isInitializing)
            assertTrue(state.isAuthenticated)
            assertEquals("Angela Remote", state.displayName)
            assertEquals("angela@example.com", state.email)
            assertEquals("family-1", state.familyId)
            assertEquals("Familia de Angela", state.familyName)
        }

    @Test
    fun startupWithSessionAndProfileFailureReturnsToLoginWithError() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(currentSession = authSession()),
                    remoteUserProfileGateway =
                        FakeRemoteUserProfileGateway(
                            profile = null,
                            getProfileError = IllegalStateException("Profile unavailable"),
                        ),
                )

            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isInitializing)
            assertFalse(state.isAuthenticated)
            assertEquals(null, state.familyId)
            assertEquals(OnboardingError.ProfileUnavailable, state.error)
            assertTrue(state.errorDiagnostic.orEmpty().contains("Profile unavailable"))
        }

    @Test
    fun sessionLookupCancellationDoesNotBecomeSessionError() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway =
                        FakeAuthGateway(
                            currentSessionError = CancellationException("Session lookup cancelled"),
                        ),
                )

            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.errorDiagnostic)
            assertFalse(viewModel.uiState.value.isLoading)
            assertFalse(viewModel.uiState.value.isInitializing)
        }

    @Test
    fun profileLookupCancellationDoesNotBecomeProfileError() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(currentSession = authSession()),
                    remoteUserProfileGateway =
                        FakeRemoteUserProfileGateway(
                            profile = null,
                            getProfileError = CancellationException("Profile lookup cancelled"),
                        ),
                )

            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.errorDiagnostic)
            assertFalse(viewModel.uiState.value.isInitializing)
        }

    @Test
    fun profileCreationCancellationDoesNotBecomeCreationError() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(currentSession = authSession()),
                    remoteUserProfileGateway =
                        FakeRemoteUserProfileGateway(
                            profile = null,
                            ensureProfileError = CancellationException("Profile creation cancelled"),
                        ),
                )

            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.errorDiagnostic)
            assertFalse(viewModel.uiState.value.isInitializing)
        }

    @Test
    fun googleLoginEmitsLoadingThenNavigateWhenProfileExists() =
        runTest {
            val signInGate = CompletableDeferred<Unit>()
            val viewModel =
                onboardingViewModel(
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
            assertEquals("angela@example.com", state.email)
            assertEquals("Familia de Angela", state.familyName)
        }

    @Test
    fun googleLoginFailureShowsError() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(signInError = IllegalStateException("Missing config")),
                )

            viewModel.onEvent(OnboardingEvent.GoogleSignInClicked)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.isAuthenticated)
            assertEquals(OnboardingError.SignInFailed, state.error)
            assertTrue(state.errorDiagnostic.orEmpty().contains("Missing config"))
        }

    @Test
    fun googleLoginCancellationDoesNotBecomeSignInError() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway =
                        FakeAuthGateway(
                            signInError = CancellationException("Google sign-in cancelled"),
                        ),
                )

            viewModel.onEvent(OnboardingEvent.GoogleSignInClicked)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.errorDiagnostic)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun repeatedGoogleLoginDoesNotClearOrDuplicateActiveRequest() =
        runTest {
            val signInGate = CompletableDeferred<Unit>()
            val authGateway = FakeAuthGateway(signInSession = authSession(), signInGate = signInGate)
            val viewModel =
                onboardingViewModel(
                    authGateway = authGateway,
                    remoteUserProfileGateway = FakeRemoteUserProfileGateway(remoteProfile()),
                )

            viewModel.onEvent(OnboardingEvent.GoogleSignInClicked)
            runCurrent()
            viewModel.onEvent(OnboardingEvent.GoogleSignInClicked)
            runCurrent()

            assertEquals(1, authGateway.signInAttempts)
            assertTrue(viewModel.uiState.value.isLoading)
            signInGate.complete(Unit)
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun alreadyCancelledScopeDoesNotLeaveAuthenticationLoading() =
        runTest {
            val job = Job().also { it.cancel() }
            val authGateway = FakeAuthGateway(signInSession = authSession())
            val cancelledScope = CoroutineScope(StandardTestDispatcher(testScheduler) + job)
            val viewModel = onboardingViewModel(authGateway = authGateway, coroutineScope = cancelledScope)

            viewModel.onEvent(OnboardingEvent.GoogleSignInClicked)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(0, authGateway.signInAttempts)
        }

    @Test
    fun credentialRequestCancellationClearsLoadingWithoutError() =
        runTest {
            val viewModel = onboardingViewModel()

            viewModel.onEvent(OnboardingEvent.GoogleCredentialRequestStarted)
            assertTrue(viewModel.uiState.value.isLoading)
            viewModel.onEvent(OnboardingEvent.GoogleCredentialRequestCancelled)

            assertFalse(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.errorDiagnostic)
        }

    @Test
    fun googleLoginAutoCreatesProfileAndNavigates() =
        runTest {
            val signInGate = CompletableDeferred<Unit>()
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(signInSession = authSession(), signInGate = signInGate),
                    remoteUserProfileGateway = FakeRemoteUserProfileGateway(profile = null),
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
            assertEquals("Angela", state.displayName)
            assertEquals("angela@example.com", state.email)
            assertEquals("family-auto-1", state.familyId)
            assertEquals("Familia de Angela", state.familyName)
        }

    @Test
    fun googleLoginWithAutoCreationFailureShowsError() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(signInSession = authSession()),
                    remoteUserProfileGateway =
                        FakeRemoteUserProfileGateway(
                            profile = null,
                            ensureProfileError = IllegalStateException("Database error"),
                        ),
                )

            viewModel.onEvent(OnboardingEvent.GoogleSignInClicked)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.isAuthenticated)
            assertEquals(OnboardingError.ProfileCreationFailed, state.error)
            assertTrue(state.errorDiagnostic.orEmpty().contains("Database error"))
        }

    @Test
    fun googleIdTokenLoginEmitsLoadingThenNavigateWhenProfileExists() =
        runTest {
            val signInGate = CompletableDeferred<Unit>()
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(signInSession = authSession(), signInGate = signInGate),
                    remoteUserProfileGateway = FakeRemoteUserProfileGateway(remoteProfile()),
                )

            viewModel.effects.test {
                viewModel.onEvent(OnboardingEvent.GoogleIdTokenReceived("test-id-token"))
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
            assertEquals("angela@example.com", state.email)
            assertEquals("Familia de Angela", state.familyName)
        }

    @Test
    fun googleIdTokenLoginFailureShowsError() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(signInError = IllegalStateException("Invalid token")),
                )

            viewModel.onEvent(OnboardingEvent.GoogleIdTokenReceived("bad-token"))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertFalse(state.isAuthenticated)
            assertEquals(OnboardingError.SignInFailed, state.error)
            assertTrue(state.errorDiagnostic.orEmpty().contains("Invalid token"))
        }

    @Test
    fun googleIdTokenLoginCancellationDoesNotBecomeSignInError() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway =
                        FakeAuthGateway(
                            signInError = CancellationException("ID token sign-in cancelled"),
                        ),
                )

            viewModel.onEvent(OnboardingEvent.GoogleIdTokenReceived("test-id-token"))
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.errorDiagnostic)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun credentialFailureIsNormalizedAndCanBeRetried() =
        runTest {
            val authGateway = FakeAuthGateway(signInError = IllegalStateException("OAuth client secret"))
            val viewModel = onboardingViewModel(authGateway = authGateway)

            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()
            viewModel.onEvent(OnboardingEvent.GoogleSignInError("GOOGLE_CLIENT_ID missing"))

            assertEquals(OnboardingError.SignInFailed, viewModel.uiState.value.error)
            assertEquals("GOOGLE_CLIENT_ID missing", viewModel.uiState.value.errorDiagnostic)
            assertTrue(viewModel.uiState.value.canSubmitLogin)

            authGateway.signInError = null
            viewModel.onEvent(OnboardingEvent.GoogleSignInClicked)
            advanceUntilIdle()

            assertEquals(null, viewModel.uiState.value.error)
            assertEquals(null, viewModel.uiState.value.errorDiagnostic)
            assertTrue(viewModel.uiState.value.isAuthenticated)
            assertEquals(1, authGateway.signInAttempts)
        }

    @Test
    fun googleIdTokenLoginAutoCreatesProfileAndNavigates() =
        runTest {
            val signInGate = CompletableDeferred<Unit>()
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(signInSession = authSession(), signInGate = signInGate),
                    remoteUserProfileGateway = FakeRemoteUserProfileGateway(profile = null),
                )

            viewModel.effects.test {
                viewModel.onEvent(OnboardingEvent.GoogleIdTokenReceived("test-id-token"))
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
            assertEquals("Angela", state.displayName)
            assertEquals("angela@example.com", state.email)
            assertEquals("Familia de Angela", state.familyName)
        }

    @Test
    fun signOutReturnsToLoginAndEmitsEffect() =
        runTest {
            val familyScope = FakeActiveFamilyScopeGateway()
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(currentSession = authSession()),
                    familyScope = familyScope,
                )

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
            assertEquals(null, state.displayName)
            assertEquals(null, state.email)
            assertEquals(null, state.familyName)
            assertEquals(FamilyId("local-family"), familyScope.current().familyId)
        }

    @Test
    fun signOutCancellationDoesNotBecomeSignOutError() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway =
                        FakeAuthGateway(
                            currentSession = authSession(),
                            signOutError = CancellationException("Sign-out cancelled"),
                        ),
                )
            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            viewModel.onEvent(OnboardingEvent.SignOutClicked)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.errorDiagnostic)
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun accountDeletionRunsOnceCleansLocalDataAndReturnsToLogin() =
        runTest {
            val deletionGate = CompletableDeferred<Unit>()
            val authGateway = FakeAuthGateway(currentSession = authSession(), deleteAccountGate = deletionGate)
            val cleaner = FakeAccountLocalDataCleaner()
            val viewModel = onboardingViewModel(authGateway = authGateway, accountLocalDataCleaner = cleaner)
            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(OnboardingEvent.DeleteAccountConfirmed)
                viewModel.onEvent(OnboardingEvent.DeleteAccountConfirmed)
                runCurrent()

                assertTrue(viewModel.uiState.value.isDeletingAccount)
                assertEquals(1, authGateway.deleteAccountAttempts)
                deletionGate.complete(Unit)
                advanceUntilIdle()

                assertIs<OnboardingEffect.NavigateToLogin>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(listOf(FamilyId("family-1")), cleaner.clearedFamilies)
            assertFalse(viewModel.uiState.value.isAuthenticated)
            assertFalse(viewModel.uiState.value.isDeletingAccount)
        }

    @Test
    fun unconfirmedAccountDeletionReturnsToCleanLoginState() =
        runTest {
            val cleaner = FakeAccountLocalDataCleaner()
            val viewModel =
                onboardingViewModel(
                    authGateway =
                        FakeAuthGateway(
                            currentSession = authSession(),
                            deleteAccountError = IllegalStateException("RPC unavailable"),
                        ),
                    accountLocalDataCleaner = cleaner,
                )
            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(OnboardingEvent.DeleteAccountConfirmed)
                advanceUntilIdle()

                assertIs<OnboardingEffect.NavigateToLogin>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertFalse(viewModel.uiState.value.isAuthenticated)
            assertNull(viewModel.uiState.value.error)
            assertNull(viewModel.uiState.value.errorDiagnostic)
            assertFalse(viewModel.uiState.value.isDeletingAccount)
            assertEquals(listOf(FamilyId("family-1")), cleaner.clearedFamilies)
        }

    @Test
    fun accountDeletionCancellationDoesNotBecomeFailure() =
        runTest {
            val cleaner = FakeAccountLocalDataCleaner()
            val viewModel =
                onboardingViewModel(
                    authGateway =
                        FakeAuthGateway(
                            currentSession = authSession(),
                            deleteAccountError = CancellationException("Deletion cancelled"),
                        ),
                    accountLocalDataCleaner = cleaner,
                )
            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            viewModel.onEvent(OnboardingEvent.DeleteAccountConfirmed)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isAuthenticated)
            assertNull(viewModel.uiState.value.error)
            assertFalse(viewModel.uiState.value.isDeletingAccount)
            assertTrue(cleaner.clearedFamilies.isEmpty())
        }

    @Test
    fun committedAccountDeletionStillReturnsToLoginWhenLocalCleanupIsCancelled() =
        runTest {
            val viewModel =
                onboardingViewModel(
                    authGateway = FakeAuthGateway(currentSession = authSession()),
                    accountLocalDataCleaner =
                        FakeAccountLocalDataCleaner(
                            error = CancellationException("Local cleanup cancelled"),
                        ),
                )
            viewModel.onEvent(OnboardingEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(OnboardingEvent.DeleteAccountConfirmed)
                advanceUntilIdle()

                assertIs<OnboardingEffect.NavigateToLogin>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertFalse(viewModel.uiState.value.isAuthenticated)
            assertNull(viewModel.uiState.value.error)
        }

    private fun TestScope.onboardingViewModel(
        authGateway: AuthGateway = FakeAuthGateway(),
        remoteUserProfileGateway: RemoteUserProfileGateway = FakeRemoteUserProfileGateway(remoteProfile()),
        accountLocalDataCleaner: AccountLocalDataCleaner = FakeAccountLocalDataCleaner(),
        familyScope: ActiveFamilyScopeGateway = FakeActiveFamilyScopeGateway(),
        coroutineScope: CoroutineScope = this,
    ): OnboardingViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return OnboardingViewModel(
            authGateway = authGateway,
            remoteUserProfileGateway = remoteUserProfileGateway,
            accountLocalDataCleaner = accountLocalDataCleaner,
            familyScope = familyScope,
            dispatchers =
                TestDispatcherProvider(
                    io = dispatcher,
                    default = dispatcher,
                    main = dispatcher,
                ),
            coroutineScope = coroutineScope,
        )
    }

    private fun authSession(): AuthSession =
        AuthSession(
            accessToken = "access-token",
            user =
                AuthUser(
                    id = "user-1",
                    email = "angela@example.com",
                    displayName = "Angela",
                ),
        )

    private fun remoteProfile(): RemoteUserProfile =
        RemoteUserProfile(
            userId = UserId("user-1"),
            familyId = FamilyId("family-1"),
            familyName = "Familia de Angela",
            displayName = "Angela Remote",
            email = "angela@example.com",
        )
}

private class FakeActiveFamilyScopeGateway : ActiveFamilyScopeGateway {
    private var scope = ActiveFamilyScope(null, FamilyId("local-family"), 1)

    override fun activateAuthenticated(
        userId: UserId,
        familyId: FamilyId,
    ): ActiveFamilyScope = activate(userId, familyId)

    override fun activateLocal(): ActiveFamilyScope = activate(null, FamilyId("local-family"))

    override fun current(): ActiveFamilyScope = scope

    override fun requireCurrent(expected: ActiveFamilyScope) = check(expected == scope)

    private fun activate(
        userId: UserId?,
        familyId: FamilyId,
    ): ActiveFamilyScope {
        if (scope.userId == userId && scope.familyId == familyId) return scope
        return ActiveFamilyScope(userId, familyId, scope.generation + 1).also { scope = it }
    }
}

private class FakeAuthGateway(
    private val currentSession: AuthSession? = null,
    private val currentSessionError: Throwable? = null,
    private val signInSession: AuthSession =
        currentSession ?: AuthSession(
            accessToken = "access-token",
            user = AuthUser("user-1", "angela@example.com", "Angela"),
        ),
    var signInError: Throwable? = null,
    private val signInGate: CompletableDeferred<Unit>? = null,
    private val signOutError: Throwable? = null,
    private val deleteAccountError: Throwable? = null,
    private val deleteAccountGate: CompletableDeferred<Unit>? = null,
) : AuthGateway {
    var signInAttempts: Int = 0
    var deleteAccountAttempts: Int = 0

    override suspend fun currentSession(): AuthSession? {
        currentSessionError?.let { throw it }
        return currentSession
    }

    override suspend fun signInWithGoogle(): AuthSession {
        signInAttempts += 1
        signInGate?.await()
        signInError?.let { throw it }
        return signInSession
    }

    override suspend fun signInWithGoogle(idToken: String): AuthSession {
        signInAttempts += 1
        signInGate?.await()
        signInError?.let { throw it }
        return signInSession
    }

    override suspend fun signOut() {
        signOutError?.let { throw it }
    }

    override suspend fun deleteAccount() {
        deleteAccountAttempts += 1
        deleteAccountGate?.await()
        deleteAccountError?.let { throw it }
    }
}

private class FakeAccountLocalDataCleaner(
    private val error: Throwable? = null,
) : AccountLocalDataCleaner {
    val clearedFamilies = mutableListOf<FamilyId>()

    override suspend fun clear(familyId: FamilyId) {
        error?.let { throw it }
        clearedFamilies += familyId
    }
}

private class FakeRemoteUserProfileGateway(
    private val profile: RemoteUserProfile?,
    private val getProfileError: Throwable? = null,
    private val ensureProfileError: Throwable? = null,
) : RemoteUserProfileGateway {
    private val createdProfiles = mutableMapOf<UserId, RemoteUserProfile>()

    override suspend fun getProfileForUser(userId: UserId): RemoteUserProfile? {
        getProfileError?.let { throw it }
        return createdProfiles[userId] ?: profile
    }

    override suspend fun ensureProfile(
        displayName: String,
        email: String?,
    ): RemoteUserProfile {
        ensureProfileError?.let { throw it }
        val createdUserId = UserId("user-1")
        return RemoteUserProfile(
            userId = createdUserId,
            familyId = FamilyId("family-auto-1"),
            familyName = "Familia de $displayName",
            displayName = displayName,
            email = email,
        ).also { createdProfiles[createdUserId] = it }
    }
}
