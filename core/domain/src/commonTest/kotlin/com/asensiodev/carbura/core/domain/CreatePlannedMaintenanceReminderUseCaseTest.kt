package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.usecase.CreatePlannedMaintenanceReminderUseCase
import com.asensiodev.carbura.core.model.CalendarDate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CreatePlannedMaintenanceReminderUseCaseTest {
    @Test
    fun createsDeterministicReminderForSavedMaintenanceDate() =
        runTest {
            val repository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val useCase = CreatePlannedMaintenanceReminderUseCase(repository, scheduler)
            val record = testMaintenanceRecord(performedOn = "2027-08-14")

            val reminder = useCase(record.familyScoped())

            assertEquals("planned-maintenance-reminder:record-1", reminder.id.value)
            assertEquals(CalendarDate("2027-08-14"), reminder.dueDate)
            assertEquals("Mantenimiento programado", reminder.title)
            assertEquals(0, reminder.notifyDaysBefore)
            assertEquals(listOf(reminder), repository.savedReminders)
            assertEquals(
                listOf(ReminderAlertKind.Manual),
                (repository.notificationMutations.single() as ReminderNotificationMutation.Upsert)
                    .notificationPlan
                    ?.alerts
                    .orEmpty()
                    .map { it.kind },
            )
            assertEquals(
                listOf(0),
                (repository.notificationMutations.single() as ReminderNotificationMutation.Upsert)
                    .notificationPlan
                    ?.alerts
                    .orEmpty()
                    .map { it.daysBefore },
            )
        }

    @Test
    fun retryConvergesOnOneReminderAndOneAlarmIdentity() =
        runTest {
            val repository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val useCase = CreatePlannedMaintenanceReminderUseCase(repository, scheduler)
            val record = testMaintenanceRecord(performedOn = "2027-08-14")

            useCase(record.familyScoped())
            useCase(record.familyScoped())

            assertEquals(1, repository.savedReminders.size)
            assertEquals(repository.notificationMutations[0], repository.notificationMutations[1])
            assertEquals(emptyList(), scheduler.scheduledPlans)
        }
}
