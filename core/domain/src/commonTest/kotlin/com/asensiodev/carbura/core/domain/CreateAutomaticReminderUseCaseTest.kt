package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateAutomaticReminderUseCase
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.ReminderId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CreateAutomaticReminderUseCaseTest {
    @Test
    fun itvRecordWithDueDateCreatesDeterministicReminderAndPlan() =
        runTest {
            val repository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val useCase = CreateAutomaticReminderUseCase(repository, scheduler)
            val record = testMaintenanceRecord(code = MaintenanceTypeCode.Itv, nextDueDate = "2027-07-01")

            val generated = useCase(record)

            assertEquals(ReminderId("maintenance-reminder:record-1"), generated?.reminder?.id)
            assertEquals("Proxima ITV", generated?.reminder?.title)
            assertEquals(CalendarDate("2027-07-01"), generated?.reminder?.dueDate)
            assertEquals(60, generated?.reminder?.notifyDaysBefore)
            assertEquals(listOf(generated?.reminder), repository.savedReminders)
            assertEquals(
                generated?.notificationPlan,
                (repository.notificationMutations.single() as ReminderNotificationMutation.Upsert).notificationPlan,
            )
            assertEquals(emptyList(), scheduler.scheduledPlans)
            assertEquals(
                listOf(ReminderAlertKind.Itv60Days, ReminderAlertKind.Itv30Days, ReminderAlertKind.Itv7Days),
                generated?.notificationPlan?.alerts?.map { it.kind },
            )
        }

    @Test
    fun insuranceRecordWithDueDateCreatesPrimary45DayReminder() =
        runTest {
            val repository = FakeReminderRepository()
            val generated =
                CreateAutomaticReminderUseCase(repository, FakeReminderNotificationScheduler())(
                    testMaintenanceRecord(code = MaintenanceTypeCode.Insurance, nextDueDate = "2027-07-01"),
                )

            assertEquals("Proximo seguro", generated?.reminder?.title)
            assertEquals(45, generated?.reminder?.notifyDaysBefore)
            assertEquals(listOf(generated?.reminder), repository.savedReminders)
        }

    @Test
    fun retryUpsertsSameLogicalReminderAndAlertIdentities() =
        runTest {
            val repository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val useCase = CreateAutomaticReminderUseCase(repository, scheduler)
            val record = testMaintenanceRecord(code = MaintenanceTypeCode.Itv, nextDueDate = "2027-07-01")

            val first = useCase(record)
            val retry = useCase(record)

            assertEquals(first?.reminder?.id, retry?.reminder?.id)
            assertEquals(first?.notificationPlan, retry?.notificationPlan)
            assertEquals(1, repository.savedReminders.size)
        }

    @Test
    fun recordWithoutDueDateSkipsReminder() =
        runTest {
            val repository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()

            val generated = CreateAutomaticReminderUseCase(repository, scheduler)(testMaintenanceRecord())

            assertNull(generated)
            assertEquals(emptyList(), repository.savedReminders)
            assertEquals(listOf(ReminderId("maintenance-reminder:record-1")), repository.deletedReminderIds)
            assertEquals(
                ReminderNotificationMutation.Delete(ReminderId("maintenance-reminder:record-1")),
                repository.notificationMutations.single(),
            )
            assertEquals(emptyList(), scheduler.cancelledReminderIds)
        }

    @Test
    fun nonReminderMaintenanceSkipsReminder() =
        runTest {
            val repository = FakeReminderRepository()

            val generated =
                CreateAutomaticReminderUseCase(repository, FakeReminderNotificationScheduler())(
                    testMaintenanceRecord(code = MaintenanceTypeCode.OilChange, nextDueDate = "2027-07-01"),
                )

            assertNull(generated)
            assertEquals(emptyList(), repository.savedReminders)
        }

    @Test
    fun schedulerFailureCannotInterruptDurableReminderIntent() =
        runTest {
            val repository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler().apply { failSchedules = true }
            val useCase = CreateAutomaticReminderUseCase(repository, scheduler)
            val eligible = testMaintenanceRecord(code = MaintenanceTypeCode.Itv, nextDueDate = "2027-07-01")
            useCase(eligible)
            assertEquals(1, repository.savedReminders.size)

            scheduler.failSchedules = false
            val retry = useCase(eligible.copy(nextDueDate = null))

            assertNull(retry)
            assertEquals(emptyList(), repository.savedReminders)
            assertEquals(listOf(ReminderId("maintenance-reminder:record-1")), repository.deletedReminderIds)
            assertEquals(emptyList(), scheduler.cancelledReminderIds)
        }

    @Test
    fun retryAsUnsupportedTypeReconcilesPriorGeneratedReminder() =
        runTest {
            val repository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val useCase = CreateAutomaticReminderUseCase(repository, scheduler)
            val eligible = testMaintenanceRecord(code = MaintenanceTypeCode.Insurance, nextDueDate = "2027-07-01")
            useCase(eligible)

            val retry = useCase(eligible.copy(maintenanceTypeCode = MaintenanceTypeCode.Repair))

            assertNull(retry)
            assertEquals(emptyList(), repository.savedReminders)
            assertEquals(listOf(ReminderId("maintenance-reminder:record-1")), repository.deletedReminderIds)
            assertEquals(
                ReminderNotificationMutation.Delete(ReminderId("maintenance-reminder:record-1")),
                repository.notificationMutations.last(),
            )
            assertEquals(emptyList(), scheduler.cancelledReminderIds)
        }
}
