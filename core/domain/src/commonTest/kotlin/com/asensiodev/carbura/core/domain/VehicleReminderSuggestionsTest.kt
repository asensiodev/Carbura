package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
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
            reminderRepository.saveReminder(testFamilyScope, manual)
            val useCase = SaveVehicleWithRemindersUseCase(vehicleRepository, reminderRepository, scheduler)

            useCase(SaveVehicleWithRemindersParams(testFamilyScope, vehicle, reconcileGeneratedReminders = true))
            useCase(
                SaveVehicleWithRemindersParams(
                    testFamilyScope,
                    vehicle.copy(nextItvDate = CalendarDate("2027-06-10")),
                    reconcileGeneratedReminders = true,
                ),
            )

            val upserts = vehicleRepository.notificationMutations.filterIsInstance<ReminderNotificationMutation.Upsert>()
            assertEquals(2, upserts.size)
            assertEquals(CalendarDate("2027-06-10"), upserts.last().reminder.dueDate)
            assertTrue(
                upserts.all {
                    it.notificationPlan
                        ?.alerts
                        ?.single()
                        ?.kind == ReminderAlertKind.Manual
                },
            )
            assertEquals(listOf(manual), reminderRepository.savedReminders)
            assertTrue(scheduler.scheduledReminders.isEmpty())
        }

    @Test
    fun clearingTargetDeletesAndCancelsOnlyOwnedReminder() =
        runTest {
            val vehicleRepository = FakeVehicleRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val vehicle = testVehicle().copy(nextItvDate = CalendarDate("2027-05-10"))
            val useCase = SaveVehicleWithRemindersUseCase(vehicleRepository, reminderRepository, scheduler)
            useCase(SaveVehicleWithRemindersParams(testFamilyScope, vehicle, true))

            useCase(SaveVehicleWithRemindersParams(testFamilyScope, vehicle.copy(nextItvDate = null), true))

            val latestDeletes = vehicleRepository.notificationMutations.takeLast(VehicleReminderKind.entries.size)
            assertTrue(
                latestDeletes.any {
                    it == ReminderNotificationMutation.Delete(vehicleReminderId(vehicle.id, VehicleReminderKind.Itv))
                },
            )
            assertTrue(reminderRepository.savedReminders.isEmpty())
            assertTrue(scheduler.cancelledReminderIds.isEmpty())
        }

    @Test
    fun declinedSuggestionsSaveVehicleWithoutReminderMutation() =
        runTest {
            val vehicleRepository = FakeVehicleRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            val vehicle = testVehicle().copy(nextItvDate = CalendarDate("2027-05-10"))

            SaveVehicleWithRemindersUseCase(vehicleRepository, reminderRepository, scheduler)(
                SaveVehicleWithRemindersParams(testFamilyScope, vehicle, false),
            )

            assertEquals(listOf(vehicle), vehicleRepository.savedVehicles)
            assertTrue(vehicleRepository.notificationMutations.isEmpty())
            assertTrue(reminderRepository.savedReminders.isEmpty())
            assertTrue(scheduler.scheduledReminders.isEmpty())
        }
}
