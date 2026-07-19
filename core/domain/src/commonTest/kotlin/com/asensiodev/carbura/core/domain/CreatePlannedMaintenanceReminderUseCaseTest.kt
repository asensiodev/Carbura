package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
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

            val reminder = useCase(record)

            assertEquals("planned-maintenance-reminder:record-1", reminder.id.value)
            assertEquals(CalendarDate("2027-08-14"), reminder.dueDate)
            assertEquals("Mantenimiento programado", reminder.title)
            assertEquals(0, reminder.notifyDaysBefore)
            assertEquals(listOf(reminder), repository.savedReminders)
            assertEquals(
                listOf(ReminderAlertKind.Manual),
                scheduler.scheduledPlans
                    .single()
                    .alerts
                    .map { it.kind },
            )
            assertEquals(
                listOf(0),
                scheduler.scheduledPlans
                    .single()
                    .alerts
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

            useCase(record)
            useCase(record)

            assertEquals(1, repository.savedReminders.size)
            assertEquals(scheduler.scheduledPlans[0], scheduler.scheduledPlans[1])
        }
}
