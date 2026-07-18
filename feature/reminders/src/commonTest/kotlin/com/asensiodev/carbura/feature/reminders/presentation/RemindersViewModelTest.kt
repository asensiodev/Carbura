package com.asensiodev.carbura.feature.reminders.presentation

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.reminder.usecase.CompleteReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.DeleteReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.GetPendingRemindersUseCase
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersViewModelTest {
    private val familyId = FamilyId("family-test")
    private val vehicle =
        Vehicle(
            id = VehicleId("vehicle-1"),
            familyId = familyId,
            name = "Coche familiar",
            type = VehicleType.Car,
            currentOdometerKm = 12000,
        )

    @Test
    fun initialStateIsLoadingWithoutShowingEmptyContent() =
        runTest {
            val state = remindersViewModel().uiState.value

            assertTrue(state.isLoading)
            assertFalse(state.isEmpty)
            assertFalse(state.hasNoVehicles)
        }

    @Test
    fun startedLoadsVehiclesAndPendingReminders() =
        runTest {
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = FakeReminderRepository(listOf(reminder("itv"))),
                )

            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(listOf(vehicle), state.vehicles)
            assertEquals(listOf("itv"), state.reminders.map { it.id.value })
            assertEquals(vehicle.id, state.selectedVehicleId)
        }

    @Test
    fun generatedReminderWithThreeAlertInstancesIsOnePendingItem() =
        runTest {
            val generatedReminder = reminder("maintenance-reminder:record-1")
            val plan = maintenanceReminderNotificationPlan(generatedReminder, MaintenanceTypeCode.Itv)
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = FakeReminderRepository(listOf(generatedReminder)),
                )

            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()

            assertEquals(3, plan?.alerts?.size)
            assertEquals(listOf(generatedReminder), viewModel.uiState.value.reminders)
        }

    @Test
    fun refreshUpdatesRemindersWithoutClearingForm() =
        runTest {
            val repository = FakeReminderRepository()
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = repository,
                )
            viewModel.onEvent(RemindersEvent.TitleChanged("Borrador"))
            repository.saveReminder(reminder("remote"))

            viewModel.onEvent(RemindersEvent.Refresh)
            advanceUntilIdle()

            assertEquals(
                listOf("remote"),
                viewModel.uiState.value.reminders
                    .map { it.id.value },
            )
            assertEquals("Borrador", viewModel.uiState.value.title)
        }

    @Test
    fun loadFailureCanBeRetried() =
        runTest {
            val vehicleRepository = FakeVehicleRepository(listOf(vehicle), failReads = true)
            val viewModel = remindersViewModel(vehicleRepository = vehicleRepository)

            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.hasLoadError)
            assertFalse(viewModel.uiState.value.isLoading)
            assertFalse(viewModel.uiState.value.isEmpty)

            vehicleRepository.failReads = false
            viewModel.onEvent(RemindersEvent.Retry)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.hasLoadError)
            assertEquals(listOf(vehicle), viewModel.uiState.value.vehicles)
        }

    @Test
    fun noVehiclesExposesPrerequisiteAndGarageNavigationEffect() =
        runTest {
            val viewModel = remindersViewModel()
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.hasNoVehicles)

            viewModel.effects.test {
                viewModel.onEvent(RemindersEvent.GarageRequested)
                runCurrent()
                assertEquals(RemindersEffect.NavigateToGarage, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun submitValidReminderSavesAndEmitsEffect() =
        runTest {
            val repository = FakeReminderRepository()
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = repository,
                )
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(RemindersEvent.TitleChanged("Pasar ITV"))
                viewModel.onEvent(RemindersEvent.DueDateChanged("2026-07-10"))
                viewModel.onEvent(RemindersEvent.SubmitReminder)
                advanceUntilIdle()

                assertIs<RemindersEffect.ReminderCreated>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(listOf("Pasar ITV"), repository.savedReminders.map { it.title })
            assertTrue(
                viewModel.uiState.value.title
                    .isBlank(),
            )
            assertEquals(1, viewModel.uiState.value.reminders.size)
        }

    @Test
    fun submitShowsProgressAndIgnoresDuplicateSubmission() =
        runTest {
            val saveGate = CompletableDeferred<Unit>()
            val repository = FakeReminderRepository(saveGate = saveGate)
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = repository,
                )
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()
            viewModel.onEvent(RemindersEvent.TitleChanged("Pasar ITV"))
            viewModel.onEvent(RemindersEvent.DueDateChanged("2026-07-10"))

            viewModel.onEvent(RemindersEvent.SubmitReminder)
            runCurrent()
            assertEquals(ReminderAction.Create, viewModel.uiState.value.activeAction)

            viewModel.onEvent(RemindersEvent.SubmitReminder)
            runCurrent()
            assertEquals(1, repository.saveCalls)

            saveGate.complete(Unit)
            advanceUntilIdle()
            assertEquals(null, viewModel.uiState.value.activeAction)
            assertEquals(1, repository.saveCalls)
        }

    @Test
    fun failedMutationKeepsFormAndAllowsRetry() =
        runTest {
            val repository = FakeReminderRepository(failSaves = true)
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = repository,
                )
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()
            viewModel.onEvent(RemindersEvent.TitleChanged("Pasar ITV"))
            viewModel.onEvent(RemindersEvent.DueDateChanged("2026-07-10"))

            viewModel.onEvent(RemindersEvent.SubmitReminder)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.hasPersistenceError)
            assertEquals("Pasar ITV", viewModel.uiState.value.title)
            assertEquals(null, viewModel.uiState.value.activeAction)

            repository.failSaves = false
            viewModel.onEvent(RemindersEvent.SubmitReminder)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.hasPersistenceError)
            assertEquals(1, viewModel.uiState.value.reminders.size)
        }

    @Test
    fun submitWithoutDueTargetShowsValidation() =
        runTest {
            val viewModel = remindersViewModel(vehicleRepository = FakeVehicleRepository(listOf(vehicle)))
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(RemindersEvent.TitleChanged("Pasar ITV"))
                viewModel.onEvent(RemindersEvent.SubmitReminder)
                advanceUntilIdle()

                assertEquals(
                    CarburaString.ValidationMissingReminderDueTarget,
                    assertIs<RemindersEffect.ValidationFailed>(awaitItem()).message,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun completeReminderRemovesItFromPendingList() =
        runTest {
            val repository = FakeReminderRepository(listOf(reminder("itv")))
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = repository,
                )
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(RemindersEvent.CompleteReminder(ReminderId("itv")))
                advanceUntilIdle()

                assertIs<RemindersEffect.ReminderCompleted>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(emptyList(), viewModel.uiState.value.reminders)
        }

    @Test
    fun deleteReminderRemovesItFromPendingList() =
        runTest {
            val repository = FakeReminderRepository(listOf(reminder("itv")))
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = repository,
                )
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(RemindersEvent.DeleteReminder(ReminderId("itv")))
                advanceUntilIdle()

                assertIs<RemindersEffect.ReminderDeleted>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(emptyList(), viewModel.uiState.value.reminders)
        }

    private fun TestScope.remindersViewModel(
        vehicleRepository: VehicleRepository = FakeVehicleRepository(),
        reminderRepository: FakeReminderRepository = FakeReminderRepository(),
    ): RemindersViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val notificationScheduler = FakeReminderNotificationScheduler()
        return RemindersViewModel(
            familyId = familyId,
            vehicleRepository = vehicleRepository,
            dispatchers =
                TestDispatcherProvider(
                    io = dispatcher,
                    default = dispatcher,
                    main = dispatcher,
                ),
            createReminderUseCase = CreateReminderUseCase(reminderRepository, notificationScheduler),
            getPendingRemindersUseCase = GetPendingRemindersUseCase(reminderRepository),
            completeReminderUseCase = CompleteReminderUseCase(reminderRepository, notificationScheduler),
            deleteReminderUseCase = DeleteReminderUseCase(reminderRepository, notificationScheduler),
            nextReminderId = { ReminderId("created-reminder") },
            coroutineScope = this,
        )
    }

    private fun reminder(id: String): Reminder =
        Reminder(
            id = ReminderId(id),
            familyId = familyId,
            vehicleId = vehicle.id,
            maintenanceTypeId = null,
            title = "Recordatorio $id",
            dueDate = CalendarDate("2026-07-10"),
        )
}

private class FakeVehicleRepository(
    private val vehicles: List<Vehicle> = emptyList(),
    var failReads: Boolean = false,
) : VehicleRepository {
    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> {
        if (failReads) error("vehicle read failed")
        return vehicles.filter { it.familyId == familyId }
    }

    override suspend fun saveVehicle(vehicle: Vehicle) = Unit

    override suspend fun deleteVehicle(vehicleId: VehicleId) = Unit
}

private class FakeReminderRepository(
    initialReminders: List<Reminder> = emptyList(),
    var failSaves: Boolean = false,
    private val saveGate: CompletableDeferred<Unit>? = null,
) : ReminderRepository {
    val savedReminders = initialReminders.toMutableList()
    var saveCalls = 0

    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> =
        savedReminders.filter { it.familyId == familyId && !it.isCompleted }

    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> = savedReminders.filter { it.vehicleId == vehicleId }

    override suspend fun saveReminder(reminder: Reminder) {
        saveCalls += 1
        saveGate?.await()
        if (failSaves) error("reminder save failed")
        savedReminders.removeAll { it.id == reminder.id }
        savedReminders += reminder
    }

    override suspend fun markReminderCompleted(reminderId: ReminderId) {
        val index = savedReminders.indexOfFirst { it.id == reminderId }
        if (index >= 0) {
            savedReminders[index] = savedReminders[index].copy(isCompleted = true)
        }
    }

    override suspend fun deleteReminder(reminderId: ReminderId) {
        savedReminders.removeAll { it.id == reminderId }
    }
}

private class FakeReminderNotificationScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(plan: ReminderNotificationPlan) = Unit

    override suspend fun cancel(reminderId: ReminderId) = Unit
}
