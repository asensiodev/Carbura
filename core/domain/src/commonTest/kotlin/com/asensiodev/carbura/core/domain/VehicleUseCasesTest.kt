package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.vehicle.usecase.DeleteVehicleUseCase
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VehicleUseCasesTest {
    private val familyId = FamilyId("family-test")
    private val vehicleId = VehicleId("vehicle-test")

    @Test
    fun deleteVehicleDelegatesAtomicNotificationCleanup() =
        runTest {
            val vehicleRepository = FakeVehicleRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            vehicleRepository.saveVehicle(
                Vehicle(
                    id = vehicleId,
                    familyId = familyId,
                    name = "Moto",
                    type = VehicleType.Motorcycle,
                    currentOdometerKm = 3000,
                ),
            )
            reminderRepository.saveReminder(reminder("itv", vehicleId))
            reminderRepository.saveReminder(reminder("other", VehicleId("other-vehicle")))

            DeleteVehicleUseCase(vehicleRepository, reminderRepository, scheduler)(vehicleId)

            assertEquals(emptyList(), vehicleRepository.savedVehicles)
            assertEquals(listOf(vehicleId), vehicleRepository.notificationDeletionIds)
            assertEquals(emptyList(), scheduler.cancelledReminderIds)
        }

    private fun reminder(
        id: String,
        vehicleId: VehicleId,
    ): Reminder =
        Reminder(
            id = ReminderId(id),
            familyId = familyId,
            vehicleId = vehicleId,
            maintenanceTypeId = null,
            title = "Recordatorio $id",
            dueDate = CalendarDate("2026-08-01"),
        )
}
