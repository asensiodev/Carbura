package com.asensiodev.carbura.core.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlert
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationPlan
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderNotificationScheduler
import com.asensiodev.carbura.core.domain.reminder.notification.reminderAlertIdentity
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.coredata.R
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

internal class AndroidReminderNotificationScheduler(
    private val context: Context,
) : ReminderNotificationScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override suspend fun schedule(plan: ReminderNotificationPlan) {
        cancel(plan.reminder.id)
        val futureAlerts = futureAlertInstances(plan, currentTimeMillis())
        if (futureAlerts.isEmpty()) return

        createNotificationChannel()
        futureAlerts.forEach { instance ->
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                instance.triggerAtMillis,
                reminderPendingIntent(
                    reminderId = plan.reminder.id,
                    title = plan.reminder.title,
                    dueDate = plan.reminder.dueDate?.iso8601,
                    alertKind = instance.alert.kind,
                ),
            )
        }
    }

    override suspend fun cancel(reminderId: ReminderId) {
        alarmManager.cancel(legacyReminderPendingIntent(reminderId))
        ReminderAlertKind.entries.forEach { alertKind ->
            alarmManager.cancel(
                reminderPendingIntent(
                    reminderId = reminderId,
                    title = null,
                    dueDate = null,
                    alertKind = alertKind,
                ),
            )
        }
    }

    private fun legacyReminderPendingIntent(reminderId: ReminderId): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            reminderId.value.hashCode(),
            Intent(context, AndroidReminderNotificationReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun reminderPendingIntent(
        reminderId: ReminderId,
        title: String?,
        dueDate: String?,
        alertKind: ReminderAlertKind,
    ): PendingIntent {
        val identity = reminderAlertIdentity(reminderId, alertKind)
        val intent =
            Intent(context, AndroidReminderNotificationReceiver::class.java).apply {
                data = Uri.parse("carbura://reminder-alert/${Uri.encode(identity)}")
                putExtra(AndroidReminderNotificationReceiver.EXTRA_REMINDER_ID, reminderId.value)
                putExtra(AndroidReminderNotificationReceiver.EXTRA_ALERT_KIND, alertKind.name)
                if (title != null) putExtra(AndroidReminderNotificationReceiver.EXTRA_REMINDER_TITLE, title)
                if (dueDate != null) putExtra(AndroidReminderNotificationReceiver.EXTRA_DUE_DATE, dueDate)
            }
        return PendingIntent.getBroadcast(
            context,
            stableAlertIntIdentity(identity),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createNotificationChannel() {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel =
            NotificationChannel(
                REMINDER_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.reminder_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        notificationManager.createNotificationChannel(channel)
    }
}

internal data class FutureAlertInstance(
    val alert: ReminderAlert,
    val triggerAtMillis: Long,
)

internal fun futureAlertInstances(
    plan: ReminderNotificationPlan,
    nowMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<FutureAlertInstance> {
    val dueDate = plan.reminder.dueDate ?: return emptyList()
    return plan.alerts.mapNotNull { alert ->
        val triggerAtMillis =
            LocalDate
                .parse(dueDate.iso8601)
                .minusDays(alert.daysBefore.toLong())
                .atTime(LocalTime.of(NOTIFICATION_HOUR, 0))
                .atZone(zoneId)
                .toInstant()
                .toEpochMilli()
        when {
            triggerAtMillis > nowMillis -> FutureAlertInstance(alert, triggerAtMillis)
            alert.kind == ReminderAlertKind.Manual -> FutureAlertInstance(alert, nowMillis + PAST_DUE_NOTIFICATION_DELAY_MILLIS)
            else -> null
        }
    }
}

internal const val REMINDER_NOTIFICATION_CHANNEL_ID = "carbura_reminders"
private const val NOTIFICATION_HOUR = 9
internal const val PAST_DUE_NOTIFICATION_DELAY_MILLIS = 5_000L

internal fun stableAlertIntIdentity(identity: String): Int {
    val digest = MessageDigest.getInstance("SHA-256").digest(identity.encodeToByteArray())
    return ((digest[0].toInt() and 0xff) shl 24) or
        ((digest[1].toInt() and 0xff) shl 16) or
        ((digest[2].toInt() and 0xff) shl 8) or
        (digest[3].toInt() and 0xff)
}
