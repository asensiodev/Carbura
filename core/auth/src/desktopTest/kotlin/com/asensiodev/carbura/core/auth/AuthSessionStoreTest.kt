package com.asensiodev.carbura.core.auth

import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.test.runTest
import java.io.File
import java.util.prefs.Preferences
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class AuthSessionStoreTest {
    @Test
    fun inMemoryStoreCopiesValuesAndClearsThem() =
        runTest {
            val store = InMemoryAuthSessionStore()
            val original = byteArrayOf(1, 2, 3)
            store.write(original)
            original.fill(9)

            val firstRead = requireNotNull(store.read())
            assertContentEquals(byteArrayOf(1, 2, 3), firstRead)
            firstRead.fill(8)
            assertContentEquals(byteArrayOf(1, 2, 3), store.read())

            store.clear()
            assertNull(store.read())
        }

    @Test
    fun unsupportedOperatingSystemFailsWithoutPlaintextFallback() {
        assertFailsWith<DesktopAuthException.SecureStorageUnavailable> {
            createDesktopCredentialVault("Linux")
        }
    }

    @Test
    fun sessionPersistenceTypesExposeNoFileOrPreferencesFields() {
        val forbiddenTypes = setOf(File::class.java, Preferences::class.java)
        val persistenceTypes =
            listOf(
                DesktopSessionManager::class.java,
                MacOsCredentialVault::class.java,
                WindowsCredentialVault::class.java,
            )

        assertTrue(
            persistenceTypes.flatMap { it.declaredFields.asList() }.none { field ->
                forbiddenTypes.any { forbidden -> forbidden.isAssignableFrom(field.type) }
            },
        )
    }

    @Test
    fun desktopSessionManagerNamespacesAndRotatesVaultEntries() =
        runTest {
            val vault = FakeCredentialVault()
            val manager = DesktopSessionManager(vault, "https://project-ref.supabase.co", "test")
            manager.saveSession(session("account-one", "refresh-one"))
            manager.saveSession(session("account-two", "refresh-two"))

            val restored = requireNotNull(manager.loadSession())
            assertTrue(vault.keys.all { it.startsWith("com.asensiodev.carbura.auth.project-ref.test.") })
            assertTrue(vault.keys.none { it.contains("account-one") })
            assertContentEquals("refresh-two".encodeToByteArray(), restored.refreshToken.encodeToByteArray())

            manager.deleteSession()
            assertTrue(vault.keys.isEmpty())
        }

    private fun session(
        account: String,
        refreshToken: String,
    ) = UserSession(
        accessToken = "access-$account",
        refreshToken = refreshToken,
        expiresIn = 3_600,
        tokenType = "bearer",
        user = UserInfo(aud = "authenticated", id = account),
    )

    private class FakeCredentialVault : DesktopCredentialVault {
        private val values = mutableMapOf<String, ByteArray>()
        val keys: Set<String> get() = values.keys

        override fun read(key: String): ByteArray? = values[key]?.copyOf()

        override fun write(
            key: String,
            value: ByteArray,
        ) {
            values.put(key, value.copyOf())?.fill(0)
        }

        override fun delete(key: String) {
            values.remove(key)?.fill(0)
        }
    }
}
