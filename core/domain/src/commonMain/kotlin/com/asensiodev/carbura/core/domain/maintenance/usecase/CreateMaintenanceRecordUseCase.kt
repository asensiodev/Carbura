package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode

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

        val normalized =
            params.copy(
                nextDueDate =
                    params.nextDueDate.takeIf {
                        params.maintenanceTypeCode == MaintenanceTypeCode.Itv ||
                            params.maintenanceTypeCode == MaintenanceTypeCode.Insurance
                    },
            )
        repository.saveMaintenanceRecord(normalized)
        return DomainResult.Success(normalized)
    }
}
