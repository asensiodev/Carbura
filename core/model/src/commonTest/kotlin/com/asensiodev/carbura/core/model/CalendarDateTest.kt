package com.asensiodev.carbura.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CalendarDateTest {
    @Test
    fun acceptsRealLeapDays() {
        assertEquals("2028-02-29", CalendarDate("2028-02-29").iso8601)
        assertEquals("2000-02-29", CalendarDate("2000-02-29").iso8601)
    }

    @Test
    fun rejectsImpossibleDatesAndYearZero() {
        listOf("0000-01-01", "1900-02-29", "2027-02-29", "2028-02-30", "2027-04-31").forEach { value ->
            assertFailsWith<IllegalArgumentException>(value) { CalendarDate(value) }
        }
    }
}
