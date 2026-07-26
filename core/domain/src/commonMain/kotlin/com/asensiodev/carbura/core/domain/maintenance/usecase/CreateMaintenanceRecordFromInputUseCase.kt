package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.validation.NumericInputResult
import com.asensiodev.carbura.core.domain.validation.parseNonNegativeCentsInput
import com.asensiodev.carbura.core.domain.validation.parseNonNegativeIntInput
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
            ?: return DomainResult.ValidationError(ValidationFailure.InvalidMaintenancePerformedDate)
    val numbers =
        when (val result = parseNumbers()) {
            is DomainResult.Success -> result.value
            is DomainResult.ValidationError -> return result
        }
    val nextDueDate =
        if (maintenanceTypeCode == MaintenanceTypeCode.Itv || maintenanceTypeCode == MaintenanceTypeCode.Insurance) {
            this.nextDueDate.trim().ifBlank { null }?.let {
                it.toCalendarDateOrNull()
                    ?: return DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceNextDueDate)
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
            odometerKm = numbers.odometerKm,
            costCents = numbers.costCents,
            workshop = workshop.trim().ifBlank { null },
            notes = notes.trim().ifBlank { null },
            nextDueDate = nextDueDate,
        ),
    )
}

private fun CreateMaintenanceRecordInput.parseNumbers(): DomainResult<ParsedMaintenanceNumbers> {
    val odometerKm =
        when (val result = odometerKm.parseNonNegativeIntInput()) {
            NumericInputResult.Blank -> null
            NumericInputResult.Negative -> return DomainResult.ValidationError(ValidationFailure.NegativeMaintenanceOdometer)
            NumericInputResult.Invalid -> return DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceOdometer)
            is NumericInputResult.Value -> result.value
        }
    val costCents =
        when (val result = cost.parseNonNegativeCentsInput()) {
            NumericInputResult.Blank -> null
            NumericInputResult.Negative -> return DomainResult.ValidationError(ValidationFailure.NegativeMaintenanceCost)
            NumericInputResult.Invalid -> return DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceCost)
            is NumericInputResult.Value -> result.value
        }
    return DomainResult.Success(ParsedMaintenanceNumbers(odometerKm, costCents))
}

private data class ParsedMaintenanceNumbers(
    val odometerKm: Int?,
    val costCents: Int?,
)

private fun String.toCalendarDateOrNull(): CalendarDate? =
    try {
        CalendarDate(this)
    } catch (_: IllegalArgumentException) {
        null
    }
