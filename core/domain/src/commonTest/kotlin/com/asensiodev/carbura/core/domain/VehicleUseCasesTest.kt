package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.vehicle.usecase.DeleteVehicleUseCase
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.UserId
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
            val scope = ActiveFamilyScope(UserId("user-test"), familyId, 1)
            val vehicleRepository = FakeVehicleRepository()
            val reminderRepository = FakeReminderRepository()
            val scheduler = FakeReminderNotificationScheduler()
            vehicleRepository.saveVehicle(
                scope,
                Vehicle(
                    id = vehicleId,
                    familyId = familyId,
                    name = "Moto",
                    type = VehicleType.Motorcycle,
                    currentOdometerKm = 3000,
                ),
            )
            reminderRepository.saveReminder(scope, reminder("itv", vehicleId))
            reminderRepository.saveReminder(scope, reminder("other", VehicleId("other-vehicle")))

            DeleteVehicleUseCase(vehicleRepository, reminderRepository, scheduler)(FamilyScoped(scope, vehicleId))

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
