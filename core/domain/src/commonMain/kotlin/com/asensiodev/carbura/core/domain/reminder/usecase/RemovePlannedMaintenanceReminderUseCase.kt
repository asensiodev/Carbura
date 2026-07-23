package com.asensiodev.carbura.core.domain.reminder.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.plannedMaintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord

class RemovePlannedMaintenanceReminderUseCase(
    private val repository: ReminderRepository,
    @Suppress("UNUSED_PARAMETER") notificationScheduler: ReminderNotificationScheduler,
) : SuspendUseCase<FamilyScoped<MaintenanceRecord>, Unit> {
    override suspend fun invoke(params: FamilyScoped<MaintenanceRecord>) {
        val reminderId = plannedMaintenanceReminderId(params.value.id)
        if (repository.getRemindersByVehicle(params.scope, params.value.vehicleId).none { it.id == reminderId }) return
        repository.deleteReminderWithNotification(params.scope, reminderId)
    }
}
