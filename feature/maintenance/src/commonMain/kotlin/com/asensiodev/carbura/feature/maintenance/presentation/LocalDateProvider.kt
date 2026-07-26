package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.model.CalendarDate

fun interface LocalDateProvider {
    fun currentDate(): CalendarDate
}

internal expect fun deviceLocalDate(): CalendarDate
