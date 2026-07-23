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
    fun sourceDeletionDelegatesBothDeterministicCancelsAtomically() =
        runTest {
            val maintenanceRepository = FakeMaintenanceRecordRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            reminderRepository.saveReminder(testFamilyScope, reminder("maintenance-reminder:record-1"))
            reminderRepository.saveReminder(testFamilyScope, reminder("planned-maintenance-reminder:record-1"))
            reminderRepository.saveReminder(testFamilyScope, reminder("manual-reminder"))

            DeleteMaintenanceRecordUseCase(maintenanceRepository, reminderRepository, scheduler)(
                MaintenanceRecordId("record-1").familyScoped(),
            )

            assertEquals(
                listOf(
                    ReminderId("maintenance-reminder:record-1"),
                    ReminderId("planned-maintenance-reminder:record-1"),
                ),
                maintenanceRepository.notificationCancellationIds,
            )
            assertEquals(3, reminderRepository.savedReminders.size)
            assertEquals(emptyList(), scheduler.cancelledReminderIds)
        }

    @Test
    fun schedulerFailureCannotRollBackAtomicSourceDeletion() =
        runTest {
            val maintenanceRepository = sourceRepository()
            val reminderRepository =
                FakeReminderRepository().apply {
                    saveReminder(
                        testFamilyScope,
                        reminder("maintenance-reminder:record-1"),
                    )
                }
            val scheduler = FakeReminderNotificationScheduler().apply { failCancels = true }

            DeleteMaintenanceRecordUseCase(
                maintenanceRepository,
                reminderRepository,
                scheduler,
            )(MaintenanceRecordId("record-1").familyScoped())

            assertEquals(0, maintenanceRepository.savedRecords.size)
            assertEquals(1, reminderRepository.savedReminders.size)
            assertEquals(2, maintenanceRepository.notificationCancellationIds.size)
        }

    @Test
    fun legacyReminderRepositoryFailureCannotRollBackAtomicSourceDeletion() =
        runTest {
            val maintenanceRepository = sourceRepository()
            val reminderRepository =
                FakeReminderRepository().apply {
                    saveReminder(testFamilyScope, reminder("maintenance-reminder:record-1"))
                    failDeletes = true
                }
            val scheduler = FakeReminderNotificationScheduler()

            DeleteMaintenanceRecordUseCase(
                maintenanceRepository,
                reminderRepository,
                scheduler,
            )(MaintenanceRecordId("record-1").familyScoped())

            assertEquals(0, maintenanceRepository.savedRecords.size)
            assertEquals(1, reminderRepository.savedReminders.size)
            assertEquals(emptyList(), scheduler.cancelledReminderIds)
        }

    @Test
    fun atomicSourceDeletionFailureCanRetryWithoutPartialReminderWork() =
        runTest {
            val maintenanceRepository = sourceRepository().apply { failDeletes = true }
            val reminderRepository =
                FakeReminderRepository().apply {
                    saveReminder(
                        testFamilyScope,
                        reminder("maintenance-reminder:record-1"),
                    )
                }
            val scheduler = FakeReminderNotificationScheduler()
            val useCase = DeleteMaintenanceRecordUseCase(maintenanceRepository, reminderRepository, scheduler)

            assertFailsWith<IllegalStateException> { useCase(MaintenanceRecordId("record-1").familyScoped()) }
            assertEquals(1, maintenanceRepository.savedRecords.size)
            assertEquals(1, reminderRepository.savedReminders.size)
            assertEquals(emptyList(), maintenanceRepository.notificationCancellationIds)

            maintenanceRepository.failDeletes = false
            useCase(MaintenanceRecordId("record-1").familyScoped())

            assertEquals(emptyList(), maintenanceRepository.savedRecords)
            assertEquals(2, maintenanceRepository.notificationCancellationIds.size)
            assertEquals(emptyList(), scheduler.cancelledReminderIds)
            assertEquals(emptyList(), reminderRepository.deletedReminderIds)
        }

    private suspend fun sourceRepository(): FakeMaintenanceRecordRepository =
        FakeMaintenanceRecordRepository().apply { saveMaintenanceRecord(testFamilyScope, testMaintenanceRecord()) }

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
