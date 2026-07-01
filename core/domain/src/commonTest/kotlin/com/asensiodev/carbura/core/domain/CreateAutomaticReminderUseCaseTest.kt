package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.ReminderId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CreateAutomaticReminderUseCaseTest {
    @Test
    fun itvRecordWithDueDateCreatesReminder() = runTest {
        val repository = FakeReminderRepository()
        val useCase = CreateAutomaticReminderUseCase(repository) { ReminderId("reminder-1") }
        val record = testMaintenanceRecord(
            code = MaintenanceTypeCode.Itv,
            nextDueDate = "2027-07-01",
        )

        val reminder = useCase(record)

        assertEquals(ReminderId("reminder-1"), reminder?.id)
        assertEquals("Proxima ITV", reminder?.title)
        assertEquals(CalendarDate("2027-07-01"), reminder?.dueDate)
        assertEquals(30, reminder?.notifyDaysBefore)
        assertEquals(listOf(reminder), repository.savedReminders)
    }

    @Test
    fun insuranceRecordWithDueDateCreatesReminder() = runTest {
        val repository = FakeReminderRepository()
        val useCase = CreateAutomaticReminderUseCase(repository) { ReminderId("reminder-1") }
        val record = testMaintenanceRecord(
            code = MaintenanceTypeCode.Insurance,
            nextDueDate = "2027-07-01",
        )

        val reminder = useCase(record)

        assertEquals("Proximo seguro", reminder?.title)
        assertEquals(listOf(reminder), repository.savedReminders)
    }

    @Test
    fun recordWithoutDueDateSkipsReminder() = runTest {
        val repository = FakeReminderRepository()
        val useCase = CreateAutomaticReminderUseCase(repository) { ReminderId("reminder-1") }

        val reminder = useCase(testMaintenanceRecord(nextDueDate = null))

        assertNull(reminder)
        assertEquals(emptyList(), repository.savedReminders)
    }

    @Test
    fun nonReminderMaintenanceSkipsReminder() = runTest {
        val repository = FakeReminderRepository()
        val useCase = CreateAutomaticReminderUseCase(repository) { ReminderId("reminder-1") }
        val record = testMaintenanceRecord(
            code = MaintenanceTypeCode.OilChange,
            nextDueDate = "2027-07-01",
        )

        val reminder = useCase(record)

        assertNull(reminder)
        assertEquals(emptyList(), repository.savedReminders)
    }
}
