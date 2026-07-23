package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType

internal val testFamilyId = FamilyId("family-1")
internal val testFamilyScope = ActiveFamilyScope(UserId("user-1"), testFamilyId, 1)

internal fun <T> T.familyScoped(): FamilyScoped<T> = FamilyScoped(testFamilyScope, this)

internal val testVehicleId = VehicleId("vehicle-1")
internal val testMaintenanceTypeId = MaintenanceTypeId("maintenance-type-1")

internal fun testVehicle(
    name: String = "Coche familiar",
    odometerKm: Int = 12000,
): Vehicle =
    Vehicle(
        id = testVehicleId,
        familyId = testFamilyId,
        name = name,
        type = VehicleType.Car,
        currentOdometerKm = odometerKm,
    )

internal fun testMaintenanceRecord(
    id: String = "record-1",
    performedOn: String = "2026-07-01",
    code: MaintenanceTypeCode? = MaintenanceTypeCode.Itv,
    odometerKm: Int? = 12000,
    costCents: Int? = 5500,
    nextDueDate: String? = null,
): MaintenanceRecord =
    MaintenanceRecord(
        id = MaintenanceRecordId(id),
        familyId = testFamilyId,
        vehicleId = testVehicleId,
        maintenanceTypeId = testMaintenanceTypeId,
        maintenanceTypeCode = code,
        performedOn = CalendarDate(performedOn),
        odometerKm = odometerKm,
        costCents = costCents,
        nextDueDate = nextDueDate?.let(::CalendarDate),
    )
