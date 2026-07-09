package com.asensiodev.carbura.feature.reminders.presentation

import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.stringresources.CarburaString

internal fun ValidationFailure.toRemindersMessage(): CarburaString = when (this) {
    ValidationFailure.BlankReminderTitle -> CarburaString.ValidationBlankReminderTitle
    ValidationFailure.MissingReminderVehicle -> CarburaString.ValidationMissingReminderVehicle
    ValidationFailure.MissingReminderDueTarget -> CarburaString.ValidationMissingReminderDueTarget
    ValidationFailure.NegativeReminderDueOdometer -> CarburaString.ValidationNegativeReminderDueOdometer
    else -> CarburaString.ValidationGeneric
}
