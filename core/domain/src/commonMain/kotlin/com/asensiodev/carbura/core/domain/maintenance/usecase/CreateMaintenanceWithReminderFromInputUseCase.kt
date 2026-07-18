package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase

class CreateMaintenanceWithReminderFromInputUseCase(
    private val createMaintenance: CreateMaintenanceWithReminderUseCase,
) : SuspendUseCase<CreateMaintenanceRecordInput, DomainResult<MaintenanceCreationResult>> {
    override suspend fun invoke(params: CreateMaintenanceRecordInput): DomainResult<MaintenanceCreationResult> =
        when (val result = params.toMaintenanceRecord()) {
            is DomainResult.Success -> createMaintenance(result.value)
            is DomainResult.ValidationError -> result
        }
}
