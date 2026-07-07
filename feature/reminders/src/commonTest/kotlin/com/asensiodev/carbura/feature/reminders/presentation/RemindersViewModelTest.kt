package com.asensiodev.carbura.feature.reminders.presentation

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.CompleteReminderUseCase
import com.asensiodev.carbura.core.domain.CreateReminderUseCase
import com.asensiodev.carbura.core.domain.GetPendingRemindersUseCase
import com.asensiodev.carbura.core.domain.ReminderRepository
import com.asensiodev.carbura.core.domain.VehicleRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersViewModelTest {
    private val familyId = FamilyId("family-test")
    private val vehicle = Vehicle(
        id = VehicleId("vehicle-1"),
        familyId = familyId,
        name = "Coche familiar",
        type = VehicleType.Car,
        currentOdometerKm = 12000,
    )

    @Test
    fun startedLoadsVehiclesAndPendingReminders() = runTest {
        val viewModel = remindersViewModel(
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
    fun submitValidReminderSavesAndEmitsEffect() = runTest {
        val repository = FakeReminderRepository()
        val viewModel = remindersViewModel(
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
        assertTrue(viewModel.uiState.value.title.isBlank())
        assertEquals(1, viewModel.uiState.value.reminders.size)
    }

    @Test
    fun submitWithoutDueTargetShowsValidation() = runTest {
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
    fun completeReminderRemovesItFromPendingList() = runTest {
        val repository = FakeReminderRepository(listOf(reminder("itv")))
        val viewModel = remindersViewModel(
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

    private fun TestScope.remindersViewModel(
        vehicleRepository: VehicleRepository = FakeVehicleRepository(),
        reminderRepository: FakeReminderRepository = FakeReminderRepository(),
    ): RemindersViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return RemindersViewModel(
            familyId = familyId,
            vehicleRepository = vehicleRepository,
            dispatchers = TestDispatcherProvider(
                io = dispatcher,
                default = dispatcher,
                main = dispatcher,
            ),
            createReminderUseCase = CreateReminderUseCase(reminderRepository),
            getPendingRemindersUseCase = GetPendingRemindersUseCase(reminderRepository),
            completeReminderUseCase = CompleteReminderUseCase(reminderRepository),
            nextReminderId = { ReminderId("created-reminder") },
            coroutineScope = this,
        )
    }

    private fun reminder(id: String): Reminder = Reminder(
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
) : VehicleRepository {
    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> =
        vehicles.filter { it.familyId == familyId }

    override suspend fun saveVehicle(vehicle: Vehicle) = Unit

    override suspend fun deleteVehicle(vehicleId: VehicleId) = Unit
}

private class FakeReminderRepository(
    initialReminders: List<Reminder> = emptyList(),
) : ReminderRepository {
    val savedReminders = initialReminders.toMutableList()

    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> =
        savedReminders.filter { it.familyId == familyId && !it.isCompleted }

    override suspend fun saveReminder(reminder: Reminder) {
        savedReminders += reminder
    }

    override suspend fun markReminderCompleted(reminderId: ReminderId) {
        val index = savedReminders.indexOfFirst { it.id == reminderId }
        if (index >= 0) {
            savedReminders[index] = savedReminders[index].copy(isCompleted = true)
        }
    }
}
