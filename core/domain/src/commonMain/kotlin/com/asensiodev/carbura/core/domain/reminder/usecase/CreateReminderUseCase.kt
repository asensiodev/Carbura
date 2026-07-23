package com.asensiodev.carbura.core.domain.reminder.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.Reminder

class CreateReminderUseCase(
    private val repository: ReminderRepository,
    @Suppress("UNUSED_PARAMETER") notificationScheduler: ReminderNotificationScheduler,
) : SuspendUseCase<FamilyScoped<Reminder>, DomainResult<Reminder>> {
    override suspend fun invoke(params: FamilyScoped<Reminder>): DomainResult<Reminder> {
        val reminder = params.value
        if (reminder.title.isBlank()) {
            return DomainResult.ValidationError(ValidationFailure.BlankReminderTitle)
        }

        if (reminder.vehicleId.value.isBlank()) {
            return DomainResult.ValidationError(ValidationFailure.MissingReminderVehicle)
        }

        val dueOdometerKm = reminder.dueOdometerKm
        if (reminder.dueDate == null && dueOdometerKm == null) {
            return DomainResult.ValidationError(ValidationFailure.MissingReminderDueTarget)
        }

        if (dueOdometerKm != null && dueOdometerKm < 0) {
            return DomainResult.ValidationError(ValidationFailure.NegativeReminderDueOdometer)
        }

        val notificationPlan =
            if (reminder.dueDate != null && !reminder.isCompleted) {
                manualReminderNotificationPlan(reminder)
            } else {
                null
            }
        require(reminder.familyId == params.scope.familyId)
        repository.saveReminderWithNotification(params.scope, reminder, notificationPlan)
        return DomainResult.Success(reminder)
    }
}
