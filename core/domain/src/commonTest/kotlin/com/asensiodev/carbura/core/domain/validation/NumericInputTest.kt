package com.asensiodev.carbura.core.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals

class NumericInputTest {
    @Test
    fun integerParserRejectsNegativeMalformedAndOverflowValues() {
        assertEquals(NumericInputResult.Blank, "  ".parseNonNegativeIntInput())
        assertEquals(NumericInputResult.Value(0), "0".parseNonNegativeIntInput())
        assertEquals(NumericInputResult.Value(Int.MAX_VALUE), Int.MAX_VALUE.toString().parseNonNegativeIntInput())
        assertEquals(NumericInputResult.Negative, "-1".parseNonNegativeIntInput())
        listOf("+1", "1.5", "1e3", "NaN", "Infinity", "2147483648", "12 km").forEach { value ->
            assertEquals(NumericInputResult.Invalid, value.parseNonNegativeIntInput(), value)
        }
    }

    @Test
    fun centsParserUsesExactTwoDigitPrecision() {
        assertEquals(NumericInputResult.Blank, "".parseNonNegativeCentsInput())
        assertEquals(NumericInputResult.Value(100), "1".parseNonNegativeCentsInput())
        assertEquals(NumericInputResult.Value(150), "1.5".parseNonNegativeCentsInput())
        assertEquals(NumericInputResult.Value(199), "1,99".parseNonNegativeCentsInput())
        assertEquals(NumericInputResult.Value(Int.MAX_VALUE), "21474836.47".parseNonNegativeCentsInput())
        assertEquals(NumericInputResult.Negative, "-1.50".parseNonNegativeCentsInput())
        listOf("1.999", "1.", ".5", "1,2.3", "NaN", "Infinity", "21474836.48").forEach { value ->
            assertEquals(NumericInputResult.Invalid, value.parseNonNegativeCentsInput(), value)
        }
    }
}
