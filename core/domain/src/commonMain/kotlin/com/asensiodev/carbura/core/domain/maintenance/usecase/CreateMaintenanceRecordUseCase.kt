package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode

class CreateMaintenanceRecordUseCase(
    private val repository: MaintenanceRecordRepository,
) : SuspendUseCase<FamilyScoped<MaintenanceRecord>, DomainResult<MaintenanceRecord>> {
    override suspend fun invoke(params: FamilyScoped<MaintenanceRecord>): DomainResult<MaintenanceRecord> =
        persist(params.value) { repository.saveMaintenanceRecord(params.scope, it) }

    suspend fun withNotification(
        params: FamilyScoped<MaintenanceRecord>,
        mutation: (MaintenanceRecord) -> ReminderNotificationMutation,
    ): DomainResult<MaintenanceRecord> =
        persist(params.value) { normalized ->
            repository.saveMaintenanceRecordWithNotification(params.scope, normalized, mutation(normalized))
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
