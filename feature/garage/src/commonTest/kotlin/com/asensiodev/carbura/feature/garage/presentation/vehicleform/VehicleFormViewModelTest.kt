package com.asensiodev.carbura.feature.garage.presentation.vehicleform

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.repository.ReminderRepository
import com.asensiodev.carbura.core.domain.reminder.usecase.SaveVehicleWithRemindersUseCase
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
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
    fun cancelledCreateClearsMutationWithoutErrorOrSuccessEffect() =
        runTest {
            val repository = FakeFormVehicleRepository().apply { cancelSaves = true }
            val viewModel = viewModel(repository)
            viewModel.onEvent(VehicleFormEvent.NameChanged("Coche familiar"))
            viewModel.onEvent(VehicleFormEvent.OdometerChanged("12000"))

            viewModel.effects.test {
                viewModel.onEvent(VehicleFormEvent.SubmitVehicle)
                advanceUntilIdle()

                assertEquals(null, viewModel.uiState.value.activeMutation)
                assertFalse(viewModel.uiState.value.persistenceError)
                assertEquals("Coche familiar", viewModel.uiState.value.name)
                expectNoEvents()
            }
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
    fun cancelledUpdateKeepsEditOpenWithoutErrorOrSuccessEffect() =
        runTest {
            val repository = FakeFormVehicleRepository().apply { cancelSaves = true }
            val viewModel = viewModel(repository)
            viewModel.onEvent(VehicleFormEvent.EditVehicleRequested(vehicle()))
            viewModel.onEvent(VehicleFormEvent.EditNameChanged("Nombre actualizado"))

            viewModel.effects.test {
                viewModel.onEvent(VehicleFormEvent.SubmitVehicleEdit)
                advanceUntilIdle()

                assertEquals(null, viewModel.uiState.value.activeMutation)
                assertFalse(viewModel.uiState.value.persistenceError)
                assertEquals(VehicleId("vehicle-existing"), viewModel.uiState.value.editingVehicleId)
                assertEquals("Nombre actualizado", viewModel.uiState.value.editName)
                expectNoEvents()
            }
        }

    @Test
    fun fullEditTracksDirtyStateUntilDismissed() =
        runTest {
            val viewModel = viewModel(FakeFormVehicleRepository())
            viewModel.onEvent(VehicleFormEvent.EditVehicleRequested(vehicle()))
            assertFalse(viewModel.uiState.value.isEditDirty)

            viewModel.onEvent(VehicleFormEvent.EditNameChanged("Nombre temporal"))
            assertTrue(viewModel.uiState.value.isEditDirty)
            viewModel.onEvent(VehicleFormEvent.EditNameChanged("Coche familiar"))
            assertFalse(viewModel.uiState.value.isEditDirty)

            viewModel.onEvent(VehicleFormEvent.EditOdometerChanged("12001"))
            viewModel.onEvent(VehicleFormEvent.DismissVehicleEdit)
            assertEquals(null, viewModel.uiState.value.editingVehicleId)
            assertFalse(viewModel.uiState.value.isEditDirty)
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
    fun unrelatedEditDoesNotRepeatReminderDecisionForExistingTarget() =
        runTest {
            val repository = FakeFormVehicleRepository()
            val viewModel = viewModel(repository)
            val vehicle = vehicle().copy(nextItvDate = CalendarDate("2027-05-10"))
            viewModel.onEvent(VehicleFormEvent.EditVehicleRequested(vehicle))
            viewModel.onEvent(VehicleFormEvent.EditNameChanged("Coche actualizado"))

            viewModel.onEvent(VehicleFormEvent.SubmitVehicleEdit)
            advanceUntilIdle()

            assertEquals(null, viewModel.uiState.value.reminderConfirmationMode)
            assertEquals("Coche actualizado", repository.vehicles.single().name)
            assertEquals(CalendarDate("2027-05-10"), repository.vehicles.single().nextItvDate)
        }

    @Test
    fun changedPlanningTargetRequiresReminderDecisionDuringEdit() =
        runTest {
            val repository = FakeFormVehicleRepository()
            val viewModel = viewModel(repository)
            val vehicle = vehicle().copy(nextItvDate = CalendarDate("2027-05-10"))
            viewModel.onEvent(VehicleFormEvent.EditVehicleRequested(vehicle))
            viewModel.onEvent(VehicleFormEvent.EditNextItvDateChanged("2027-06-10"))

            viewModel.onEvent(VehicleFormEvent.SubmitVehicleEdit)
            advanceUntilIdle()

            assertEquals(VehicleSaveMode.Edit, viewModel.uiState.value.reminderConfirmationMode)
            assertTrue(repository.vehicles.isEmpty())
        }

    @Test
    fun removedPlanningTargetRequiresReminderDecisionDuringEdit() =
        runTest {
            val repository = FakeFormVehicleRepository()
            val viewModel = viewModel(repository)
            val vehicle = vehicle().copy(nextItvDate = CalendarDate("2027-05-10"))
            viewModel.onEvent(VehicleFormEvent.EditVehicleRequested(vehicle))
            viewModel.onEvent(VehicleFormEvent.EditNextItvDateChanged(""))

            viewModel.onEvent(VehicleFormEvent.SubmitVehicleEdit)
            advanceUntilIdle()

            assertEquals(VehicleSaveMode.Edit, viewModel.uiState.value.reminderConfirmationMode)
            assertTrue(
                viewModel.uiState.value.reminderSuggestions
                    .isEmpty(),
            )
            assertTrue(repository.vehicles.isEmpty())
        }

    @Test
    fun cancelledReconciliationDoesNotPublishCreateSuccess() =
        runTest {
            val repository = FakeFormVehicleRepository()
            val reminderRepository = FakeFormReminderRepository().apply { cancelSaves = true }
            val viewModel = viewModel(repository, reminderRepository = reminderRepository)
            viewModel.onEvent(VehicleFormEvent.NameChanged("Coche familiar"))
            viewModel.onEvent(VehicleFormEvent.OdometerChanged("12000"))
            viewModel.onEvent(VehicleFormEvent.NextItvDateChanged("2027-05-10"))
            viewModel.onEvent(VehicleFormEvent.SubmitVehicle)
            advanceUntilIdle()
            assertEquals(VehicleSaveMode.Create, viewModel.uiState.value.reminderConfirmationMode)

            viewModel.effects.test {
                viewModel.onEvent(VehicleFormEvent.ConfirmReminderSuggestions)
                advanceUntilIdle()

                assertEquals(null, viewModel.uiState.value.activeMutation)
                assertFalse(viewModel.uiState.value.persistenceError)
                assertEquals("Coche familiar", viewModel.uiState.value.name)
                expectNoEvents()
            }
        }

    @Test
    fun alreadyCancelledScopeDoesNotActivateMutationState() =
        runTest {
            val job = Job().apply { cancel() }
            val cancelledScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + job)
            val viewModel = viewModel(FakeFormVehicleRepository(), scope = cancelledScope)
            viewModel.onEvent(VehicleFormEvent.NameChanged("Coche familiar"))

            viewModel.onEvent(VehicleFormEvent.SubmitVehicle)
            runCurrent()

            assertEquals(null, viewModel.uiState.value.activeMutation)
            assertFalse(viewModel.uiState.value.persistenceError)
            assertEquals("Coche familiar", viewModel.uiState.value.name)
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

    private fun TestScope.viewModel(
        repository: FakeFormVehicleRepository,
        reminderRepository: ReminderRepository? = null,
        scope: CoroutineScope = this,
    ): VehicleFormViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return VehicleFormViewModel(
            familyId = familyId,
            dispatchers = TestDispatcherProvider(dispatcher, dispatcher, dispatcher),
            vehicleRepository = repository,
            saveVehicleWithRemindersUseCase =
                reminderRepository?.let {
                    SaveVehicleWithRemindersUseCase(repository, it, EmptyFormScheduler())
                },
            nextVehicleId = { VehicleId("vehicle-new") },
            coroutineScope = scope,
        )
    }

    private fun vehicle() = Vehicle(VehicleId("vehicle-existing"), familyId, "Coche familiar", VehicleType.Car, currentOdometerKm = 12_000)
}

private class FakeFormVehicleRepository : VehicleRepository {
    val vehicles = mutableListOf<Vehicle>()
    var failSaves = false
    var cancelSaves = false
    var saveCalls = 0
    var saveGate: CompletableDeferred<Unit>? = null

    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> = vehicles.filter { it.familyId == familyId }

    override suspend fun saveVehicle(vehicle: Vehicle) {
        saveCalls += 1
        saveGate?.await()
        if (cancelSaves) throw CancellationException("Save cancelled")
        if (failSaves) error("Save failed")
        vehicles.removeAll { it.id == vehicle.id }
        vehicles += vehicle
    }

    override suspend fun deleteVehicle(vehicleId: VehicleId) = Unit
}

private class FakeFormReminderRepository : ReminderRepository {
    var cancelSaves = false

    override suspend fun getPendingReminders(familyId: FamilyId): List<Reminder> = emptyList()

    override suspend fun getRemindersByVehicle(vehicleId: VehicleId): List<Reminder> = emptyList()

    override suspend fun saveReminder(reminder: Reminder) {
        if (cancelSaves) throw CancellationException("Reconciliation cancelled")
    }

    override suspend fun markReminderCompleted(reminderId: ReminderId) = Unit

    override suspend fun deleteReminder(reminderId: ReminderId) = Unit
}

private class EmptyFormScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(plan: ReminderNotificationPlan) = Unit

    override suspend fun cancel(reminderId: ReminderId) = Unit
}
