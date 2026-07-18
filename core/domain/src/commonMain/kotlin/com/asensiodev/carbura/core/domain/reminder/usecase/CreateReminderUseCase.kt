package com.asensiodev.carbura.core.domain.reminder.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.Reminder

class CreateReminderUseCase(
    private val repository: ReminderRepository,
    private val notificationScheduler: ReminderNotificationScheduler,
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
        if (params.dueDate != null && !params.isCompleted) {
            notificationScheduler.schedule(manualReminderNotificationPlan(params))
        } else {
            notificationScheduler.cancel(params.id)
        }
        return DomainResult.Success(params)
    }
}
