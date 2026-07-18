package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordInput
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateAutomaticReminderUseCase
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class MaintenanceCreationOrchestrationTest {
    @Test
    fun canonicalItvInputKeepsCodeAndOptionalNextDueDate() =
        runTest {
            val repository = FakeMaintenanceRecordRepository()
            val useCase = CreateMaintenanceRecordFromInputUseCase(CreateMaintenanceRecordUseCase(repository))

            val result = useCase(input(MaintenanceTypeCode.Itv, nextDueDate = "2027-07-01"))

            val record = assertIs<DomainResult.Success<*>>(result).value as com.asensiodev.carbura.core.model.MaintenanceRecord
            assertEquals(MaintenanceTypeCode.Itv, record.maintenanceTypeCode)
            assertEquals("type-itv", record.maintenanceTypeId.value)
            assertEquals(CalendarDate("2027-07-01"), record.nextDueDate)
        }

    @Test
    fun canonicalInsuranceDoesNotRequireCustomLabel() =
        runTest {
            val repository = FakeMaintenanceRecordRepository()

            val result =
                CreateMaintenanceRecordFromInputUseCase(CreateMaintenanceRecordUseCase(repository))(
                    input(MaintenanceTypeCode.Insurance, nextDueDate = ""),
                )

            val record = assertIs<DomainResult.Success<*>>(result).value as com.asensiodev.carbura.core.model.MaintenanceRecord
            assertEquals(MaintenanceTypeCode.Insurance, record.maintenanceTypeCode)
            assertNull(record.nextDueDate)
        }

    @Test
    fun customTypeRequiresCustomLabel() =
        runTest {
            val result =
                CreateMaintenanceRecordFromInputUseCase(CreateMaintenanceRecordUseCase(FakeMaintenanceRecordRepository()))(
                    input(MaintenanceTypeCode.Custom, customTypeLabel = ""),
                )

            assertEquals(
                DomainResult.ValidationError(ValidationFailure.BlankMaintenanceType),
                result,
            )
        }

    @Test
    fun impossiblePerformedDateIsRejectedBeforePersistence() =
        runTest {
            val repository = FakeMaintenanceRecordRepository()
            val invalid = input(MaintenanceTypeCode.Itv).copy(performedOn = "2027-02-29")

            val result = CreateMaintenanceRecordFromInputUseCase(CreateMaintenanceRecordUseCase(repository))(invalid)

            assertEquals(DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceDate), result)
            assertEquals(emptyList(), repository.savedRecords)
        }

    @Test
    fun nextDueDateAcceptsLeapDayAndRejectsImpossibleDate() =
        runTest {
            val validRepository = FakeMaintenanceRecordRepository()
            val useCase = CreateMaintenanceRecordFromInputUseCase(CreateMaintenanceRecordUseCase(validRepository))

            val valid = useCase(input(MaintenanceTypeCode.Insurance, "2028-02-29"))
            val invalid = useCase(input(MaintenanceTypeCode.Insurance, "2028-02-30"))

            assertIs<DomainResult.Success<*>>(valid)
            assertEquals(DomainResult.ValidationError(ValidationFailure.InvalidMaintenanceDate), invalid)
        }

    @Test
    fun unsupportedCanonicalInputDiscardsNextDueDate() =
        runTest {
            val repository = FakeMaintenanceRecordRepository()

            val result =
                CreateMaintenanceRecordFromInputUseCase(CreateMaintenanceRecordUseCase(repository))(
                    input(MaintenanceTypeCode.Repair, "2027-07-01"),
                )

            assertIs<DomainResult.Success<*>>(result)
            assertNull(repository.savedRecords.single().nextDueDate)
        }

    @Test
    fun orchestrationPersistsMaintenanceReminderAndPlan() =
        runTest {
            val maintenanceRepository = FakeMaintenanceRecordRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val useCase =
                CreateMaintenanceWithReminderUseCase(
                    CreateMaintenanceRecordUseCase(maintenanceRepository),
                    CreateAutomaticReminderUseCase(reminderRepository, scheduler),
                )
            val record = testMaintenanceRecord(code = MaintenanceTypeCode.Itv, nextDueDate = "2027-07-01")

            val result = useCase(record)

            val creation = assertIs<DomainResult.Success<*>>(result).value
            assertEquals(listOf(record), maintenanceRepository.savedRecords)
            assertEquals(1, reminderRepository.savedReminders.size)
            assertEquals(1, scheduler.scheduledPlans.size)
            assertEquals(
                "maintenance-reminder:record-1",
                (creation as com.asensiodev.carbura.core.domain.maintenance.usecase.MaintenanceCreationResult)
                    .generatedReminder
                    ?.reminder
                    ?.id
                    ?.value,
            )
        }

    @Test
    fun inputOrchestrationValidatesAndCreatesGeneratedReminder() =
        runTest {
            val maintenanceRepository = FakeMaintenanceRecordRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val useCase =
                CreateMaintenanceWithReminderFromInputUseCase(
                    CreateMaintenanceWithReminderUseCase(
                        CreateMaintenanceRecordUseCase(maintenanceRepository),
                        CreateAutomaticReminderUseCase(reminderRepository, scheduler),
                    ),
                )

            val result = useCase(input(MaintenanceTypeCode.Insurance, "2027-07-01"))

            assertIs<DomainResult.Success<*>>(result)
            assertEquals(
                MaintenanceTypeCode.Insurance,
                maintenanceRepository.savedRecords.single().maintenanceTypeCode,
            )
            assertEquals(
                "maintenance-reminder:record-1",
                reminderRepository.savedReminders
                    .single()
                    .id.value,
            )
            assertEquals(
                listOf(45, 37, 7),
                scheduler.scheduledPlans
                    .single()
                    .alerts
                    .map { it.daysBefore },
            )
        }

    @Test
    fun retryUsesSameMaintenanceReminderAndAlertPlan() =
        runTest {
            val maintenanceRepository = FakeMaintenanceRecordRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val useCase =
                CreateMaintenanceWithReminderUseCase(
                    CreateMaintenanceRecordUseCase(maintenanceRepository),
                    CreateAutomaticReminderUseCase(reminderRepository, scheduler),
                )
            val record = testMaintenanceRecord(code = MaintenanceTypeCode.Insurance, nextDueDate = "2027-07-01")

            useCase(record)
            useCase(record)

            assertEquals(1, maintenanceRepository.savedRecords.size)
            assertEquals(1, reminderRepository.savedReminders.size)
            assertEquals(scheduler.scheduledPlans[0], scheduler.scheduledPlans[1])
        }

    private fun input(
        code: MaintenanceTypeCode,
        nextDueDate: String = "",
        customTypeLabel: String? = null,
    ) = CreateMaintenanceRecordInput(
        id = MaintenanceRecordId("record-1"),
        familyId = testFamilyId,
        vehicleId = testVehicleId,
        type = "",
        performedOn = "2026-07-01",
        odometerKm = "12000",
        cost = "55.00",
        workshop = "Workshop",
        notes = "Notes",
        maintenanceTypeCode = code,
        customTypeLabel = customTypeLabel,
        nextDueDate = nextDueDate,
    )
}
