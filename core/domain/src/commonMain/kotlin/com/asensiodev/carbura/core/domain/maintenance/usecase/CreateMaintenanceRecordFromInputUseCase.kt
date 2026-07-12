package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId

class CreateMaintenanceRecordFromInputUseCase(
    private val createMaintenanceRecordUseCase: CreateMaintenanceRecordUseCase,
) : SuspendUseCase<CreateMaintenanceRecordInput, DomainResult<MaintenanceRecord>> {
    override suspend fun invoke(params: CreateMaintenanceRecordInput): DomainResult<MaintenanceRecord> {
        val type = params.type.trim()
        if (type.isBlank()) {
            return DomainResult.ValidationError(ValidationFailure.BlankMaintenanceType)
        }

        val performedOn =
            runCatching { CalendarDate(params.performedOn.trim()) }.getOrNull()
                ?: return DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceDate)
        val odometerKm = params.odometerKm.toIntOrNull() ?: -1
        val costCents =
            params.cost.toCostCentsOrNull()
                ?: if (params.cost.isBlank()) null else return DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceCost)
        val maintenanceTypeId = MaintenanceTypeId("type-${type.lowercase().replace(' ', '-')}")

        val record =
            MaintenanceRecord(
                id = params.id,
                familyId = params.familyId,
                vehicleId = params.vehicleId,
                maintenanceTypeId = maintenanceTypeId,
                maintenanceTypeCode = MaintenanceTypeCode.Custom,
                performedOn = performedOn,
                odometerKm = odometerKm,
                costCents = costCents,
                workshop = params.workshop.trim().ifBlank { null },
                notes = params.notes.trim().ifBlank { null },
            )
        return createMaintenanceRecordUseCase(record)
    }
}

private fun String.toCostCentsOrNull(): Int? {
    val trimmed = trim()
    if (trimmed.isBlank()) return null
    return trimmed.replace(',', '.').toDoubleOrNull()?.let { (it * 100).toInt() }
}
