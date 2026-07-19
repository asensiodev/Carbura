package com.asensiodev.carbura.core.domain.reminder.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.plannedMaintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord

class RemovePlannedMaintenanceReminderUseCase(
    private val repository: ReminderRepository,
    private val notificationScheduler: ReminderNotificationScheduler,
) : SuspendUseCase<MaintenanceRecord, Unit> {
    override suspend fun invoke(params: MaintenanceRecord) {
        val reminderId = plannedMaintenanceReminderId(params.id)
        if (repository.getRemindersByVehicle(params.vehicleId).none { it.id == reminderId }) return
        notificationScheduler.cancel(reminderId)
        repository.deleteReminder(reminderId)
    }
}
