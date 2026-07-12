package com.asensiodev.carbura.core.domain.reminder.usecase

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

enum class VehicleReminderKind {
    Itv,
    Insurance,
    Service,
}

data class VehicleReminderSuggestion(
    val kind: VehicleReminderKind,
    val reminder: Reminder,
)

fun vehicleReminderId(
    vehicleId: VehicleId,
    kind: VehicleReminderKind,
): ReminderId = ReminderId("vehicle-reminder:${vehicleId.value}:${kind.name.lowercase()}")

class DeriveVehicleReminderSuggestionsUseCase {
    operator fun invoke(vehicle: Vehicle): List<VehicleReminderSuggestion> =
        buildList {
            vehicle.nextItvDate?.let {
                add(suggestion(vehicle, VehicleReminderKind.Itv, "Proxima ITV", dueDate = it))
            }
            vehicle.insuranceRenewalDate?.let {
                add(suggestion(vehicle, VehicleReminderKind.Insurance, "Proximo seguro", dueDate = it))
            }
            vehicle.nextServiceOdometerKm?.let {
                add(suggestion(vehicle, VehicleReminderKind.Service, "Proxima revision", dueOdometerKm = it))
            }
        }

    private fun suggestion(
        vehicle: Vehicle,
        kind: VehicleReminderKind,
        title: String,
        dueDate: com.asensiodev.carbura.core.model.CalendarDate? = null,
        dueOdometerKm: Int? = null,
    ) = VehicleReminderSuggestion(
        kind = kind,
        reminder =
            Reminder(
                id = vehicleReminderId(vehicle.id, kind),
                familyId = vehicle.familyId,
                vehicleId = vehicle.id,
                maintenanceTypeId = null,
                title = title,
                dueDate = dueDate,
                dueOdometerKm = dueOdometerKm,
            ),
    )
}

data class SaveVehicleWithRemindersParams(
    val vehicle: Vehicle,
    val reconcileGeneratedReminders: Boolean,
)

class SaveVehicleWithRemindersUseCase(
    private val vehicleRepository: VehicleRepository,
    private val reminderRepository: ReminderRepository,
    private val notificationScheduler: ReminderNotificationScheduler,
    private val deriveSuggestions: DeriveVehicleReminderSuggestionsUseCase = DeriveVehicleReminderSuggestionsUseCase(),
) {
    suspend operator fun invoke(params: SaveVehicleWithRemindersParams) {
        vehicleRepository.saveVehicle(params.vehicle)
        if (!params.reconcileGeneratedReminders) return

        val suggestions = deriveSuggestions(params.vehicle).associateBy { it.kind }
        VehicleReminderKind.entries.forEach { kind ->
            val id = vehicleReminderId(params.vehicle.id, kind)
            val suggestion = suggestions[kind]
            if (suggestion == null) {
                reminderRepository.deleteReminder(id)
                notificationScheduler.cancel(id)
            } else {
                reminderRepository.saveReminder(suggestion.reminder)
                if (suggestion.reminder.dueDate != null) {
                    notificationScheduler.schedule(suggestion.reminder)
                } else {
                    notificationScheduler.cancel(id)
                }
            }
        }
    }
}
