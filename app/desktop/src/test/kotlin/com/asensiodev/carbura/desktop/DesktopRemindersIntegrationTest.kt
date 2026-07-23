package com.asensiodev.carbura.desktop

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.auth.SupabaseSettings
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormEffect
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormEvent
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormViewModel
import com.asensiodev.carbura.feature.reminders.presentation.RemindersEffect
import com.asensiodev.carbura.feature.reminders.presentation.RemindersEvent
import com.asensiodev.carbura.feature.reminders.presentation.RemindersViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopRemindersIntegrationTest {
    private lateinit var databaseDirectory: Path
    private lateinit var driver: SqlDriver
    private lateinit var koin: Koin

    @BeforeTest
    fun setUp() {
        databaseDirectory = Files.createTempDirectory("carbura-desktop-reminders")
        driver = JdbcSqliteDriver("jdbc:sqlite:${databaseDirectory.resolve("reminders.db")}")
        CarburaDatabase.Schema.create(driver)
        val testDatabaseModule = module { single<SqlDriver> { driver } }
        koin =
            startKoin {
                allowOverride(true)
                modules(desktopModules(SupabaseSettings("", "")) + testDatabaseModule)
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
    fun remindersViewModelResolvesWithoutAuthenticationDependencies() {
        val familyId = koin.get<FamilyId>()
        assertNotNull(koin.get<RemindersViewModel> { parametersOf(familyId) })
    }

    @Test
    fun garageVehiclesDrivePersistentReminderMutationsAndFilters() =
        runTest {
            Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
            val familyId = koin.get<FamilyId>()
            val vehicleForm = koin.get<VehicleFormViewModel> { parametersOf(familyId) }
            val reminders = koin.get<RemindersViewModel> { parametersOf(familyId) }

            val firstVehicleCreated =
                async(start = CoroutineStart.UNDISPATCHED) {
                    vehicleForm.effects.first { it is VehicleFormEffect.VehicleCreated }
                }
            vehicleForm.onEvent(VehicleFormEvent.NameChanged("Family car"))
            vehicleForm.onEvent(VehicleFormEvent.OdometerChanged("12000"))
            vehicleForm.onEvent(VehicleFormEvent.SubmitVehicle)
            awaitVehicleCreation(vehicleForm, firstVehicleCreated)
            val secondVehicleCreated =
                async(start = CoroutineStart.UNDISPATCHED) {
                    vehicleForm.effects.first { it is VehicleFormEffect.VehicleCreated }
                }
            vehicleForm.onEvent(VehicleFormEvent.NameChanged("Work van"))
            vehicleForm.onEvent(VehicleFormEvent.OdometerChanged("34000"))
            vehicleForm.onEvent(VehicleFormEvent.SubmitVehicle)
            awaitVehicleCreation(vehicleForm, secondVehicleCreated)

            reminders.onEvent(RemindersEvent.Started)
            val loaded = reminders.uiState.first { !it.isLoading && it.vehicles.size == 2 }
            val familyCar = loaded.vehicles.single { it.name == "Family car" }
            val workVan = loaded.vehicles.single { it.name == "Work van" }

            val reminderCreated =
                async(start = CoroutineStart.UNDISPATCHED) {
                    reminders.effects.first { it is RemindersEffect.ReminderCreated }
                }
            reminders.onEvent(RemindersEvent.TitleChanged("Annual inspection"))
            reminders.onEvent(RemindersEvent.VehicleSelected(familyCar.id))
            reminders.onEvent(RemindersEvent.DueDateChanged("2027-03-14"))
            reminders.onEvent(RemindersEvent.SubmitReminder)
            assertIs<RemindersEffect.ReminderCreated>(awaitReminderAction(reminders, reminderCreated))

            val createdReminder =
                reminders.uiState
                    .first { it.reminders.size == 1 }
                    .reminders
                    .single()
            assertEquals(familyCar.id, createdReminder.vehicleId)
            assertEquals("2027-03-14", createdReminder.dueDate?.iso8601)

            reminders.onEvent(RemindersEvent.VehicleFilterToggled(workVan.id))
            val filtered = reminders.uiState.first { workVan.id in it.selectedFilterVehicleIds }
            assertTrue(filtered.hasNoMatchingReminders)
            assertEquals(emptyList(), filtered.visibleReminders)
            reminders.onEvent(RemindersEvent.VehicleFiltersCleared)
            assertEquals(listOf(createdReminder), reminders.uiState.first { it.selectedFilterVehicleIds.isEmpty() }.visibleReminders)

            val reminderCompleted =
                async(start = CoroutineStart.UNDISPATCHED) {
                    reminders.effects.first { it is RemindersEffect.ReminderCompleted }
                }
            reminders.onEvent(RemindersEvent.CompleteReminder(createdReminder.id))
            assertIs<RemindersEffect.ReminderCompleted>(awaitReminderAction(reminders, reminderCompleted))
            assertEquals(emptyList(), reminders.uiState.first { it.reminders.isEmpty() }.reminders)

            val secondReminderCreated =
                async(start = CoroutineStart.UNDISPATCHED) {
                    reminders.effects.first { it is RemindersEffect.ReminderCreated }
                }
            reminders.onEvent(RemindersEvent.TitleChanged("Oil service"))
            reminders.onEvent(RemindersEvent.VehicleSelected(workVan.id))
            reminders.onEvent(RemindersEvent.DueOdometerChanged("40000"))
            reminders.onEvent(RemindersEvent.SubmitReminder)
            assertIs<RemindersEffect.ReminderCreated>(awaitReminderAction(reminders, secondReminderCreated))
            val secondReminder =
                reminders.uiState
                    .first { it.reminders.size == 1 }
                    .reminders
                    .single()

            val reminderDeleted =
                async(start = CoroutineStart.UNDISPATCHED) {
                    reminders.effects.first { it is RemindersEffect.ReminderDeleted }
                }
            reminders.onEvent(RemindersEvent.DeleteReminder(secondReminder.id))
            assertIs<RemindersEffect.ReminderDeleted>(awaitReminderAction(reminders, reminderDeleted))
            assertEquals(emptyList(), reminders.uiState.first { it.reminders.isEmpty() }.reminders)
        }

    private suspend fun awaitVehicleCreation(
        viewModel: VehicleFormViewModel,
        effect: Deferred<VehicleFormEffect>,
    ) {
        assertIs<VehicleFormEffect.VehicleCreated>(effect.await())
        viewModel.uiState.first { it.activeMutation == null }
    }

    private suspend fun awaitReminderAction(
        viewModel: RemindersViewModel,
        effect: Deferred<RemindersEffect>,
    ): RemindersEffect {
        val result = effect.await()
        viewModel.uiState.first { it.activeAction == null }
        return result
    }
}
