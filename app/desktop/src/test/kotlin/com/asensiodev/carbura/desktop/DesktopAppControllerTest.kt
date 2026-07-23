package com.asensiodev.carbura.desktop

import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.auth.AuthSession
import com.asensiodev.carbura.core.domain.auth.AuthUser
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway
import com.asensiodev.carbura.core.domain.sync.LocalDataAdoptionGateway
import com.asensiodev.carbura.core.domain.sync.LocalDataCounts
import com.asensiodev.carbura.core.domain.sync.LocalDataDecision
import com.asensiodev.carbura.core.domain.sync.LocalDataSnapshot
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.sync.SyncResult
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import com.asensiodev.carbura.core.domain.user.RemoteUserProfile
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopAppControllerTest {
    @Test
    fun missingPublicConfigurationStartsInLocalModeWithoutResolvingAuth() =
        runTest {
            val fixture = Fixture(backgroundScope, configurationAvailable = false)

            fixture.controller.start()
            runCurrent()

            assertIs<DesktopStartupState.LocalMode>(fixture.controller.state.value)
            assertEquals(0, fixture.auth.currentSessionCalls)
            assertEquals(FamilyId("local-family"), fixture.scope.current().familyId)
        }

    @Test
    fun restoredSessionResolvesExistingFamilyBeforeInitialSync() =
        runTest {
            val fixture = Fixture(backgroundScope)
            fixture.auth.session = SESSION

            fixture.controller.start()
            runCurrent()

            val authenticated = assertIs<DesktopStartupState.Authenticated>(fixture.controller.state.value)
            assertEquals(FAMILY, authenticated.account.familyId)
            assertEquals(1, fixture.sync.calls)
            assertEquals(FAMILY, fixture.scope.current().familyId)
        }

    @Test
    fun unresolvedLocalDataBlocksSyncUntilExplicitExclusion() =
        runTest {
            val fixture = Fixture(backgroundScope)
            fixture.auth.session = SESSION
            fixture.adoption.snapshot = SNAPSHOT

            fixture.controller.start()
            runCurrent()

            assertIs<DesktopStartupState.AwaitingImportDecision>(fixture.controller.state.value)
            assertEquals(0, fixture.sync.calls)

            fixture.controller.useAccountData()
            runCurrent()

            assertEquals(LocalDataDecision.Exclude, fixture.adoption.savedDecision)
            assertEquals(SNAPSHOT.counts, fixture.controller.excludedLocalData.value)
            assertEquals(1, fixture.sync.calls)
            assertIs<DesktopStartupState.Authenticated>(fixture.controller.state.value)
        }

    @Test
    fun failedInitialSyncKeepsAuthenticatedAccountAvailableForRetry() =
        runTest {
            val fixture = Fixture(backgroundScope)
            fixture.auth.session = SESSION
            fixture.sync.result = SyncResult.Failure("Sin conexión")

            fixture.controller.start()
            runCurrent()

            val failure = assertIs<DesktopStartupState.RecoverableFailure>(fixture.controller.state.value)
            assertEquals(DesktopFailureStage.Sync, failure.stage)
            assertEquals(FAMILY, failure.account?.familyId)
        }

    @Test
    fun authenticationFailuresUseFixedMessageWithoutLeakingSecrets() =
        runTest {
            val fixture = Fixture(backgroundScope)
            fixture.auth.currentSessionError = IllegalStateException("refresh-token=do-not-display")

            fixture.controller.start()
            runCurrent()

            val failure = assertIs<DesktopStartupState.RecoverableFailure>(fixture.controller.state.value)
            assertEquals("No se pudo restaurar la sesión.", failure.message)
        }

    @Test
    fun cancellingImportSignsOutBeforeReturningToLocalMode() =
        runTest {
            val fixture = Fixture(backgroundScope)
            fixture.auth.session = SESSION
            fixture.adoption.snapshot = SNAPSHOT
            fixture.controller.start()
            runCurrent()

            fixture.controller.cancelImportDecision()
            runCurrent()

            assertIs<DesktopStartupState.LocalMode>(fixture.controller.state.value)
            assertEquals(1, fixture.auth.signOutCalls)
            assertEquals(0, fixture.sync.calls)
            assertEquals(FamilyId("local-family"), fixture.scope.current().familyId)
        }

    private class Fixture(
        scope: kotlinx.coroutines.CoroutineScope,
        configurationAvailable: Boolean = true,
    ) {
        val auth = FakeAuthGateway()
        val profile = FakeProfileGateway()
        val adoption = FakeAdoptionGateway()
        val sync = FakeSyncManager()
        val scope = FakeFamilyScopeGateway()
        val controller =
            DesktopAppController(
                configurationAvailable = configurationAvailable,
                authGateway = { auth },
                profileGateway = { profile },
                adoptionGateway = { adoption },
                syncManager = { sync },
                familyScope = this.scope,
                coroutineScope = scope,
            )
    }

    private class FakeAuthGateway : AuthGateway {
        var session: AuthSession? = null
        var currentSessionError: Throwable? = null
        var currentSessionCalls = 0
        var signOutCalls = 0

        override suspend fun currentSession(): AuthSession? {
            currentSessionCalls += 1
            currentSessionError?.let { throw it }
            return session
        }

        override suspend fun signInWithGoogle(): AuthSession = requireNotNull(session)

        override suspend fun signInWithGoogle(idToken: String): AuthSession = requireNotNull(session)

        override suspend fun signOut() {
            signOutCalls += 1
            session = null
        }

        override suspend fun deleteAccount() = Unit
    }

    private class FakeProfileGateway : RemoteUserProfileGateway {
        override suspend fun getProfileForUser(userId: UserId): RemoteUserProfile = PROFILE

        override suspend fun ensureProfile(
            displayName: String,
            email: String?,
        ): RemoteUserProfile = PROFILE
    }

    private class FakeAdoptionGateway : LocalDataAdoptionGateway {
        var snapshot: LocalDataSnapshot? = null
        var savedDecision: LocalDataDecision? = null

        override fun unresolvedSnapshot(): LocalDataSnapshot? = snapshot

        override fun decision(
            userId: UserId,
            familyId: FamilyId,
            snapshotDigest: String,
        ): LocalDataDecision? = savedDecision

        override suspend fun import(
            userId: UserId,
            familyId: FamilyId,
            approvedSnapshotDigest: String,
        ) {
            savedDecision = LocalDataDecision.Import
            snapshot = null
        }

        override suspend fun exclude(
            userId: UserId,
            familyId: FamilyId,
            approvedSnapshotDigest: String,
        ) {
            savedDecision = LocalDataDecision.Exclude
        }
    }

    private class FakeSyncManager : SyncManager {
        override val status = MutableStateFlow(SyncStatus())
        var calls = 0
        var result: SyncResult = SyncResult.Success(1)

        override suspend fun syncNow(): SyncResult {
            calls += 1
            return result
        }

        override suspend fun syncNowSilently(): SyncResult = syncNow()

        override fun acknowledgeFailure(failureId: Long) = Unit
    }

    private class FakeFamilyScopeGateway : ActiveFamilyScopeGateway {
        private var active = ActiveFamilyScope(null, FamilyId("local-family"), 1)

        override fun activateAuthenticated(
            userId: UserId,
            familyId: FamilyId,
        ): ActiveFamilyScope = activate(userId, familyId)

        override fun activateLocal(): ActiveFamilyScope = activate(null, FamilyId("local-family"))

        override fun current(): ActiveFamilyScope = active

        override fun requireCurrent(expected: ActiveFamilyScope) = check(expected == active)

        private fun activate(
            userId: UserId?,
            familyId: FamilyId,
        ): ActiveFamilyScope {
            if (active.userId == userId && active.familyId == familyId) return active
            return ActiveFamilyScope(userId, familyId, active.generation + 1).also { active = it }
        }
    }

    private companion object {
        val USER = UserId("user")
        val FAMILY = FamilyId("family")
        val SESSION = AuthSession("token", AuthUser(USER.value, "user@example.com", "Usuario"))
        val PROFILE = RemoteUserProfile(USER, FAMILY, "Familia", "Usuario", "user@example.com")
        val SNAPSHOT = LocalDataSnapshot("digest", LocalDataCounts(1, 2, 3))
    }
}
