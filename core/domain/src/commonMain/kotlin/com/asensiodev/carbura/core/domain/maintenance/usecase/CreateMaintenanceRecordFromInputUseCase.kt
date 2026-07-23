package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId

class CreateMaintenanceRecordFromInputUseCase(
    private val createMaintenanceRecordUseCase: CreateMaintenanceRecordUseCase,
) : SuspendUseCase<FamilyScoped<CreateMaintenanceRecordInput>, DomainResult<MaintenanceRecord>> {
    override suspend fun invoke(params: FamilyScoped<CreateMaintenanceRecordInput>): DomainResult<MaintenanceRecord> =
        when (val result = params.value.toMaintenanceRecord()) {
            is DomainResult.Success -> createMaintenanceRecordUseCase(FamilyScoped(params.scope, result.value))
            is DomainResult.ValidationError -> result
        }
}

internal fun CreateMaintenanceRecordInput.toMaintenanceRecord(): DomainResult<MaintenanceRecord> {
    val customTypeLabel = customTypeLabel?.trim() ?: type.trim()
    if (maintenanceTypeCode == MaintenanceTypeCode.Custom && customTypeLabel.isBlank()) {
        return DomainResult.ValidationError(ValidationFailure.BlankMaintenanceType)
    }

    val performedOn =
        this.performedOn.trim().toCalendarDateOrNull()
            ?: return DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceDate)
    val odometerKm = this.odometerKm.toIntOrNull() ?: -1
    val costCents =
        this.cost.toCostCentsOrNull()
            ?: if (this.cost.isBlank()) null else return DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceCost)
    val nextDueDate =
        if (maintenanceTypeCode == MaintenanceTypeCode.Itv || maintenanceTypeCode == MaintenanceTypeCode.Insurance) {
            this.nextDueDate.trim().ifBlank { null }?.let {
                it.toCalendarDateOrNull()
                    ?: return DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceDate)
            }
        } else {
            null
        }
    val maintenanceTypeId =
        MaintenanceTypeId(
            if (maintenanceTypeCode == MaintenanceTypeCode.Custom) {
                "type-${customTypeLabel.lowercase().replace(' ', '-')}"
            } else {
                "type-${maintenanceTypeCode.name.lowercase()}"
            },
        )
    return DomainResult.Success(
        MaintenanceRecord(
            id = id,
            familyId = familyId,
            vehicleId = vehicleId,
            maintenanceTypeId = maintenanceTypeId,
            maintenanceTypeCode = maintenanceTypeCode,
            maintenanceTypeLabel = customTypeLabel.takeIf { maintenanceTypeCode == MaintenanceTypeCode.Custom },
            performedOn = performedOn,
            odometerKm = odometerKm,
            costCents = costCents,
            workshop = workshop.trim().ifBlank { null },
            notes = notes.trim().ifBlank { null },
            nextDueDate = nextDueDate,
        ),
    )
}

private fun String.toCalendarDateOrNull(): CalendarDate? =
    try {
        CalendarDate(this)
    } catch (_: IllegalArgumentException) {
        null
    }

private fun String.toCostCentsOrNull(): Int? {
    val trimmed = trim()
    if (trimmed.isBlank()) return null
    return trimmed.replace(',', '.').toDoubleOrNull()?.let { (it * 100).toInt() }
}
