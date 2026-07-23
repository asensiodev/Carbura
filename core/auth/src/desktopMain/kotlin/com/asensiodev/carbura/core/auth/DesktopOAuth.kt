package com.asensiodev.carbura.core.auth

import com.asensiodev.carbura.core.domain.auth.AuthSession
import com.asensiodev.carbura.core.domain.auth.AuthUser
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.CodeVerifierCache
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.awt.Desktop
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal const val DESKTOP_REDIRECT_URI = "http://127.0.0.1:43821/auth/callback"
internal const val DESKTOP_CALLBACK_HOST = "127.0.0.1:43821"
internal const val DESKTOP_CALLBACK_PATH = "/auth/callback"
internal const val DESKTOP_CALLBACK_PORT = 43821

sealed class DesktopAuthException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class AttemptAlreadyActive : DesktopAuthException("A Desktop sign-in attempt is already active.")

    class CallbackUnavailable(
        cause: Throwable,
    ) : DesktopAuthException("The local authentication callback is unavailable.", cause)

    class InvalidCallback : DesktopAuthException("The authentication callback was rejected.")

    class OAuthRejected : DesktopAuthException("The provider rejected authentication.")

    class TimedOut : DesktopAuthException("Desktop authentication timed out.")

    class BrowserUnavailable(
        cause: Throwable? = null,
    ) : DesktopAuthException("The system browser is unavailable.", cause)

    class SecureStorageUnavailable(
        cause: Throwable? = null,
    ) : DesktopAuthException("Secure credential storage is unavailable.", cause)
}

fun interface SystemBrowser {
    fun open(uri: URI)
}

class OperatingSystemBrowser : SystemBrowser {
    override fun open(uri: URI) {
        try {
            check(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            Desktop.getDesktop().browse(uri)
        } catch (exception: Exception) {
            throw DesktopAuthException.BrowserUnavailable(exception)
        }
    }
}

internal class ReadyCodeVerifierCache : CodeVerifierCache {
    private val mutex = Mutex()
    private var verifier: String? = null
    private var acceptingWrites = false
    private var ready = CompletableDeferred<Unit>()

    suspend fun prepare() =
        mutex.withLock {
            verifier = null
            acceptingWrites = true
            ready = CompletableDeferred()
        }

    suspend fun awaitReady() {
        val signal = mutex.withLock { ready }
        signal.await()
    }

    override suspend fun saveCodeVerifier(codeVerifier: String) {
        mutex.withLock {
            if (acceptingWrites) {
                verifier = codeVerifier
                ready.complete(Unit)
            }
        }
    }

    override suspend fun loadCodeVerifier(): String? = mutex.withLock { verifier }

    override suspend fun deleteCodeVerifier() {
        mutex.withLock {
            acceptingWrites = false
            verifier = null
        }
    }
}

internal sealed interface OAuthCallback {
    data class Code(
        val value: String,
    ) : OAuthCallback

    data object Error : OAuthCallback
}

internal class LoopbackCallbackListener(
    private val port: Int = DESKTOP_CALLBACK_PORT,
    private val timeout: Duration = 5.minutes,
) {
    fun bind(): ServerSocket =
        try {
            ServerSocket().apply {
                reuseAddress = false
                bind(InetSocketAddress(InetAddress.getByName("127.0.0.1"), port), 1)
            }
        } catch (exception: IOException) {
            throw DesktopAuthException.CallbackUnavailable(exception)
        }

    suspend fun await(server: ServerSocket): OAuthCallback =
        try {
            withTimeout(timeout) {
                suspendCancellableCoroutine { continuation ->
                    continuation.invokeOnCancellation { server.close() }
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val callback = server.accept().use(::readCallback)
                            if (continuation.isActive) continuation.resume(callback)
                        } catch (exception: Exception) {
                            if (continuation.isActive) continuation.resumeWithException(exception)
                        }
                    }
                }
            }
        } catch (exception: kotlinx.coroutines.TimeoutCancellationException) {
            throw DesktopAuthException.TimedOut()
        }

    private fun readCallback(socket: Socket): OAuthCallback {
        socket.soTimeout = timeout.inWholeMilliseconds.coerceIn(1, Int.MAX_VALUE.toLong()).toInt()
        val requestBytes = ByteArray(MAX_REQUEST_BYTES)
        var size = 0
        try {
            val input = socket.getInputStream()
            var complete = false
            while (size < requestBytes.size && !complete) {
                val next = input.read()
                if (next < 0) {
                    complete = true
                } else {
                    requestBytes[size++] = next.toByte()
                    complete = size >= HEADER_END.size && requestBytes.endsWith(size, HEADER_END)
                }
            }
            if (!requestBytes.endsWith(size, HEADER_END)) return reject(socket)
            if ((0 until size).any { index -> !requestBytes[index].isAllowedHeaderByte() }) return reject(socket)
            val request = String(requestBytes, 0, size, StandardCharsets.US_ASCII)
            val result = parseRequest(request) ?: return reject(socket)
            respond(socket, 200, "Authentication complete. You may close this window.")
            return result
        } catch (exception: IOException) {
            throw DesktopAuthException.InvalidCallback()
        } finally {
            requestBytes.fill(0)
        }
    }

    @Suppress("CyclomaticComplexMethod", "ReturnCount")
    private fun parseRequest(request: String): OAuthCallback? {
        val lines = request.split("\r\n")
        val requestParts = lines.firstOrNull()?.split(' ') ?: return null
        if (requestParts.size != 3 || requestParts[0] != "GET" || requestParts[2] != "HTTP/1.1") return null
        val headerLines = lines.drop(1).dropLastWhile(String::isEmpty)
        if (headerLines.any { it.indexOf(':') <= 0 }) return null
        val hosts =
            headerLines.mapNotNull { line ->
                val separator = line.indexOf(':')
                if (separator <= 0 || !line.substring(0, separator).equals("Host", ignoreCase = true)) {
                    null
                } else {
                    line.substring(separator + 1).trim()
                }
            }
        if (hosts != listOf(DESKTOP_CALLBACK_HOST)) return null

        val target = requestParts[1]
        val queryIndex = target.indexOf('?')
        val path = if (queryIndex < 0) target else target.substring(0, queryIndex)
        if (path != DESKTOP_CALLBACK_PATH) return null
        val query = if (queryIndex < 0) "" else target.substring(queryIndex + 1)
        val parameters = parseQuery(query) ?: return null
        val codes = parameters["code"].orEmpty()
        val errors = parameters["error"].orEmpty()
        return when {
            codes.size == 1 && errors.isEmpty() && codes.single().isNotEmpty() -> OAuthCallback.Code(codes.single())
            errors.size == 1 && codes.isEmpty() && errors.single().isNotEmpty() -> OAuthCallback.Error
            else -> null
        }
    }

    private fun parseQuery(query: String): Map<String, List<String>>? {
        if (query.isEmpty()) return emptyMap()
        return try {
            query
                .split('&')
                .map { field ->
                    val separator = field.indexOf('=')
                    val key = if (separator < 0) field else field.substring(0, separator)
                    val value = if (separator < 0) "" else field.substring(separator + 1)
                    URLDecoder.decode(key, StandardCharsets.UTF_8) to URLDecoder.decode(value, StandardCharsets.UTF_8)
                }.groupBy({ it.first }, { it.second })
        } catch (exception: IllegalArgumentException) {
            null
        }
    }

    private fun reject(socket: Socket): Nothing {
        respond(socket, 400, "Authentication callback rejected.")
        throw DesktopAuthException.InvalidCallback()
    }

    private fun respond(
        socket: Socket,
        status: Int,
        body: String,
    ) {
        val bodyBytes = body.toByteArray(StandardCharsets.UTF_8)
        try {
            val response =
                "HTTP/1.1 $status ${if (status == 200) "OK" else "Bad Request"}\r\n" +
                    "Content-Type: text/plain; charset=utf-8\r\n" +
                    "Content-Length: ${bodyBytes.size}\r\n" +
                    "Connection: close\r\n\r\n"
            socket.getOutputStream().write(response.toByteArray(StandardCharsets.US_ASCII))
            socket.getOutputStream().write(bodyBytes)
            socket.getOutputStream().flush()
        } finally {
            bodyBytes.fill(0)
        }
    }

    private fun ByteArray.endsWith(
        size: Int,
        suffix: ByteArray,
    ): Boolean {
        if (size < suffix.size) return false
        return suffix.indices.all { index -> this[size - suffix.size + index] == suffix[index] }
    }

    private fun Byte.isAllowedHeaderByte(): Boolean {
        val value = toInt() and 0xff
        return value == '\t'.code || value == '\r'.code || value == '\n'.code || value in 0x20..0x7e
    }

    private companion object {
        const val MAX_REQUEST_BYTES = 8 * 1024
        val HEADER_END = byteArrayOf('\r'.code.toByte(), '\n'.code.toByte(), '\r'.code.toByte(), '\n'.code.toByte())
    }
}

internal interface DesktopOAuthTransaction {
    suspend fun prepare()

    fun authorizationUrl(): String

    suspend fun awaitReady()

    suspend fun exchange(code: String): AuthSession

    suspend fun clear()
}

internal class SupabaseDesktopOAuthTransaction(
    private val client: SupabaseClient,
    private val verifierCache: ReadyCodeVerifierCache,
) : DesktopOAuthTransaction {
    override suspend fun prepare() = verifierCache.prepare()

    override fun authorizationUrl(): String = client.auth.getOAuthUrl(Google, DESKTOP_REDIRECT_URI)

    override suspend fun awaitReady() = verifierCache.awaitReady()

    override suspend fun exchange(code: String): AuthSession {
        try {
            val session = client.auth.exchangeCodeForSession(code)
            val user = client.auth.retrieveUserForCurrentSession(updateSession = true)
            return AuthSession(
                accessToken = session.accessToken,
                user =
                    AuthUser(
                        id = user.id,
                        email = user.email,
                        displayName =
                            user.userMetadata
                                ?.get("full_name")
                                ?.jsonPrimitive
                                ?.contentOrNull,
                    ),
            )
        } catch (error: CancellationException) {
            withContext(NonCancellable) { client.auth.clearSession() }
            throw error
        } catch (error: Exception) {
            withContext(NonCancellable) { client.auth.clearSession() }
            throw error
        }
    }

    override suspend fun clear() = verifierCache.deleteCodeVerifier()
}

internal class DesktopOAuthCoordinator(
    private val transaction: DesktopOAuthTransaction,
    private val browser: SystemBrowser,
    private val listener: LoopbackCallbackListener = LoopbackCallbackListener(),
) {
    private val active = AtomicBoolean(false)

    @Suppress("RethrowCaughtException", "ThrowsCount")
    suspend fun authenticate(): AuthSession {
        if (!active.compareAndSet(false, true)) throw DesktopAuthException.AttemptAlreadyActive()
        var server: ServerSocket? = null
        try {
            server = listener.bind()
            transaction.prepare()
            val authorizationUrl = transaction.authorizationUrl()
            transaction.awaitReady()
            browser.open(URI(authorizationUrl))
            return when (val callback = listener.await(server)) {
                is OAuthCallback.Code -> transaction.exchange(callback.value)
                OAuthCallback.Error -> throw DesktopAuthException.OAuthRejected()
            }
        } catch (exception: CancellationException) {
            throw exception
        } finally {
            server?.close()
            try {
                withContext(NonCancellable) {
                    transaction.clear()
                }
            } finally {
                active.set(false)
            }
        }
    }
}
