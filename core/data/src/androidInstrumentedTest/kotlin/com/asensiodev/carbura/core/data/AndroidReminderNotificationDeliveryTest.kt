package com.asensiodev.carbura.core.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class AndroidReminderNotificationDeliveryTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var driver: AndroidSqliteDriver
    private lateinit var database: CarburaDatabase
    private lateinit var receiver: AndroidReminderNotificationReceiver
    private lateinit var activeScope: ActiveFamilyScope

    @Before
    fun setUp() {
        context.deleteDatabase(DATABASE_NAME)
        driver = AndroidSqliteDriver(CarburaDatabase.Schema, context, DATABASE_NAME)
        database = CarburaDatabase(driver)
        val familyScope =
            SqlDelightActiveFamilyScopeGateway(database).apply {
                activeScope = activateAuthenticated(UserId("user"), FamilyId("family"))
            }
        startKoin {
            modules(
                module {
                    single { database }
                    single<CoroutineScope> { scope }
                    single<ActiveFamilyScopeGateway> { familyScope }
                },
            )
        }
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        notificationManager.createNotificationChannel(
            NotificationChannel(REMINDER_NOTIFICATION_CHANNEL_ID, "Reminders", NotificationManager.IMPORTANCE_DEFAULT),
        )
        notificationManager.cancelAll()
        receiver = AndroidReminderNotificationReceiver()
        context.registerReceiver(receiver, IntentFilter(TEST_ACTION), Context.RECEIVER_NOT_EXPORTED)
        insertActiveReminder(revision = 2)
    }

    @After
    fun tearDown() {
        context.unregisterReceiver(receiver)
        notificationManager.cancelAll()
        stopKoin()
        scope.cancel()
        driver.close()
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun matchingRevisionDisplaysAndStaleRevisionIsSuppressed() {
        context.sendBroadcast(notificationIntent(revision = 2))

        assertTrue(waitUntil { notificationManager.activeNotifications.size == 1 })

        notificationManager.cancelAll()
        context.sendBroadcast(notificationIntent(revision = 1))
        Thread.sleep(500)

        assertEquals(0, notificationManager.activeNotifications.size)
    }

    private fun insertActiveReminder(revision: Long) {
        database.carburaDatabaseQueries.upsertReminder(
            id = REMINDER_ID,
            familyId = "family",
            vehicleId = "vehicle",
            maintenanceTypeId = null,
            title = "ITV",
            dueDate = "2027-07-01",
            dueOdometerKm = null,
            notifyDaysBefore = 7,
            isCompleted = 0,
            updatedAt = 1,
            pendingSync = 0,
            deletedAt = null,
        )
        database.carburaDatabaseQueries.replaceNotificationRevision(
            familyId = "family",
            reminderId = REMINDER_ID,
            revision = revision,
        )
    }

    private fun notificationIntent(revision: Long) =
        Intent(TEST_ACTION).apply {
            setPackage(context.packageName)
            putExtra(AndroidReminderNotificationReceiver.EXTRA_REMINDER_ID, REMINDER_ID)
            putExtra(AndroidReminderNotificationReceiver.EXTRA_FAMILY_ID, activeScope.familyId.value)
            putExtra(AndroidReminderNotificationReceiver.EXTRA_USER_ID, activeScope.userId?.value)
            putExtra(AndroidReminderNotificationReceiver.EXTRA_SCOPE_GENERATION, activeScope.generation)
            putExtra(AndroidReminderNotificationReceiver.EXTRA_REMINDER_TITLE, "ITV")
            putExtra(AndroidReminderNotificationReceiver.EXTRA_ALERT_KIND, ReminderAlertKind.Manual.name)
            putExtra(AndroidReminderNotificationReceiver.EXTRA_DUE_DATE, "2027-07-01")
            putExtra(AndroidReminderNotificationReceiver.EXTRA_REVISION, revision)
        }

    private fun waitUntil(predicate: () -> Boolean): Boolean {
        repeat(20) {
            if (predicate()) return true
            Thread.sleep(100)
        }
        return false
    }

    private companion object {
        const val DATABASE_NAME = "receiver-instrumented.db"
        const val TEST_ACTION = "com.asensiodev.carbura.TEST_REMINDER_NOTIFICATION"
        const val REMINDER_ID = "instrumented-reminder"
    }
}
