package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.model.CalendarDate
import java.time.LocalDate

internal actual fun deviceLocalDate(): CalendarDate = CalendarDate(LocalDate.now().toString())
