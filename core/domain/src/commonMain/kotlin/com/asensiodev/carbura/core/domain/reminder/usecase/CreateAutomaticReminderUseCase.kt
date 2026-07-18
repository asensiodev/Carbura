package com.asensiodev.carbura.core.domain.reminder.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder

data class GeneratedMaintenanceReminder(
    val reminder: Reminder,
    val notificationPlan: ReminderNotificationPlan,
)

class CreateAutomaticReminderUseCase(
    private val repository: ReminderRepository,
    private val notificationScheduler: ReminderNotificationScheduler,
) : SuspendUseCase<MaintenanceRecord, GeneratedMaintenanceReminder?> {
    override suspend fun invoke(params: MaintenanceRecord): GeneratedMaintenanceReminder? {
        val reminderId = maintenanceReminderId(params.id)
        val dueDate = params.nextDueDate
        val maintenanceTypeCode = params.maintenanceTypeCode
        if (dueDate == null || (maintenanceTypeCode != MaintenanceTypeCode.Itv && maintenanceTypeCode != MaintenanceTypeCode.Insurance)) {
            notificationScheduler.cancel(reminderId)
            repository.deleteReminder(reminderId)
            return null
        }
        val title =
            when (maintenanceTypeCode) {
                MaintenanceTypeCode.Itv -> "Proxima ITV"
                MaintenanceTypeCode.Insurance -> "Proximo seguro"
                else -> error("Maintenance reminder eligibility must be reconciled before creation")
            }

        val reminder =
            Reminder(
                id = reminderId,
                familyId = params.familyId,
                vehicleId = params.vehicleId,
                maintenanceTypeId = params.maintenanceTypeId,
                title = title,
                dueDate = dueDate,
                notifyDaysBefore = if (maintenanceTypeCode == MaintenanceTypeCode.Itv) 60 else 45,
            )
        val notificationPlan = maintenanceReminderNotificationPlan(reminder, maintenanceTypeCode) ?: return null
        repository.saveReminder(reminder)
        notificationScheduler.schedule(notificationPlan)
        return GeneratedMaintenanceReminder(reminder, notificationPlan)
    }
}
