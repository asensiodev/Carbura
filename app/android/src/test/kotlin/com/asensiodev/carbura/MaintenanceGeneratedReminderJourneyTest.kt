package com.asensiodev.carbura

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordInput
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateAutomaticReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.GetPendingRemindersUseCase
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.UserId
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
            val scope = ActiveFamilyScope(UserId("user-e2e"), familyId, 1)
            val vehicleRepository = InMemoryVehicleRepository()
            val reminderRepository = InMemoryReminderRepository()
            val maintenanceRepository = InMemoryMaintenanceRepository(reminderRepository)
            val scheduler = RecordingScheduler()
            vehicleRepository.saveVehicle(
                scope,
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
                    FamilyScoped(
                        scope,
                        CreateMaintenanceRecordInput(
                            id = MaintenanceRecordId("itv-record-e2e"),
                            familyId = familyId,
                            vehicleId = vehicleRepository.observeVehicles(scope).single().id,
                            performedOn = "2026-07-18",
                            odometerKm = "42000",
                            cost = "",
                            workshop = "",
                            notes = "",
                            maintenanceTypeCode = MaintenanceTypeCode.Itv,
                            nextDueDate = "2027-07-18",
                        ),
                    ),
                )

            assertIs<DomainResult.Success<*>>(result)
            assertEquals(MaintenanceTypeCode.Itv, maintenanceRepository.records.single().maintenanceTypeCode)
            assertEquals(1, GetPendingRemindersUseCase(reminderRepository)(scope).size)
            assertEquals(
                "maintenance-reminder:itv-record-e2e",
                reminderRepository.reminders
                    .single()
                    .id.value,
            )
            assertEquals(
                listOf(60, 30, 7),
                maintenanceRepository.plans
                    .single()
                    .alerts
                    .map { it.daysBefore },
            )
        }
}

private class InMemoryVehicleRepository : VehicleRepository {
    private val vehicles = mutableListOf<Vehicle>()

    override suspend fun observeVehicles(scope: ActiveFamilyScope): List<Vehicle> = vehicles.filter { it.familyId == scope.familyId }

    override suspend fun saveVehicle(
        scope: ActiveFamilyScope,
        vehicle: Vehicle,
    ) {
        vehicles.removeAll { it.id == vehicle.id }
        vehicles += vehicle
    }

    override suspend fun deleteVehicle(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ) {
        vehicles.removeAll { it.id == vehicleId }
    }
}

private class InMemoryMaintenanceRepository(
    private val reminderRepository: ReminderRepository,
) : MaintenanceRecordRepository {
    val records = mutableListOf<MaintenanceRecord>()
    val plans = mutableListOf<ReminderNotificationPlan>()

    override suspend fun saveMaintenanceRecord(
        scope: ActiveFamilyScope,
        record: MaintenanceRecord,
    ) {
        records.removeAll { it.id == record.id }
        records += record
    }

    override suspend fun saveMaintenanceRecordWithNotification(
        scope: ActiveFamilyScope,
        record: MaintenanceRecord,
        mutation: ReminderNotificationMutation,
    ) {
        saveMaintenanceRecord(scope, record)
        when (mutation) {
            is ReminderNotificationMutation.Upsert -> {
                reminderRepository.saveReminder(scope, mutation.reminder)
                mutation.notificationPlan?.let(plans::add)
            }
            is ReminderNotificationMutation.Delete -> reminderRepository.deleteReminder(scope, mutation.reminderId)
        }
    }

    override suspend fun getVehicleHistory(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ): List<MaintenanceRecord> =
        records.filter {
            it.familyId ==
                scope.familyId &&
                it.vehicleId == vehicleId
        }

    override suspend fun deleteMaintenanceRecord(
        scope: ActiveFamilyScope,
        recordId: MaintenanceRecordId,
    ) {
        records.removeAll { it.id == recordId }
    }
}

private class InMemoryReminderRepository : ReminderRepository {
    val reminders = mutableListOf<Reminder>()

    override suspend fun getPendingReminders(scope: ActiveFamilyScope): List<Reminder> =
        reminders.filter { it.familyId == scope.familyId && !it.isCompleted }

    override suspend fun getRemindersByVehicle(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ): List<Reminder> =
        reminders.filter {
            it.familyId ==
                scope.familyId &&
                it.vehicleId == vehicleId
        }

    override suspend fun saveReminder(
        scope: ActiveFamilyScope,
        reminder: Reminder,
    ) {
        reminders.removeAll { it.id == reminder.id }
        reminders += reminder
    }

    override suspend fun markReminderCompleted(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) = Unit

    override suspend fun deleteReminder(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) {
        reminders.removeAll { it.id == reminderId }
    }
}

private class RecordingScheduler : ReminderNotificationScheduler {
    val plans = mutableListOf<ReminderNotificationPlan>()

    override suspend fun schedule(
        scope: ActiveFamilyScope,
        plan: ReminderNotificationPlan,
    ) {
        plans += plan
    }

    override suspend fun cancel(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) = Unit
}
