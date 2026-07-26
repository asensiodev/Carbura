package com.asensiodev.carbura.core.domain.validation

sealed interface NumericInputResult {
    data object Blank : NumericInputResult

    data object Negative : NumericInputResult

    data object Invalid : NumericInputResult

    data class Value(
        val value: Int,
    ) : NumericInputResult
}

fun String.parseNonNegativeIntInput(): NumericInputResult {
    val value = trim()
    if (value.isEmpty()) return NumericInputResult.Blank
    if (NEGATIVE_INTEGER.matches(value)) return NumericInputResult.Negative
    if (!NON_NEGATIVE_INTEGER.matches(value)) return NumericInputResult.Invalid
    val parsed = value.toLongOrNull() ?: return NumericInputResult.Invalid
    return if (parsed <= Int.MAX_VALUE) NumericInputResult.Value(parsed.toInt()) else NumericInputResult.Invalid
}

fun String.parseNonNegativeCentsInput(): NumericInputResult {
    val value = trim()
    if (value.isEmpty()) return NumericInputResult.Blank
    if (NEGATIVE_DECIMAL.matches(value)) return NumericInputResult.Negative
    if (!NON_NEGATIVE_DECIMAL.matches(value)) return NumericInputResult.Invalid
    return parseCents(value)?.let(NumericInputResult::Value) ?: NumericInputResult.Invalid
}

private fun parseCents(value: String): Int? {
    val separatorIndex = value.indexOfAny(charArrayOf('.', ','))
    val wholeText = if (separatorIndex >= 0) value.substring(0, separatorIndex) else value
    val fractionText = if (separatorIndex >= 0) value.substring(separatorIndex + 1) else ""
    val whole = wholeText.toLongOrNull() ?: return null
    val fraction = fractionText.padEnd(2, '0').ifEmpty { "00" }.toLongOrNull() ?: return null
    if (whole > Int.MAX_VALUE / 100L) return null
    val cents = whole * 100L + fraction
    return cents.takeIf { it <= Int.MAX_VALUE }?.toInt()
}

private val NON_NEGATIVE_INTEGER = Regex("[0-9]+")
private val NEGATIVE_INTEGER = Regex("-[0-9]+")
private val NON_NEGATIVE_DECIMAL = Regex("[0-9]+([.,][0-9]{1,2})?")
private val NEGATIVE_DECIMAL = Regex("-[0-9]+([.,][0-9]{1,2})?")
