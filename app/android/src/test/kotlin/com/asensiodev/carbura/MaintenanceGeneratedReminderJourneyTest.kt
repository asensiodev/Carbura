package com.asensiodev.carbura

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordInput
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateAutomaticReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.GetPendingRemindersUseCase
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MaintenanceGeneratedReminderJourneyTest {
    @Test
    fun vehicleToItvWithNextDateProducesOneReminderWithThreeAlerts() =
        runTest {
            val familyId = FamilyId("family-e2e")
            val vehicleId = VehicleId("vehicle-e2e")
            val vehicleRepository = InMemoryVehicleRepository()
            val maintenanceRepository = InMemoryMaintenanceRepository()
            val reminderRepository = InMemoryReminderRepository()
            val scheduler = RecordingScheduler()
            vehicleRepository.saveVehicle(
                Vehicle(
                    id = vehicleId,
                    familyId = familyId,
                    name = "Coche familiar",
                    type = VehicleType.Car,
                    currentOdometerKm = 42_000,
                ),
            )
            val createMaintenance =
                CreateMaintenanceWithReminderFromInputUseCase(
                    CreateMaintenanceWithReminderUseCase(
                        CreateMaintenanceRecordUseCase(maintenanceRepository),
                        CreateAutomaticReminderUseCase(reminderRepository, scheduler),
                    ),
                )

            val result =
                createMaintenance(
                    CreateMaintenanceRecordInput(
                        id = MaintenanceRecordId("itv-record-e2e"),
                        familyId = familyId,
                        vehicleId = vehicleRepository.observeVehicles(familyId).single().id,
                        performedOn = "2026-07-18",
                        odometerKm = "42000",
                        cost = "",
                        workshop = "",
                        notes = "",
                        maintenanceTypeCode = MaintenanceTypeCode.Itv,
                        nextDueDate = "2027-07-18",
                    ),
                )

            assertIs<DomainResult.Success<*>>(result)
            assertEquals(MaintenanceTypeCode.Itv, maintenanceRepository.records.single().maintenanceTypeCode)
            assertEquals(1, GetPendingRemindersUseCase(reminderRepository)(familyId).size)
            assertEquals(
                "maintenance-reminder:itv-record-e2e",
                reminderRepository.reminders
                    .single()
                    .id.value,
            )
            assertEquals(
                listOf(60, 30, 7),
                scheduler.plans
                    .single()
                    .alerts
                    .map { it.daysBefore },
            )
        }
}

private class InMemoryVehicleRepository : VehicleRepository {
    private val vehicles = mutableListOf<Vehicle>()

    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> = vehicles.filter { it.familyId == familyId }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        vehicles.removeAll { it.id == vehicle.id }
        vehicles += vehicle
    }

    override suspend fun deleteVehicle(vehicleId: VehicleId) {
        vehicles.removeAll { it.id == vehicleId }
    }
}

private class InMemoryMaintenanceRepository : MaintenanceRecordRepository {
    val records = mutableListOf<MaintenanceRecord>()

    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) {
        records.removeAll { it.id == record.id }
        records += record
    }

    override suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord> = records.filter { it.vehicleId == vehicleId }

    override suspend fun deleteMaintenanceRecord(recordId: MaintenanceRecordId) {
        records.removeAll { it.id == recordId }
    }
}

private class InMemoryReminderRepository : ReminderRepository {
    val reminders = mutableListOf<Reminder>()

    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> =
        reminders.filter { it.familyId == familyId && !it.isCompleted }

    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> = reminders.filter { it.vehicleId == vehicleId }

    override suspend fun saveReminder(reminder: Reminder) {
        reminders.removeAll { it.id == reminder.id }
        reminders += reminder
    }

    override suspend fun markReminderCompleted(reminderId: ReminderId) = Unit

    override suspend fun deleteReminder(reminderId: ReminderId) {
        reminders.removeAll { it.id == reminderId }
    }
}

private class RecordingScheduler : ReminderNotificationScheduler {
    val plans = mutableListOf<ReminderNotificationPlan>()

    override suspend fun schedule(plan: ReminderNotificationPlan) {
        plans += plan
    }

    override suspend fun cancel(reminderId: ReminderId) = Unit
}
