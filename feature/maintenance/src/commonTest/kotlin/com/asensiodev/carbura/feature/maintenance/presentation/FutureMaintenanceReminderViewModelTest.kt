package com.asensiodev.carbura.feature.maintenance.presentation

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.DeleteMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.GetVehicleHistoryUseCase
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateAutomaticReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreatePlannedMaintenanceReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.RemovePlannedMaintenanceReminderUseCase
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FutureMaintenanceReminderViewModelTest {
    @Test
    fun futureMaintenanceWaitsForReminderChoiceWithoutPersisting() =
        runTest {
            val repository = FutureMaintenanceRepository()
            val viewModel = viewModel(repository = repository)

            prepareFutureSubmission(viewModel)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.showFutureReminderOffer)
            assertEquals(emptyList(), repository.savedRecords)
            assertNull(viewModel.uiState.value.activeMutation)
        }

    @Test
    fun maintenanceDatedTodaySavesWithoutReminderChoice() =
        runTest {
            val repository = FutureMaintenanceRepository()
            val viewModel = viewModel(repository = repository)

            viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.showFutureReminderOffer)
            assertEquals(1, repository.savedRecords.size)
        }

    @Test
    fun dismissingFutureReminderOfferPreservesFormWithoutSaving() =
        runTest {
            val repository = FutureMaintenanceRepository()
            val viewModel = viewModel(repository = repository)
            viewModel.onEvent(MaintenanceHistoryEvent.NotesChanged("Planificado"))
            prepareFutureSubmission(viewModel)

            viewModel.onEvent(MaintenanceHistoryEvent.DismissFutureReminderOffer)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.showFutureReminderOffer)
            assertEquals("2026-08-14", viewModel.uiState.value.performedOn)
            assertEquals("Planificado", viewModel.uiState.value.notes)
            assertEquals(emptyList(), repository.savedRecords)
        }

    @Test
    fun saveOnlyPersistsFutureMaintenanceWithoutPlannedReminder() =
        runTest {
            val repository = FutureMaintenanceRepository()
            val reminderRepository = FutureReminderRepository()
            val viewModel = viewModel(repository, reminderRepository)
            viewModel.onEvent(MaintenanceHistoryEvent.TypeSelected(MaintenanceTypeCode.Repair))
            prepareFutureSubmission(viewModel)

            viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceOnly)
            advanceUntilIdle()

            assertEquals(CalendarDate("2026-08-14"), repository.savedRecords.single().performedOn)
            assertEquals(emptyList(), reminderRepository.savedReminders)
        }

    @Test
    fun acceptingFutureOfferCreatesOnePlannedReminderAndReportsSuccess() =
        runTest {
            val reminderRepository = FutureReminderRepository()
            val scheduler = FutureNotificationScheduler()
            val viewModel =
                viewModel(
                    reminderRepository = reminderRepository,
                    scheduler = scheduler,
                    nextRecordId = { MaintenanceRecordId("future-record") },
                )
            viewModel.onEvent(MaintenanceHistoryEvent.TypeSelected(MaintenanceTypeCode.Repair))
            prepareFutureSubmission(viewModel)

            viewModel.effects.test {
                viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder)
                advanceUntilIdle()
                assertTrue(assertIs<MaintenanceHistoryEffect.MaintenanceCreated>(awaitItem()).reminderCreated)
                cancelAndIgnoreRemainingEvents()
            }

            val reminder = reminderRepository.savedReminders.single()
            assertEquals("planned-maintenance-reminder:future-record", reminder.id.value)
            assertEquals(CalendarDate("2026-08-14"), reminder.dueDate)
            assertEquals(
                listOf(0),
                scheduler.scheduledPlans
                    .single()
                    .alerts
                    .map { it.daysBefore },
            )
        }

    @Test
    fun futureItvKeepsPlannedAndNextDueRemindersDistinct() =
        runTest {
            val reminderRepository = FutureReminderRepository()
            val viewModel =
                viewModel(
                    reminderRepository = reminderRepository,
                    nextRecordId = { MaintenanceRecordId("future-itv") },
                )
            viewModel.onEvent(MaintenanceHistoryEvent.NextDueDateChanged("2027-07-17"))
            prepareFutureSubmission(viewModel)

            viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder)
            advanceUntilIdle()

            assertEquals(
                mapOf(
                    "maintenance-reminder:future-itv" to CalendarDate("2027-07-17"),
                    "planned-maintenance-reminder:future-itv" to CalendarDate("2026-08-14"),
                ),
                reminderRepository.savedReminders.associate { it.id.value to it.dueDate },
            )
        }

    @Test
    fun duplicateFutureConfirmationIsIgnoredWhileSaveStarts() =
        runTest {
            val saveGate = CompletableDeferred<Unit>()
            val repository = FutureMaintenanceRepository(saveGate = saveGate)
            val reminderRepository = FutureReminderRepository()
            val viewModel = viewModel(repository, reminderRepository)
            prepareFutureSubmission(viewModel)

            viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder)
            viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder)
            testScheduler.runCurrent()

            assertEquals(1, repository.saveCalls)
            saveGate.complete(Unit)
            advanceUntilIdle()
            assertEquals(1, reminderRepository.savedReminders.size)
        }

    @Test
    fun plannedReminderFailureKeepsStableMaintenanceRetryable() =
        runTest {
            val repository = FutureMaintenanceRepository()
            val reminderRepository = FutureReminderRepository()
            val scheduler = FutureNotificationScheduler(IllegalStateException("schedule failed"))
            var allocationCount = 0
            val viewModel =
                viewModel(
                    repository,
                    reminderRepository,
                    scheduler,
                    nextRecordId = { MaintenanceRecordId("future-${++allocationCount}") },
                )
            prepareFutureSubmission(viewModel)
            viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.persistenceError)
            scheduler.scheduleError = null
            prepareFutureSubmission(viewModel)
            viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder)
            advanceUntilIdle()

            assertEquals(1, allocationCount)
            assertEquals(1, repository.savedRecords.size)
            assertEquals(1, reminderRepository.savedReminders.size)
            assertFalse(viewModel.uiState.value.persistenceError)
        }

    @Test
    fun saveOnlyRetryRemovesPlannedReminderPersistedBeforeSchedulingFailure() =
        runTest {
            val reminderRepository = FutureReminderRepository()
            val scheduler = FutureNotificationScheduler(IllegalStateException("schedule failed"))
            val viewModel = viewModel(reminderRepository = reminderRepository, scheduler = scheduler)
            prepareFutureSubmission(viewModel)
            viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder)
            advanceUntilIdle()
            assertEquals(1, reminderRepository.savedReminders.size)

            scheduler.scheduleError = null
            prepareFutureSubmission(viewModel)
            viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceOnly)
            advanceUntilIdle()

            assertEquals(emptyList(), reminderRepository.savedReminders)
            assertFalse(viewModel.uiState.value.persistenceError)
        }

    @Test
    fun directRetryWithNonFutureDateRemovesPartiallyPersistedPlannedReminder() =
        runTest {
            val reminderRepository = FutureReminderRepository()
            val scheduler = FutureNotificationScheduler(IllegalStateException("schedule failed"))
            val viewModel = viewModel(reminderRepository = reminderRepository, scheduler = scheduler)
            prepareFutureSubmission(viewModel)
            viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder)
            advanceUntilIdle()
            assertEquals(1, reminderRepository.savedReminders.size)

            scheduler.scheduleError = null
            viewModel.onEvent(MaintenanceHistoryEvent.PerformedOnChanged("2026-07-17"))
            viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
            advanceUntilIdle()

            assertEquals(emptyList(), reminderRepository.savedReminders)
            assertFalse(viewModel.uiState.value.persistenceError)
        }

    @Test
    fun cancelledPlannedReminderDoesNotBecomePersistenceError() =
        runTest {
            val scheduler = FutureNotificationScheduler(CancellationException("cancelled"))
            val viewModel = viewModel(scheduler = scheduler)
            prepareFutureSubmission(viewModel)

            viewModel.onEvent(MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.activeMutation)
            assertFalse(viewModel.uiState.value.persistenceError)
            assertEquals("2026-08-14", viewModel.uiState.value.performedOn)
        }

    private fun prepareFutureSubmission(viewModel: MaintenanceHistoryViewModel) {
        viewModel.onEvent(MaintenanceHistoryEvent.PerformedOnChanged("2026-08-14"))
        viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
    }

    private fun TestScope.viewModel(
        repository: FutureMaintenanceRepository = FutureMaintenanceRepository(),
        reminderRepository: FutureReminderRepository = FutureReminderRepository(),
        scheduler: FutureNotificationScheduler = FutureNotificationScheduler(),
        nextRecordId: () -> MaintenanceRecordId = { MaintenanceRecordId("future-record") },
    ): MaintenanceHistoryViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return MaintenanceHistoryViewModel(
            vehicleId = vehicle.id,
            familyId = familyId,
            dispatchers = TestDispatcherProvider(dispatcher, dispatcher, dispatcher),
            createMaintenanceWithReminderFromInputUseCase =
                CreateMaintenanceWithReminderFromInputUseCase(
                    CreateMaintenanceWithReminderUseCase(
                        CreateMaintenanceRecordUseCase(repository),
                        CreateAutomaticReminderUseCase(reminderRepository, scheduler),
                    ),
                ),
            createPlannedMaintenanceReminderUseCase = CreatePlannedMaintenanceReminderUseCase(reminderRepository, scheduler),
            removePlannedMaintenanceReminderUseCase = RemovePlannedMaintenanceReminderUseCase(reminderRepository, scheduler),
            getVehicleHistoryUseCase = GetVehicleHistoryUseCase(repository),
            deleteMaintenanceRecordUseCase = DeleteMaintenanceRecordUseCase(repository, reminderRepository, scheduler),
            vehicleRepository = FutureVehicleRepository(vehicle),
            nextRecordId = nextRecordId,
            localDateProvider = LocalDateProvider { CalendarDate("2026-07-17") },
            coroutineScope = this,
        )
    }

    private companion object {
        val familyId = FamilyId("family-test")
        val vehicle = Vehicle(VehicleId("vehicle-test"), familyId, "Coche familiar", VehicleType.Car, currentOdometerKm = 12_300)
    }
}

private class FutureMaintenanceRepository(
    private val saveGate: CompletableDeferred<Unit>? = null,
) : MaintenanceRecordRepository {
    val savedRecords = mutableListOf<MaintenanceRecord>()
    var saveCalls = 0

    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) {
        saveCalls += 1
        saveGate?.await()
        savedRecords.removeAll { it.id == record.id }
        savedRecords += record
    }

    override suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord> =
        savedRecords.filter { it.vehicleId == vehicleId }

    override suspend fun deleteMaintenanceRecord(recordId: MaintenanceRecordId) {
        savedRecords.removeAll { it.id == recordId }
    }
}

private class FutureReminderRepository : ReminderRepository {
    val savedReminders = mutableListOf<Reminder>()

    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> =
        savedReminders.filter { it.familyId == familyId && !it.isCompleted }

    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> = savedReminders.filter { it.vehicleId == vehicleId }

    override suspend fun saveReminder(reminder: Reminder) {
        savedReminders.removeAll { it.id == reminder.id }
        savedReminders += reminder
    }

    override suspend fun markReminderCompleted(reminderId: ReminderId) = Unit

    override suspend fun deleteReminder(reminderId: ReminderId) {
        savedReminders.removeAll { it.id == reminderId }
    }
}

private class FutureNotificationScheduler(
    var scheduleError: Throwable? = null,
) : ReminderNotificationScheduler {
    val scheduledPlans = mutableListOf<ReminderNotificationPlan>()

    override suspend fun schedule(plan: ReminderNotificationPlan) {
        scheduleError?.let { throw it }
        scheduledPlans += plan
    }

    override suspend fun cancel(reminderId: ReminderId) = Unit
}

private class FutureVehicleRepository(
    private val vehicle: Vehicle,
) : VehicleRepository {
    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> = listOf(vehicle)

    override suspend fun saveVehicle(vehicle: Vehicle) = Unit

    override suspend fun deleteVehicle(vehicleId: VehicleId) = Unit
}
