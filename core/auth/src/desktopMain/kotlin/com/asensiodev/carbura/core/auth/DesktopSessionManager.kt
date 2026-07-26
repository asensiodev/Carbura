package com.asensiodev.carbura.core.auth

import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.net.URI
import java.nio.charset.StandardCharsets

internal class DesktopSessionManager(
    private val vault: DesktopCredentialVault,
    supabaseUrl: String,
    environment: String,
    private val json: Json = Json { encodeDefaults = true },
) : SessionManager {
    private val mutex = Mutex()
    private val namespace = "com.asensiodev.carbura.auth.${projectName(supabaseUrl)}.$environment"
    private val currentAccountKey = "$namespace.current-account"

    var restoredFromVault: Boolean = false
        private set

    fun markRestoredSessionValidated() {
        restoredFromVault = false
    }

    override suspend fun saveSession(session: UserSession) {
        val account =
            session.user?.id?.takeIf(String::isNotBlank)
                ?: throw DesktopAuthException.SecureStorageUnavailable()
        val encoded = json.encodeToString(session).toByteArray(StandardCharsets.UTF_8)
        val accountBytes = account.toByteArray(StandardCharsets.UTF_8)
        try {
            mutex.withLock {
                val previousBytes = vault.read(currentAccountKey)
                val previousAccount = previousBytes?.toString(StandardCharsets.UTF_8)
                try {
                    vault.write(sessionKey(account), encoded)
                    vault.write(currentAccountKey, accountBytes)
                    if (previousAccount != null && previousAccount != account) vault.delete(sessionKey(previousAccount))
                } finally {
                    previousBytes?.fill(0)
                }
            }
        } finally {
            encoded.fill(0)
            accountBytes.fill(0)
        }
    }

    override suspend fun loadSession(): UserSession? =
        mutex.withLock {
            val accountBytes = vault.read(currentAccountKey) ?: return@withLock null
            try {
                val account = accountBytes.toString(StandardCharsets.UTF_8)
                val sessionBytes = vault.read(sessionKey(account)) ?: return@withLock null
                try {
                    val session = json.decodeFromString<UserSession>(sessionBytes.toString(StandardCharsets.UTF_8))
                    restoredFromVault = true
                    session
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    throw DesktopAuthException.SecureStorageUnavailable(exception)
                } finally {
                    sessionBytes.fill(0)
                }
            } finally {
                accountBytes.fill(0)
            }
        }

    override suspend fun deleteSession() {
        mutex.withLock {
            val accountBytes = vault.read(currentAccountKey)
            try {
                accountBytes?.toString(StandardCharsets.UTF_8)?.let { vault.delete(sessionKey(it)) }
                vault.delete(currentAccountKey)
                restoredFromVault = false
            } finally {
                accountBytes?.fill(0)
            }
        }
    }

    private fun sessionKey(account: String): String = "$namespace.account.$account.session"

    private companion object {
        fun projectName(url: String): String =
            URI(url).host?.substringBefore('.')?.takeIf(String::isNotBlank)
                ?: throw DesktopAuthException.SecureStorageUnavailable()
    }
}
