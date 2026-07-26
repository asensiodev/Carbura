package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateAutomaticReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.GeneratedMaintenanceReminder
import com.asensiodev.carbura.core.domain.reminder.usecase.deriveGeneratedMaintenanceReminder
import com.asensiodev.carbura.core.model.MaintenanceRecord

data class MaintenanceCreationResult(
    val record: MaintenanceRecord,
    val generatedReminder: GeneratedMaintenanceReminder?,
)

class CreateMaintenanceWithReminderUseCase(
    private val createMaintenanceRecord: CreateMaintenanceRecordUseCase,
    @Suppress("UNUSED_PARAMETER") createAutomaticReminder: CreateAutomaticReminderUseCase,
) {
    suspend operator fun invoke(params: FamilyScoped<MaintenanceRecord>): DomainResult<MaintenanceCreationResult> {
        val record = params.value
        var generatedReminder: GeneratedMaintenanceReminder? = null
        return when (
            val result =
                createMaintenanceRecord.withNotification(params) { normalized ->
                    generatedReminder = deriveGeneratedMaintenanceReminder(normalized)
                    generatedReminder?.let {
                        ReminderNotificationMutation.Upsert(it.reminder, it.notificationPlan)
                    } ?: ReminderNotificationMutation.Delete(maintenanceReminderId(normalized.id))
                }
        ) {
            is DomainResult.Success ->
                DomainResult.Success(
                    MaintenanceCreationResult(
                        record = result.value,
                        generatedReminder = generatedReminder,
                    ),
                )
            is DomainResult.ValidationError -> result
        }
    }
}
