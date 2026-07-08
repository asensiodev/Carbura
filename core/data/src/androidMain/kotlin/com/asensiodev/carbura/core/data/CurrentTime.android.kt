package com.asensiodev.carbura.core.data

import java.time.Instant

internal actual fun currentTimeMillis(): Long = System.currentTimeMillis()

internal actual fun epochMillisToIsoString(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).toString()

internal actual fun isoStringToEpochMillis(value: String): Long = Instant.parse(value).toEpochMilli()
