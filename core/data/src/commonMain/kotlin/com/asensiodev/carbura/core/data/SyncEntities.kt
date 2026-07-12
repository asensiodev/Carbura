package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.model.VehicleType

internal data class SyncVehicle(
    val id: String,
    val familyId: String,
    val name: String,
    val type: VehicleType,
    val brand: String?,
    val model: String?,
    val licensePlate: String?,
    val currentOdometerKm: Int,
    val nextItvDate: String? = null,
    val insuranceRenewalDate: String? = null,
    val nextServiceOdometerKm: Int? = null,
    val updatedAt: Long,
    val pendingSync: Boolean,
    val deletedAt: Long?,
)

internal data class SyncMaintenanceRecord(
    val id: String,
    val familyId: String,
    val vehicleId: String,
    val maintenanceTypeId: String,
    val maintenanceTypeCode: String?,
    val performedOn: String,
    val odometerKm: Int?,
    val costCents: Int?,
    val currency: String,
    val workshop: String?,
    val notes: String?,
    val nextDueDate: String?,
    val updatedAt: Long,
    val pendingSync: Boolean,
    val deletedAt: Long?,
)

internal data class SyncReminder(
    val id: String,
    val familyId: String,
    val vehicleId: String,
    val maintenanceTypeId: String?,
    val title: String,
    val dueDate: String?,
    val dueOdometerKm: Int?,
    val notifyDaysBefore: Int,
    val isCompleted: Boolean,
    val updatedAt: Long,
    val pendingSync: Boolean,
    val deletedAt: Long?,
)
