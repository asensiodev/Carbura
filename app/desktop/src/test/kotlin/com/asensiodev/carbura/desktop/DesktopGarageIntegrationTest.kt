package com.asensiodev.carbura.desktop

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.dataModule
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.feature.garage.di.garageModule
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageLoadState
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewEffect
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewEvent
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewViewModel
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormEffect
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormEvent
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormViewModel
import com.asensiodev.carbura.feature.reminders.di.remindersModule
import com.asensiodev.carbura.feature.reminders.presentation.RemindersEvent
import com.asensiodev.carbura.feature.reminders.presentation.RemindersViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
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
import kotlin.test.assertIs
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopGarageIntegrationTest {
    private lateinit var databaseDirectory: Path
    private lateinit var driver: SqlDriver
    private lateinit var koin: Koin

    @BeforeTest
    fun setUp() {
        databaseDirectory = Files.createTempDirectory("carbura-desktop-garage")
        driver = JdbcSqliteDriver("jdbc:sqlite:${databaseDirectory.resolve("garage.db")}")
        CarburaDatabase.Schema.create(driver)
        val testDatabaseModule = module { single<SqlDriver> { driver } }
        koin =
            startKoin {
                allowOverride(true)
                modules(dataModule, garageModule, remindersModule, desktopLocalModeModule, testDatabaseModule)
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
    fun garageViewModelsResolveWithoutAuthenticationDependencies() {
        val familyId = koin.get<FamilyId>()
        assertNotNull(koin.get<GarageOverviewViewModel> { parametersOf(familyId) })
        assertNotNull(koin.get<VehicleFormViewModel> { parametersOf(familyId) })
    }

    @Test
    fun vehicleCrudPersistsThroughDesktopGarageWiring() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
            val familyId = koin.get<FamilyId>()
            val overview = koin.get<GarageOverviewViewModel> { parametersOf(familyId) }
            val form = koin.get<VehicleFormViewModel> { parametersOf(familyId) }

            overview.onEvent(GarageOverviewEvent.Started)
            assertEquals(GarageLoadState.Loaded, overview.uiState.first { it.loadState == GarageLoadState.Loaded }.loadState)

            val created = async(start = CoroutineStart.UNDISPATCHED) { form.effects.first { it is VehicleFormEffect.VehicleCreated } }
            form.onEvent(VehicleFormEvent.NameChanged("Road trip van"))
            form.onEvent(VehicleFormEvent.OdometerChanged("42000"))
            form.onEvent(VehicleFormEvent.SubmitVehicle)
            assertIs<VehicleFormEffect.VehicleCreated>(created.await())

            overview.onEvent(GarageOverviewEvent.Refresh)
            val savedVehicle =
                overview.uiState
                    .first { it.vehicles.size == 1 }
                    .vehicles
                    .single()
            assertEquals("Road trip van", savedVehicle.name)
            assertEquals(42000, savedVehicle.currentOdometerKm)

            overview.onEvent(GarageOverviewEvent.SearchQueryChanged("ROAD"))
            assertEquals(listOf(savedVehicle), overview.uiState.value.visibleVehicles)
            overview.onEvent(GarageOverviewEvent.SearchCleared)
            assertEquals(listOf(savedVehicle), overview.uiState.value.visibleVehicles)

            form.onEvent(VehicleFormEvent.EditVehicleRequested(savedVehicle))
            form.onEvent(VehicleFormEvent.EditNameChanged("Weekend van"))
            form.onEvent(VehicleFormEvent.EditOdometerChanged("41000"))
            form.onEvent(VehicleFormEvent.SubmitVehicleEdit)
            assertNotNull(form.uiState.first { it.odometerDecreaseConfirmation != null }.odometerDecreaseConfirmation)

            val updated = async(start = CoroutineStart.UNDISPATCHED) { form.effects.first { it is VehicleFormEffect.VehicleUpdated } }
            form.onEvent(VehicleFormEvent.ConfirmOdometerDecrease)
            assertIs<VehicleFormEffect.VehicleUpdated>(updated.await())

            overview.onEvent(GarageOverviewEvent.Refresh)
            val updatedVehicle =
                overview.uiState
                    .first { it.vehicles.singleOrNull()?.name == "Weekend van" }
                    .vehicles
                    .single()
            assertEquals(41000, updatedVehicle.currentOdometerKm)

            val deleted =
                async(start = CoroutineStart.UNDISPATCHED) { overview.effects.first { it is GarageOverviewEffect.VehicleDeleted } }
            overview.onEvent(GarageOverviewEvent.DeleteVehicleConfirmed(updatedVehicle.id))
            assertIs<GarageOverviewEffect.VehicleDeleted>(deleted.await())
            assertEquals(emptyList(), overview.uiState.first { it.vehicles.isEmpty() }.vehicles)
        }

    @Test
    fun planningTargetsPersistAndReconcileGeneratedReminders() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
            val familyId = koin.get<FamilyId>()
            val form = koin.get<VehicleFormViewModel> { parametersOf(familyId) }
            val reminders = koin.get<RemindersViewModel> { parametersOf(familyId) }
            val created = async(start = CoroutineStart.UNDISPATCHED) { form.effects.first { it is VehicleFormEffect.VehicleCreated } }

            form.onEvent(VehicleFormEvent.NameChanged("Planned car"))
            form.onEvent(VehicleFormEvent.OdometerChanged("42000"))
            form.onEvent(VehicleFormEvent.NextItvDateChanged("2027-05-10"))
            form.onEvent(VehicleFormEvent.InsuranceRenewalDateChanged("2027-08-15"))
            form.onEvent(VehicleFormEvent.NextServiceOdometerChanged("50000"))
            form.onEvent(VehicleFormEvent.SubmitVehicle)
            assertEquals(
                3,
                form.uiState
                    .first { it.reminderSuggestions.size == 3 }
                    .reminderSuggestions.size,
            )
            form.onEvent(VehicleFormEvent.ConfirmReminderSuggestions)
            awaitVehicleEffect(form, created)

            val vehicle =
                koin
                    .get<VehicleRepository>()
                    .observeVehicles(familyId)
                    .single()
            assertEquals("2027-05-10", vehicle.nextItvDate?.iso8601)
            assertEquals("2027-08-15", vehicle.insuranceRenewalDate?.iso8601)
            assertEquals(50000, vehicle.nextServiceOdometerKm)
            reminders.onEvent(RemindersEvent.Started)
            assertEquals(
                3,
                reminders.uiState
                    .first { it.reminders.size == 3 }
                    .reminders.size,
            )

            form.onEvent(VehicleFormEvent.EditVehicleRequested(vehicle))
            form.onEvent(VehicleFormEvent.EditNextItvDateChanged("2028-05-10"))
            form.onEvent(VehicleFormEvent.EditNextServiceOdometerChanged(""))
            form.onEvent(VehicleFormEvent.SubmitVehicleEdit)
            form.uiState.first { it.reminderSuggestions.size == 2 }
            val updated = async(start = CoroutineStart.UNDISPATCHED) { form.effects.first { it is VehicleFormEffect.VehicleUpdated } }
            form.onEvent(VehicleFormEvent.ConfirmReminderSuggestions)
            awaitVehicleEffect(form, updated)

            val edited =
                koin
                    .get<VehicleRepository>()
                    .observeVehicles(familyId)
                    .single()
            assertEquals("2028-05-10", edited.nextItvDate?.iso8601)
            assertEquals(null, edited.nextServiceOdometerKm)
            reminders.onEvent(RemindersEvent.Refresh)
            assertEquals(
                2,
                reminders.uiState
                    .first { it.reminders.size == 2 }
                    .reminders.size,
            )
        }

    @Test
    fun quickOdometerUpdatePersistsIncreaseAndConfirmedDecrease() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
            val familyId = koin.get<FamilyId>()
            val form = koin.get<VehicleFormViewModel> { parametersOf(familyId) }
            createVehicle(form, "Mileage car", "42000")
            val repository = koin.get<VehicleRepository>()
            val vehicle = repository.observeVehicles(familyId).single()

            updateOdometer(form, vehicle, "43000")
            val increased = repository.observeVehicles(familyId).single()
            assertEquals(43000, increased.currentOdometerKm)

            form.onEvent(VehicleFormEvent.QuickOdometerUpdateRequested(increased))
            form.onEvent(VehicleFormEvent.EditOdometerChanged("41000"))
            form.onEvent(VehicleFormEvent.SubmitVehicleEdit)
            val confirmation = form.uiState.first { it.odometerDecreaseConfirmation != null }.odometerDecreaseConfirmation
            assertNotNull(confirmation)
            assertEquals(43000, confirmation.currentOdometerKm)
            assertEquals(41000, confirmation.proposedOdometerKm)
            val updated = async(start = CoroutineStart.UNDISPATCHED) { form.effects.first { it is VehicleFormEffect.VehicleUpdated } }
            form.onEvent(VehicleFormEvent.ConfirmOdometerDecrease)
            awaitVehicleEffect(form, updated)
            assertEquals(41000, repository.observeVehicles(familyId).single().currentOdometerKm)
        }

    private suspend fun TestScope.createVehicle(
        form: VehicleFormViewModel,
        name: String,
        odometer: String,
    ) {
        val effect =
            async(start = CoroutineStart.UNDISPATCHED) {
                form.effects.first { it is VehicleFormEffect.VehicleCreated }
            }
        form.onEvent(VehicleFormEvent.NameChanged(name))
        form.onEvent(VehicleFormEvent.OdometerChanged(odometer))
        form.onEvent(VehicleFormEvent.SubmitVehicle)
        awaitVehicleEffect(form, effect)
    }

    private suspend fun TestScope.updateOdometer(
        form: VehicleFormViewModel,
        vehicle: Vehicle,
        odometer: String,
    ) {
        val updated =
            async(start = CoroutineStart.UNDISPATCHED) {
                form.effects.first { it is VehicleFormEffect.VehicleUpdated }
            }
        form.onEvent(VehicleFormEvent.QuickOdometerUpdateRequested(vehicle))
        form.onEvent(VehicleFormEvent.EditOdometerChanged(odometer))
        form.onEvent(VehicleFormEvent.SubmitVehicleEdit)
        awaitVehicleEffect(form, updated)
    }

    private suspend fun awaitVehicleEffect(
        form: VehicleFormViewModel,
        effect: Deferred<VehicleFormEffect>,
    ) {
        effect.await()
        form.uiState.first { it.activeMutation == null }
    }
}
