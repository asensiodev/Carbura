package com.asensiodev.carbura.feature.reminders.presentation

import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId

sealed interface RemindersEvent {
    data object Started : RemindersEvent
    data class TitleChanged(val value: String) : RemindersEvent
    data class VehicleSelected(val vehicleId: VehicleId) : RemindersEvent
    data class DueDateChanged(val value: String) : RemindersEvent
    data class DueOdometerChanged(val value: String) : RemindersEvent
    data object SubmitReminder : RemindersEvent
    data class CompleteReminder(val reminderId: ReminderId) : RemindersEvent
}
