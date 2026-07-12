package com.asensiodev.carbura.feature.reminders.presentation

import com.asensiodev.carbura.core.stringresources.CarburaString

sealed interface RemindersEffect {
    data class ReminderCreated(
        val title: String,
    ) : RemindersEffect

    data class ReminderCompleted(
        val title: String,
    ) : RemindersEffect

    data class ReminderDeleted(
        val title: String,
    ) : RemindersEffect

    data class ValidationFailed(
        val message: CarburaString,
    ) : RemindersEffect
}
