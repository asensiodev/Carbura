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
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
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
    private val activeScope = ActiveFamilyScope(UserId("user-test"), familyId, 1)
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
            repository.saveReminder(activeScope, reminder("remote"))

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
    fun loadCancellationPropagatesWithoutErrorAndAllowsRetry() =
        runTest {
            val cancellation = CancellationException("load cancelled")
            val vehicleRepository = FakeVehicleRepository(listOf(vehicle), readFailure = cancellation)
            val viewModel = remindersViewModel(vehicleRepository = vehicleRepository)

            viewModel.onEvent(RemindersEvent.Started)
            val loadJob = coroutineContext.job.children.single()
            var completionCause: Throwable? = null
            loadJob.invokeOnCompletion { completionCause = it }
            advanceUntilIdle()

            assertTrue(loadJob.isCancelled)
            assertEquals(cancellation.message, assertIs<CancellationException>(completionCause).message)
            assertFalse(viewModel.uiState.value.isLoading)
            assertFalse(viewModel.uiState.value.hasLoadError)
            viewModel.effects.test {
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }

            vehicleRepository.readFailure = null
            viewModel.onEvent(RemindersEvent.Retry)
            advanceUntilIdle()

            assertEquals(listOf(vehicle), viewModel.uiState.value.vehicles)
            assertFalse(viewModel.uiState.value.hasLoadError)
        }

    @Test
    fun alreadyCancelledScopeDoesNotLeaveLoadingStuck() =
        runTest {
            val dispatcher = StandardTestDispatcher(testScheduler)
            val cancelledScope = TestScope(dispatcher).apply { cancel() }
            val viewModel = remindersViewModel(coroutineScope = cancelledScope)

            viewModel.onEvent(RemindersEvent.Started)
            runCurrent()

            assertFalse(viewModel.uiState.value.isLoading)
            assertFalse(viewModel.uiState.value.hasLoadError)
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
    fun createCancellationPropagatesWithoutErrorOrEffect() =
        runTest {
            val cancellation = CancellationException("create cancelled")
            val repository = FakeReminderRepository()
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = repository,
                )
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()
            viewModel.onEvent(RemindersEvent.TitleChanged("Pasar ITV"))
            viewModel.onEvent(RemindersEvent.DueDateChanged("2026-07-10"))
            repository.saveFailure = cancellation

            viewModel.onEvent(RemindersEvent.SubmitReminder)
            val createJob = coroutineContext.job.children.single()
            var completionCause: Throwable? = null
            createJob.invokeOnCompletion { completionCause = it }
            advanceUntilIdle()

            assertTrue(createJob.isCancelled)
            assertEquals(cancellation.message, assertIs<CancellationException>(completionCause).message)
            assertEquals(null, viewModel.uiState.value.activeAction)
            assertFalse(viewModel.uiState.value.hasPersistenceError)
            viewModel.effects.test {
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
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
    fun completeCancellationPropagatesWithoutErrorOrEffect() =
        runTest {
            val cancellation = CancellationException("complete cancelled")
            val repository = FakeReminderRepository(listOf(reminder("itv")))
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = repository,
                )
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()
            repository.completeFailure = cancellation

            viewModel.onEvent(RemindersEvent.CompleteReminder(ReminderId("itv")))
            val completeJob = coroutineContext.job.children.single()
            var completionCause: Throwable? = null
            completeJob.invokeOnCompletion { completionCause = it }
            advanceUntilIdle()

            assertTrue(completeJob.isCancelled)
            assertEquals(cancellation.message, assertIs<CancellationException>(completionCause).message)
            assertEquals(null, viewModel.uiState.value.activeAction)
            assertFalse(viewModel.uiState.value.hasPersistenceError)
            assertEquals(
                listOf("itv"),
                viewModel.uiState.value.reminders
                    .map { it.id.value },
            )
            viewModel.effects.test {
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
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

    @Test
    fun deleteCancellationPropagatesWithoutErrorOrEffect() =
        runTest {
            val cancellation = CancellationException("delete cancelled")
            val repository = FakeReminderRepository(listOf(reminder("itv")))
            val viewModel =
                remindersViewModel(
                    vehicleRepository = FakeVehicleRepository(listOf(vehicle)),
                    reminderRepository = repository,
                )
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()
            repository.deleteFailure = cancellation

            viewModel.onEvent(RemindersEvent.DeleteReminder(ReminderId("itv")))
            val deleteJob = coroutineContext.job.children.single()
            var completionCause: Throwable? = null
            deleteJob.invokeOnCompletion { completionCause = it }
            advanceUntilIdle()

            assertTrue(deleteJob.isCancelled)
            assertEquals(cancellation.message, assertIs<CancellationException>(completionCause).message)
            assertEquals(null, viewModel.uiState.value.activeAction)
            assertFalse(viewModel.uiState.value.hasPersistenceError)
            assertEquals(
                listOf("itv"),
                viewModel.uiState.value.reminders
                    .map { it.id.value },
            )
            viewModel.effects.test {
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun TestScope.remindersViewModel(
        vehicleRepository: VehicleRepository = FakeVehicleRepository(),
        reminderRepository: FakeReminderRepository = FakeReminderRepository(),
        coroutineScope: CoroutineScope = this,
    ): RemindersViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val notificationScheduler = FakeReminderNotificationScheduler()
        return RemindersViewModel(
            scope = activeScope,
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
            coroutineScope = coroutineScope,
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
    var readFailure: Throwable? = null,
) : VehicleRepository {
    override suspend fun observeVehicles(scope: ActiveFamilyScope): List<Vehicle> {
        readFailure?.let { throw it }
        if (failReads) error("vehicle read failed")
        return vehicles.filter { it.familyId == scope.familyId }
    }

    override suspend fun saveVehicle(
        scope: ActiveFamilyScope,
        vehicle: Vehicle,
    ) = Unit

    override suspend fun deleteVehicle(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ) = Unit
}

private class FakeReminderRepository(
    initialReminders: List<Reminder> = emptyList(),
    var failSaves: Boolean = false,
    private val saveGate: CompletableDeferred<Unit>? = null,
) : ReminderRepository {
    val savedReminders = initialReminders.toMutableList()
    var saveCalls = 0
    var saveFailure: Throwable? = null
    var completeFailure: Throwable? = null
    var deleteFailure: Throwable? = null

    override suspend fun getPendingReminders(scope: ActiveFamilyScope): List<Reminder> =
        savedReminders.filter { it.familyId == scope.familyId && !it.isCompleted }

    override suspend fun getRemindersByVehicle(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ): List<Reminder> =
        savedReminders.filter {
            it.familyId ==
                scope.familyId &&
                it.vehicleId == vehicleId
        }

    override suspend fun saveReminder(
        scope: ActiveFamilyScope,
        reminder: Reminder,
    ) {
        saveCalls += 1
        saveGate?.await()
        saveFailure?.let { throw it }
        if (failSaves) error("reminder save failed")
        savedReminders.removeAll { it.id == reminder.id }
        savedReminders += reminder
    }

    override suspend fun markReminderCompleted(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) {
        completeFailure?.let { throw it }
        val index = savedReminders.indexOfFirst { it.id == reminderId }
        if (index >= 0) {
            savedReminders[index] = savedReminders[index].copy(isCompleted = true)
        }
    }

    override suspend fun deleteReminder(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) {
        deleteFailure?.let { throw it }
        savedReminders.removeAll { it.id == reminderId }
    }
}

private class FakeReminderNotificationScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(
        scope: ActiveFamilyScope,
        plan: ReminderNotificationPlan,
    ) = Unit

    override suspend fun cancel(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) = Unit
}
