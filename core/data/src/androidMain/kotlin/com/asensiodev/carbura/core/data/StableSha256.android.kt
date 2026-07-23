package com.asensiodev.carbura.core.data

import java.security.MessageDigest

internal actual fun stableSha256(value: String): String =
    MessageDigest.getInstance("SHA-256").digest(value.encodeToByteArray()).joinToString("") { byte ->
        byte.toUByte().toString(16).padStart(2, '0')
    }
