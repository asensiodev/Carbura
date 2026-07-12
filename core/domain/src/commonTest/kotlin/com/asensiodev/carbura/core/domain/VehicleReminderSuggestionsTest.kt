package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.usecase.DeriveVehicleReminderSuggestionsUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.SaveVehicleWithRemindersParams
import com.asensiodev.carbura.core.domain.reminder.usecase.SaveVehicleWithRemindersUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.VehicleReminderKind
import com.asensiodev.carbura.core.domain.reminder.usecase.vehicleReminderId
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VehicleReminderSuggestionsTest {
    @Test
    fun populatedTargetsProduceStableSuggestions() {
        val vehicle =
            testVehicle().copy(
                nextItvDate = CalendarDate("2027-05-10"),
                insuranceRenewalDate = CalendarDate("2027-01-20"),
                nextServiceOdometerKm = 25000,
            )

        val suggestions = DeriveVehicleReminderSuggestionsUseCase()(vehicle)

        assertEquals(VehicleReminderKind.entries, suggestions.map { it.kind })
        assertEquals("vehicle-reminder:${vehicle.id.value}:itv", suggestions[0].reminder.id.value)
        assertEquals(CalendarDate("2027-05-10"), suggestions[0].reminder.dueDate)
        assertEquals(25000, suggestions[2].reminder.dueOdometerKm)
    }

    @Test
    fun emptyTargetsProduceNoSuggestions() {
        assertTrue(DeriveVehicleReminderSuggestionsUseCase()(testVehicle()).isEmpty())
    }

    @Test
    fun confirmedReconciliationUpsertsOwnedRemindersAndPreservesManualReminder() =
        runTest {
            val vehicleRepository = FakeVehicleRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val vehicle = testVehicle().copy(nextItvDate = CalendarDate("2027-05-10"))
            val manual =
                Reminder(
                    id = ReminderId("manual"),
                    familyId = vehicle.familyId,
                    vehicleId = vehicle.id,
                    maintenanceTypeId = null,
                    title = "Manual",
                    dueOdometerKm = 20000,
                )
            reminderRepository.saveReminder(manual)
            val useCase = SaveVehicleWithRemindersUseCase(vehicleRepository, reminderRepository, scheduler)

            useCase(SaveVehicleWithRemindersParams(vehicle, reconcileGeneratedReminders = true))
            useCase(
                SaveVehicleWithRemindersParams(
                    vehicle.copy(nextItvDate = CalendarDate("2027-06-10")),
                    reconcileGeneratedReminders = true,
                ),
            )

            assertEquals(2, reminderRepository.savedReminders.size)
            assertEquals(CalendarDate("2027-06-10"), reminderRepository.savedReminders.single { it.id != manual.id }.dueDate)
            assertEquals(2, scheduler.scheduledReminders.size)
        }

    @Test
    fun clearingTargetDeletesAndCancelsOnlyOwnedReminder() =
        runTest {
            val vehicleRepository = FakeVehicleRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val vehicle = testVehicle().copy(nextItvDate = CalendarDate("2027-05-10"))
            val useCase = SaveVehicleWithRemindersUseCase(vehicleRepository, reminderRepository, scheduler)
            useCase(SaveVehicleWithRemindersParams(vehicle, true))

            useCase(SaveVehicleWithRemindersParams(vehicle.copy(nextItvDate = null), true))

            assertTrue(reminderRepository.savedReminders.isEmpty())
            assertTrue(vehicleReminderId(vehicle.id, VehicleReminderKind.Itv).value in scheduler.cancelledReminderIds)
        }

    @Test
    fun declinedSuggestionsSaveVehicleWithoutReminderMutation() =
        runTest {
            val vehicleRepository = FakeVehicleRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val vehicle = testVehicle().copy(nextItvDate = CalendarDate("2027-05-10"))

            SaveVehicleWithRemindersUseCase(vehicleRepository, reminderRepository, scheduler)(
                SaveVehicleWithRemindersParams(vehicle, false),
            )

            assertEquals(listOf(vehicle), vehicleRepository.savedVehicles)
            assertTrue(reminderRepository.savedReminders.isEmpty())
            assertTrue(scheduler.scheduledReminders.isEmpty())
        }
}
