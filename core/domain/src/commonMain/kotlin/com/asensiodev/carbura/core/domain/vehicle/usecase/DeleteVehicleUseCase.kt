package com.asensiodev.carbura.core.domain.vehicle.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.VehicleId

class DeleteVehicleUseCase(
    private val repository: VehicleRepository,
    private val reminderRepository: ReminderRepository,
    private val notificationScheduler: ReminderNotificationScheduler,
) : SuspendUseCase<VehicleId, Unit> {
    override suspend fun invoke(params: VehicleId) {
        val reminderIds = reminderRepository
            .getRemindersByVehicle(params)
            .map { it.id }
        repository.deleteVehicle(params)
        reminderIds.forEach { notificationScheduler.cancel(it) }
    }
}
