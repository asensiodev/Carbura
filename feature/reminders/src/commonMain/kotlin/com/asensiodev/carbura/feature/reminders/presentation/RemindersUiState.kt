package com.asensiodev.carbura.feature.reminders.presentation

import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.stringresources.CarburaString

data class RemindersUiState(
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val reminders: List<Reminder> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val selectedFilterVehicleIds: Set<VehicleId> = emptySet(),
    val title: String = "",
    val selectedVehicleId: VehicleId? = null,
    val dueDate: String = "",
    val dueOdometerKm: String = "",
    val errorMessage: CarburaString? = null,
    val hasPersistenceError: Boolean = false,
    val activeAction: ReminderAction? = null,
) {
    val hasNoVehicles: Boolean = !isLoading && !hasLoadError && vehicles.isEmpty()
    val isEmpty: Boolean = !isLoading && !hasLoadError && reminders.isEmpty()
    val visibleReminders: List<Reminder> =
        if (selectedFilterVehicleIds.isEmpty()) {
            reminders
        } else {
            reminders.filter { it.vehicleId in selectedFilterVehicleIds }
        }
    val hasNoMatchingReminders: Boolean = !isLoading && !hasLoadError && reminders.isNotEmpty() && visibleReminders.isEmpty()
}

sealed interface ReminderAction {
    data object Create : ReminderAction

    data class Complete(
        val reminderId: com.asensiodev.carbura.core.model.ReminderId,
    ) : ReminderAction

    data class Delete(
        val reminderId: com.asensiodev.carbura.core.model.ReminderId,
    ) : ReminderAction
}
