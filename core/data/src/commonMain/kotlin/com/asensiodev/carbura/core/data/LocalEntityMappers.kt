package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.MaintenanceRecords
import com.asensiodev.carbura.core.data.local.Reminders
import com.asensiodev.carbura.core.data.local.Vehicles
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType

internal fun Vehicles.toVehicle(): Vehicle = Vehicle(
    id = VehicleId(id),
    familyId = FamilyId(familyId),
    name = name,
    type = VehicleType.valueOf(type),
    brand = brand,
    model = model,
    licensePlate = licensePlate,
    currentOdometerKm = currentOdometerKm.toInt(),
)

internal fun MaintenanceRecords.toMaintenanceRecord(): MaintenanceRecord = MaintenanceRecord(
    id = MaintenanceRecordId(id),
    familyId = FamilyId(familyId),
    vehicleId = VehicleId(vehicleId),
    maintenanceTypeId = MaintenanceTypeId(maintenanceTypeId),
    maintenanceTypeCode = maintenanceTypeCode?.let(MaintenanceTypeCode::valueOf),
    performedOn = CalendarDate(performedOn),
    odometerKm = odometerKm?.toInt(),
    costCents = costCents?.toInt(),
    currency = currency,
    workshop = workshop,
    notes = notes,
    nextDueDate = nextDueDate?.let(::CalendarDate),
)

internal fun Reminders.toReminder(): Reminder = Reminder(
    id = ReminderId(id),
    familyId = FamilyId(familyId),
    vehicleId = VehicleId(vehicleId),
    maintenanceTypeId = maintenanceTypeId?.let(::MaintenanceTypeId),
    title = title,
    dueDate = dueDate?.let(::CalendarDate),
    dueOdometerKm = dueOdometerKm?.toInt(),
    notifyDaysBefore = notifyDaysBefore.toInt(),
    isCompleted = isCompleted == 1L,
)

internal fun Vehicles.toSyncVehicle(): SyncVehicle = SyncVehicle(
    id = id,
    familyId = familyId,
    name = name,
    type = VehicleType.valueOf(type),
    brand = brand,
    model = model,
    licensePlate = licensePlate,
    currentOdometerKm = currentOdometerKm.toInt(),
    updatedAt = updatedAt,
    pendingSync = pendingSync == 1L,
    deletedAt = deletedAt,
)

internal fun MaintenanceRecords.toSyncMaintenanceRecord(): SyncMaintenanceRecord = SyncMaintenanceRecord(
    id = id,
    familyId = familyId,
    vehicleId = vehicleId,
    maintenanceTypeId = maintenanceTypeId,
    maintenanceTypeCode = maintenanceTypeCode,
    performedOn = performedOn,
    odometerKm = odometerKm?.toInt(),
    costCents = costCents?.toInt(),
    currency = currency,
    workshop = workshop,
    notes = notes,
    nextDueDate = nextDueDate,
    updatedAt = updatedAt,
    pendingSync = pendingSync == 1L,
    deletedAt = deletedAt,
)

internal fun Reminders.toSyncReminder(): SyncReminder = SyncReminder(
    id = id,
    familyId = familyId,
    vehicleId = vehicleId,
    maintenanceTypeId = maintenanceTypeId,
    title = title,
    dueDate = dueDate,
    dueOdometerKm = dueOdometerKm?.toInt(),
    notifyDaysBefore = notifyDaysBefore.toInt(),
    isCompleted = isCompleted == 1L,
    updatedAt = updatedAt,
    pendingSync = pendingSync == 1L,
    deletedAt = deletedAt,
)
