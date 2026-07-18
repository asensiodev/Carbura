package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.MaintenanceRecordId

class DeleteMaintenanceRecordUseCase(
    private val repository: MaintenanceRecordRepository,
    private val reminderRepository: ReminderRepository,
    private val notificationScheduler: ReminderNotificationScheduler,
) : SuspendUseCase<MaintenanceRecordId, Unit> {
    override suspend fun invoke(params: MaintenanceRecordId) {
        val reminderId = maintenanceReminderId(params)
        notificationScheduler.cancel(reminderId)
        reminderRepository.deleteReminder(reminderId)
        repository.deleteMaintenanceRecord(params)
    }
}
