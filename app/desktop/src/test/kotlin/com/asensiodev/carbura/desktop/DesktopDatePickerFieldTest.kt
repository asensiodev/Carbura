package com.asensiodev.carbura.desktop

import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DesktopDatePickerFieldTest {
    @Test
    fun dateFormattingUsesRequestedLocaleWithoutChangingCanonicalValue() {
        assertEquals("Jul 24, 2026", formatDesktopDate("2026-07-24", Locale.US))
        assertEquals("24 jul 2026", formatDesktopDate("2026-07-24", Locale.forLanguageTag("es-ES")))
        assertEquals("not-a-date", formatDesktopDate("not-a-date", Locale.US))
    }

    @Test
    fun utcPickerRoundTripDoesNotDependOnLocalTimeZone() {
        val millis =
            LocalDate
                .parse("2028-02-29")
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        assertEquals(millis, isoDateToUtcMillis("2028-02-29"))
        assertEquals("2028-02-29", utcMillisToIsoDate(millis))
    }

    @Test
    fun pairedFieldsStackOnlyBelowThreshold() {
        assertTrue(useStackedDesktopFields(479.dp))
        assertFalse(useStackedDesktopFields(480.dp))
    }

    @Test
    fun synchronizationTimestampIsLocalizedInsteadOfRawEpoch() {
        val epochMillis = 1_753_334_400_000L
        val formatted = formatDesktopTimestamp(epochMillis, Locale.US, ZoneId.of("UTC"))
        assertNotEquals(epochMillis.toString(), formatted)
        assertTrue(formatted.contains("2025"))
    }

    @Test
    fun currencyDecimalUsesTheSelectedLocale() {
        assertEquals("1.234,50", formatDesktopDecimalCurrency(123450, Locale.forLanguageTag("es-ES")))
        assertEquals("1,234.50", formatDesktopDecimalCurrency(123450, Locale.US))
    }
}
