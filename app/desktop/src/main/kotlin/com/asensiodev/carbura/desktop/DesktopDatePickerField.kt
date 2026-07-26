package com.asensiodev.carbura.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.asensiodev.carbura.desktop.resources.Res
import com.asensiodev.carbura.desktop.resources.form_accept
import com.asensiodev.carbura.desktop.resources.form_cancel
import org.jetbrains.compose.resources.stringResource
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale

@Composable
internal fun DesktopDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    selectLabel: String,
    clearLabel: String,
    optional: Boolean,
    enabled: Boolean,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { visible = true },
                modifier = Modifier.weight(1f),
                enabled = enabled,
            ) {
                Text(formatDesktopDate(value).ifBlank { selectLabel })
            }
            if (optional && value.isNotBlank()) {
                TextButton(onClick = { onValueChange("") }, enabled = enabled) { Text(clearLabel) }
            }
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    }
    if (visible) {
        var selectedDate by remember(value) { mutableStateOf(parseDesktopDate(value) ?: LocalDate.now()) }
        var displayedMonth by remember(value) { mutableStateOf(YearMonth.from(selectedDate)) }
        AlertDialog(
            onDismissRequest = { visible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(selectedDate.toString())
                        visible = false
                    },
                ) { Text(stringResource(Res.string.form_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { visible = false }) { Text(stringResource(Res.string.form_cancel)) }
            },
            title = { Text(label) },
            text = {
                DesktopCalendar(
                    displayedMonth = displayedMonth,
                    selectedDate = selectedDate,
                    onPreviousMonth = { displayedMonth = displayedMonth.minusMonths(1) },
                    onNextMonth = { displayedMonth = displayedMonth.plusMonths(1) },
                    onDateSelected = { selectedDate = it },
                )
            },
        )
    }
}

@Composable
private fun DesktopCalendar(
    displayedMonth: YearMonth,
    selectedDate: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    Column(modifier = Modifier.width(360.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onPreviousMonth) { Text("<") }
            Text(
                text = formatDesktopMonth(displayedMonth),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
            )
            TextButton(onClick = onNextMonth) { Text(">") }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            DESKTOP_WEEKDAY_LABELS.forEach { weekday ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(weekday, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        desktopCalendarDates(displayedMonth).chunked(DAYS_PER_WEEK).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (date != null) {
                            TextButton(
                                onClick = { onDateSelected(date) },
                                colors =
                                    ButtonDefaults.textButtonColors(
                                        containerColor =
                                            if (date == selectedDate) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                Color.Transparent
                                            },
                                    ),
                            ) {
                                Text(date.dayOfMonth.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun desktopCalendarDates(month: YearMonth): List<LocalDate?> {
    val leadingEmptyDays = month.atDay(1).dayOfWeek.value - 1
    return List(CALENDAR_CELL_COUNT) { index ->
        val day = index - leadingEmptyDays + 1
        day.takeIf { it in 1..month.lengthOfMonth() }?.let(month::atDay)
    }
}

internal fun formatDesktopMonth(month: YearMonth): String =
    month
        .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-ES")))
        .replaceFirstChar(Char::uppercase)

private fun parseDesktopDate(value: String): LocalDate? =
    try {
        LocalDate.parse(value)
    } catch (_: DateTimeParseException) {
        null
    }

internal fun formatDesktopDate(
    iso8601: String,
    locale: Locale = Locale.getDefault(),
): String =
    try {
        LocalDate.parse(iso8601).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale))
    } catch (_: DateTimeParseException) {
        iso8601
    }

internal fun isoDateToUtcMillis(value: String): Long? =
    try {
        LocalDate
            .parse(value)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
    } catch (_: DateTimeParseException) {
        null
    }

internal fun utcMillisToIsoDate(value: Long): String =
    Instant
        .ofEpochMilli(value)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toString()

internal fun formatDesktopTimestamp(
    epochMillis: Long,
    locale: Locale = Locale.getDefault(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): String =
    DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(locale)
        .format(Instant.ofEpochMilli(epochMillis).atZone(zoneId))

internal fun formatDesktopDecimalCurrency(
    cents: Int,
    locale: Locale = Locale.getDefault(),
): String =
    NumberFormat.getNumberInstance(locale).run {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
        format(cents / 100.0)
    }

private val DESKTOP_WEEKDAY_LABELS = listOf("L", "M", "X", "J", "V", "S", "D")
private const val DAYS_PER_WEEK = 7
private const val CALENDAR_CELL_COUNT = 42
