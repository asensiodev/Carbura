package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateAutomaticReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.GeneratedMaintenanceReminder
import com.asensiodev.carbura.core.model.MaintenanceRecord

data class MaintenanceCreationResult(
    val record: MaintenanceRecord,
    val generatedReminder: GeneratedMaintenanceReminder?,
)

class CreateMaintenanceWithReminderUseCase(
    private val createMaintenanceRecord: CreateMaintenanceRecordUseCase,
    private val createAutomaticReminder: CreateAutomaticReminderUseCase,
) {
    suspend operator fun invoke(record: MaintenanceRecord): DomainResult<MaintenanceCreationResult> =
        when (val result = createMaintenanceRecord(record)) {
            is DomainResult.Success ->
                DomainResult.Success(
                    MaintenanceCreationResult(
                        record = result.value,
                        generatedReminder = createAutomaticReminder(result.value),
                    ),
                )
            is DomainResult.ValidationError -> result
        }
}
