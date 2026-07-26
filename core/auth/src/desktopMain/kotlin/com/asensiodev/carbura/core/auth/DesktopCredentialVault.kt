@file:Suppress("ktlint:standard:function-naming", "ktlint:standard:property-naming")

package com.asensiodev.carbura.core.auth

import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.WString
import com.sun.jna.platform.win32.WinBase.FILETIME
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import com.sun.jna.win32.StdCallLibrary
import java.nio.charset.StandardCharsets
import java.util.Locale

internal interface DesktopCredentialVault {
    fun read(key: String): ByteArray?

    fun write(
        key: String,
        value: ByteArray,
    )

    fun delete(key: String)
}

internal fun createDesktopCredentialVault(osName: String = System.getProperty("os.name").orEmpty()): DesktopCredentialVault =
    when {
        osName.lowercase(Locale.ROOT).contains("mac") -> MacOsCredentialVault()
        osName.lowercase(Locale.ROOT).contains("win") -> WindowsCredentialVault()
        else -> throw DesktopAuthException.SecureStorageUnavailable()
    }

internal class MacOsCredentialVault(
    private val security: MacSecurityLibrary = Native.load("Security", MacSecurityLibrary::class.java),
    private val coreFoundation: CoreFoundationLibrary = Native.load("CoreFoundation", CoreFoundationLibrary::class.java),
) : DesktopCredentialVault {
    override fun read(key: String): ByteArray? =
        withKey(key) { keyMemory, keySize ->
            val passwordLength = IntByReference()
            val passwordData = PointerByReference()
            val status =
                security.SecKeychainFindGenericPassword(
                    null,
                    keySize,
                    keyMemory,
                    ACCOUNT_BYTES.size,
                    ACCOUNT_MEMORY,
                    passwordLength,
                    passwordData,
                    null,
                )
            if (status == ITEM_NOT_FOUND) return@withKey null
            checkStatus(status)
            val pointer = passwordData.value ?: return@withKey ByteArray(0)
            try {
                pointer.getByteArray(0, passwordLength.value)
            } finally {
                security.SecKeychainItemFreeContent(null, pointer)
            }
        }

    override fun write(
        key: String,
        value: ByteArray,
    ) {
        withKey(key) { keyMemory, keySize ->
            val item = PointerByReference()
            val status =
                security.SecKeychainFindGenericPassword(
                    null,
                    keySize,
                    keyMemory,
                    ACCOUNT_BYTES.size,
                    ACCOUNT_MEMORY,
                    null,
                    null,
                    item,
                )
            withSecret(value) { secret ->
                if (status == ITEM_NOT_FOUND) {
                    checkStatus(
                        security.SecKeychainAddGenericPassword(
                            null,
                            keySize,
                            keyMemory,
                            ACCOUNT_BYTES.size,
                            ACCOUNT_MEMORY,
                            value.size,
                            secret,
                            null,
                        ),
                    )
                } else {
                    checkStatus(status)
                    val itemPointer = item.value ?: throw DesktopAuthException.SecureStorageUnavailable()
                    try {
                        checkStatus(security.SecKeychainItemModifyAttributesAndData(itemPointer, null, value.size, secret))
                    } finally {
                        coreFoundation.CFRelease(itemPointer)
                    }
                }
            }
        }
    }

    override fun delete(key: String) {
        withKey(key) { keyMemory, keySize ->
            val item = PointerByReference()
            val status =
                security.SecKeychainFindGenericPassword(
                    null,
                    keySize,
                    keyMemory,
                    ACCOUNT_BYTES.size,
                    ACCOUNT_MEMORY,
                    null,
                    null,
                    item,
                )
            if (status == ITEM_NOT_FOUND) return@withKey
            checkStatus(status)
            val itemPointer = item.value ?: throw DesktopAuthException.SecureStorageUnavailable()
            try {
                checkStatus(security.SecKeychainItemDelete(itemPointer))
            } finally {
                coreFoundation.CFRelease(itemPointer)
            }
        }
    }

    private fun <T> withKey(
        key: String,
        block: (Memory, Int) -> T,
    ): T {
        val bytes = key.toByteArray(StandardCharsets.UTF_8)
        val memory = Memory(bytes.size.toLong().coerceAtLeast(1))
        return try {
            if (bytes.isNotEmpty()) memory.write(0, bytes, 0, bytes.size)
            block(memory, bytes.size)
        } finally {
            bytes.fill(0)
            memory.clear()
            memory.close()
        }
    }

    private fun <T> withSecret(
        value: ByteArray,
        block: (Memory) -> T,
    ): T {
        val memory = Memory(value.size.toLong().coerceAtLeast(1))
        return try {
            if (value.isNotEmpty()) memory.write(0, value, 0, value.size)
            block(memory)
        } finally {
            memory.clear()
            memory.close()
        }
    }

    private fun checkStatus(status: Int) {
        if (status != SUCCESS) throw DesktopAuthException.SecureStorageUnavailable()
    }

    private companion object {
        const val SUCCESS = 0
        const val ITEM_NOT_FOUND = -25300
        val ACCOUNT_BYTES = "carbura-desktop".toByteArray(StandardCharsets.UTF_8)
        val ACCOUNT_MEMORY =
            Memory(ACCOUNT_BYTES.size.toLong()).apply {
                write(0, ACCOUNT_BYTES, 0, ACCOUNT_BYTES.size)
            }
    }
}

internal interface MacSecurityLibrary : Library {
    @Suppress("LongParameterList", "FunctionNaming")
    fun SecKeychainFindGenericPassword(
        keychainOrArray: Pointer?,
        serviceNameLength: Int,
        serviceName: Pointer,
        accountNameLength: Int,
        accountName: Pointer,
        passwordLength: IntByReference?,
        passwordData: PointerByReference?,
        itemRef: PointerByReference?,
    ): Int

    @Suppress("LongParameterList", "FunctionNaming")
    fun SecKeychainAddGenericPassword(
        keychain: Pointer?,
        serviceNameLength: Int,
        serviceName: Pointer,
        accountNameLength: Int,
        accountName: Pointer,
        passwordLength: Int,
        passwordData: Pointer,
        itemRef: PointerByReference?,
    ): Int

    @Suppress("FunctionNaming")
    fun SecKeychainItemModifyAttributesAndData(
        itemRef: Pointer,
        attrList: Pointer?,
        length: Int,
        data: Pointer,
    ): Int

    @Suppress("FunctionNaming")
    fun SecKeychainItemDelete(itemRef: Pointer): Int

    @Suppress("FunctionNaming")
    fun SecKeychainItemFreeContent(
        attrList: Pointer?,
        data: Pointer?,
    ): Int
}

internal interface CoreFoundationLibrary : Library {
    @Suppress("FunctionNaming")
    fun CFRelease(value: Pointer)
}

internal class WindowsCredentialVault(
    private val credentials: WindowsCredentialLibrary = Native.load("Advapi32", WindowsCredentialLibrary::class.java),
) : DesktopCredentialVault {
    override fun read(key: String): ByteArray? {
        val reference = PointerByReference()
        if (!credentials.CredReadW(WString(key), CREDENTIAL_TYPE_GENERIC, 0, reference)) {
            if (Native.getLastError() == ERROR_NOT_FOUND) return null
            throw DesktopAuthException.SecureStorageUnavailable()
        }
        val pointer = reference.value ?: throw DesktopAuthException.SecureStorageUnavailable()
        return try {
            val credential = WindowsCredential(pointer).apply { read() }
            credential.CredentialBlob?.getByteArray(0, credential.CredentialBlobSize) ?: ByteArray(0)
        } finally {
            credentials.CredFree(pointer)
        }
    }

    override fun write(
        key: String,
        value: ByteArray,
    ) {
        val secret = Memory(value.size.toLong().coerceAtLeast(1))
        try {
            if (value.isNotEmpty()) secret.write(0, value, 0, value.size)
            val credential =
                WindowsCredential().apply {
                    Type = CREDENTIAL_TYPE_GENERIC
                    TargetName = WString(key)
                    CredentialBlobSize = value.size
                    CredentialBlob = secret
                    Persist = CREDENTIAL_PERSIST_LOCAL_MACHINE
                    UserName = WString("carbura-desktop")
                    write()
                }
            if (!credentials.CredWriteW(credential, 0)) throw DesktopAuthException.SecureStorageUnavailable()
        } finally {
            secret.clear()
            secret.close()
        }
    }

    override fun delete(key: String) {
        if (!credentials.CredDeleteW(WString(key), CREDENTIAL_TYPE_GENERIC, 0) && Native.getLastError() != ERROR_NOT_FOUND) {
            throw DesktopAuthException.SecureStorageUnavailable()
        }
    }

    private companion object {
        const val CREDENTIAL_TYPE_GENERIC = 1
        const val CREDENTIAL_PERSIST_LOCAL_MACHINE = 2
        const val ERROR_NOT_FOUND = 1168
    }
}

@Structure.FieldOrder(
    "Flags",
    "Type",
    "TargetName",
    "Comment",
    "LastWritten",
    "CredentialBlobSize",
    "CredentialBlob",
    "Persist",
    "AttributeCount",
    "Attributes",
    "TargetAlias",
    "UserName",
)
@Suppress("VariableNaming")
internal open class WindowsCredential(
    pointer: Pointer? = null,
) : Structure(pointer) {
    @JvmField var Flags: Int = 0

    @JvmField var Type: Int = 0

    @JvmField var TargetName: WString? = null

    @JvmField var Comment: WString? = null

    @JvmField var LastWritten: FILETIME = FILETIME()

    @JvmField var CredentialBlobSize: Int = 0

    @JvmField var CredentialBlob: Pointer? = null

    @JvmField var Persist: Int = 0

    @JvmField var AttributeCount: Int = 0

    @JvmField var Attributes: Pointer? = null

    @JvmField var TargetAlias: WString? = null

    @JvmField var UserName: WString? = null
}

internal interface WindowsCredentialLibrary : StdCallLibrary {
    @Suppress("FunctionNaming")
    fun CredWriteW(
        credential: WindowsCredential,
        flags: Int,
    ): Boolean

    @Suppress("FunctionNaming")
    fun CredReadW(
        targetName: WString,
        type: Int,
        flags: Int,
        credential: PointerByReference,
    ): Boolean

    @Suppress("FunctionNaming")
    fun CredDeleteW(
        targetName: WString,
        type: Int,
        flags: Int,
    ): Boolean

    @Suppress("FunctionNaming")
    fun CredFree(buffer: Pointer)
}
