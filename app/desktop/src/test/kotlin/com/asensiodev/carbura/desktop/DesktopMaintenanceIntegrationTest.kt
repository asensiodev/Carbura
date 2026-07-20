package com.asensiodev.carbura.desktop

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.dataModule
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.feature.garage.di.garageModule
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormEffect
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormEvent
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormViewModel
import com.asensiodev.carbura.feature.maintenance.di.maintenanceModule
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryEffect
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryEvent
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryViewModel
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceLoadState
import com.asensiodev.carbura.feature.reminders.di.remindersModule
import com.asensiodev.carbura.feature.reminders.presentation.RemindersEvent
import com.asensiodev.carbura.feature.reminders.presentation.RemindersViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopMaintenanceIntegrationTest {
    private lateinit var databaseDirectory: Path
    private lateinit var driver: SqlDriver
    private lateinit var koin: Koin

    @BeforeTest
    fun setUp() {
        databaseDirectory = Files.createTempDirectory("carbura-desktop-maintenance")
        driver = JdbcSqliteDriver("jdbc:sqlite:${databaseDirectory.resolve("maintenance.db")}")
        CarburaDatabase.Schema.create(driver)
        val testDatabaseModule = module { single<SqlDriver> { driver } }
        koin =
            startKoin {
                allowOverride(true)
                modules(
                    dataModule,
                    garageModule,
                    maintenanceModule,
                    remindersModule,
                    desktopLocalModeModule,
                    testDatabaseModule,
                )
            }.koin
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
        driver.close()
        databaseDirectory.toFile().deleteRecursively()
    }

    @Test
    fun maintenanceViewModelResolvesWithoutAuthenticationDependencies() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
            assertNotNull(createJourney().maintenance)
        }

    @Test
    fun maintenanceHistoryPersistsInOrderWithGeneratedReminder() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
            val journey = createJourney()
            val itvEffect =
                createRecord(
                    journey.maintenance,
                    type = MaintenanceTypeCode.Itv,
                    performedOn = "2025-01-10",
                    odometer = "90500",
                    nextDueDate = "2027-01-10",
                    cost = "129.90",
                    workshop = "Central Garage",
                    notes = "Passed inspection",
                )
            assertTrue(itvEffect.reminderCreated)
            assertFalse(
                createRecord(
                    journey.maintenance,
                    MaintenanceTypeCode.GeneralReview,
                    "2024-06-01",
                    "80000",
                ).reminderCreated,
            )

            val history =
                journey.maintenance.uiState
                    .first { it.records.size == 2 }
                    .records
            assertEquals(listOf("2025-01-10", "2024-06-01"), history.map { it.performedOn.iso8601 })
            val itvRecord = history.first()
            assertEquals(12990, itvRecord.costCents)
            assertEquals("Central Garage", itvRecord.workshop)
            assertEquals("Passed inspection", itvRecord.notes)
            assertEquals("2027-01-10", itvRecord.nextDueDate?.iso8601)

            val reloaded = koin.get<MaintenanceHistoryViewModel> { parametersOf(journey.vehicleId, journey.familyId) }
            reloaded.onEvent(MaintenanceHistoryEvent.Started)
            assertEquals(
                2,
                reloaded.uiState
                    .first { it.records.size == 2 }
                    .records.size,
            )
            refreshReminders(journey.reminders, expectedCount = 1, started = true)
        }

    @Test
    fun futureReminderChoicesAndDeletionKeepGeneratedStateConvergent() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
            val journey = createJourney()
            createRecord(
                journey.maintenance,
                MaintenanceTypeCode.Itv,
                "2025-01-10",
                "90500",
                nextDueDate = "2027-01-10",
            )
            refreshReminders(journey.reminders, expectedCount = 1, started = true)

            val withReminder =
                createRecord(
                    journey.maintenance,
                    MaintenanceTypeCode.OilChange,
                    "2999-04-20",
                    "100000",
                    futureReminderChoice = true,
                )
            assertTrue(withReminder.reminderCreated)
            val futureRecord =
                journey.maintenance.uiState
                    .first { it.records.size == 2 }
                    .records
                    .first()
            refreshReminders(journey.reminders, expectedCount = 2)

            val saveOnly =
                createRecord(
                    journey.maintenance,
                    MaintenanceTypeCode.Repair,
                    "2998-03-10",
                    "99000",
                    futureReminderChoice = false,
                )
            assertFalse(saveOnly.reminderCreated)
            refreshReminders(journey.reminders, expectedCount = 2)

            deleteRecord(journey.maintenance, futureRecord.id)
            refreshReminders(journey.reminders, expectedCount = 1)
            val itvRecord =
                journey.maintenance.uiState.value.records.single {
                    it.maintenanceTypeCode == MaintenanceTypeCode.Itv
                }
            deleteRecord(journey.maintenance, itvRecord.id)
            refreshReminders(journey.reminders, expectedCount = 0)
        }

    @Test
    fun maintenanceEditPersistsAndUpdatesDeterministicReminder() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
            val journey = createJourney()
            createRecord(
                journey.maintenance,
                MaintenanceTypeCode.Itv,
                "2025-01-10",
                "90500",
                nextDueDate = "2027-01-10",
            )
            val record =
                journey.maintenance.uiState.value.records
                    .single()
            val updated =
                async(start = CoroutineStart.UNDISPATCHED) {
                    journey.maintenance.effects.first { it is MaintenanceHistoryEffect.MaintenanceUpdated }
                }

            journey.maintenance.onEvent(MaintenanceHistoryEvent.EditMaintenance(record.id))
            assertTrue(journey.maintenance.uiState.value.isEditing)
            journey.maintenance.onEvent(MaintenanceHistoryEvent.TypeSelected(MaintenanceTypeCode.Insurance))
            journey.maintenance.onEvent(MaintenanceHistoryEvent.NextDueDateChanged("2028-02-20"))
            journey.maintenance.onEvent(MaintenanceHistoryEvent.OdometerChanged("91000"))
            journey.maintenance.onEvent(MaintenanceHistoryEvent.SubmitMaintenanceEdit)

            assertIs<MaintenanceHistoryEffect.MaintenanceUpdated>(updated.await())
            val persisted =
                journey.maintenance.uiState.value.records
                    .single()
            assertEquals(record.id, persisted.id)
            assertEquals(MaintenanceTypeCode.Insurance, persisted.maintenanceTypeCode)
            assertEquals("2028-02-20", persisted.nextDueDate?.iso8601)
            assertEquals(91000, persisted.odometerKm)
            refreshReminders(journey.reminders, expectedCount = 1, started = true)
        }

    @Test
    fun customMaintenanceLabelPreservesExactCasingAfterReload() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
            val journey = createJourney()
            createRecord(
                journey.maintenance,
                MaintenanceTypeCode.Custom,
                "2026-01-10",
                "90500",
                customTypeLabel = "eBike ECU Check",
            )

            val reloaded = koin.get<MaintenanceHistoryViewModel> { parametersOf(journey.vehicleId, journey.familyId) }
            reloaded.onEvent(MaintenanceHistoryEvent.Started)
            val record =
                reloaded.uiState
                    .first { it.records.size == 1 }
                    .records
                    .single()
            assertEquals("eBike ECU Check", record.maintenanceTypeLabel)
        }

    private suspend fun TestScope.createJourney(): MaintenanceJourney {
        val familyId = koin.get<FamilyId>()
        val vehicleForm = koin.get<VehicleFormViewModel> { parametersOf(familyId) }
        val vehicleCreated =
            async(start = CoroutineStart.UNDISPATCHED) {
                vehicleForm.effects.first { it is VehicleFormEffect.VehicleCreated }
            }
        vehicleForm.onEvent(VehicleFormEvent.NameChanged("Family car"))
        vehicleForm.onEvent(VehicleFormEvent.OdometerChanged("90000"))
        vehicleForm.onEvent(VehicleFormEvent.SubmitVehicle)
        assertIs<VehicleFormEffect.VehicleCreated>(vehicleCreated.await())
        val vehicleId =
            koin
                .get<VehicleRepository>()
                .observeVehicles(familyId)
                .single()
                .id
        val maintenance = koin.get<MaintenanceHistoryViewModel> { parametersOf(vehicleId, familyId) }
        maintenance.onEvent(MaintenanceHistoryEvent.Started)
        assertEquals(
            MaintenanceLoadState.Content,
            maintenance.uiState.first { it.loadState == MaintenanceLoadState.Content }.loadState,
        )
        return MaintenanceJourney(
            familyId = familyId,
            vehicleId = vehicleId,
            maintenance = maintenance,
            reminders = koin.get<RemindersViewModel> { parametersOf(familyId) },
        )
    }

    private suspend fun TestScope.createRecord(
        viewModel: MaintenanceHistoryViewModel,
        type: MaintenanceTypeCode,
        performedOn: String,
        odometer: String,
        nextDueDate: String = "",
        cost: String = "",
        workshop: String = "",
        notes: String = "",
        customTypeLabel: String = "",
        futureReminderChoice: Boolean? = null,
    ): MaintenanceHistoryEffect.MaintenanceCreated {
        val created =
            async(start = CoroutineStart.UNDISPATCHED) {
                viewModel.effects.first { it is MaintenanceHistoryEffect.MaintenanceCreated }
            }
        viewModel.onEvent(MaintenanceHistoryEvent.TypeSelected(type))
        if (type == MaintenanceTypeCode.Custom) {
            viewModel.onEvent(MaintenanceHistoryEvent.CustomTypeLabelChanged(customTypeLabel))
        }
        viewModel.onEvent(MaintenanceHistoryEvent.PerformedOnChanged(performedOn))
        viewModel.onEvent(MaintenanceHistoryEvent.NextDueDateChanged(nextDueDate))
        viewModel.onEvent(MaintenanceHistoryEvent.OdometerChanged(odometer))
        viewModel.onEvent(MaintenanceHistoryEvent.CostChanged(cost))
        viewModel.onEvent(MaintenanceHistoryEvent.WorkshopChanged(workshop))
        viewModel.onEvent(MaintenanceHistoryEvent.NotesChanged(notes))
        viewModel.onEvent(MaintenanceHistoryEvent.SubmitMaintenance)
        if (futureReminderChoice != null) {
            assertTrue(viewModel.uiState.first { it.showFutureReminderOffer }.showFutureReminderOffer)
            viewModel.onEvent(
                if (futureReminderChoice) {
                    MaintenanceHistoryEvent.SaveFutureMaintenanceWithReminder
                } else {
                    MaintenanceHistoryEvent.SaveFutureMaintenanceOnly
                },
            )
        }
        return assertIs(created.await())
    }

    private suspend fun TestScope.deleteRecord(
        viewModel: MaintenanceHistoryViewModel,
        recordId: MaintenanceRecordId,
    ) {
        val deleted =
            async(start = CoroutineStart.UNDISPATCHED) {
                viewModel.effects.first { it is MaintenanceHistoryEffect.MaintenanceDeleted }
            }
        viewModel.onEvent(MaintenanceHistoryEvent.DeleteMaintenance(recordId))
        assertIs<MaintenanceHistoryEffect.MaintenanceDeleted>(deleted.await())
    }

    private suspend fun refreshReminders(
        viewModel: RemindersViewModel,
        expectedCount: Int,
        started: Boolean = false,
    ) {
        viewModel.onEvent(if (started) RemindersEvent.Started else RemindersEvent.Refresh)
        assertEquals(
            expectedCount,
            viewModel.uiState
                .first { it.reminders.size == expectedCount }
                .reminders.size,
        )
    }

    private data class MaintenanceJourney(
        val familyId: FamilyId,
        val vehicleId: VehicleId,
        val maintenance: MaintenanceHistoryViewModel,
        val reminders: RemindersViewModel,
    )
}
