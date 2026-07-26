package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.notification.plannedMaintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.MaintenanceRecordId

class DeleteMaintenanceRecordUseCase(
    private val repository: MaintenanceRecordRepository,
    @Suppress("UNUSED_PARAMETER") reminderRepository: ReminderRepository,
    @Suppress("UNUSED_PARAMETER") notificationScheduler: ReminderNotificationScheduler,
) : SuspendUseCase<FamilyScoped<MaintenanceRecordId>, Unit> {
    override suspend fun invoke(params: FamilyScoped<MaintenanceRecordId>) {
        val reminderIds = listOf(maintenanceReminderId(params.value), plannedMaintenanceReminderId(params.value))
        repository.deleteMaintenanceRecordWithNotifications(params.scope, params.value, reminderIds)
    }
}
