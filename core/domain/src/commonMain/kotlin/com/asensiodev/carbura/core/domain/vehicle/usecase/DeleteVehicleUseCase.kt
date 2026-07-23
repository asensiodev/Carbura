package com.asensiodev.carbura.core.domain.vehicle.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.VehicleId

class DeleteVehicleUseCase(
    private val repository: VehicleRepository,
    @Suppress("UNUSED_PARAMETER") reminderRepository: ReminderRepository,
    @Suppress("UNUSED_PARAMETER") notificationScheduler: ReminderNotificationScheduler,
) : SuspendUseCase<FamilyScoped<VehicleId>, Unit> {
    override suspend fun invoke(params: FamilyScoped<VehicleId>) {
        repository.deleteVehicleWithNotifications(params.scope, params.value)
    }
}
