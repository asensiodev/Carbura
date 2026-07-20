package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.model.VehicleType
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncDtosTest {
    @Test
    fun vehicleDtoRoundTripKeepsRemoteShape() {
        val vehicle =
            SyncVehicle(
                id = "vehicle-1",
                familyId = "family-1",
                name = "Moto",
                type = VehicleType.Motorcycle,
                brand = "Honda",
                model = null,
                licensePlate = null,
                currentOdometerKm = 1234,
                updatedAt = 1_700_000_000_000,
                pendingSync = true,
                deletedAt = null,
            )

        val dto = vehicle.toRemoteDto()

        assertEquals("motorcycle", dto.vehicleType)
        assertEquals(vehicle.copy(pendingSync = false), dto.toSyncVehicle())
    }

    @Test
    fun maintenanceDtoUsesLocalTypeKeyInsteadOfRemoteUuid() {
        val record =
            SyncMaintenanceRecord(
                id = "maintenance-1",
                familyId = "family-1",
                vehicleId = "vehicle-1",
                maintenanceTypeId = "type-oil-change",
                maintenanceTypeCode = "OilChange",
                maintenanceTypeLabel = "eBike ECU Check",
                performedOn = "2026-07-08",
                odometerKm = 1000,
                costCents = null,
                currency = "EUR",
                workshop = null,
                notes = null,
                nextDueDate = "2027-07-08",
                updatedAt = 1_700_000_000_000,
                pendingSync = true,
                deletedAt = null,
            )

        val dto = record.toRemoteDto()

        assertEquals(null, dto.maintenanceTypeId)
        assertEquals("type-oil-change", dto.maintenanceTypeKey)
        assertEquals("oil_change", dto.maintenanceTypeCode)
        assertEquals("eBike ECU Check", dto.maintenanceTypeLabel)
        assertEquals(record.copy(pendingSync = false), dto.toSyncMaintenanceRecord())
    }

    @Test
    fun canonicalMaintenanceAndGeneratedReminderDtoRoundTripsRemainUnchanged() {
        listOf("Itv" to "itv", "Insurance" to "insurance").forEach { (localCode, remoteCode) ->
            val record = maintenanceRecord(localCode)
            val dto = record.toRemoteDto()

            assertEquals(remoteCode, dto.maintenanceTypeCode)
            assertEquals("2027-07-08", dto.nextDueDate)
            assertEquals(record.copy(pendingSync = false), dto.toSyncMaintenanceRecord())
        }

        val reminder =
            SyncReminder(
                id = "maintenance-reminder:maintenance-1",
                familyId = "family-1",
                vehicleId = "vehicle-1",
                maintenanceTypeId = "type-itv",
                title = "Proxima ITV",
                dueDate = "2027-07-08",
                dueOdometerKm = null,
                notifyDaysBefore = 60,
                isCompleted = false,
                updatedAt = 1_700_000_000_000,
                pendingSync = true,
                deletedAt = null,
            )
        val reminderDto = reminder.toRemoteDto()

        assertEquals(reminder.copy(pendingSync = false), reminderDto.toSyncReminder())
    }

    private fun maintenanceRecord(code: String): SyncMaintenanceRecord =
        SyncMaintenanceRecord(
            id = "maintenance-1",
            familyId = "family-1",
            vehicleId = "vehicle-1",
            maintenanceTypeId = "type-${code.lowercase()}",
            maintenanceTypeCode = code,
            performedOn = "2026-07-08",
            odometerKm = 1000,
            costCents = null,
            currency = "EUR",
            workshop = null,
            notes = null,
            nextDueDate = "2027-07-08",
            updatedAt = 1_700_000_000_000,
            pendingSync = true,
            deletedAt = null,
        )
}
