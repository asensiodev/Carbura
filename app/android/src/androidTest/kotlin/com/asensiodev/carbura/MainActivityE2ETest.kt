package com.asensiodev.carbura

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.asensiodev.carbura.core.auth.authModule
import com.asensiodev.carbura.core.data.dataModule
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.auth.AuthGateway
import com.asensiodev.carbura.core.domain.auth.AuthSession
import com.asensiodev.carbura.core.domain.auth.AuthUser
import com.asensiodev.carbura.core.domain.reminder.notification.NoOpNotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.sync.SyncResult
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import com.asensiodev.carbura.core.domain.user.RemoteUserProfile
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.UserId
import com.asensiodev.carbura.feature.garage.di.garageModule
import com.asensiodev.carbura.feature.maintenance.di.maintenanceModule
import com.asensiodev.carbura.feature.onboarding.di.onboardingModule
import com.asensiodev.carbura.feature.reminders.di.remindersModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class MainActivityE2ETest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var driver: SqlDriver

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        driver = AndroidSqliteDriver(CarburaDatabase.Schema, context, null)
        startKoin {
            allowOverride(true)
            androidContext(context)
            modules(
                authModule,
                dataModule,
                onboardingModule,
                garageModule,
                maintenanceModule,
                remindersModule,
                e2eBoundaryModule(context, driver),
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        driver.close()
    }

    @Test
    fun createsVehicleAndFutureItvWithRenderedHistoryAndReminder() {
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.onNodeWithTag("garage_title").assertIsDisplayed()
            composeRule.onNodeWithText("Añadir vehículo").performClick()
            composeRule.onNodeWithTag("vehicle_name_input").performTextReplacement(VEHICLE_NAME)
            composeRule.onNodeWithTag("vehicle_odometer_input").performScrollTo().performTextReplacement("42000")
            composeRule.onNodeWithTag("save_vehicle_button").performScrollTo().performClick()

            composeRule.onNodeWithText(VEHICLE_NAME).performScrollTo().assertIsDisplayed()
            composeRule.onNodeWithText("Ver vehículo e historial").performClick()
            composeRule.onNodeWithTag("add_maintenance_fab").performClick()
            composeRule.onNodeWithTag("maintenance_type_dropdown").performClick()
            composeRule.onNodeWithTag("maintenance_type_option_Itv").performClick()

            selectTomorrowInDatePicker()
            composeRule.onNodeWithTag("maintenance_odometer_input").performScrollTo().performTextReplacement("42000")
            closeSoftKeyboard()
            composeRule.onNodeWithTag("save_maintenance_button").assertIsDisplayed().performClick()
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onNodeWithTag("future_maintenance_reminder_dialog").isDisplayed()
            }
            composeRule.onNodeWithTag("future_maintenance_reminder_dialog").assertIsDisplayed()
            composeRule.onNodeWithTag("create_future_maintenance_reminder").performClick()

            composeRule.waitUntil(timeoutMillis = 5_000) {
                !composeRule.onNodeWithTag("full_screen_maintenance_form").isDisplayed()
            }
            composeRule.onNodeWithText("Historial").assertIsDisplayed()
            composeRule.onNodeWithText("ITV").assertIsDisplayed()
            pressBack()
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onNodeWithTag("reminders_tab").isDisplayed()
            }
            composeRule.onNodeWithTag("reminders_tab").performClick()
            composeRule.onNodeWithTag("reminders_title").assertIsDisplayed()
            composeRule.onNodeWithText("Mantenimiento programado").assertIsDisplayed()
            composeRule.onAllNodesWithText(VEHICLE_NAME)[0].assertIsDisplayed()
        }
    }

    private fun selectTomorrowInDatePicker() {
        val tomorrow = LocalDate.now().plusDays(1)
        composeRule.onNodeWithTag("maintenance_performed_date").performClick()
        if (tomorrow.month != LocalDate.now().month) {
            composeRule.onNodeWithTag("maintenance_date_picker").performTouchInput { swipeLeft() }
        }
        val localizedTomorrow =
            tomorrow.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault()))
        composeRule.onNodeWithText(localizedTomorrow).performClick()
        composeRule.onNodeWithTag("maintenance_date_confirm").performClick()
        val renderedTomorrow =
            tomorrow.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))
        composeRule.onNodeWithTag("maintenance_performed_date").assertTextContains(renderedTomorrow)
    }

    private fun e2eBoundaryModule(
        context: Context,
        sqlDriver: SqlDriver,
    ) = module {
        single<SqlDriver> { sqlDriver }
        single<AuthGateway> { RestoredAuthGateway }
        single<RemoteUserProfileGateway> { RestoredProfileGateway }
        single<SyncManager> { NoOpSyncManager }
        single<ReminderNotificationScheduler> { NoOpNotificationScheduler }
        single<NotificationOutboxRecovery> { NoOpNotificationOutboxRecovery }
        single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
        single<Context> { context }
    }

    private companion object {
        const val VEHICLE_NAME = "Coche E2E"
    }
}

private object RestoredAuthGateway : AuthGateway {
    private val session = AuthSession("test-token", AuthUser("user-e2e", "e2e@carbura.test", "E2E User"))

    override suspend fun currentSession(): AuthSession = session

    override suspend fun signInWithGoogle(): AuthSession = session

    override suspend fun signInWithGoogle(idToken: String): AuthSession = session

    override suspend fun signOut() = Unit

    override suspend fun deleteAccount() = Unit
}

private object RestoredProfileGateway : RemoteUserProfileGateway {
    private val profile =
        RemoteUserProfile(
            userId = UserId("user-e2e"),
            familyId = FamilyId("family-e2e"),
            familyName = "Familia E2E",
            displayName = "E2E User",
            email = "e2e@carbura.test",
        )

    override suspend fun getProfileForUser(userId: UserId): RemoteUserProfile = profile

    override suspend fun ensureProfile(
        displayName: String,
        email: String?,
    ): RemoteUserProfile = profile
}

private object NoOpSyncManager : SyncManager {
    override val status: StateFlow<SyncStatus> = MutableStateFlow(SyncStatus())

    override suspend fun syncNow(): SyncResult = SyncResult.Success(System.currentTimeMillis())

    override fun acknowledgeFailure(failureId: Long) = Unit
}

private object NoOpNotificationScheduler : ReminderNotificationScheduler {
    override suspend fun schedule(
        scope: ActiveFamilyScope,
        plan: ReminderNotificationPlan,
    ) = Unit

    override suspend fun cancel(
        scope: ActiveFamilyScope,
        reminderId: ReminderId,
    ) = Unit
}
