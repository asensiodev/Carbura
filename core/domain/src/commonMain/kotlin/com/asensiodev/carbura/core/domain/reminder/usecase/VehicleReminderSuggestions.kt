package com.asensiodev.carbura.core.domain.reminder.usecase

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
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
    val scope: com.asensiodev.carbura.core.model.ActiveFamilyScope,
    val vehicle: Vehicle,
    val reconcileGeneratedReminders: Boolean,
)

class SaveVehicleWithRemindersUseCase(
    private val vehicleRepository: VehicleRepository,
    @Suppress("UNUSED_PARAMETER") reminderRepository: ReminderRepository,
    @Suppress("UNUSED_PARAMETER") notificationScheduler: ReminderNotificationScheduler,
    private val deriveSuggestions: DeriveVehicleReminderSuggestionsUseCase = DeriveVehicleReminderSuggestionsUseCase(),
) {
    suspend operator fun invoke(params: SaveVehicleWithRemindersParams) {
        val mutations =
            if (params.reconcileGeneratedReminders) {
                val suggestions = deriveSuggestions(params.vehicle).associateBy { it.kind }
                VehicleReminderKind.entries.map { kind ->
                    val suggestion = suggestions[kind]
                    if (suggestion == null) {
                        ReminderNotificationMutation.Delete(vehicleReminderId(params.vehicle.id, kind))
                    } else {
                        ReminderNotificationMutation.Upsert(
                            reminder = suggestion.reminder,
                            notificationPlan =
                                suggestion.reminder.dueDate?.let {
                                    manualReminderNotificationPlan(suggestion.reminder)
                                },
                        )
                    }
                }
            } else {
                emptyList()
            }
        vehicleRepository.saveVehicleWithNotifications(params.scope, params.vehicle, mutations)
    }
}
