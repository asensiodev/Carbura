package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.maintenance.usecase.DeleteMaintenanceRecordUseCase
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeleteMaintenanceRecordUseCaseTest {
    @Test
    fun sourceDeletionDeletesOnlyDeterministicReminderAndCancelsItsPlan() =
        runTest {
            val maintenanceRepository = FakeMaintenanceRecordRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            reminderRepository.saveReminder(reminder("maintenance-reminder:record-1"))
            reminderRepository.saveReminder(reminder("manual-reminder"))

            DeleteMaintenanceRecordUseCase(maintenanceRepository, reminderRepository, scheduler)(
                MaintenanceRecordId("record-1"),
            )

            assertEquals(listOf(ReminderId("maintenance-reminder:record-1")), reminderRepository.deletedReminderIds)
            assertEquals(listOf("manual-reminder"), reminderRepository.savedReminders.map { it.id.value })
            assertEquals(listOf("maintenance-reminder:record-1"), scheduler.cancelledReminderIds)
        }

    @Test
    fun cancellationFailureLeavesReminderAndSourceRetryable() =
        runTest {
            val maintenanceRepository = sourceRepository()
            val reminderRepository = FakeReminderRepository().apply { saveReminder(reminder("maintenance-reminder:record-1")) }
            val scheduler = FakeReminderNotificationScheduler().apply { failCancels = true }

            assertFailsWith<IllegalStateException> {
                DeleteMaintenanceRecordUseCase(maintenanceRepository, reminderRepository, scheduler)(MaintenanceRecordId("record-1"))
            }

            assertEquals(1, maintenanceRepository.savedRecords.size)
            assertEquals(1, reminderRepository.savedReminders.size)
        }

    @Test
    fun reminderDeletionFailureLeavesSourceRetryable() =
        runTest {
            val maintenanceRepository = sourceRepository()
            val reminderRepository =
                FakeReminderRepository().apply {
                    saveReminder(reminder("maintenance-reminder:record-1"))
                    failDeletes = true
                }
            val scheduler = FakeReminderNotificationScheduler()

            assertFailsWith<IllegalStateException> {
                DeleteMaintenanceRecordUseCase(maintenanceRepository, reminderRepository, scheduler)(MaintenanceRecordId("record-1"))
            }

            assertEquals(1, maintenanceRepository.savedRecords.size)
            assertEquals(1, reminderRepository.savedReminders.size)
            assertEquals(listOf("maintenance-reminder:record-1"), scheduler.cancelledReminderIds)
        }

    @Test
    fun sourceDeletionFailureCanRetryAfterReminderWasTombstoned() =
        runTest {
            val maintenanceRepository = sourceRepository().apply { failDeletes = true }
            val reminderRepository = FakeReminderRepository().apply { saveReminder(reminder("maintenance-reminder:record-1")) }
            val scheduler = FakeReminderNotificationScheduler()
            val useCase = DeleteMaintenanceRecordUseCase(maintenanceRepository, reminderRepository, scheduler)

            assertFailsWith<IllegalStateException> { useCase(MaintenanceRecordId("record-1")) }
            assertEquals(1, maintenanceRepository.savedRecords.size)
            assertEquals(emptyList(), reminderRepository.savedReminders)

            maintenanceRepository.failDeletes = false
            useCase(MaintenanceRecordId("record-1"))

            assertEquals(emptyList(), maintenanceRepository.savedRecords)
            assertEquals(2, scheduler.cancelledReminderIds.size)
            assertEquals(2, reminderRepository.deletedReminderIds.size)
        }

    private suspend fun sourceRepository(): FakeMaintenanceRecordRepository =
        FakeMaintenanceRecordRepository().apply { saveMaintenanceRecord(testMaintenanceRecord()) }

    private fun reminder(id: String): Reminder =
        Reminder(
            id = ReminderId(id),
            familyId = testFamilyId,
            vehicleId = testVehicleId,
            maintenanceTypeId = null,
            title = "Reminder",
            dueOdometerKm = 100,
        )
}
