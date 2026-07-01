package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder
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
