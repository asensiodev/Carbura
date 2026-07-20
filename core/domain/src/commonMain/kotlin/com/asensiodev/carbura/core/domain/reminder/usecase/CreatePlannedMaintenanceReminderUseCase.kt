package com.asensiodev.carbura.core.domain.reminder.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.plannedMaintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.Reminder

class CreatePlannedMaintenanceReminderUseCase(
    private val repository: ReminderRepository,
    @Suppress("UNUSED_PARAMETER") notificationScheduler: ReminderNotificationScheduler,
) : SuspendUseCase<MaintenanceRecord, Reminder> {
    override suspend fun invoke(params: MaintenanceRecord): Reminder {
        val reminder = derivePlannedMaintenanceReminder(params)
        val notificationPlan = manualReminderNotificationPlan(reminder)
        repository.saveReminderWithNotification(reminder, notificationPlan)
        return reminder
    }
}

fun derivePlannedMaintenanceReminder(record: MaintenanceRecord): Reminder =
    Reminder(
        id = plannedMaintenanceReminderId(record.id),
        familyId = record.familyId,
        vehicleId = record.vehicleId,
        maintenanceTypeId = record.maintenanceTypeId,
        title = "Mantenimiento programado",
        dueDate = record.performedOn,
        notifyDaysBefore = 0,
    )
