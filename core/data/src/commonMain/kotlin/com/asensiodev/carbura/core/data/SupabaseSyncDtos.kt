package com.asensiodev.carbura.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RemoteVehicleDto(
    val id: String,
    @SerialName("family_id") val familyId: String,
    val name: String,
    @SerialName("vehicle_type") val vehicleType: String,
    val brand: String? = null,
    val model: String? = null,
    @SerialName("license_plate") val licensePlate: String? = null,
    @SerialName("current_odometer_km") val currentOdometerKm: Int,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
internal data class RemoteMaintenanceRecordDto(
    val id: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("vehicle_id") val vehicleId: String,
    @SerialName("maintenance_type_id") val maintenanceTypeId: String? = null,
    @SerialName("maintenance_type_key") val maintenanceTypeKey: String? = null,
    @SerialName("maintenance_type_code") val maintenanceTypeCode: String? = null,
    @SerialName("performed_on") val performedOn: String,
    @SerialName("odometer_km") val odometerKm: Int? = null,
    @SerialName("cost_cents") val costCents: Int? = null,
    val currency: String,
    val workshop: String? = null,
    val notes: String? = null,
    @SerialName("next_due_date") val nextDueDate: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)

@Serializable
internal data class RemoteReminderDto(
    val id: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("vehicle_id") val vehicleId: String,
    @SerialName("maintenance_type_id") val maintenanceTypeId: String? = null,
    @SerialName("maintenance_type_key") val maintenanceTypeKey: String? = null,
    val title: String,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("due_odometer_km") val dueOdometerKm: Int? = null,
    @SerialName("notify_days_before") val notifyDaysBefore: Int,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null,
)
