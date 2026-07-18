package com.asensiodev.carbura.core.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlert
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.maintenanceReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.manualReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.reminderAlertIdentity
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.VehicleId
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidReminderNotificationSchedulerTest {
    @Test
    fun pastGeneratedAlertInstantsAreSkippedAndFutureAlertsRemain() {
        val plan =
            ReminderNotificationPlan(
                reminder = reminder("reminder-1", "2027-07-01"),
                alerts =
                    listOf(
                        ReminderAlert(ReminderAlertKind.Itv60Days, 60),
                        ReminderAlert(ReminderAlertKind.Itv30Days, 30),
                        ReminderAlert(ReminderAlertKind.Itv7Days, 7),
                    ),
            )

        val alerts =
            futureAlertInstances(
                plan = plan,
                nowMillis = Instant.parse("2027-05-15T10:00:00Z").toEpochMilli(),
                zoneId = ZoneId.of("UTC"),
            )

        assertEquals(
            listOf(ReminderAlertKind.Itv30Days, ReminderAlertKind.Itv7Days),
            alerts.map { it.alert.kind },
        )
    }

    @Test
    fun pastManualAlertUsesImmediateLegacyFallback() {
        val now = Instant.parse("2027-07-01T10:00:00Z").toEpochMilli()

        val alerts =
            futureAlertInstances(
                manualReminderNotificationPlan(reminder("manual", "2027-07-01")),
                nowMillis = now,
                zoneId = ZoneId.of("UTC"),
            )

        assertEquals(1, alerts.size)
        assertEquals(ReminderAlertKind.Manual, alerts.single().alert.kind)
        assertEquals(now + PAST_DUE_NOTIFICATION_DELAY_MILLIS, alerts.single().triggerAtMillis)
    }

    @Test
    fun cancelRemovesExactLegacyAlarmIdentity() =
        runBlocking {
            val context: Context = RuntimeEnvironment.getApplication()
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val reminderId = ReminderId("legacy-reminder")
            val legacyIntent = Intent(context, AndroidReminderNotificationReceiver::class.java)
            val legacyPendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    reminderId.value.hashCode(),
                    legacyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            alarmManager.set(AlarmManager.RTC_WAKEUP, 123_456L, legacyPendingIntent)
            assertEquals(1, shadowOf(alarmManager).scheduledAlarms.size)

            AndroidReminderNotificationScheduler(context).cancel(reminderId)

            assertTrue(shadowOf(alarmManager).scheduledAlarms.isEmpty())
            assertEquals(null, legacyIntent.data)
            assertEquals(null, legacyIntent.action)
        }

    @Test
    fun cancelRemovesEveryGeneratedAlertAlarm() =
        runBlocking {
            val context: Context = RuntimeEnvironment.getApplication()
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val reminder = reminder("maintenance-reminder:record-1", "2099-07-01")
            val plan = maintenanceReminderNotificationPlan(reminder, MaintenanceTypeCode.Itv)!!
            val scheduler = AndroidReminderNotificationScheduler(context)
            scheduler.schedule(plan)
            assertEquals(3, shadowOf(alarmManager).scheduledAlarms.size)

            scheduler.cancel(reminder.id)

            assertTrue(shadowOf(alarmManager).scheduledAlarms.isEmpty())
        }

    @Test
    fun dataUriKeepsPendingIntentsDistinctWhenRequestCodeHashesCollide() =
        runBlocking {
            val context: Context = RuntimeEnvironment.getApplication()
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val first = reminder("Aa", "2099-07-01")
            val second = reminder("BB", "2099-07-01")
            val firstIdentity = reminderAlertIdentity(first.id, ReminderAlertKind.Manual)
            val secondIdentity = reminderAlertIdentity(second.id, ReminderAlertKind.Manual)
            assertNotEquals(firstIdentity, secondIdentity)
            assertEquals(firstIdentity.hashCode(), secondIdentity.hashCode())
            assertNotEquals(stableAlertIntIdentity(firstIdentity), stableAlertIntIdentity(secondIdentity))
            assertEquals(stableAlertIntIdentity(firstIdentity), stableAlertIntIdentity(firstIdentity))
            val scheduler = AndroidReminderNotificationScheduler(context)

            scheduler.schedule(manualReminderNotificationPlan(first))
            scheduler.schedule(manualReminderNotificationPlan(second))

            assertEquals(2, shadowOf(alarmManager).scheduledAlarms.size)
        }

    private fun reminder(
        id: String,
        dueDate: String?,
    ): Reminder =
        Reminder(
            id = ReminderId(id),
            familyId = FamilyId("family-1"),
            vehicleId = VehicleId("vehicle-1"),
            maintenanceTypeId = null,
            title = "Reminder",
            dueDate = dueDate?.let(::CalendarDate),
        )
}
