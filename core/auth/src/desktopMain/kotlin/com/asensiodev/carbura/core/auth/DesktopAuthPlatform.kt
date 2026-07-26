package com.asensiodev.carbura.core.auth

import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.auth.AuthSession
import com.asensiodev.carbura.core.domain.auth.AuthUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

internal actual fun AuthConfig.configurePlatformAuth(settings: SupabaseSettings) {
    flowType = FlowType.PKCE
    alwaysAutoRefresh = true
    autoLoadFromStorage = false
    autoSaveToStorage = true
    enableLifecycleCallbacks = false
    codeVerifierCache = ReadyCodeVerifierCache()
    sessionManager =
        DesktopSessionManager(
            vault = createDesktopCredentialVault(),
            supabaseUrl = settings.url,
            environment = settings.environment,
        )
}

internal actual fun SupabaseClientBuilder.configurePlatformClient() {
    defaultLogLevel = LogLevel.NONE
}

internal actual fun createPlatformAuthGateway(client: SupabaseClient): AuthGateway {
    val verifierCache =
        client.auth.codeVerifierCache as? ReadyCodeVerifierCache
            ?: throw DesktopAuthException.SecureStorageUnavailable()
    val sessionManager =
        client.auth.sessionManager as? DesktopSessionManager
            ?: throw DesktopAuthException.SecureStorageUnavailable()
    return DesktopSupabaseAuthGateway(
        client = client,
        sessionManager = sessionManager,
        oauth =
            DesktopOAuthCoordinator(
                transaction = SupabaseDesktopOAuthTransaction(client, verifierCache),
                browser = OperatingSystemBrowser(),
            ),
    )
}

internal class DesktopSupabaseAuthGateway(
    private val client: SupabaseClient,
    private val sessionManager: DesktopSessionManager,
    private val oauth: DesktopOAuthCoordinator,
) : AuthGateway {
    private val restorationMutex = Mutex()
    private var restorationComplete = false

    override suspend fun currentSession(): AuthSession? {
        client.auth.awaitInitialization()
        restorationMutex.withLock {
            if (!restorationComplete) {
                if (client.auth.currentSessionOrNull() == null) client.auth.loadFromStorage(autoRefresh = false)
                if (sessionManager.restoredFromVault) {
                    try {
                        client.auth.refreshCurrentSession()
                        client.auth.retrieveUserForCurrentSession(updateSession = true)
                        sessionManager.markRestoredSessionValidated()
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: RestException) {
                        if (exception.statusCode in 400..499) client.auth.clearSession()
                    } catch (_: Exception) {
                        // Keep the vault-restored session available to route already cached local data offline.
                    }
                }
                restorationComplete = true
            }
        }
        return client.auth.currentSessionOrNull()?.toDomainSession()
    }

    override suspend fun signInWithGoogle(): AuthSession = oauth.authenticate()

    override suspend fun signInWithGoogle(idToken: String): AuthSession {
        client.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
        }
        return currentSession() ?: error("Google ID token sign-in completed without an active Supabase session.")
    }

    override suspend fun signOut() {
        withContext(NonCancellable) {
            try {
                client.auth.signOut(SignOutScope.LOCAL)
            } finally {
                client.auth.clearSession()
            }
        }
    }

    override suspend fun deleteAccount() {
        withContext(NonCancellable) {
            try {
                client.postgrest.rpc(function = "delete_current_user_account")
            } finally {
                client.auth.clearSession()
            }
        }
    }
}

private fun io.github.jan.supabase.auth.user.UserSession.toDomainSession(): AuthSession =
    AuthSession(
        accessToken = accessToken,
        user =
            AuthUser(
                id = user?.id.orEmpty(),
                email = user?.email,
                displayName =
                    user
                        ?.userMetadata
                        ?.get("full_name")
                        ?.jsonPrimitive
                        ?.contentOrNull,
            ),
    )
