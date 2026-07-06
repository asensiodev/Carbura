package com.asensiodev.carbura.feature.maintenance.presentation

import app.cash.turbine.test
import com.asensiodev.carbura.core.domain.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.GetVehicleHistoryUseCase
import com.asensiodev.carbura.core.domain.MaintenanceRecordRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.VehicleId
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
class MaintenanceHistoryViewModelTest {
    private val familyId = FamilyId("family-test")
    private val vehicleId = VehicleId("vehicle-test")

    @Test
    fun loadReturnsEmptyStateWhenVehicleHasNoRecords() = runTest {
        val viewModel = maintenanceHistoryViewModel()

        viewModel.onEvent(MaintenanceHistoryEvent.Started)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEmpty)
        assertEquals(emptyList(), state.records)
    }

    @Test
    fun validMaintenanceCreationAddsRecordToHistory() = runTest {
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
    fun blankMaintenanceTypeReturnsValidationError() = runTest {
        val viewModel = maintenanceHistoryViewModel()

        viewModel.effects.test {
            viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged(" "))
            viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
            advanceUntilIdle()

            assertIs<MaintenanceHistoryEffect.ValidationFailed>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(viewModel.uiState.value.records.isEmpty())
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun negativeOdometerReturnsValidationError() = runTest {
        val viewModel = maintenanceHistoryViewModel()

        viewModel.effects.test {
            viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged("Aceite"))
            viewModel.onEvent(MaintenanceHistoryEvent.OdometerChanged("-1"))
            viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
            advanceUntilIdle()

            assertIs<MaintenanceHistoryEffect.ValidationFailed>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(viewModel.uiState.value.records.isEmpty())
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun invalidDateReturnsValidationError() = runTest {
        val viewModel = maintenanceHistoryViewModel()

        viewModel.effects.test {
            viewModel.onEvent(MaintenanceHistoryEvent.TypeChanged("Aceite"))
            viewModel.onEvent(MaintenanceHistoryEvent.PerformedOnChanged("04/07/2026"))
            viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
            advanceUntilIdle()

            assertIs<MaintenanceHistoryEffect.ValidationFailed>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        assertTrue(viewModel.uiState.value.records.isEmpty())
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun historyIsOrderedByDateDescending() = runTest {
        val repository = FakeMaintenanceRecordRepository()
        repository.saveMaintenanceRecord(record("record-old", "2026-01-01"))
        repository.saveMaintenanceRecord(record("record-new", "2026-07-04"))
        val viewModel = maintenanceHistoryViewModel(repository = repository)

        viewModel.onEvent(MaintenanceHistoryEvent.Started)
        advanceUntilIdle()

        val records = viewModel.uiState.value.records
        assertEquals(listOf("record-new", "record-old"), records.map { it.id.value })
    }

    private fun TestScope.maintenanceHistoryViewModel(
        repository: FakeMaintenanceRecordRepository = FakeMaintenanceRecordRepository(),
        nextRecordId: () -> MaintenanceRecordId = { MaintenanceRecordId("record-test") },
    ): MaintenanceHistoryViewModel {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        return MaintenanceHistoryViewModel(
            vehicleId = vehicleId,
            familyId = familyId,
            dispatchers = TestDispatcherProvider(
                io = dispatcher,
                default = dispatcher,
                main = dispatcher,
            ),
            createMaintenanceRecordUseCase = CreateMaintenanceRecordUseCase(repository),
            getVehicleHistoryUseCase = GetVehicleHistoryUseCase(repository),
            nextRecordId = nextRecordId,
            coroutineScope = this,
        )
    }

    private fun record(id: String, date: String): MaintenanceRecord = MaintenanceRecord(
        id = MaintenanceRecordId(id),
        familyId = familyId,
        vehicleId = vehicleId,
        maintenanceTypeId = MaintenanceTypeId("type-test"),
        maintenanceTypeCode = MaintenanceTypeCode.Custom,
        performedOn = CalendarDate(date),
        odometerKm = 1,
    )
}

private class FakeMaintenanceRecordRepository : MaintenanceRecordRepository {
    private val records = mutableListOf<MaintenanceRecord>()

    override suspend fun saveMaintenanceRecord(record: MaintenanceRecord) {
        records += record
    }

    override suspend fun getVehicleHistory(vehicleId: VehicleId): List<MaintenanceRecord> =
        records.filter { it.vehicleId == vehicleId }
}
