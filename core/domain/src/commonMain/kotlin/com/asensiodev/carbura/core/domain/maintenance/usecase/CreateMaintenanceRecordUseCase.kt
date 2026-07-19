package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode

class CreateMaintenanceRecordUseCase(
    private val repository: MaintenanceRecordRepository,
) : SuspendUseCase<MaintenanceRecord, DomainResult<MaintenanceRecord>> {
    override suspend fun invoke(params: MaintenanceRecord): DomainResult<MaintenanceRecord> =
        persist(params) { repository.saveMaintenanceRecord(it) }

    suspend fun withNotification(
        params: MaintenanceRecord,
        mutation: (MaintenanceRecord) -> ReminderNotificationMutation,
    ): DomainResult<MaintenanceRecord> =
        persist(params) { normalized ->
            repository.saveMaintenanceRecordWithNotification(normalized, mutation(normalized))
        }

    private suspend fun persist(
        params: MaintenanceRecord,
        save: suspend (MaintenanceRecord) -> Unit,
    ): DomainResult<MaintenanceRecord> {
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
        save(normalized)
        return DomainResult.Success(normalized)
    }
}
