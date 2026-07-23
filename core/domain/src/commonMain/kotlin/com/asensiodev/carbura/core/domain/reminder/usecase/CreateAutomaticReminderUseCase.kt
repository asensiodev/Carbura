package com.asensiodev.carbura.core.domain.reminder.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.family.FamilyScoped
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
    @Suppress("UNUSED_PARAMETER") notificationScheduler: ReminderNotificationScheduler,
) : SuspendUseCase<FamilyScoped<MaintenanceRecord>, GeneratedMaintenanceReminder?> {
    override suspend fun invoke(params: FamilyScoped<MaintenanceRecord>): GeneratedMaintenanceReminder? {
        val reminderId = maintenanceReminderId(params.value.id)
        val generated = deriveGeneratedMaintenanceReminder(params.value)
        if (generated == null) {
            repository.deleteReminderWithNotification(params.scope, reminderId)
            return null
        }
        repository.saveReminderWithNotification(params.scope, generated.reminder, generated.notificationPlan)
        return generated
    }
}

fun deriveGeneratedMaintenanceReminder(params: MaintenanceRecord): GeneratedMaintenanceReminder? {
    val dueDate = params.nextDueDate ?: return null
    val maintenanceTypeCode = params.maintenanceTypeCode
    if (maintenanceTypeCode != MaintenanceTypeCode.Itv && maintenanceTypeCode != MaintenanceTypeCode.Insurance) return null
    val reminderId = maintenanceReminderId(params.id)
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
    return GeneratedMaintenanceReminder(reminder, notificationPlan)
}
