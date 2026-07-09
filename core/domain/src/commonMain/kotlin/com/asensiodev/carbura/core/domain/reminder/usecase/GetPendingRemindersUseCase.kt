package com.asensiodev.carbura.core.domain.reminder.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder

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
