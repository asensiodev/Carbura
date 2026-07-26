package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.family.FamilyScoped

class CreateMaintenanceWithReminderFromInputUseCase(
    private val createMaintenance: CreateMaintenanceWithReminderUseCase,
) : SuspendUseCase<FamilyScoped<CreateMaintenanceRecordInput>, DomainResult<MaintenanceCreationResult>> {
    override suspend fun invoke(params: FamilyScoped<CreateMaintenanceRecordInput>): DomainResult<MaintenanceCreationResult> =
        when (val result = params.value.toMaintenanceRecord()) {
            is DomainResult.Success -> createMaintenance(FamilyScoped(params.scope, result.value))
            is DomainResult.ValidationError -> result
        }
}
