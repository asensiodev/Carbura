package com.asensiodev.carbura.feature.maintenance.presentation

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.DeleteMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.GetVehicleHistoryUseCase
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.testing.TestDispatcherProvider
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MaintenanceHistoryViewModelTest {
    private val familyId = FamilyId("family-test")
    private val vehicleId = VehicleId("vehicle-test")
    private val vehicle =
        Vehicle(
            id = vehicleId,
            familyId = familyId,
            name = "Coche familiar",
            type = VehicleType.Car,
            brand = "Seat",
            model = "León",
            licensePlate = "1234 ABC",
            currentOdometerKm = 12_300,
        )

    @Test
    fun initialStateUsesProvidedLocalDateAndDoesNotFlashEmptyContent() =
        runTest {
            val viewModel = maintenanceHistoryViewModel(localDate = CalendarDate("2030-02-03"))

            assertEquals("2030-02-03", viewModel.uiState.value.performedOn)
            assertEquals(MaintenanceLoadState.Loading, viewModel.uiState.value.loadState)
            assertFalse(viewModel.uiState.value.isEmpty)
        }

    @Test
    fun loadExposesSelectedVehicleResolvedById() =
        runTest {
            val otherVehicle = vehicle.copy(id = VehicleId("other"), name = "Otro")
            val viewModel = maintenanceHistoryViewModel(vehicleRepository = FakeVehicleRepository(listOf(otherVehicle, vehicle)))

            viewModel.onEvent(MaintenanceHistoryEvent.Started)
            advanceUntilIdle()

            assertEquals(vehicle, viewModel.uiState.value.vehicle)
            assertEquals(MaintenanceLoadState.Content, viewModel.uiState.value.loadState)
        }

    @Test
    fun failedLoadShowsRecoverableErrorAndRetryLoadsContent() =
        runTest {
            val vehicleRepository = FakeVehicleRepository(listOf(vehicle), failLoads = true)
            val viewModel = maintenanceHistoryViewModel(vehicleRepository = vehicleRepository)

            viewModel.onEvent(MaintenanceHistoryEvent.Started)
            advanceUntilIdle()
            assertEquals(MaintenanceLoadState.Error, viewModel.uiState.value.loadState)

            vehicleRepository.failLoads = false
            viewModel.onEvent(MaintenanceHistoryEvent.Retry)
            advanceUntilIdle()

            assertEquals(MaintenanceLoadState.Content, viewModel.uiState.value.loadState)
            assertEquals(vehicle, viewModel.uiState.value.vehicle)
        }

    @Test
    fun loadReturnsEmptyStateWhenVehicleHasNoRecords() =
        runTest {
            val viewModel = maintenanceHistoryViewModel()

            viewModel.onEvent(MaintenanceHistoryEvent.Started)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isEmpty)
            assertEquals(emptyList(), state.records)
        }

    @Test
    fun refreshUpdatesHistoryWithoutClearingForm() =
        runTest {
            val repository = FakeMaintenanceRecordRepository()
            val viewModel = maintenanceHistoryViewModel(repository = repository)
            viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged("Borrador"))
            repository.saveMaintenanceRecord(record("remote", "2026-07-17"))

            viewModel.onEvent(MaintenanceHistoryEvent.Refresh)
            advanceUntilIdle()

            assertEquals(
                listOf("remote"),
                viewModel.uiState.value.records
                    .map { it.id.value },
            )
            assertEquals("Borrador", viewModel.uiState.value.type)
        }

    @Test
    fun validMaintenanceCreationAddsRecordToHistory() =
        runTest {
            val viewModel = maintenanceHistoryViewModel(nextRecordId = { MaintenanceRecordId("record-1") })

            viewModel.effects.test {
                viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged("Aceite"))
                viewModel.onEvent(MaintenanceHistoryEvent.PerformedOnChanged("2026-07-04"))
                viewModel.onEvent(MaintenanceHistoryEvent.OdometerChanged("12300"))
                viewModel.onEvent(MaintenanceHistoryEvent.CostChanged("89.50"))
                viewModel.onEvent(MaintenanceHistoryEvent.WorkshopChanged("Taller Centro"))
                viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
                advanceUntilIdle()

                assertIs<MaintenanceHistoryEffect.MaintenanceCreated>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            val state = viewModel.uiState.value
            assertEquals(1, state.records.size)
            assertEquals(CalendarDate("2026-07-04"), state.records.single().performedOn)
            assertEquals(12300, state.records.single().odometerKm)
            assertEquals(8950, state.records.single().costCents)
            assertEquals("", state.type)
            assertEquals("0", state.odometerKm)
        }

    @Test
    fun blankMaintenanceTypeReturnsValidationError() =
        runTest {
            val viewModel = maintenanceHistoryViewModel()

            viewModel.effects.test {
                viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged(" "))
                viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
                advanceUntilIdle()

                assertIs<MaintenanceHistoryEffect.ValidationFailed>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertTrue(
                viewModel.uiState.value.records
                    .isEmpty(),
            )
            assertNotNull(viewModel.uiState.value.validationError)
        }

    @Test
    fun negativeOdometerReturnsValidationError() =
        runTest {
            val viewModel = maintenanceHistoryViewModel()

            viewModel.effects.test {
                viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged("Aceite"))
                viewModel.onEvent(MaintenanceHistoryEvent.OdometerChanged("-1"))
                viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
                advanceUntilIdle()

                assertIs<MaintenanceHistoryEffect.ValidationFailed>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertTrue(
                viewModel.uiState.value.records
                    .isEmpty(),
            )
            assertNotNull(viewModel.uiState.value.validationError)
        }

    @Test
    fun invalidDateReturnsValidationError() =
        runTest {
            val viewModel = maintenanceHistoryViewModel()

            viewModel.effects.test {
                viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged("Aceite"))
                viewModel.onEvent(MaintenanceHistoryEvent.PerformedOnChanged("04/07/2026"))
                viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
                advanceUntilIdle()

                assertIs<MaintenanceHistoryEffect.ValidationFailed>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertTrue(
                viewModel.uiState.value.records
                    .isEmpty(),
            )
            assertNotNull(viewModel.uiState.value.validationError)
        }

    @Test
    fun historyIsOrderedByDateDescending() =
        runTest {
            val repository = FakeMaintenanceRecordRepository()
            repository.saveMaintenanceRecord(record("record-old", "2026-01-01"))
            repository.saveMaintenanceRecord(record("record-new", "2026-07-04"))
            val viewModel = maintenanceHistoryViewModel(repository = repository)

            viewModel.onEvent(MaintenanceHistoryEvent.Started)
            advanceUntilIdle()

            val records = viewModel.uiState.value.records
            assertEquals(listOf("record-new", "record-old"), records.map { it.id.value })
        }

    @Test
    fun deleteMaintenanceRemovesRecordFromHistory() =
        runTest {
            val repository = FakeMaintenanceRecordRepository()
            repository.saveMaintenanceRecord(record("record-1", "2026-07-04"))
            val viewModel = maintenanceHistoryViewModel(repository = repository)
            viewModel.onEvent(MaintenanceHistoryEvent.Started)
            advanceUntilIdle()

            viewModel.effects.test {
                viewModel.onEvent(MaintenanceHistoryEvent.DeleteMaintenance(MaintenanceRecordId("record-1")))
                advanceUntilIdle()

                assertIs<MaintenanceHistoryEffect.MaintenanceDeleted>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            assertEquals(emptyList(), viewModel.uiState.value.records)
        }

    @Test
    fun duplicateSaveIsIgnoredWhilePersistenceIsActive() =
        runTest {
            val saveGate = CompletableDeferred<Unit>()
            val repository = FakeMaintenanceRecordRepository(saveGate = saveGate)
            val viewModel = maintenanceHistoryViewModel(repository = repository)
            viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged("Aceite"))

            viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
            viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
            testScheduler.runCurrent()

            assertEquals(MaintenanceMutation.Saving, viewModel.uiState.value.activeMutation)
            assertEquals(1, repository.saveCalls)
            saveGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(1, repository.savedRecords.size)
            assertEquals(null, viewModel.uiState.value.activeMutation)
        }

    @Test
    fun duplicateDeleteIsIgnoredWhilePersistenceIsActive() =
        runTest {
            val deleteGate = CompletableDeferred<Unit>()
            val repository = FakeMaintenanceRecordRepository(deleteGate = deleteGate)
            repository.saveMaintenanceRecord(record("record-1", "2026-07-17"))
            val viewModel = maintenanceHistoryViewModel(repository = repository)
            viewModel.onEvent(MaintenanceHistoryEvent.Started)
            advanceUntilIdle()

            val event = MaintenanceHistoryEvent.DeleteMaintenance(MaintenanceRecordId("record-1"))
            viewModel.onEvent(event)
            viewModel.onEvent(event)
            testScheduler.runCurrent()

            assertEquals(MaintenanceMutation.Deleting(MaintenanceRecordId("record-1")), viewModel.uiState.value.activeMutation)
            assertEquals(1, repository.deleteCalls)
            deleteGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(0, repository.savedRecords.size)
        }

    @Test
    fun persistenceFailureIsSeparateFromValidationAndCanBeRetried() =
        runTest {
            val repository = FakeMaintenanceRecordRepository(failSaves = true)
            val viewModel = maintenanceHistoryViewModel(repository = repository)
            viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged("Aceite"))

            viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.persistenceError)
            assertEquals(null, viewModel.uiState.value.validationError)
            repository.failSaves = false
            viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.persistenceError)
            assertEquals(1, repository.savedRecords.size)
        }

    private fun TestScope.maintenanceHistoryViewModel(
        repository: FakeMaintenanceRecordRepository = FakeMaintenanceRecordRepository(),
        vehicleRepository: FakeVehicleRepository = FakeVehicleRepository(listOf(vehicle)),
        nextRecordId: () -> MaintenanceRecordId = { MaintenanceRecordId("record-test") },
        localDate: CalendarDate = CalendarDate("2026-07-17"),
    ): MaintenanceHistoryViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return MaintenanceHistoryViewModel(
            vehicleId = vehicleId,
            familyId = familyId,
            dispatchers =
                TestDispatcherProvider(
                    io = dispatcher,
                    default = dispatcher,
                    main = dispatcher,
                ),
            createMaintenanceRecordFromInputUseCase =
                CreateMaintenanceRecordFromInputUseCase(
                    CreateMaintenanceRecordUseCase(repository),
                ),
            getVehicleHistoryUseCase = GetVehicleHistoryUseCase(repository),
            deleteMaintenanceRecordUseCase = DeleteMaintenanceRecordUseCase(repository),
            vehicleRepository = vehicleRepository,
            nextRecordId = nextRecordId,
            localDateProvider = LocalDateProvider { localDate },
            coroutineScope = this,
        )
    }

    private fun record(
        id: String,
        date: String,
    ): MaintenanceRecord =
        MaintenanceRecord(
            id = MaintenanceRecordId(id),
            familyId = familyId,
            vehicleId = vehicleId,
            maintenanceTypeId = MaintenanceTypeId("type-test"),
            maintenanceTypeCode = MaintenanceTypeCode.Custom,
            performedOn = CalendarDate(date),
            odometerKm = 1,
        )
}

private class FakeMaintenanceRecordRepository(
    var failSaves: Boolean = false,
    private val saveGate: CompletableDeferred<Unit>? = null,
    private val deleteGate: CompletableDeferred<Unit>? = null,
) : MaintenanceRecordRepository {
    val savedRecords = mutableListOf<MaintenanceRecord>()
    var saveCalls = 0
    var deleteCalls = 0

    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) {
        saveCalls += 1
        saveGate?.await()
        if (failSaves) error("save failed")
        savedRecords += record
    }

    override suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord> =
        savedRecords.filter { it.vehicleId == vehicleId }

    override suspend fun deleteMaintenanceRecord(recordId: MaintenanceRecordId) {
        deleteCalls += 1
        deleteGate?.await()
        savedRecords.removeAll { it.id == recordId }
    }
}

private class FakeVehicleRepository(
    private val vehicles: List<Vehicle>,
    var failLoads: Boolean = false,
) : VehicleRepository {
    override suspend fun observeVehicles(familyId: FamilyId): List<Vehicle> {
        if (failLoads) error("load failed")
        return vehicles.filter { it.familyId == familyId }
    }

    override suspend fun saveVehicle(vehicle: Vehicle) = Unit

    override suspend fun deleteVehicle(vehicleId: VehicleId) = Unit
}
