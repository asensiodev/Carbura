package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.model.VehicleType

internal fun SyncVehicle.toRemoteDto(): RemoteVehicleDto = RemoteVehicleDto(
    id = id,
    familyId = familyId,
    name = name,
    vehicleType = type.toRemoteVehicleType(),
    brand = brand,
    model = model,
    licensePlate = licensePlate,
    currentOdometerKm = currentOdometerKm,
    updatedAt = epochMillisToIsoString(updatedAt),
    deletedAt = deletedAt?.let(::epochMillisToIsoString),
)

internal fun RemoteVehicleDto.toSyncVehicle(): SyncVehicle = SyncVehicle(
    id = id,
    familyId = familyId,
    name = name,
    type = vehicleType.toVehicleType(),
    brand = brand,
    model = model,
    licensePlate = licensePlate,
    currentOdometerKm = currentOdometerKm,
    updatedAt = isoStringToEpochMillis(updatedAt),
    pendingSync = false,
    deletedAt = deletedAt?.let(::isoStringToEpochMillis),
)

internal fun SyncMaintenanceRecord.toRemoteDto(): RemoteMaintenanceRecordDto = RemoteMaintenanceRecordDto(
    id = id,
    familyId = familyId,
    vehicleId = vehicleId,
    maintenanceTypeId = null,
    maintenanceTypeKey = maintenanceTypeId,
    maintenanceTypeCode = maintenanceTypeCode?.toRemoteMaintenanceTypeCode(),
    performedOn = performedOn,
    odometerKm = odometerKm,
    costCents = costCents,
    currency = currency,
    workshop = workshop,
    notes = notes,
    nextDueDate = nextDueDate,
    updatedAt = epochMillisToIsoString(updatedAt),
    deletedAt = deletedAt?.let(::epochMillisToIsoString),
)

internal fun RemoteMaintenanceRecordDto.toSyncMaintenanceRecord(): SyncMaintenanceRecord = SyncMaintenanceRecord(
    id = id,
    familyId = familyId,
    vehicleId = vehicleId,
    maintenanceTypeId = maintenanceTypeKey ?: maintenanceTypeId.orEmpty(),
    maintenanceTypeCode = maintenanceTypeCode?.toLocalMaintenanceTypeCode(),
    performedOn = performedOn,
    odometerKm = odometerKm,
    costCents = costCents,
    currency = currency,
    workshop = workshop,
    notes = notes,
    nextDueDate = nextDueDate,
    updatedAt = isoStringToEpochMillis(updatedAt),
    pendingSync = false,
    deletedAt = deletedAt?.let(::isoStringToEpochMillis),
)

internal fun SyncReminder.toRemoteDto(): RemoteReminderDto = RemoteReminderDto(
    id = id,
    familyId = familyId,
    vehicleId = vehicleId,
    maintenanceTypeId = null,
    maintenanceTypeKey = maintenanceTypeId,
    title = title,
    dueDate = dueDate,
    dueOdometerKm = dueOdometerKm,
    notifyDaysBefore = notifyDaysBefore,
    completedAt = if (isCompleted) epochMillisToIsoString(updatedAt) else null,
    updatedAt = epochMillisToIsoString(updatedAt),
    deletedAt = deletedAt?.let(::epochMillisToIsoString),
)

internal fun RemoteReminderDto.toSyncReminder(): SyncReminder = SyncReminder(
    id = id,
    familyId = familyId,
    vehicleId = vehicleId,
    maintenanceTypeId = maintenanceTypeKey ?: maintenanceTypeId,
    title = title,
    dueDate = dueDate,
    dueOdometerKm = dueOdometerKm,
    notifyDaysBefore = notifyDaysBefore,
    isCompleted = completedAt != null,
    updatedAt = isoStringToEpochMillis(updatedAt),
    pendingSync = false,
    deletedAt = deletedAt?.let(::isoStringToEpochMillis),
)

private fun VehicleType.toRemoteVehicleType(): String = when (this) {
    VehicleType.Car -> "car"
    VehicleType.Motorcycle -> "motorcycle"
    VehicleType.Van -> "van"
    VehicleType.Other -> "other"
}

private fun String.toVehicleType(): VehicleType = when (this) {
    "car" -> VehicleType.Car
    "motorcycle" -> VehicleType.Motorcycle
    "van" -> VehicleType.Van
    else -> VehicleType.Other
}

private fun String.toRemoteMaintenanceTypeCode(): String = when (this) {
    "Itv" -> "itv"
    "Insurance" -> "insurance"
    "OilChange" -> "oil_change"
    "Tires" -> "tires"
    "GeneralReview" -> "general_review"
    "Repair" -> "repair"
    else -> "custom"
}

private fun String.toLocalMaintenanceTypeCode(): String = when (this) {
    "itv" -> "Itv"
    "insurance" -> "Insurance"
    "oil_change" -> "OilChange"
    "tires" -> "Tires"
    "general_review" -> "GeneralReview"
    "repair" -> "Repair"
    else -> "Custom"
}
