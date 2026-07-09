package com.asensiodev.carbura.feature.garage.presentation

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.domain.vehicle.usecase.DeleteVehicleUseCase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class GarageViewModelTest {
    private val familyId = FamilyId("family-test")

    @Test
    fun loadReturnsEmptyStateWhenGarageHasNoVehicles() = runTest {
        val viewModel = garageViewModel()

        viewModel.onEvent(GarageEvent.Started)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEmpty)
        assertEquals(emptyList(), state.vehicles)
    }

    @Test
    fun validVehicleCreationAddsVehicleToGarage() = runTest {
        val viewModel = garageViewModel(nextVehicleId = { VehicleId("vehicle-1") })

        viewModel.effects.test {
            viewModel.onEvent(GarageEvent.NameChanged("Coche familiar"))
            viewModel.onEvent(GarageEvent.OdometerChanged("12000"))
            viewModel.onEvent(GarageEvent.SubmitVehicle)
            advanceUntilIdle()

            assertIs<GarageEffect.VehicleCreated>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value
        assertEquals(1, state.vehicles.size)
        assertEquals("Coche familiar", state.vehicles.single().name)
        assertEquals(VehicleType.Car, state.vehicles.single().type)
        assertEquals(12000, state.vehicles.single().currentOdometerKm)
        assertEquals("", state.name)
        assertEquals("0", state.odometerKm)
    }

    @Test
    fun selectedVehicleTypeIsUsedWhenCreatingVehicle() = runTest {
        val viewModel = garageViewModel(nextVehicleId = { VehicleId("vehicle-1") })

        viewModel.effects.test {
            viewModel.onEvent(GarageEvent.NameChanged("Moto"))
            viewModel.onEvent(GarageEvent.OdometerChanged("3000"))
            viewModel.onEvent(GarageEvent.TypeSelected(VehicleType.Motorcycle))
            viewModel.onEvent(GarageEvent.SubmitVehicle)
            advanceUntilIdle()

            assertIs<GarageEffect.VehicleCreated>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(VehicleType.Motorcycle, viewModel.uiState.value.vehicles.single().type)
    }

    @Test
    fun blankVehicleNameReturnsValidationError() = runTest {
        val viewModel = garageViewModel()

        viewModel.effects.test {
            viewModel.onEvent(GarageEvent.NameChanged(" "))
            viewModel.onEvent(GarageEvent.OdometerChanged("12000"))
            viewModel.onEvent(GarageEvent.SubmitVehicle)
            advanceUntilIdle()

            assertIs<GarageEffect.ValidationFailed>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value
        assertTrue(state.vehicles.isEmpty())
        assertNotNull(state.errorMessage)
    }

    @Test
    fun negativeOdometerReturnsValidationError() = runTest {
        val viewModel = garageViewModel()

        viewModel.effects.test {
            viewModel.onEvent(GarageEvent.NameChanged("Coche familiar"))
            viewModel.onEvent(GarageEvent.OdometerChanged("-1"))
            viewModel.onEvent(GarageEvent.SubmitVehicle)
            advanceUntilIdle()

            assertIs<GarageEffect.ValidationFailed>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        val state = viewModel.uiState.value
        assertTrue(state.vehicles.isEmpty())
        assertNotNull(state.errorMessage)
    }

    @Test
    fun selectingVehicleEmitsNavigationEffect() = runTest {
        val viewModel = garageViewModel(nextVehicleId = { VehicleId("vehicle-1") })

        viewModel.effects.test {
            viewModel.onEvent(GarageEvent.NameChanged("Coche familiar"))
            viewModel.onEvent(GarageEvent.OdometerChanged("12000"))
            viewModel.onEvent(GarageEvent.SubmitVehicle)
            advanceUntilIdle()
            assertIs<GarageEffect.VehicleCreated>(awaitItem())

            viewModel.onEvent(GarageEvent.VehicleSelected(VehicleId("vehicle-1")))
            advanceUntilIdle()

            val effect = assertIs<GarageEffect.NavigateToVehicleHistory>(awaitItem())
            assertEquals(VehicleId("vehicle-1"), effect.vehicleId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deletingVehicleRemovesItFromGarageAndEmitsEffect() = runTest {
        val viewModel = garageViewModel(nextVehicleId = { VehicleId("vehicle-1") })

        viewModel.effects.test {
            viewModel.onEvent(GarageEvent.NameChanged("Coche familiar"))
            viewModel.onEvent(GarageEvent.OdometerChanged("12000"))
            viewModel.onEvent(GarageEvent.SubmitVehicle)
            advanceUntilIdle()
            assertIs<GarageEffect.VehicleCreated>(awaitItem())

            viewModel.onEvent(GarageEvent.DeleteVehicleConfirmed(VehicleId("vehicle-1")))
            advanceUntilIdle()

            val effect = assertIs<GarageEffect.VehicleDeleted>(awaitItem())
            assertEquals("Coche familiar", effect.vehicleName)
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(viewModel.uiState.value.vehicles.isEmpty())
    }

    private fun TestScope.garageViewModel(
        nextVehicleId: () -> VehicleId = { VehicleId("vehicle-test") },
    ): GarageViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val vehicleRepository = FakeVehicleRepository()
        return GarageViewModel(
            familyId = familyId,
            vehicleRepository = vehicleRepository,
            dispatchers = TestDispatcherProvider(
                io = dispatcher,
                default = dispatcher,
                main = dispatcher,
            ),
            deleteVehicleUseCase = DeleteVehicleUseCase(
                repository = vehicleRepository,
                reminderRepository = FakeReminderRepository(),
                notificationScheduler = FakeReminderNotificationScheduler(),
            ),
            nextVehicleId = nextVehicleId,
            coroutineScope = this,
        )
    }
}

private class FakeVehicleRepository : VehicleRepository {
    private val vehicles = mutableListOf<Vehicle>()

    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> =
        vehicles.filter { it.familyId == familyId }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        vehicles += vehicle
    }

    override suspend fun deleteVehicle(vehicleId: VehicleId) {
        vehicles.removeAll { it.id == vehicleId }
    }
}

private class FakeReminderRepository : ReminderRepository {
    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> = emptyList()
    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> = emptyList()
    override suspend fun saveReminder(reminder: Reminder) = Unit
    override suspend fun markReminderCompleted(reminderId: ReminderId) = Unit
    override suspend fun deleteReminder(reminderId: ReminderId) = Unit
}

private class FakeReminderNotificationScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(reminder: Reminder) = Unit
    override suspend fun cancel(reminderId: ReminderId) = Unit
}
