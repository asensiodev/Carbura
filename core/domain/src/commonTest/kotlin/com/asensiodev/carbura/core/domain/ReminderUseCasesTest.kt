package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.usecase.CompleteReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.DeleteReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.GetPendingRemindersUseCase
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.VehicleId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ReminderUseCasesTest {
    private val familyId = FamilyId("family-test")
    private val vehicleId = VehicleId("vehicle-test")
    private val scope = ActiveFamilyScope(UserId("user-test"), familyId, 1)

    @Test
    fun validReminderIsSaved() =
        runTest {
            val repository = FakeReminderRepository()
            val useCase = CreateReminderUseCase(repository, FakeReminderNotificationScheduler())
            val reminder = reminder(dueDate = "2026-07-10")

            val result = useCase(FamilyScoped(scope, reminder))

            assertIs<DomainResult.Success<Reminder>>(result)
            assertEquals(listOf(reminder), repository.savedReminders)
        }

    @Test
    fun dateReminderRecordsScheduleIntentAfterSave() =
        runTest {
            val scheduler = FakeReminderNotificationScheduler()
            val reminder = reminder(id = "itv", dueDate = "2026-07-10")

            val repository = FakeReminderRepository()
            CreateReminderUseCase(repository, scheduler)(FamilyScoped(scope, reminder))

            val mutation = repository.notificationMutations.single() as ReminderNotificationMutation.Upsert
            assertEquals(reminder, mutation.reminder)
            assertEquals(
                "itv",
                mutation.notificationPlan
                    ?.reminder
                    ?.id
                    ?.value,
            )
            assertEquals(emptyList(), scheduler.scheduledReminderIds)
        }

    @Test
    fun odometerOnlyReminderRecordsCancelIntent() =
        runTest {
            val scheduler = FakeReminderNotificationScheduler()
            val reminder = reminder(id = "oil", dueOdometerKm = 20000)

            val repository = FakeReminderRepository()
            CreateReminderUseCase(repository, scheduler)(FamilyScoped(scope, reminder))

            assertEquals(emptyList(), scheduler.scheduledReminderIds)
            assertEquals(null, (repository.notificationMutations.single() as ReminderNotificationMutation.Upsert).notificationPlan)
        }

    @Test
    fun blankTitleReturnsValidationError() =
        runTest {
            val result =
                CreateReminderUseCase(
                    FakeReminderRepository(),
                    FakeReminderNotificationScheduler(),
                )(FamilyScoped(scope, reminder(title = " ")))

            assertEquals(
                ValidationFailure.BlankReminderTitle,
                assertIs<DomainResult.ValidationError>(result).reason,
            )
        }

    @Test
    fun missingDueTargetReturnsValidationError() =
        runTest {
            val result =
                CreateReminderUseCase(FakeReminderRepository(), FakeReminderNotificationScheduler())(FamilyScoped(scope, reminder()))

            assertEquals(
                ValidationFailure.MissingReminderDueTarget,
                assertIs<DomainResult.ValidationError>(result).reason,
            )
        }

    @Test
    fun negativeDueOdometerReturnsValidationError() =
        runTest {
            val result =
                CreateReminderUseCase(
                    FakeReminderRepository(),
                    FakeReminderNotificationScheduler(),
                )(FamilyScoped(scope, reminder(dueOdometerKm = -1)))

            assertEquals(
                ValidationFailure.NegativeReminderDueOdometer,
                assertIs<DomainResult.ValidationError>(result).reason,
            )
        }

    @Test
    fun pendingRemindersAreSortedByDueTarget() =
        runTest {
            val repository = FakeReminderRepository()
            val useCase = GetPendingRemindersUseCase(repository)
            repository.saveReminder(scope, reminder(id = "late", dueDate = "2026-08-01"))
            repository.saveReminder(scope, reminder(id = "early", dueDate = "2026-07-01"))

            val reminders = useCase(scope)

            assertEquals(listOf("early", "late"), reminders.map { it.id.value })
        }

    @Test
    fun completeReminderMarksItCompleted() =
        runTest {
            val repository = FakeReminderRepository()
            repository.saveReminder(scope, reminder(id = "reminder-1", dueDate = "2026-07-01"))

            CompleteReminderUseCase(repository, FakeReminderNotificationScheduler())(FamilyScoped(scope, ReminderId("reminder-1")))

            assertEquals(emptyList(), repository.getPendingReminders(scope))
        }

    @Test
    fun completeReminderRecordsCancelIntent() =
        runTest {
            val scheduler = FakeReminderNotificationScheduler()

            val repository = FakeReminderRepository()
            CompleteReminderUseCase(repository, scheduler)(FamilyScoped(scope, ReminderId("reminder-1")))

            assertEquals(
                ReminderNotificationMutation.Delete(ReminderId("reminder-1")),
                repository.notificationMutations.single(),
            )
            assertEquals(emptyList(), scheduler.cancelledReminderIds)
        }

    @Test
    fun deleteReminderRecordsCancelIntent() =
        runTest {
            val scheduler = FakeReminderNotificationScheduler()

            val repository = FakeReminderRepository()
            DeleteReminderUseCase(repository, scheduler)(FamilyScoped(scope, ReminderId("reminder-1")))

            assertEquals(
                ReminderNotificationMutation.Delete(ReminderId("reminder-1")),
                repository.notificationMutations.single(),
            )
            assertEquals(emptyList(), scheduler.cancelledReminderIds)
        }

    private fun reminder(
        id: String = "reminder-1",
        title: String = "Pasar ITV",
        dueDate: String? = null,
        dueOdometerKm: Int? = null,
    ): Reminder =
        Reminder(
            id = ReminderId(id),
            familyId = familyId,
            vehicleId = vehicleId,
            maintenanceTypeId = null,
            title = title,
            dueDate = dueDate?.let(::CalendarDate),
            dueOdometerKm = dueOdometerKm,
        )
}
