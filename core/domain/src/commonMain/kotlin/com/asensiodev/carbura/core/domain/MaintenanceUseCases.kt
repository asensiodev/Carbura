package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId

class CreateMaintenanceRecordUseCase(
    private val repository: MaintenanceRecordRepository,
) : SuspendUseCase<MaintenanceRecord, DomainResult<MaintenanceRecord>> {
    override suspend fun invoke(params: MaintenanceRecord): DomainResult<MaintenanceRecord> {
        val odometerKm = params.odometerKm
        if (odometerKm != null && odometerKm < 0) {
            return DomainResult.ValidationError(ValidationFailure.NegativeMaintenanceOdometer)
        }

        val costCents = params.costCents
        if (costCents != null && costCents < 0) {
            return DomainResult.ValidationError(ValidationFailure.NegativeMaintenanceCost)
        }

        repository.saveMaintenanceRecord(params)
        return DomainResult.Success(params)
    }
}

class DeleteMaintenanceRecordUseCase(
    private val repository: MaintenanceRecordRepository,
) : SuspendUseCase<MaintenanceRecordId, Unit> {
    override suspend fun invoke(params: MaintenanceRecordId) {
        repository.deleteMaintenanceRecord(params)
    }
}
