package com.asensiodev.carbura.core.model

data class Reminder(
    val id: ReminderId,
    val familyId: FamilyId,
    val vehicleId: VehicleId,
    val maintenanceTypeId: MaintenanceTypeId?,
    val title: String,
    val dueDate: CalendarDate? = null,
    val dueOdometerKm: Int? = null,
    val notifyDaysBefore: Int = 30,
    val isCompleted: Boolean = false,
)
