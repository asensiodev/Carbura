package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class ReminderUseCasesTest {
    private val familyId = FamilyId("family-test")
    private val vehicleId = VehicleId("vehicle-test")

    @Test
    fun validReminderIsSaved() = runTest {
        val repository = FakeReminderRepository()
        val useCase = CreateReminderUseCase(repository)
        val reminder = reminder(dueDate = "2026-07-10")

        val result = useCase(reminder)

        assertIs<DomainResult.Success<Reminder>>(result)
        assertEquals(listOf(reminder), repository.savedReminders)
    }

    @Test
    fun dateReminderSchedulesNotificationAfterSave() = runTest {
        val scheduler = FakeReminderNotificationScheduler()
        val reminder = reminder(id = "itv", dueDate = "2026-07-10")

        CreateReminderUseCase(FakeReminderRepository(), scheduler)(reminder)

        assertEquals(listOf("itv"), scheduler.scheduledReminderIds)
        assertEquals(emptyList(), scheduler.cancelledReminderIds)
    }

    @Test
    fun odometerOnlyReminderDoesNotScheduleDateNotification() = runTest {
        val scheduler = FakeReminderNotificationScheduler()
        val reminder = reminder(id = "oil", dueOdometerKm = 20000)

        CreateReminderUseCase(FakeReminderRepository(), scheduler)(reminder)

        assertEquals(emptyList(), scheduler.scheduledReminderIds)
        assertEquals(listOf("oil"), scheduler.cancelledReminderIds)
    }

    @Test
    fun blankTitleReturnsValidationError() = runTest {
        val result = CreateReminderUseCase(FakeReminderRepository())(reminder(title = " "))

        assertEquals(
            ValidationFailure.BlankReminderTitle,
            assertIs<DomainResult.ValidationError>(result).reason,
        )
    }

    @Test
    fun missingDueTargetReturnsValidationError() = runTest {
        val result = CreateReminderUseCase(FakeReminderRepository())(reminder())

        assertEquals(
            ValidationFailure.MissingReminderDueTarget,
            assertIs<DomainResult.ValidationError>(result).reason,
        )
    }

    @Test
    fun negativeDueOdometerReturnsValidationError() = runTest {
        val result = CreateReminderUseCase(FakeReminderRepository())(reminder(dueOdometerKm = -1))

        assertEquals(
            ValidationFailure.NegativeReminderDueOdometer,
            assertIs<DomainResult.ValidationError>(result).reason,
        )
    }

    @Test
    fun pendingRemindersAreSortedByDueTarget() = runTest {
        val repository = FakeReminderRepository()
        val useCase = GetPendingRemindersUseCase(repository)
        repository.saveReminder(reminder(id = "late", dueDate = "2026-08-01"))
        repository.saveReminder(reminder(id = "early", dueDate = "2026-07-01"))

        val reminders = useCase(familyId)

        assertEquals(listOf("early", "late"), reminders.map { it.id.value })
    }

    @Test
    fun completeReminderMarksItCompleted() = runTest {
        val repository = FakeReminderRepository()
        repository.saveReminder(reminder(id = "reminder-1", dueDate = "2026-07-01"))

        CompleteReminderUseCase(repository)(ReminderId("reminder-1"))

        assertEquals(emptyList(), repository.getPendingReminders(familyId))
    }

    @Test
    fun completeReminderCancelsNotification() = runTest {
        val scheduler = FakeReminderNotificationScheduler()

        CompleteReminderUseCase(FakeReminderRepository(), scheduler)(ReminderId("reminder-1"))

        assertEquals(listOf("reminder-1"), scheduler.cancelledReminderIds)
    }

    @Test
    fun deleteReminderCancelsNotification() = runTest {
        val scheduler = FakeReminderNotificationScheduler()

        DeleteReminderUseCase(FakeReminderRepository(), scheduler)(ReminderId("reminder-1"))

        assertEquals(listOf("reminder-1"), scheduler.cancelledReminderIds)
    }

    private fun reminder(
        id: String = "reminder-1",
        title: String = "Pasar ITV",
        dueDate: String? = null,
        dueOdometerKm: Int? = null,
    ): Reminder = Reminder(
        id = ReminderId(id),
        familyId = familyId,
        vehicleId = vehicleId,
        maintenanceTypeId = null,
        title = title,
        dueDate = dueDate?.let(::CalendarDate),
        dueOdometerKm = dueOdometerKm,
    )
}
