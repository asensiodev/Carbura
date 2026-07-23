package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.maintenance.usecase.UpdateMaintenanceRecordInput
import com.asensiodev.carbura.core.domain.maintenance.usecase.UpdateMaintenanceRecordResult
import com.asensiodev.carbura.core.domain.maintenance.usecase.UpdateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderId
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.VehicleId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UpdateMaintenanceRecordUseCaseTest {
    private val familyId = FamilyId("family-1")
    private val vehicleId = VehicleId("vehicle-1")
    private val recordId = MaintenanceRecordId("record-1")
    private val maintenanceRepository = FakeMaintenanceRecordRepository()
    private val reminderRepository = FakeReminderRepository()
    private val useCase = UpdateMaintenanceRecordUseCase(maintenanceRepository, reminderRepository)

    @Test
    fun validUpdatePreservesIdentityAndReconcilesAutomaticReminder() =
        runTest {
            maintenanceRepository.savedRecords += originalRecord()

            val result = useCase(validInput())

            val success = assertIs<UpdateMaintenanceRecordResult.Success>(result)
            assertEquals(recordId, success.record.id)
            assertEquals(familyId, success.record.familyId)
            assertEquals(vehicleId, success.record.vehicleId)
            assertEquals("EUR", success.record.currency)
            assertEquals(120000, success.record.odometerKm)
            val mutation = assertIs<ReminderNotificationMutation.Upsert>(maintenanceRepository.notificationMutations.single())
            assertEquals(maintenanceReminderId(recordId), mutation.reminder.id)
            assertEquals(CalendarDate("2028-05-01"), mutation.reminder.dueDate)
        }

    @Test
    fun missingOrWrongOwnerDoesNotInsertOrMutateReminders() =
        runTest {
            maintenanceRepository.savedRecords += originalRecord()

            val result = useCase(validInput().copy(expectedFamilyId = FamilyId("other-family")))

            assertIs<UpdateMaintenanceRecordResult.NotFound>(result)
            assertEquals(originalRecord(), maintenanceRepository.savedRecords.single())
            assertTrue(maintenanceRepository.notificationMutations.isEmpty())
        }

    private fun originalRecord() =
        MaintenanceRecord(
            id = recordId,
            familyId = familyId,
            vehicleId = vehicleId,
            maintenanceTypeId = MaintenanceTypeId("type-repair"),
            maintenanceTypeCode = MaintenanceTypeCode.Repair,
            performedOn = CalendarDate("2025-01-01"),
            odometerKm = 100000,
        )

    private fun validInput() =
        UpdateMaintenanceRecordInput(
            scope = testFamilyScope,
            recordId = recordId,
            expectedFamilyId = familyId,
            expectedVehicleId = vehicleId,
            maintenanceTypeCode = MaintenanceTypeCode.Itv,
            customTypeLabel = null,
            performedOn = "2026-05-01",
            odometerKm = "120000",
            cost = "99.50",
            workshop = " Garage ",
            notes = " Updated ",
            nextDueDate = "2028-05-01",
            currentDate = CalendarDate("2026-01-01"),
        )
}
