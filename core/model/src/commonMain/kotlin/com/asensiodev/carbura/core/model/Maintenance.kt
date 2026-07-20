package com.asensiodev.carbura.core.model

data class MaintenanceType(
    val id: MaintenanceTypeId,
    val familyId: FamilyId?,
    val code: MaintenanceTypeCode?,
    val name: String,
    val isGlobal: Boolean,
)

enum class MaintenanceTypeCode {
    Itv,
    Insurance,
    OilChange,
    Tires,
    GeneralReview,
    Repair,
    Custom,
}

data class MaintenanceRecord(
    val id: MaintenanceRecordId,
    val familyId: FamilyId,
    val vehicleId: VehicleId,
    val maintenanceTypeId: MaintenanceTypeId,
    val maintenanceTypeCode: MaintenanceTypeCode?,
    val maintenanceTypeLabel: String? = null,
    val performedOn: CalendarDate,
    val odometerKm: Int? = null,
    val costCents: Int? = null,
    val currency: String = "EUR",
    val workshop: String? = null,
    val notes: String? = null,
    val nextDueDate: CalendarDate? = null,
)
