package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.VehicleId

data class CreateMaintenanceRecordInput(
    val id: MaintenanceRecordId,
    val familyId: FamilyId,
    val vehicleId: VehicleId,
    val type: String,
    val performedOn: String,
    val odometerKm: String,
    val cost: String,
    val workshop: String,
    val notes: String,
)

class CreateMaintenanceRecordFromInputUseCase(
    private val createMaintenanceRecordUseCase: CreateMaintenanceRecordUseCase,
) : SuspendUseCase<CreateMaintenanceRecordInput, DomainResult<MaintenanceRecord>> {
    override suspend fun invoke(params: CreateMaintenanceRecordInput): DomainResult<MaintenanceRecord> {
        val type = params.type.trim()
        if (type.isBlank()) {
            return DomainResult.ValidationError(ValidationFailure.BlankMaintenanceType)
        }

        val performedOn = runCatching { CalendarDate(params.performedOn.trim()) }.getOrNull()
            ?: return DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceDate)
        val odometerKm = params.odometerKm.toIntOrNull() ?: -1
        val costCents = params.cost.toCostCentsOrNull()
            ?: if (params.cost.isBlank()) null else return DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceCost)

        val record = MaintenanceRecord(
            id = params.id,
            familyId = params.familyId,
            vehicleId = params.vehicleId,
            maintenanceTypeId = MaintenanceTypeId("type-${type.lowercase().replace(' ', '-')}") ,
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

private fun String.toCostCentsOrNull(): Int? {
    val trimmed = trim()
    if (trimmed.isBlank()) return null
    return trimmed.replace(',', '.').toDoubleOrNull()?.let { (it * 100).toInt() }
}
