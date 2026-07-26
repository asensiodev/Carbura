package com.asensiodev.carbura.feature.garage.presentation.overview

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.domain.vehicle.usecase.DeleteVehicleUseCase
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GarageOverviewViewModelTest {
    private val familyId = FamilyId("family-test")

    @Test
    fun loadFailureCanBeRetriedAndRefreshKeepsLoadedContentVisible() =
        runTest {
            val repository = FakeOverviewVehicleRepository().apply { failLoads = true }
            val viewModel = viewModel(repository)
            viewModel.onEvent(GarageOverviewEvent.Started)
            advanceUntilIdle()
            assertEquals(GarageLoadState.Error, viewModel.uiState.value.loadState)

            repository.failLoads = false
            repository.vehicles += vehicle()
            viewModel.onEvent(GarageOverviewEvent.Retry)
            advanceUntilIdle()
            assertEquals(listOf(vehicle()), viewModel.uiState.value.vehicles)

            repository.vehicles += vehicle(id = "vehicle-2")
            viewModel.onEvent(GarageOverviewEvent.Refresh)
            assertEquals(GarageLoadState.Loaded, viewModel.uiState.value.loadState)
            advanceUntilIdle()
            assertEquals(2, viewModel.uiState.value.vehicles.size)
        }

    @Test
    fun refreshAppendsNewVehiclesWithoutReorderingExistingVehicles() =
        runTest {
            val first = vehicle(id = "vehicle-1", name = "B vehicle")
            val second = vehicle(id = "vehicle-2", name = "C vehicle")
            val added = vehicle(id = "vehicle-3", name = "A vehicle")
            val repository = FakeOverviewVehicleRepository(mutableListOf(first, second))
            val viewModel = viewModel(repository)
            viewModel.onEvent(GarageOverviewEvent.Started)
            advanceUntilIdle()

            repository.vehicles.add(0, added)
            viewModel.onEvent(GarageOverviewEvent.Refresh)
            advanceUntilIdle()

            assertEquals(
                listOf(first.id, second.id, added.id),
                viewModel.uiState.value.vehicles
                    .map { it.id },
            )
        }

    @Test
    fun cancelledLoadRestoresPriorStateAndCanBeRetried() =
        runTest {
            val repository = FakeOverviewVehicleRepository().apply { failLoads = true }
            val viewModel = viewModel(repository)
            viewModel.onEvent(GarageOverviewEvent.Started)
            advanceUntilIdle()
            assertEquals(GarageLoadState.Error, viewModel.uiState.value.loadState)

            repository.failLoads = false
            repository.cancelLoads = true
            viewModel.onEvent(GarageOverviewEvent.Retry)
            advanceUntilIdle()
            assertEquals(GarageLoadState.Error, viewModel.uiState.value.loadState)

            repository.cancelLoads = false
            repository.vehicles += vehicle()
            viewModel.onEvent(GarageOverviewEvent.Retry)
            advanceUntilIdle()
            assertEquals(GarageLoadState.Loaded, viewModel.uiState.value.loadState)
            assertEquals(listOf(vehicle()), viewModel.uiState.value.vehicles)
        }

    @Test
    fun duplicateDeleteIsIgnoredAndSuccessfulDeleteEmitsEffect() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val repository = FakeOverviewVehicleRepository(mutableListOf(vehicle())).apply { deleteGate = gate }
            val viewModel = viewModel(repository)
            viewModel.onEvent(GarageOverviewEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(GarageOverviewEvent.DeleteVehicleConfirmed(VehicleId("vehicle-1")))
                viewModel.onEvent(GarageOverviewEvent.DeleteVehicleConfirmed(VehicleId("vehicle-1")))
                runCurrent()
                assertEquals(1, repository.deleteCalls)
                gate.complete(Unit)
                advanceUntilIdle()
                assertIs<GarageOverviewEffect.VehicleDeleted>(awaitItem())
                assertTrue(
                    viewModel.uiState.value.vehicles
                        .isEmpty(),
                )
            }
        }

    @Test
    fun deleteFailureKeepsVehicleAndExposesError() =
        runTest {
            val repository = FakeOverviewVehicleRepository(mutableListOf(vehicle())).apply { failDeletes = true }
            val viewModel = viewModel(repository)
            viewModel.onEvent(GarageOverviewEvent.Started)
            advanceUntilIdle()
            viewModel.onEvent(GarageOverviewEvent.DeleteVehicleConfirmed(VehicleId("vehicle-1")))
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.deleteError)
            assertEquals(1, viewModel.uiState.value.vehicles.size)
        }

    @Test
    fun cancelledDeleteClearsDeletionWithoutErrorOrEffectAndCanBeRetried() =
        runTest {
            val repository = FakeOverviewVehicleRepository(mutableListOf(vehicle())).apply { cancelDeletes = true }
            val viewModel = viewModel(repository)
            viewModel.onEvent(GarageOverviewEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(GarageOverviewEvent.DeleteVehicleConfirmed(VehicleId("vehicle-1")))
                advanceUntilIdle()
                assertEquals(null, viewModel.uiState.value.deletingVehicleId)
                assertFalse(viewModel.uiState.value.deleteError)
                assertEquals(1, viewModel.uiState.value.vehicles.size)
                expectNoEvents()

                repository.cancelDeletes = false
                viewModel.onEvent(GarageOverviewEvent.DeleteVehicleConfirmed(VehicleId("vehicle-1")))
                advanceUntilIdle()
                assertIs<GarageOverviewEffect.VehicleDeleted>(awaitItem())
            }
        }

    @Test
    fun alreadyCancelledScopeDoesNotActivateLoadOrDeleteState() =
        runTest {
            val job = Job()
            val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + job)
            val repository = FakeOverviewVehicleRepository(mutableListOf(vehicle())).apply { failLoads = true }
            val viewModel = viewModel(repository, scope)

            viewModel.onEvent(GarageOverviewEvent.Started)
            advanceUntilIdle()
            assertEquals(GarageLoadState.Error, viewModel.uiState.value.loadState)
            job.cancel()
            repository.failLoads = false

            viewModel.onEvent(GarageOverviewEvent.Retry)
            viewModel.onEvent(GarageOverviewEvent.DeleteVehicleConfirmed(VehicleId("vehicle-1")))
            runCurrent()

            assertEquals(GarageLoadState.Error, viewModel.uiState.value.loadState)
            assertEquals(null, viewModel.uiState.value.deletingVehicleId)
            assertFalse(viewModel.uiState.value.deleteError)
        }

    @Test
    fun selectingVehicleEmitsNavigationEffect() =
        runTest {
            val viewModel = viewModel(FakeOverviewVehicleRepository())
            viewModel.effects.test {
                viewModel.onEvent(GarageOverviewEvent.VehicleSelected(VehicleId("vehicle-1")))
                assertEquals(
                    VehicleId("vehicle-1"),
                    assertIs<GarageOverviewEffect.NavigateToVehicleHistory>(awaitItem()).vehicleId,
                )
            }
        }

    private fun TestScope.viewModel(
        repository: FakeOverviewVehicleRepository,
        scope: CoroutineScope = this,
    ): GarageOverviewViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return GarageOverviewViewModel(
            scope = ActiveFamilyScope(UserId("user-test"), familyId, 1),
            vehicleRepository = repository,
            dispatchers = TestDispatcherProvider(dispatcher, dispatcher, dispatcher),
            deleteVehicleUseCase = DeleteVehicleUseCase(repository, EmptyReminderRepository(), EmptyScheduler()),
            coroutineScope = scope,
        )
    }

    private fun vehicle(
        id: String = "vehicle-1",
        name: String = "Coche familiar",
    ) = Vehicle(VehicleId(id), familyId, name, VehicleType.Car, currentOdometerKm = 12_000)
}

private class FakeOverviewVehicleRepository(
    val vehicles: MutableList<Vehicle> = mutableListOf(),
) : VehicleRepository {
    var failLoads = false
    var cancelLoads = false
    var failDeletes = false
    var cancelDeletes = false
    var deleteCalls = 0
    var deleteGate: CompletableDeferred<Unit>? = null

    override suspend fun observeVehicles(scope: ActiveFamilyScope): List<Vehicle> {
        if (cancelLoads) throw CancellationException("Load cancelled")
        if (failLoads) error("Load failed")
        return vehicles.filter { it.familyId == scope.familyId }
    }

    override suspend fun saveVehicle(
        scope: ActiveFamilyScope,
        vehicle: Vehicle,
    ) = Unit

    override suspend fun deleteVehicle(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ) {
        deleteCalls += 1
        deleteGate?.await()
        if (cancelDeletes) throw CancellationException("Delete cancelled")
        if (failDeletes) error("Delete failed")
        vehicles.removeAll { it.id == vehicleId }
    }
}

private class EmptyReminderRepository : ReminderRepository {
    override suspend fun getPendingReminders(scope: ActiveFamilyScope): List<Reminder> = emptyList()

    override suspend fun getRemindersByVehicle(
        scope: ActiveFamilyScope,
        vehicleId: VehicleId,
    ): List<Reminder> = emptyList()

    override suspend fun saveReminder(
        scope: ActiveFamilyScope,
        reminder: Reminder,
    ) = Unit

    override suspend fun markReminderCompleted(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) = Unit

    override suspend fun deleteReminder(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) = Unit
}

private class EmptyScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(
        scope: ActiveFamilyScope,
        plan: ReminderNotificationPlan,
    ) = Unit

    override suspend fun cancel(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) = Unit
}
