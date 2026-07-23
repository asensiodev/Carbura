package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationMutation
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderId
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.plannedMaintenanceReminderId
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import kotlinx.coroutines.CancellationException
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@Suppress("TooManyFunctions")
class LocalRepositoriesTest {
    private val familyId = FamilyId("family-test")
    private val vehicleId = VehicleId("vehicle-test")

    @Test
    fun vehicleRepositoryReadsSavedVehiclesFromRecreatedDatabase() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val vehicleRepository = LocalVehicleRepository(firstDatabase)
            vehicleRepository.saveVehicle(
                scope,
                Vehicle(
                    id = vehicleId,
                    familyId = familyId,
                    name = "Coche familiar",
                    type = VehicleType.Car,
                    currentOdometerKm = 12000,
                    nextItvDate = CalendarDate("2027-05-10"),
                    insuranceRenewalDate = CalendarDate("2027-01-20"),
                    nextServiceOdometerKm = 25000,
                ),
            )

            val recreatedRepository = LocalVehicleRepository(recreatedDatabase)

            val vehicles = recreatedRepository.observeVehicles(scope)
            assertEquals(1, vehicles.size)
            assertEquals("Coche familiar", vehicles.single().name)
            assertEquals(12000, vehicles.single().currentOdometerKm)
            assertEquals(CalendarDate("2027-05-10"), vehicles.single().nextItvDate)
            assertEquals(CalendarDate("2027-01-20"), vehicles.single().insuranceRenewalDate)
            assertEquals(25000, vehicles.single().nextServiceOdometerKm)
        }

    @Test
    fun vehicleRepositoryUpdatesExistingVehicleAndMarksItPending() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalVehicleRepository(firstDatabase)
            val original =
                Vehicle(
                    id = vehicleId,
                    familyId = familyId,
                    name = "Coche",
                    type = VehicleType.Car,
                    licensePlate = "1234 ABC",
                    currentOdometerKm = 12000,
                )
            repository.saveVehicle(scope, original)

            repository.saveVehicle(
                scope,
                original.copy(
                    name = "Coche familiar",
                    type = VehicleType.Van,
                    licensePlate = "5678 XYZ",
                    currentOdometerKm = 15000,
                ),
            )

            val vehicles = LocalVehicleRepository(recreatedDatabase).observeVehicles(scope)
            val pendingVehicle = SqlDelightLocalSyncDataSource(recreatedDatabase).getPendingVehicles(scope).single()
            assertEquals(1, vehicles.size)
            assertEquals(vehicleId, vehicles.single().id)
            assertEquals(familyId, vehicles.single().familyId)
            assertEquals("Coche familiar", vehicles.single().name)
            assertEquals(VehicleType.Van, vehicles.single().type)
            assertEquals("5678 XYZ", vehicles.single().licensePlate)
            assertEquals(15000, vehicles.single().currentOdometerKm)
            assertEquals(vehicleId.value, pendingVehicle.id)
            assertEquals(true, pendingVehicle.pendingSync)
            assertEquals(null, pendingVehicle.deletedAt)
            assertEquals(true, pendingVehicle.updatedAt > 0)
        }

    @Test
    fun maintenanceRepositoryReadsHistoryOrderedByDateFromRecreatedDatabase() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalMaintenanceRecordRepository(firstDatabase)
            repository.saveMaintenanceRecord(scope, record("old", "2026-01-01"))
            repository.saveMaintenanceRecord(scope, record("new", "2026-07-04"))

            val recreatedRepository = LocalMaintenanceRecordRepository(recreatedDatabase)

            val history = recreatedRepository.getVehicleHistory(scope, vehicleId)
            assertEquals(listOf("new", "old"), history.map { it.id.value })
        }

    @Test
    fun maintenanceRepositoryRoundTripsCanonicalTypesAndNextDueDates() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalMaintenanceRecordRepository(firstDatabase)
            val itv = record("itv", "2026-07-01", MaintenanceTypeCode.Itv, "2027-07-01")
            val insurance = record("insurance", "2026-07-02", MaintenanceTypeCode.Insurance, "2027-07-02")

            repository.saveMaintenanceRecord(scope, itv)
            repository.saveMaintenanceRecord(scope, insurance)

            val records = LocalMaintenanceRecordRepository(recreatedDatabase).getVehicleHistory(scope, vehicleId)
            assertEquals(listOf(insurance, itv), records)
        }

    @Test
    fun generatedRecordAndReminderAreBothPendingSync() =
        runTestWithRecreatedDatabase { database, _ ->
            val scope = database.activateTestFamily(familyId)
            val record = record("record-1", "2026-07-01", MaintenanceTypeCode.Itv, "2027-07-01")
            val generatedReminder = reminder("maintenance-reminder:record-1", dueDate = "2027-07-01")
            LocalMaintenanceRecordRepository(database).saveMaintenanceRecord(scope, record)
            LocalReminderRepository(database).saveReminder(scope, generatedReminder)

            val syncDataSource = SqlDelightLocalSyncDataSource(database)

            assertEquals(true, syncDataSource.getPendingMaintenanceRecords(scope).single().pendingSync)
            assertEquals("Itv", syncDataSource.getPendingMaintenanceRecords(scope).single().maintenanceTypeCode)
            assertEquals("2027-07-01", syncDataSource.getPendingMaintenanceRecords(scope).single().nextDueDate)
            assertEquals(true, syncDataSource.getPendingReminders(scope).single().pendingSync)
            assertEquals("maintenance-reminder:record-1", syncDataSource.getPendingReminders(scope).single().id)
        }

    @Test
    fun maintenanceRepositoryDeletesRecordFromRecreatedDatabase() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalMaintenanceRecordRepository(firstDatabase)
            repository.saveMaintenanceRecord(scope, record("oil", "2026-07-04"))
            repository.deleteMaintenanceRecord(scope, MaintenanceRecordId("oil"))

            val recreatedRepository = LocalMaintenanceRecordRepository(recreatedDatabase)

            assertEquals(emptyList(), recreatedRepository.getVehicleHistory(scope, vehicleId))
        }

    @Test
    fun reminderRepositoryReadsPendingRemindersFromRecreatedDatabase() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalReminderRepository(firstDatabase)
            repository.saveReminder(scope, reminder("late", dueDate = "2026-08-01"))
            repository.saveReminder(scope, reminder("early", dueDate = "2026-07-01"))

            val recreatedRepository = LocalReminderRepository(recreatedDatabase)

            val reminders = recreatedRepository.getPendingReminders(scope)
            assertEquals(listOf("early", "late"), reminders.map { it.id.value })
        }

    @Test
    fun reminderRepositoryHidesCompletedRemindersFromRecreatedDatabase() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalReminderRepository(firstDatabase)
            repository.saveReminder(scope, reminder("completed", dueOdometerKm = 20000))
            repository.markReminderCompleted(scope, ReminderId("completed"))

            val recreatedRepository = LocalReminderRepository(recreatedDatabase)

            assertEquals(emptyList(), recreatedRepository.getPendingReminders(scope))
        }

    @Test
    fun reminderRepositoryDeletesReminderFromRecreatedDatabase() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalReminderRepository(firstDatabase)
            repository.saveReminder(scope, reminder("itv", dueDate = "2026-08-01"))
            repository.deleteReminder(scope, ReminderId("itv"))

            val recreatedRepository = LocalReminderRepository(recreatedDatabase)

            assertEquals(emptyList(), recreatedRepository.getPendingReminders(scope))
        }

    @Test
    fun deletingVehicleRemovesVehicleMaintenanceAndReminders() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val vehicleRepository = LocalVehicleRepository(firstDatabase)
            val maintenanceRepository = LocalMaintenanceRecordRepository(firstDatabase)
            val reminderRepository = LocalReminderRepository(firstDatabase)
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
            maintenanceRepository.saveMaintenanceRecord(scope, record("oil", "2026-07-01"))
            reminderRepository.saveReminder(scope, reminder("itv", dueDate = "2026-08-01"))

            vehicleRepository.deleteVehicle(scope, vehicleId)

            val recreatedVehicleRepository = LocalVehicleRepository(recreatedDatabase)
            val recreatedMaintenanceRepository = LocalMaintenanceRecordRepository(recreatedDatabase)
            val recreatedReminderRepository = LocalReminderRepository(recreatedDatabase)
            assertEquals(emptyList(), recreatedVehicleRepository.observeVehicles(scope))
            assertEquals(emptyList(), recreatedMaintenanceRepository.getVehicleHistory(scope, vehicleId))
            assertEquals(emptyList(), recreatedReminderRepository.getPendingReminders(scope))
        }

    @Test
    fun deletingVehicleKeepsPendingTombstonesForSync() =
        runTestWithRecreatedDatabase { firstDatabase, _ ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val vehicleRepository = LocalVehicleRepository(firstDatabase)
            val maintenanceRepository = LocalMaintenanceRecordRepository(firstDatabase)
            val reminderRepository = LocalReminderRepository(firstDatabase)
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
            maintenanceRepository.saveMaintenanceRecord(scope, record("oil", "2026-07-01"))
            reminderRepository.saveReminder(scope, reminder("itv", dueDate = "2026-08-01"))

            vehicleRepository.deleteVehicle(scope, vehicleId)

            val syncDataSource = SqlDelightLocalSyncDataSource(firstDatabase)
            val pendingVehicle = syncDataSource.getPendingVehicles(scope).single { it.id == vehicleId.value }
            val pendingRecord = syncDataSource.getPendingMaintenanceRecords(scope).single { it.id == "oil" }
            val pendingReminder = syncDataSource.getPendingReminders(scope).single { it.id == "itv" }
            assertNotNull(pendingVehicle.deletedAt)
            assertNotNull(pendingRecord.deletedAt)
            assertNotNull(pendingReminder.deletedAt)
            assertEquals(emptyList(), vehicleRepository.observeVehicles(scope))
            assertEquals(emptyList(), maintenanceRepository.getVehicleHistory(scope, vehicleId))
            assertEquals(emptyList(), reminderRepository.getPendingReminders(scope))
        }

    @Test
    fun accountCleanupCancelsNotificationsAndRemovesFamilyRowsIdempotently() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val vehicleRepository = LocalVehicleRepository(firstDatabase)
            val maintenanceRepository = LocalMaintenanceRecordRepository(firstDatabase)
            val reminderRepository = LocalReminderRepository(firstDatabase)
            val scheduler = RecordingCleanupScheduler()
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
            maintenanceRepository.saveMaintenanceRecord(scope, record("oil", "2026-07-01"))
            reminderRepository.saveReminder(scope, reminder("itv", dueDate = "2026-08-01"))
            val cleaner = SqlDelightAccountLocalDataCleaner(firstDatabase, scheduler)

            cleaner.clear(familyId)
            cleaner.clear(familyId)

            assertEquals(listOf(ReminderId("itv")), scheduler.cancelledReminderIds)
            assertEquals(
                "Cancel",
                recreatedDatabase.carburaDatabaseQueries
                    .selectDesiredNotifications(familyId.value)
                    .executeAsOne()
                    .action,
            )
            assertEquals(emptyList(), LocalVehicleRepository(recreatedDatabase).observeVehicles(scope))
            assertEquals(emptyList(), LocalMaintenanceRecordRepository(recreatedDatabase).getVehicleHistory(scope, vehicleId))
            assertEquals(emptyList(), LocalReminderRepository(recreatedDatabase).getPendingReminders(scope))
        }

    @Test
    fun accountCleanupRemovesRowsBeforeRethrowingNotificationCancellation() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val vehicleRepository = LocalVehicleRepository(firstDatabase)
            val reminderRepository = LocalReminderRepository(firstDatabase)
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
            reminderRepository.saveReminder(scope, reminder("itv", dueDate = "2026-08-01"))
            val cleaner =
                SqlDelightAccountLocalDataCleaner(
                    firstDatabase,
                    RecordingCleanupScheduler(cancelError = CancellationException("Scheduler cancelled")),
                )

            assertFailsWith<CancellationException> { cleaner.clear(familyId) }

            assertEquals(emptyList(), LocalVehicleRepository(recreatedDatabase).observeVehicles(scope))
            assertEquals(emptyList(), LocalReminderRepository(recreatedDatabase).getPendingReminders(scope))
        }

    @Test
    fun savingReminderAtomicallyRecordsLatestScheduleAction() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalReminderRepository(firstDatabase)
            val reminder = reminder("itv", dueDate = "2026-08-01")

            repository.saveReminderWithNotification(scope, reminder, manualReminderNotificationPlan(reminder))

            val desired = recreatedDatabase.carburaDatabaseQueries.selectDesiredNotifications(familyId.value).executeAsOne()
            assertEquals(reminder.id.value, desired.reminderId)
            assertEquals("Schedule", desired.action)
            assertNotNull(desired.payload)
            assertEquals(1L, desired.revision)
            assertEquals(
                1L,
                recreatedDatabase.carburaDatabaseQueries
                    .selectSyncRemindersByFamily(familyId.value)
                    .executeAsOne()
                    .pendingSync,
            )
        }

    @Test
    fun completingAndDeletingReminderAtomicallyRecordCancelAction() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalReminderRepository(firstDatabase)
            val reminder = reminder("itv", dueDate = "2026-08-01")
            repository.saveReminderWithNotification(scope, reminder, manualReminderNotificationPlan(reminder))

            repository.markReminderCompletedWithNotification(scope, reminder.id)

            var persisted = recreatedDatabase.carburaDatabaseQueries.selectSyncRemindersByFamily(familyId.value).executeAsOne()
            var desired = recreatedDatabase.carburaDatabaseQueries.selectDesiredNotifications(familyId.value).executeAsOne()
            assertEquals(1L, persisted.isCompleted)
            assertEquals(1L, persisted.pendingSync)
            assertEquals("Cancel", desired.action)
            assertEquals(2L, desired.revision)

            repository.deleteReminderWithNotification(scope, reminder.id)

            persisted = recreatedDatabase.carburaDatabaseQueries.selectSyncRemindersByFamily(familyId.value).executeAsOne()
            desired = recreatedDatabase.carburaDatabaseQueries.selectDesiredNotifications(familyId.value).executeAsOne()
            assertNotNull(persisted.deletedAt)
            assertEquals(1L, persisted.pendingSync)
            assertEquals("Cancel", desired.action)
            assertEquals(2L, desired.revision)
        }

    @Test
    fun maintenanceDeletionAtomicallyRecordsCancelsForGeneratedReminders() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val maintenanceRepository = LocalMaintenanceRecordRepository(firstDatabase)
            val reminderRepository = LocalReminderRepository(firstDatabase)
            val maintenance = record("oil", "2026-07-01")
            val reminderIds = listOf(maintenanceReminderId(maintenance.id), plannedMaintenanceReminderId(maintenance.id))
            maintenanceRepository.saveMaintenanceRecord(scope, maintenance)
            reminderIds.forEach { id ->
                reminderRepository.saveReminder(scope, reminder(id.value, dueDate = "2026-08-01"))
            }

            maintenanceRepository.deleteMaintenanceRecordWithNotifications(scope, maintenance.id, reminderIds)

            assertEquals(
                reminderIds.map { it.value }.sorted(),
                recreatedDatabase.carburaDatabaseQueries
                    .selectDesiredNotifications(familyId.value)
                    .executeAsList()
                    .onEach { assertEquals("Cancel", it.action) }
                    .map { it.reminderId },
            )
            assertTrue(LocalMaintenanceRecordRepository(recreatedDatabase).getVehicleHistory(scope, vehicleId).isEmpty())
            assertTrue(LocalReminderRepository(recreatedDatabase).getPendingReminders(scope).isEmpty())
        }

    @Test
    fun maintenanceCreationAtomicallyPersistsGeneratedReminderAndSchedule() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalMaintenanceRecordRepository(firstDatabase)
            val maintenance = record("oil", "2026-07-01")
            val generatedReminder = reminder("maintenance-reminder:oil", dueDate = "2026-08-01")

            repository.saveMaintenanceRecordWithNotification(
                scope,
                maintenance,
                ReminderNotificationMutation.Upsert(
                    generatedReminder,
                    manualReminderNotificationPlan(generatedReminder),
                ),
            )

            assertEquals(1, LocalMaintenanceRecordRepository(recreatedDatabase).getVehicleHistory(scope, vehicleId).size)
            assertEquals(listOf(generatedReminder), LocalReminderRepository(recreatedDatabase).getPendingReminders(scope))
            assertEquals(
                "Schedule",
                recreatedDatabase.carburaDatabaseQueries
                    .selectDesiredNotifications(familyId.value)
                    .executeAsOne()
                    .action,
            )
        }

    @Test
    fun vehicleDeletionAtomicallyRecordsCancelsForEveryAffectedReminder() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val vehicleRepository = LocalVehicleRepository(firstDatabase)
            val reminderRepository = LocalReminderRepository(firstDatabase)
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
            listOf("itv", "insurance").forEach { id ->
                reminderRepository.saveReminder(scope, reminder(id, dueDate = "2026-08-01"))
            }

            vehicleRepository.deleteVehicleWithNotifications(scope, vehicleId)

            val desired = recreatedDatabase.carburaDatabaseQueries.selectDesiredNotifications(familyId.value).executeAsList()
            assertEquals(listOf("insurance", "itv"), desired.map { it.reminderId })
            assertTrue(desired.all { it.action == "Cancel" })
            assertTrue(LocalVehicleRepository(recreatedDatabase).observeVehicles(scope).isEmpty())
            assertTrue(LocalReminderRepository(recreatedDatabase).getPendingReminders(scope).isEmpty())
        }

    @Test
    fun vehicleSaveAtomicallyReconcilesAllGeneratedReminderIntents() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val repository = LocalVehicleRepository(firstDatabase)
            val vehicle =
                Vehicle(
                    id = vehicleId,
                    familyId = familyId,
                    name = "Moto",
                    type = VehicleType.Motorcycle,
                    currentOdometerKm = 3000,
                )
            val scheduledReminder = reminder("itv", dueDate = "2026-08-01")

            repository.saveVehicleWithNotifications(
                scope,
                vehicle,
                listOf(
                    ReminderNotificationMutation.Upsert(
                        scheduledReminder,
                        manualReminderNotificationPlan(scheduledReminder),
                    ),
                    ReminderNotificationMutation.Delete(ReminderId("insurance")),
                ),
            )

            assertEquals(listOf(vehicle), LocalVehicleRepository(recreatedDatabase).observeVehicles(scope))
            assertEquals(listOf(scheduledReminder), LocalReminderRepository(recreatedDatabase).getPendingReminders(scope))
            assertEquals(
                listOf("Cancel", "Schedule"),
                recreatedDatabase.carburaDatabaseQueries
                    .selectDesiredNotifications(familyId.value)
                    .executeAsList()
                    .map { it.action },
            )
        }

    @Test
    fun syncedReminderKeepsRemoteSyncStateAndNotificationIntentAtomic() =
        runTestWithRecreatedDatabase { firstDatabase, recreatedDatabase ->
            val scope = firstDatabase.activateTestFamily(familyId)
            val syncDataSource = SqlDelightLocalSyncDataSource(firstDatabase)
            val reminder =
                SyncReminder(
                    id = "remote-reminder",
                    familyId = familyId.value,
                    vehicleId = vehicleId.value,
                    maintenanceTypeId = null,
                    title = "ITV",
                    dueDate = "2026-08-01",
                    dueOdometerKm = null,
                    notifyDaysBefore = 30,
                    isCompleted = false,
                    updatedAt = 100,
                    pendingSync = false,
                    deletedAt = null,
                )

            syncDataSource.upsertSyncedReminder(scope, reminder)

            var persisted = recreatedDatabase.carburaDatabaseQueries.selectSyncRemindersByFamily(familyId.value).executeAsOne()
            var desired = recreatedDatabase.carburaDatabaseQueries.selectDesiredNotifications(familyId.value).executeAsOne()
            assertEquals(0L, persisted.pendingSync)
            assertEquals("Schedule", desired.action)

            syncDataSource.upsertSyncedReminder(scope, reminder.copy(isCompleted = true, updatedAt = 200))

            persisted = recreatedDatabase.carburaDatabaseQueries.selectSyncRemindersByFamily(familyId.value).executeAsOne()
            desired = recreatedDatabase.carburaDatabaseQueries.selectDesiredNotifications(familyId.value).executeAsOne()
            assertEquals(0L, persisted.pendingSync)
            assertEquals(1L, persisted.isCompleted)
            assertEquals("Cancel", desired.action)
            assertEquals(2L, desired.revision)
        }

    private fun runTestWithRecreatedDatabase(block: suspend (CarburaDatabase, CarburaDatabase) -> Unit) =
        kotlinx.coroutines.test.runTest {
            val databaseFile = File.createTempFile("carbura-test", ".db").also { it.deleteOnExit() }
            val firstDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")
            CarburaDatabase.Schema.create(firstDriver)
            val recreatedDriver = JdbcSqliteDriver("jdbc:sqlite:${databaseFile.absolutePath}")

            block(CarburaDatabase(firstDriver), CarburaDatabase(recreatedDriver))
        }

    private fun record(
        id: String,
        performedOn: String,
        code: MaintenanceTypeCode = MaintenanceTypeCode.Custom,
        nextDueDate: String? = null,
    ): MaintenanceRecord =
        MaintenanceRecord(
            id = MaintenanceRecordId(id),
            familyId = familyId,
            vehicleId = vehicleId,
            maintenanceTypeId = MaintenanceTypeId("type-$id"),
            maintenanceTypeCode = code,
            performedOn = CalendarDate(performedOn),
            odometerKm = 1,
            nextDueDate = nextDueDate?.let(::CalendarDate),
        )

    private fun reminder(
        id: String,
        dueDate: String? = null,
        dueOdometerKm: Int? = null,
    ): Reminder =
        Reminder(
            id = ReminderId(id),
            familyId = familyId,
            vehicleId = vehicleId,
            maintenanceTypeId = null,
            title = "Recordatorio $id",
            dueDate = dueDate?.let(::CalendarDate),
            dueOdometerKm = dueOdometerKm,
        )
}

private class RecordingCleanupScheduler(
    private val cancelError: Throwable? = null,
) : ReminderNotificationScheduler {
    val cancelledReminderIds = mutableListOf<ReminderId>()

    override suspend fun schedule(
        scope: ActiveFamilyScope,
        plan: ReminderNotificationPlan,
    ) = Unit

    override suspend fun cancel(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) {
        cancelError?.let { throw it }
        cancelledReminderIds += reminderId
    }
}
