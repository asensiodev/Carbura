package com.asensiodev.carbura.desktop

import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.auth.AuthSession
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
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class DesktopAccount(
    val userId: UserId,
    val email: String?,
    val displayName: String,
    val familyId: FamilyId,
    val familyName: String?,
)

internal enum class DesktopFailureStage {
    Restoration,
    Authentication,
    Profile,
    Adoption,
    Sync,
    SignOut,
}

internal sealed interface DesktopStartupState {
    data object Restoring : DesktopStartupState

    data class LocalMode(
        val authError: String? = null,
    ) : DesktopStartupState

    data object Authenticating : DesktopStartupState

    data object ResolvingProfile : DesktopStartupState

    data class AwaitingImportDecision(
        val account: DesktopAccount,
        val snapshot: LocalDataSnapshot,
    ) : DesktopStartupState

    data class InitialSync(
        val account: DesktopAccount,
    ) : DesktopStartupState

    data class Authenticated(
        val account: DesktopAccount,
    ) : DesktopStartupState

    data class RecoverableFailure(
        val stage: DesktopFailureStage,
        val message: String,
        val account: DesktopAccount? = null,
    ) : DesktopStartupState
}

internal class DesktopAppController(
    private val configurationAvailable: Boolean,
    private val authGateway: () -> AuthGateway,
    private val profileGateway: () -> RemoteUserProfileGateway,
    private val adoptionGateway: () -> LocalDataAdoptionGateway,
    private val syncManager: () -> SyncManager,
    private val familyScope: ActiveFamilyScopeGateway,
    private val coroutineScope: CoroutineScope,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
    private val foregroundThrottleMillis: Long = 60_000L,
) {
    private val _state = MutableStateFlow<DesktopStartupState>(DesktopStartupState.Restoring)
    private val _syncStatus = MutableStateFlow(SyncStatus())
    private val _contentRevision = MutableStateFlow(0L)
    private val _excludedLocalData = MutableStateFlow<LocalDataCounts?>(null)
    private var operation: Job? = null
    private var syncStatusCollection: Job? = null
    private var lastForegroundSyncAt: Long? = null

    val state: StateFlow<DesktopStartupState> = _state.asStateFlow()
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    val contentRevision: StateFlow<Long> = _contentRevision.asStateFlow()
    val excludedLocalData: StateFlow<LocalDataCounts?> = _excludedLocalData.asStateFlow()

    fun start() {
        launchOperation {
            if (!configurationAvailable) {
                showLocalMode()
                return@launchOperation
            }
            _state.value = DesktopStartupState.Restoring
            try {
                val session = authGateway().currentSession()
                if (session == null) showLocalMode() else resolveSession(session)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                familyScope.activateLocal()
                _state.value =
                    DesktopStartupState.RecoverableFailure(
                        DesktopFailureStage.Restoration,
                        error.safeMessage("No se pudo restaurar la sesión."),
                    )
            }
        }
    }

    fun signIn() {
        if (!configurationAvailable) {
            showLocalMode("La conexión con Supabase no está configurada para esta compilación.")
            return
        }
        launchOperation {
            _state.value = DesktopStartupState.Authenticating
            try {
                val gateway = authGateway()
                resolveSession(gateway.currentSession() ?: gateway.signInWithGoogle())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                familyScope.activateLocal()
                _state.value =
                    DesktopStartupState.RecoverableFailure(
                        DesktopFailureStage.Authentication,
                        error.safeMessage("No se pudo iniciar sesión con Google."),
                    )
            }
        }
    }

    fun importAndMerge() = completeDecision(LocalDataDecision.Import)

    fun useAccountData() = completeDecision(LocalDataDecision.Exclude)

    fun cancelImportDecision() {
        adoptionGateway().cancel()
        leaveAuthenticatedMode()
    }

    fun retry() {
        when (val current = _state.value) {
            is DesktopStartupState.RecoverableFailure -> {
                val account = current.account
                if (account == null) start() else launchOperation { runInitialSync(account) }
            }
            else -> Unit
        }
    }

    fun enterLocalMode() {
        operation?.cancel()
        operation = null
        leaveAuthenticatedMode()
    }

    fun syncNow() {
        val account = activeAccount() ?: return
        launchOperation { performSync(account, reportFailure = true, initial = false) }
    }

    fun onForeground() {
        val now = currentTimeMillis()
        val previous = lastForegroundSyncAt
        if (activeAccount() == null || previous != null && now - previous < foregroundThrottleMillis) return
        lastForegroundSyncAt = now
        syncSilently()
    }

    fun onPeriodicTick() = syncSilently()

    fun signOut() {
        launchOperation {
            try {
                authGateway().signOut()
                showLocalMode()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                familyScope.activateLocal()
                _state.value =
                    DesktopStartupState.RecoverableFailure(
                        DesktopFailureStage.SignOut,
                        error.safeMessage("La sesión local se cerró, pero no se pudo confirmar la revocación remota."),
                    )
            }
        }
    }

    private suspend fun resolveSession(session: AuthSession) {
        _state.value = DesktopStartupState.ResolvingProfile
        try {
            val userId = UserId(session.user.id)
            val profiles = profileGateway()
            val profile =
                profiles.getProfileForUser(userId)
                    ?: profiles.ensureProfile(
                        displayName = session.user.displayName ?: session.user.email?.substringBefore('@') ?: "Usuario",
                        email = session.user.email,
                    )
            check(profile.userId == userId) { "El perfil autenticado no coincide con la sesión." }
            familyScope.activateAuthenticated(userId, profile.familyId)
            val account = profile.toDesktopAccount(session)
            val adoption = adoptionGateway()
            val snapshot = adoption.unresolvedSnapshot()
            if (snapshot == null) {
                runInitialSync(account)
                return
            }
            when (adoption.decision(userId, profile.familyId, snapshot.digest)) {
                LocalDataDecision.Import -> {
                    adoption.import(userId, profile.familyId, snapshot.digest)
                    _excludedLocalData.value = null
                    runInitialSync(account)
                }
                LocalDataDecision.Exclude -> {
                    adoption.exclude(userId, profile.familyId, snapshot.digest)
                    _excludedLocalData.value = snapshot.counts
                    runInitialSync(account)
                }
                null -> _state.value = DesktopStartupState.AwaitingImportDecision(account, snapshot)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            familyScope.activateLocal()
            _state.value =
                DesktopStartupState.RecoverableFailure(
                    DesktopFailureStage.Profile,
                    error.safeMessage("No se pudo resolver el perfil y la familia."),
                )
        }
    }

    private fun completeDecision(decision: LocalDataDecision) {
        val awaiting = _state.value as? DesktopStartupState.AwaitingImportDecision ?: return
        launchOperation {
            try {
                val adoption = adoptionGateway()
                when (decision) {
                    LocalDataDecision.Import -> {
                        adoption.import(awaiting.account.userId, awaiting.account.familyId, awaiting.snapshot.digest)
                        _excludedLocalData.value = null
                    }
                    LocalDataDecision.Exclude -> {
                        adoption.exclude(awaiting.account.userId, awaiting.account.familyId, awaiting.snapshot.digest)
                        _excludedLocalData.value = awaiting.snapshot.counts
                    }
                }
                familyScope.activateAuthenticated(awaiting.account.userId, awaiting.account.familyId)
                runInitialSync(awaiting.account)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                familyScope.activateLocal()
                _state.value =
                    DesktopStartupState.RecoverableFailure(
                        DesktopFailureStage.Adoption,
                        error.safeMessage("No se pudo aplicar la decisión sobre los datos locales."),
                    )
            }
        }
    }

    private suspend fun runInitialSync(account: DesktopAccount) {
        _state.value = DesktopStartupState.InitialSync(account)
        performSync(account, reportFailure = true, initial = true)
    }

    private suspend fun performSync(
        account: DesktopAccount,
        reportFailure: Boolean,
        initial: Boolean,
    ) {
        familyScope.activateAuthenticated(account.userId, account.familyId)
        val manager = observedSyncManager()
        val result = if (reportFailure) manager.syncNow() else manager.syncNowSilently()
        when (result) {
            is SyncResult.Success -> {
                _contentRevision.value += 1L
                _state.value = DesktopStartupState.Authenticated(account)
            }
            is SyncResult.Failure -> {
                if (initial || reportFailure) {
                    _state.value =
                        DesktopStartupState.RecoverableFailure(
                            DesktopFailureStage.Sync,
                            result.message.ifBlank { "No se pudo sincronizar. Los cambios locales siguen guardados." },
                            account,
                        )
                }
            }
        }
    }

    private fun syncSilently() {
        val account = activeAccount() ?: return
        coroutineScope.launch {
            try {
                performSync(account, reportFailure = false, initial = false)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // SyncManager retains the recoverable diagnostic and pending local changes.
            }
        }
    }

    private fun observedSyncManager(): SyncManager {
        val manager = syncManager()
        if (syncStatusCollection == null) {
            syncStatusCollection = coroutineScope.launch { manager.status.collect { _syncStatus.value = it } }
        }
        return manager
    }

    private fun activeAccount(): DesktopAccount? =
        when (val current = _state.value) {
            is DesktopStartupState.Authenticated -> current.account
            is DesktopStartupState.InitialSync -> current.account
            is DesktopStartupState.RecoverableFailure -> current.account
            else -> null
        }

    private fun showLocalMode(authError: String? = null) {
        familyScope.activateLocal()
        _excludedLocalData.value = null
        _state.value = DesktopStartupState.LocalMode(authError)
    }

    private fun leaveAuthenticatedMode() {
        launchOperation {
            try {
                if (configurationAvailable) authGateway().signOut()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                // Desktop auth clears local credentials even if server revocation fails.
            } finally {
                showLocalMode()
            }
        }
    }

    private fun launchOperation(block: suspend () -> Unit) {
        if (operation?.isActive == true) return
        operation = coroutineScope.launch { block() }
    }
}

private fun RemoteUserProfile.toDesktopAccount(session: AuthSession): DesktopAccount =
    DesktopAccount(
        userId = userId,
        email = email ?: session.user.email,
        displayName = displayName.ifBlank { session.user.displayName ?: session.user.email ?: "Usuario" },
        familyId = familyId,
        familyName = familyName,
    )

private fun Exception.safeMessage(fallback: String): String = fallback
