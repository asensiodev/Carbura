package com.asensiodev.carbura.core.data

internal expect fun currentTimeMillis(): Long

internal expect fun epochMillisToIsoString(epochMillis: Long): String

internal expect fun isoStringToEpochMillis(value: String): Long
