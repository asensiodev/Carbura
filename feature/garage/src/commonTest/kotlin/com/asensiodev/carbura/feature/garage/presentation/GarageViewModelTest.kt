package com.asensiodev.carbura.feature.garage.presentation

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.domain.vehicle.usecase.DeleteVehicleUseCase
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GarageViewModelTest {
    private val familyId = FamilyId("family-test")

    @Test
    fun loadReturnsEmptyStateWhenGarageHasNoVehicles() =
        runTest {
            val viewModel = garageViewModel()

            viewModel.onEvent(GarageEvent.Started)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isEmpty)
            assertEquals(emptyList(), state.vehicles)
        }

    @Test
    fun validVehicleCreationAddsVehicleToGarage() =
        runTest {
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
    fun selectedVehicleTypeIsUsedWhenCreatingVehicle() =
        runTest {
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

            assertEquals(
                VehicleType.Motorcycle,
                viewModel.uiState.value.vehicles
                    .single()
                    .type,
            )
        }

    @Test
    fun blankVehicleNameReturnsValidationError() =
        runTest {
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
    fun negativeOdometerReturnsValidationError() =
        runTest {
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
    fun selectingVehicleEmitsNavigationEffect() =
        runTest {
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
    fun deletingVehicleRemovesItFromGarageAndEmitsEffect() =
        runTest {
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

            assertTrue(
                viewModel.uiState.value.vehicles
                    .isEmpty(),
            )
        }

    @Test
    fun openingVehicleEditPrefillsAllEditableFields() =
        runTest {
            val vehicle = vehicle(licensePlate = "1234 ABC")
            val viewModel = garageViewModel(initialVehicles = listOf(vehicle))
            viewModel.onEvent(GarageEvent.Started)
            advanceUntilIdle()

            viewModel.onEvent(GarageEvent.EditVehicleRequested(vehicle.id))

            val state = viewModel.uiState.value
            assertEquals(VehicleEditMode.Full, state.editMode)
            assertEquals(vehicle.id, state.editingVehicleId)
            assertEquals(vehicle.name, state.editName)
            assertEquals("1234 ABC", state.editLicensePlate)
            assertEquals("12000", state.editOdometerKm)
            assertEquals(vehicle.type, state.editType)
        }

    @Test
    fun validVehicleEditUpdatesSameVehicleAndClearsEditState() =
        runTest {
            val vehicle = vehicle()
            val viewModel = garageViewModel(initialVehicles = listOf(vehicle))
            viewModel.onEvent(GarageEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(GarageEvent.EditVehicleRequested(vehicle.id))
                viewModel.onEvent(GarageEvent.EditNameChanged("Moto familiar"))
                viewModel.onEvent(GarageEvent.EditTypeSelected(VehicleType.Motorcycle))
                viewModel.onEvent(GarageEvent.EditLicensePlateChanged("5678 XYZ"))
                viewModel.onEvent(GarageEvent.EditOdometerChanged("13000"))
                viewModel.onEvent(GarageEvent.SubmitVehicleEdit)
                advanceUntilIdle()

                assertIs<GarageEffect.VehicleUpdated>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            val state = viewModel.uiState.value
            val updated = state.vehicles.single()
            assertEquals(vehicle.id, updated.id)
            assertEquals("Moto familiar", updated.name)
            assertEquals(VehicleType.Motorcycle, updated.type)
            assertEquals("5678 XYZ", updated.licensePlate)
            assertEquals(13000, updated.currentOdometerKm)
            assertEquals(null, state.editingVehicleId)
        }

    @Test
    fun invalidVehicleEditKeepsOriginalVehicleAndShowsError() =
        runTest {
            val vehicle = vehicle()
            val viewModel = garageViewModel(initialVehicles = listOf(vehicle))
            viewModel.onEvent(GarageEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(GarageEvent.EditVehicleRequested(vehicle.id))
                viewModel.onEvent(GarageEvent.EditNameChanged(" "))
                viewModel.onEvent(GarageEvent.SubmitVehicleEdit)
                advanceUntilIdle()

                assertIs<GarageEffect.ValidationFailed>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(
                vehicle,
                viewModel.uiState.value.vehicles
                    .single(),
            )
            assertNotNull(viewModel.uiState.value.editErrorMessage)
        }

    @Test
    fun quickOdometerDecreaseRequiresConfirmationAndCanBeCancelled() =
        runTest {
            val vehicle = vehicle()
            val viewModel = garageViewModel(initialVehicles = listOf(vehicle))
            viewModel.onEvent(GarageEvent.Started)
            advanceUntilIdle()
            viewModel.onEvent(GarageEvent.QuickOdometerUpdateRequested(vehicle.id))
            viewModel.onEvent(GarageEvent.EditOdometerChanged("11000"))

            viewModel.onEvent(GarageEvent.SubmitVehicleEdit)
            advanceUntilIdle()

            assertEquals(
                OdometerDecreaseConfirmation(12000, 11000),
                viewModel.uiState.value.odometerDecreaseConfirmation,
            )
            assertEquals(
                12000,
                viewModel.uiState.value.vehicles
                    .single()
                    .currentOdometerKm,
            )

            viewModel.onEvent(GarageEvent.CancelOdometerDecrease)
            assertEquals(null, viewModel.uiState.value.odometerDecreaseConfirmation)
            assertEquals(
                12000,
                viewModel.uiState.value.vehicles
                    .single()
                    .currentOdometerKm,
            )
        }

    @Test
    fun confirmedQuickOdometerDecreaseUpdatesVehicle() =
        runTest {
            val vehicle = vehicle()
            val viewModel = garageViewModel(initialVehicles = listOf(vehicle))
            viewModel.onEvent(GarageEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(GarageEvent.QuickOdometerUpdateRequested(vehicle.id))
                viewModel.onEvent(GarageEvent.EditOdometerChanged("11000"))
                viewModel.onEvent(GarageEvent.SubmitVehicleEdit)
                advanceUntilIdle()
                viewModel.onEvent(GarageEvent.ConfirmOdometerDecrease)
                advanceUntilIdle()

                assertIs<GarageEffect.VehicleUpdated>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(
                11000,
                viewModel.uiState.value.vehicles
                    .single()
                    .currentOdometerKm,
            )
        }

    @Test
    fun vehicleEditPrefillsPlanningFields() =
        runTest {
            val vehicle =
                vehicle().copy(
                    nextItvDate = CalendarDate("2027-05-10"),
                    insuranceRenewalDate = CalendarDate("2027-01-20"),
                    nextServiceOdometerKm = 25000,
                )
            val viewModel = garageViewModel(initialVehicles = listOf(vehicle))
            viewModel.onEvent(GarageEvent.Started)
            advanceUntilIdle()

            viewModel.onEvent(GarageEvent.EditVehicleRequested(vehicle.id))

            assertEquals("2027-05-10", viewModel.uiState.value.editNextItvDate)
            assertEquals("2027-01-20", viewModel.uiState.value.editInsuranceRenewalDate)
            assertEquals("25000", viewModel.uiState.value.editNextServiceOdometerKm)
        }

    @Test
    fun vehicleWithPlanningTargetsRequiresExplicitReminderConfirmation() =
        runTest {
            val viewModel = garageViewModel(nextVehicleId = { VehicleId("vehicle-planned") })
            viewModel.onEvent(GarageEvent.NameChanged("Coche"))
            viewModel.onEvent(GarageEvent.OdometerChanged("12000"))
            viewModel.onEvent(GarageEvent.NextItvDateChanged("2027-05-10"))

            viewModel.onEvent(GarageEvent.SubmitVehicle)
            advanceUntilIdle()

            assertEquals(VehicleSaveMode.Create, viewModel.uiState.value.reminderConfirmationMode)
            assertEquals(1, viewModel.uiState.value.reminderSuggestions.size)
            assertTrue(
                viewModel.uiState.value.vehicles
                    .isEmpty(),
            )

            viewModel.onEvent(GarageEvent.DeclineReminderSuggestions)
            advanceUntilIdle()
            assertEquals(
                CalendarDate("2027-05-10"),
                viewModel.uiState.value.vehicles
                    .single()
                    .nextItvDate,
            )
        }

    private fun TestScope.garageViewModel(
        nextVehicleId: () -> VehicleId = { VehicleId("vehicle-test") },
        initialVehicles: List<Vehicle> = emptyList(),
    ): GarageViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val vehicleRepository = FakeVehicleRepository(initialVehicles)
        return GarageViewModel(
            familyId = familyId,
            vehicleRepository = vehicleRepository,
            dispatchers =
                TestDispatcherProvider(
                    io = dispatcher,
                    default = dispatcher,
                    main = dispatcher,
                ),
            deleteVehicleUseCase =
                DeleteVehicleUseCase(
                    repository = vehicleRepository,
                    reminderRepository = FakeReminderRepository(),
                    notificationScheduler = FakeReminderNotificationScheduler(),
                ),
            nextVehicleId = nextVehicleId,
            coroutineScope = this,
        )
    }

    private fun vehicle(licensePlate: String? = null): Vehicle =
        Vehicle(
            id = VehicleId("vehicle-existing"),
            familyId = familyId,
            name = "Coche familiar",
            type = VehicleType.Car,
            licensePlate = licensePlate,
            currentOdometerKm = 12000,
        )
}

private class FakeVehicleRepository(
    initialVehicles: List<Vehicle> = emptyList(),
) : VehicleRepository {
    private val vehicles = initialVehicles.toMutableList()

    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> = vehicles.filter { it.familyId == familyId }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        vehicles.removeAll { it.id == vehicle.id }
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
