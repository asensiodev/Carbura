package com.asensiodev.carbura.feature.reminders.presentation

import androidx.annotation.StringRes
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featurereminders.R

@StringRes
internal fun CarburaString.remindersStringRes(): Int = when (this) {
    CarburaString.ReminderCreatedMessage -> R.string.reminder_created_message
    CarburaString.ReminderCompletedMessage -> R.string.reminder_completed_message
    CarburaString.ReminderDeletedMessage -> R.string.reminder_deleted_message
    CarburaString.ValidationBlankReminderTitle -> R.string.validation_blank_reminder_title
    CarburaString.ValidationMissingReminderVehicle -> R.string.validation_missing_reminder_vehicle
    CarburaString.ValidationMissingReminderDueTarget -> R.string.validation_missing_reminder_due_target
    CarburaString.ValidationNegativeReminderDueOdometer -> R.string.validation_negative_reminder_due_odometer
    CarburaString.ValidationInvalidReminderDate -> R.string.validation_invalid_reminder_date
    else -> R.string.validation_generic
}
