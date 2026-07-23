package com.asensiodev.carbura.core.auth

import com.asensiodev.carbura.core.domain.auth.AuthSession
import com.asensiodev.carbura.core.domain.auth.AuthUser
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import kotlin.concurrent.thread
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DesktopOAuthTest {
    private var openServer: ServerSocket? = null

    @AfterTest
    fun closeServer() {
        openServer?.close()
        openServer = null
    }

    @Test
    fun validCallbackAcceptsOneCode() =
        runBlocking {
            val callback = receive("GET /auth/callback?code=one-time-code HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n")

            assertEquals(OAuthCallback.Code("one-time-code"), callback)
        }

    @Test
    fun providerErrorIsAcceptedWithoutExposingItsValue() =
        runBlocking {
            val callback = receive("GET /auth/callback?error=access_denied HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n")

            assertEquals(OAuthCallback.Error, callback)
        }

    @Test
    fun malformedCallbacksAreRejected() =
        runBlocking {
            val requests =
                listOf(
                    "POST /auth/callback?code=a HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n",
                    "GET /wrong?code=a HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n",
                    "GET /auth/callback?code=a HTTP/1.1\r\nHost: localhost:$DESKTOP_CALLBACK_PORT\r\n\r\n",
                    "GET /auth/callback?code=a&code=b HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n",
                    "GET /auth/callback?error=a&error=b HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n",
                    "GET /auth/callback?code=a&error=b HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n",
                    "GET /auth/callback HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n",
                    "GET /auth/callback?code=a HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n",
                    "GET /auth/callback?code=%ZZ HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n",
                    "GET /auth/callback?code=a HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n",
                )

            requests.forEach { request ->
                assertFailsWith<DesktopAuthException.InvalidCallback> { receive(request) }
            }
        }

    @Test
    fun oversizedHeadersAreRejected() =
        runBlocking {
            val request =
                "GET /auth/callback?code=a HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\nX-Fill: ${"a".repeat(8192)}\r\n\r\n"

            assertFailsWith<DesktopAuthException.InvalidCallback> { receive(request) }
            Unit
        }

    @Test
    fun coordinatorWaitsForVerifierBeforeOpeningBrowserAndExchangesOnce() =
        runBlocking {
            val transaction = FakeTransaction()
            val browser = CallbackBrowser(transaction, validRequest())
            val coordinator = DesktopOAuthCoordinator(transaction, browser)

            val session = coordinator.authenticate()

            assertEquals("user-id", session.user.id)
            assertTrue(browser.verifierWasReady)
            assertEquals(listOf("one-time-code"), transaction.exchangedCodes)
            assertEquals(1, transaction.clearCount)
        }

    @Test
    fun supabaseAuthorizationUsesPkceS256AndMemoryOnlyVerifier() {
        runBlocking {
            val cache = ReadyCodeVerifierCache()
            val client =
                createSupabaseClient("https://project.supabase.co", "public-anon-key") {
                    install(Auth) {
                        flowType = FlowType.PKCE
                        codeVerifierCache = cache
                        autoLoadFromStorage = false
                        autoSaveToStorage = false
                    }
                }
            try {
                val transaction = SupabaseDesktopOAuthTransaction(client, cache)
                transaction.prepare()
                val authorizationUri = URI(transaction.authorizationUrl())
                transaction.awaitReady()

                val query =
                    authorizationUri.rawQuery.split('&').associate { field ->
                        field.substringBefore('=') to field.substringAfter('=', "")
                    }
                assertEquals("s256", query["code_challenge_method"]?.lowercase())
                val verifier = assertNotNull(cache.loadCodeVerifier())
                val expectedChallenge =
                    Base64
                        .getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(StandardCharsets.US_ASCII)))
                assertEquals(expectedChallenge, query["code_challenge"])
                cache.deleteCodeVerifier()
                assertEquals(null, cache.loadCodeVerifier())
            } finally {
                client.close()
                cache.deleteCodeVerifier()
            }
        }
    }

    @Test
    fun callbackCannotBeReplayedAfterSuccessfulAttempt() =
        runBlocking {
            val transaction = FakeTransaction()
            val coordinator = DesktopOAuthCoordinator(transaction, CallbackBrowser(transaction, validRequest()))
            coordinator.authenticate()

            assertFailsWith<Exception> { send(validRequest()) }
            assertEquals(1, transaction.exchangedCodes.size)
        }

    @Test
    fun concurrentAttemptIsRejected() =
        runBlocking {
            val readyGate = CompletableDeferred<Unit>()
            val transaction = FakeTransaction(readyGate)
            val coordinator = DesktopOAuthCoordinator(transaction, CallbackBrowser(transaction, validRequest()))
            val first = async(Dispatchers.Default) { coordinator.authenticate() }
            transaction.prepared.await()

            assertFailsWith<DesktopAuthException.AttemptAlreadyActive> { coordinator.authenticate() }

            readyGate.complete(Unit)
            first.await()
            assertEquals(1, transaction.exchangedCodes.size)
        }

    @Test
    fun timeoutClosesListener() =
        runBlocking {
            val listener = LoopbackCallbackListener(timeout = 40.milliseconds)
            val server = listener.bind().also { openServer = it }

            assertFailsWith<DesktopAuthException.TimedOut> { listener.await(server) }
            assertTrue(server.isClosed)
        }

    @Test
    fun cancellationClosesListenerAndClearsVerifier() =
        runBlocking {
            val transaction = FakeTransaction()
            val browserOpened = CompletableDeferred<Unit>()
            val coordinator =
                DesktopOAuthCoordinator(
                    transaction,
                    SystemBrowser { browserOpened.complete(Unit) },
                )
            val attempt = async(Dispatchers.Default) { coordinator.authenticate() }
            browserOpened.await()

            attempt.cancelAndJoin()

            assertEquals(1, transaction.clearCount)
            ServerSocket().use { probe ->
                probe.bind(InetSocketAddress(InetAddress.getByName("127.0.0.1"), DESKTOP_CALLBACK_PORT))
                assertEquals("127.0.0.1", probe.inetAddress.hostAddress)
            }
        }

    @Test
    fun occupiedPortFailsBeforeBrowserOrPkceStarts() =
        runBlocking {
            openServer =
                ServerSocket().apply {
                    bind(InetSocketAddress(InetAddress.getByName("127.0.0.1"), DESKTOP_CALLBACK_PORT))
                }
            val transaction = FakeTransaction()
            var browserOpened = false
            val coordinator = DesktopOAuthCoordinator(transaction, SystemBrowser { browserOpened = true })

            assertFailsWith<DesktopAuthException.CallbackUnavailable> { coordinator.authenticate() }
            assertFalse(browserOpened)
            assertFalse(transaction.prepared.isCompleted)
            assertEquals(1, transaction.clearCount)
        }

    @Test
    fun listenerBindsOnlyIpv4Loopback() {
        val server = LoopbackCallbackListener().bind().also { openServer = it }

        assertEquals("127.0.0.1", server.inetAddress.hostAddress)
        assertFalse(server.inetAddress.isAnyLocalAddress)
    }

    private suspend fun receive(request: String): OAuthCallback =
        coroutineScope {
            val listener = LoopbackCallbackListener(timeout = 2.seconds)
            val server = listener.bind().also { openServer = it }
            try {
                val callback = async(Dispatchers.Default) { listener.await(server) }
                send(request)
                callback.await()
            } finally {
                server.close()
                openServer = null
            }
        }

    private fun validRequest() = "GET /auth/callback?code=one-time-code HTTP/1.1\r\nHost: $DESKTOP_CALLBACK_HOST\r\n\r\n"

    private fun send(request: String): String =
        Socket().use { socket ->
            socket.connect(InetSocketAddress("127.0.0.1", DESKTOP_CALLBACK_PORT), 1_000)
            socket.getOutputStream().write(request.toByteArray(StandardCharsets.US_ASCII))
            socket.getOutputStream().flush()
            socket.shutdownOutput()
            socket.getInputStream().readBytes().toString(StandardCharsets.US_ASCII)
        }

    private inner class CallbackBrowser(
        private val transaction: FakeTransaction,
        private val request: String,
    ) : SystemBrowser {
        var verifierWasReady = false

        override fun open(uri: URI) {
            assertEquals("https", uri.scheme)
            verifierWasReady = transaction.ready
            thread(name = "oauth-test-callback") { send(request) }
        }
    }

    private class FakeTransaction(
        private val readyGate: CompletableDeferred<Unit>? = null,
    ) : DesktopOAuthTransaction {
        val prepared = CompletableDeferred<Unit>()
        val exchangedCodes = mutableListOf<String>()
        var ready = false
        var clearCount = 0

        override suspend fun prepare() {
            prepared.complete(Unit)
        }

        override fun authorizationUrl() = "https://example.test/authorize"

        override suspend fun awaitReady() {
            readyGate?.await()
            ready = true
        }

        override suspend fun exchange(code: String): AuthSession {
            exchangedCodes += code
            return AuthSession("access-token", AuthUser("user-id", "user@example.test", "User"))
        }

        override suspend fun clear() {
            clearCount += 1
        }
    }
}
