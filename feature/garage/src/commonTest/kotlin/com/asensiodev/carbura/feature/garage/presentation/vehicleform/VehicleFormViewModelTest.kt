package com.asensiodev.carbura.feature.garage.presentation.vehicleform

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleFormViewModelTest {
    private val familyId = FamilyId("family-test")

    @Test
    fun duplicateCreateIsIgnoredAndSuccessResetsForm() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val repository = FakeFormVehicleRepository().apply { saveGate = gate }
            val viewModel = viewModel(repository)
            viewModel.onEvent(VehicleFormEvent.NameChanged("Coche familiar"))
            viewModel.onEvent(VehicleFormEvent.OdometerChanged("12000"))
            viewModel.onEvent(VehicleFormEvent.SubmitVehicle)
            viewModel.onEvent(VehicleFormEvent.SubmitVehicle)
            runCurrent()
            assertEquals(1, repository.saveCalls)
            gate.complete(Unit)
            advanceUntilIdle()
            assertEquals("", viewModel.uiState.value.name)
            assertEquals(null, viewModel.uiState.value.activeMutation)
        }

    @Test
    fun saveFailureKeepsCreateInputAndShowsPersistenceError() =
        runTest {
            val repository = FakeFormVehicleRepository().apply { failSaves = true }
            val viewModel = viewModel(repository)
            viewModel.onEvent(VehicleFormEvent.NameChanged("Coche familiar"))
            viewModel.onEvent(VehicleFormEvent.OdometerChanged("12000"))
            viewModel.onEvent(VehicleFormEvent.SubmitVehicle)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.persistenceError)
            assertEquals("Coche familiar", viewModel.uiState.value.name)
        }

    @Test
    fun fullEditPrefillsFieldsAndUpdatesSameVehicle() =
        runTest {
            val repository = FakeFormVehicleRepository()
            val viewModel = viewModel(repository)
            val vehicle = vehicle().copy(licensePlate = "1234 ABC")
            viewModel.onEvent(VehicleFormEvent.EditVehicleRequested(vehicle))
            assertEquals("1234 ABC", viewModel.uiState.value.editLicensePlate)
            viewModel.onEvent(VehicleFormEvent.EditNameChanged("Moto familiar"))
            viewModel.onEvent(VehicleFormEvent.EditTypeSelected(VehicleType.Motorcycle))
            viewModel.onEvent(VehicleFormEvent.SubmitVehicleEdit)
            advanceUntilIdle()
            assertEquals(vehicle.id, repository.vehicles.single().id)
            assertEquals("Moto familiar", repository.vehicles.single().name)
            assertEquals(null, viewModel.uiState.value.editingVehicleId)
        }

    @Test
    fun odometerDecreaseRequiresConfirmationBeforeUpdate() =
        runTest {
            val repository = FakeFormVehicleRepository()
            val viewModel = viewModel(repository)
            viewModel.onEvent(VehicleFormEvent.QuickOdometerUpdateRequested(vehicle()))
            viewModel.onEvent(VehicleFormEvent.EditOdometerChanged("11000"))
            viewModel.onEvent(VehicleFormEvent.SubmitVehicleEdit)
            advanceUntilIdle()
            assertEquals(OdometerDecreaseConfirmation(12_000, 11_000), viewModel.uiState.value.odometerDecreaseConfirmation)
            viewModel.onEvent(VehicleFormEvent.ConfirmOdometerDecrease)
            advanceUntilIdle()
            assertEquals(11_000, repository.vehicles.single().currentOdometerKm)
        }

    @Test
    fun planningTargetRequiresExplicitReminderDecision() =
        runTest {
            val repository = FakeFormVehicleRepository()
            val viewModel = viewModel(repository)
            viewModel.onEvent(VehicleFormEvent.NameChanged("Coche"))
            viewModel.onEvent(VehicleFormEvent.OdometerChanged("12000"))
            viewModel.onEvent(VehicleFormEvent.NextItvDateChanged("2027-05-10"))
            viewModel.onEvent(VehicleFormEvent.SubmitVehicle)
            advanceUntilIdle()
            assertEquals(VehicleSaveMode.Create, viewModel.uiState.value.reminderConfirmationMode)
            assertTrue(repository.vehicles.isEmpty())
            viewModel.onEvent(VehicleFormEvent.DeclineReminderSuggestions)
            advanceUntilIdle()
            assertEquals(CalendarDate("2027-05-10"), repository.vehicles.single().nextItvDate)
        }

    @Test
    fun invalidCreateAndEditExposeValidationEffects() =
        runTest {
            val viewModel = viewModel(FakeFormVehicleRepository())
            viewModel.effects.test {
                viewModel.onEvent(VehicleFormEvent.SubmitVehicle)
                advanceUntilIdle()
                assertIs<VehicleFormEffect.ValidationFailed>(awaitItem())
                assertNotNull(viewModel.uiState.value.createValidationError)
            }
        }

    private fun TestScope.viewModel(repository: FakeFormVehicleRepository): VehicleFormViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return VehicleFormViewModel(
            familyId = familyId,
            dispatchers = TestDispatcherProvider(dispatcher, dispatcher, dispatcher),
            vehicleRepository = repository,
            nextVehicleId = { VehicleId("vehicle-new") },
            coroutineScope = this,
        )
    }

    private fun vehicle() = Vehicle(VehicleId("vehicle-existing"), familyId, "Coche familiar", VehicleType.Car, currentOdometerKm = 12_000)
}

private class FakeFormVehicleRepository : VehicleRepository {
    val vehicles = mutableListOf<Vehicle>()
    var failSaves = false
    var saveCalls = 0
    var saveGate: CompletableDeferred<Unit>? = null

    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> = vehicles.filter { it.familyId == familyId }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        saveCalls += 1
        saveGate?.await()
        if (failSaves) error("Save failed")
        vehicles.removeAll { it.id == vehicle.id }
        vehicles += vehicle
    }

    override suspend fun deleteVehicle(vehicleId: VehicleId) = Unit
}
