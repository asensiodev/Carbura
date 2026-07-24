package com.asensiodev.carbura.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.asensiodev.carbura.desktop.resources.Res
import com.asensiodev.carbura.desktop.resources.form_accept
import com.asensiodev.carbura.desktop.resources.form_cancel
import org.jetbrains.compose.resources.stringResource
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = isoDateToUtcMillis(value),
                yearRange = 1..9999,
            )
        DatePickerDialog(
            onDismissRequest = { visible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onValueChange(utcMillisToIsoDate(it)) }
                        visible = false
                    },
                ) { Text(stringResource(Res.string.form_accept)) }
            },
            dismissButton = {
                TextButton(onClick = { visible = false }) { Text(stringResource(Res.string.form_cancel)) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
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
