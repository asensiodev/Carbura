package com.asensiodev.carbura.feature.reminders.presentation

import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.reminder.usecase.CompleteReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.DeleteReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.GetPendingRemindersUseCase
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderVehicleFilterViewModelTest {
    private val familyId = FamilyId("family-filter")
    private val firstVehicle = vehicle("vehicle-1", "Coche familiar")
    private val secondVehicle = vehicle("vehicle-2", "Moto")

    @Test
    fun emptySelectionShowsAllRemindersInSourceOrder() {
        val reminders = listOf(reminder("first", firstVehicle.id), reminder("second", secondVehicle.id))

        val state = RemindersUiState(isLoading = false, reminders = reminders)

        assertEquals(reminders, state.visibleReminders)
    }

    @Test
    fun vehicleFiltersSupportSingleAndMultipleSelection() =
        runTest {
            val viewModel = viewModel()
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()

            viewModel.onEvent(RemindersEvent.VehicleFilterToggled(firstVehicle.id))
            assertEquals(setOf(firstVehicle.id), viewModel.uiState.value.selectedFilterVehicleIds)
            assertEquals(
                listOf("first"),
                viewModel.uiState.value.visibleReminders
                    .map { it.id.value },
            )

            viewModel.onEvent(RemindersEvent.VehicleFilterToggled(secondVehicle.id))
            assertEquals(setOf(firstVehicle.id, secondVehicle.id), viewModel.uiState.value.selectedFilterVehicleIds)
            assertEquals(
                listOf("first", "second"),
                viewModel.uiState.value.visibleReminders
                    .map { it.id.value },
            )
        }

    @Test
    fun togglingFinalVehicleOffReturnsToAll() =
        runTest {
            val viewModel = viewModel()
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()

            viewModel.onEvent(RemindersEvent.VehicleFilterToggled(firstVehicle.id))
            viewModel.onEvent(RemindersEvent.VehicleFilterToggled(firstVehicle.id))

            assertEquals(emptySet(), viewModel.uiState.value.selectedFilterVehicleIds)
            assertEquals(
                listOf("first", "second"),
                viewModel.uiState.value.visibleReminders
                    .map { it.id.value },
            )
        }

    @Test
    fun allFilterClearsMultipleSelections() =
        runTest {
            val viewModel = viewModel()
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()
            viewModel.onEvent(RemindersEvent.VehicleFilterToggled(firstVehicle.id))
            viewModel.onEvent(RemindersEvent.VehicleFilterToggled(secondVehicle.id))

            viewModel.onEvent(RemindersEvent.VehicleFiltersCleared)

            assertEquals(emptySet(), viewModel.uiState.value.selectedFilterVehicleIds)
        }

    @Test
    fun refreshRemovesUnavailableVehicleFilters() =
        runTest {
            val vehicleRepository = FilterVehicleRepository(mutableListOf(firstVehicle, secondVehicle))
            val viewModel = viewModel(vehicleRepository)
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()
            viewModel.onEvent(RemindersEvent.VehicleFilterToggled(secondVehicle.id))
            vehicleRepository.vehicles.remove(secondVehicle)

            viewModel.onEvent(RemindersEvent.Refresh)
            advanceUntilIdle()

            assertEquals(emptySet(), viewModel.uiState.value.selectedFilterVehicleIds)
        }

    @Test
    fun staleToggleCannotRestoreUnavailableVehicleFilter() =
        runTest {
            val vehicleRepository = FilterVehicleRepository(mutableListOf(firstVehicle, secondVehicle))
            val viewModel = viewModel(vehicleRepository)
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()
            vehicleRepository.vehicles.remove(secondVehicle)
            viewModel.onEvent(RemindersEvent.Refresh)
            advanceUntilIdle()

            viewModel.onEvent(RemindersEvent.VehicleFilterToggled(secondVehicle.id))

            assertEquals(emptySet(), viewModel.uiState.value.selectedFilterVehicleIds)
        }

    @Test
    fun completingFinalMatchingReminderPreservesFilterAndShowsNoMatch() =
        runTest {
            val viewModel = viewModel()
            viewModel.onEvent(RemindersEvent.Started)
            advanceUntilIdle()
            viewModel.onEvent(RemindersEvent.VehicleFilterToggled(firstVehicle.id))

            viewModel.onEvent(RemindersEvent.CompleteReminder(ReminderId("first")))
            advanceUntilIdle()

            assertEquals(setOf(firstVehicle.id), viewModel.uiState.value.selectedFilterVehicleIds)
            assertEquals(
                listOf("second"),
                viewModel.uiState.value.reminders
                    .map { it.id.value },
            )
            assertEquals(emptyList(), viewModel.uiState.value.visibleReminders)
            assertEquals(true, viewModel.uiState.value.hasNoMatchingReminders)
        }

    private fun TestScope.viewModel(
        vehicleRepository: FilterVehicleRepository = FilterVehicleRepository(mutableListOf(firstVehicle, secondVehicle)),
    ): RemindersViewModel {
        val reminderRepository = FilterReminderRepository(listOf(reminder("first", firstVehicle.id), reminder("second", secondVehicle.id)))
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scheduler = FilterNotificationScheduler()
        return RemindersViewModel(
            familyId = familyId,
            vehicleRepository = vehicleRepository,
            dispatchers = TestDispatcherProvider(dispatcher, dispatcher, dispatcher),
            createReminderUseCase = CreateReminderUseCase(reminderRepository, scheduler),
            getPendingRemindersUseCase = GetPendingRemindersUseCase(reminderRepository),
            completeReminderUseCase = CompleteReminderUseCase(reminderRepository, scheduler),
            deleteReminderUseCase = DeleteReminderUseCase(reminderRepository, scheduler),
            coroutineScope = this,
        )
    }

    private fun vehicle(
        id: String,
        name: String,
    ) = Vehicle(
        id = VehicleId(id),
        familyId = familyId,
        name = name,
        type = VehicleType.Car,
        currentOdometerKm = 10_000,
    )

    private fun reminder(
        id: String,
        vehicleId: VehicleId,
    ) = Reminder(
        id = ReminderId(id),
        familyId = familyId,
        vehicleId = vehicleId,
        maintenanceTypeId = null,
        title = "Recordatorio $id",
        dueDate = CalendarDate("2026-08-01"),
    )
}

private class FilterVehicleRepository(
    val vehicles: MutableList<Vehicle>,
) : VehicleRepository {
    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> = vehicles.filter { it.familyId == familyId }

    override suspend fun saveVehicle(vehicle: Vehicle) = Unit

    override suspend fun deleteVehicle(vehicleId: VehicleId) = Unit
}

private class FilterReminderRepository(
    reminders: List<Reminder>,
) : ReminderRepository {
    private val reminders = reminders.toMutableList()

    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> =
        reminders.filter { it.familyId == familyId && !it.isCompleted }

    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> = reminders.filter { it.vehicleId == vehicleId }

    override suspend fun saveReminder(reminder: Reminder) = Unit

    override suspend fun markReminderCompleted(reminderId: ReminderId) {
        val index = reminders.indexOfFirst { it.id == reminderId }
        if (index >= 0) reminders[index] = reminders[index].copy(isCompleted = true)
    }

    override suspend fun deleteReminder(reminderId: ReminderId) = Unit
}

private class FilterNotificationScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(plan: ReminderNotificationPlan) = Unit

    override suspend fun cancel(reminderId: ReminderId) = Unit
}
