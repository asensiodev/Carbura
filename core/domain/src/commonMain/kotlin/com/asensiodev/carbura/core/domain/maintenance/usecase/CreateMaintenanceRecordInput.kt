package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecordId
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
