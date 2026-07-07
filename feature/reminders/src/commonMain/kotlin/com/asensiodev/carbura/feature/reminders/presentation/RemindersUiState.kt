package com.asensiodev.carbura.feature.reminders.presentation

import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.stringresources.CarburaString

data class RemindersUiState(
    val isLoading: Boolean = false,
    val reminders: List<Reminder> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val title: String = "",
    val selectedVehicleId: VehicleId? = null,
    val dueDate: String = "",
    val dueOdometerKm: String = "",
    val errorMessage: CarburaString? = null,
) {
    val isEmpty: Boolean = reminders.isEmpty()
}
