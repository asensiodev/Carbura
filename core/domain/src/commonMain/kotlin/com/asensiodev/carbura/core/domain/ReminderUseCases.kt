package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.ReminderId

class CreateAutomaticReminderUseCase(
    private val repository: ReminderRepository,
    private val idFactory: () -> ReminderId,
) : SuspendUseCase<MaintenanceRecord, Reminder?> {
    override suspend fun invoke(params: MaintenanceRecord): Reminder? {
        val dueDate = params.nextDueDate ?: return null
        val title = when (params.maintenanceTypeCode) {
            MaintenanceTypeCode.Itv -> "Proxima ITV"
            MaintenanceTypeCode.Insurance -> "Proximo seguro"
            else -> return null
        }

        val reminder = Reminder(
            id = idFactory(),
            familyId = params.familyId,
            vehicleId = params.vehicleId,
            maintenanceTypeId = params.maintenanceTypeId,
            title = title,
            dueDate = dueDate,
            notifyDaysBefore = 30,
        )
        repository.saveReminder(reminder)
        return reminder
    }
}

class CreateReminderUseCase(
    private val repository: ReminderRepository,
) : SuspendUseCase<Reminder, DomainResult<Reminder>> {
    override suspend fun invoke(params: Reminder): DomainResult<Reminder> {
        if (params.title.isBlank()) {
            return DomainResult.ValidationError(ValidationFailure.BlankReminderTitle)
        }

        if (params.vehicleId.value.isBlank()) {
            return DomainResult.ValidationError(ValidationFailure.MissingReminderVehicle)
        }

        val dueOdometerKm = params.dueOdometerKm
        if (params.dueDate == null && dueOdometerKm == null) {
            return DomainResult.ValidationError(ValidationFailure.MissingReminderDueTarget)
        }

        if (dueOdometerKm != null && dueOdometerKm < 0) {
            return DomainResult.ValidationError(ValidationFailure.NegativeReminderDueOdometer)
        }

        repository.saveReminder(params)
        return DomainResult.Success(params)
    }
}

class GetPendingRemindersUseCase(
    private val repository: ReminderRepository,
) : SuspendUseCase<FamilyId, List<Reminder>> {
    override suspend fun invoke(params: FamilyId): List<Reminder> =
        repository.getPendingReminders(params).sortedWith(
            compareBy<Reminder> { it.dueDate?.iso8601 ?: "9999-12-31" }
                .thenBy { it.dueOdometerKm ?: Int.MAX_VALUE }
                .thenBy { it.title }
        )
}

class CompleteReminderUseCase(
    private val repository: ReminderRepository,
) : SuspendUseCase<ReminderId, Unit> {
    override suspend fun invoke(params: ReminderId) {
        repository.markReminderCompleted(params)
    }
}

class DeleteReminderUseCase(
    private val repository: ReminderRepository,
) : SuspendUseCase<ReminderId, Unit> {
    override suspend fun invoke(params: ReminderId) {
        repository.deleteReminder(params)
    }
}
