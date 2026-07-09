package com.asensiodev.carbura.core.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.asensiodev.carbura.core.domain.ReminderNotificationScheduler
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

internal class AndroidReminderNotificationScheduler(
    private val context: Context,
) : ReminderNotificationScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override suspend fun schedule(reminder: Reminder) {
        val dueDate = reminder.dueDate ?: run {
            cancel(reminder.id)
            return
        }
        createNotificationChannel()
        val triggerAtMillis = notificationTriggerMillis(dueDate.iso8601, reminder.notifyDaysBefore)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            reminderPendingIntent(reminder.id, reminder.title),
        )
    }

    override suspend fun cancel(reminderId: ReminderId) {
        alarmManager.cancel(reminderPendingIntent(reminderId, title = null))
    }

    private fun notificationTriggerMillis(dueDate: String, notifyDaysBefore: Int): Long {
        val targetDate = LocalDate.parse(dueDate).minusDays(notifyDaysBefore.toLong())
        val targetMillis = targetDate
            .atTime(LocalTime.of(NOTIFICATION_HOUR, 0))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val now = currentTimeMillis()
        return if (targetMillis > now) targetMillis else now + PAST_DUE_NOTIFICATION_DELAY_MILLIS
    }

    private fun reminderPendingIntent(reminderId: ReminderId, title: String?): PendingIntent {
        val intent = Intent(context, AndroidReminderNotificationReceiver::class.java).apply {
            putExtra(AndroidReminderNotificationReceiver.EXTRA_REMINDER_ID, reminderId.value)
            if (title != null) {
                putExtra(AndroidReminderNotificationReceiver.EXTRA_REMINDER_TITLE, title)
            }
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.value.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            REMINDER_NOTIFICATION_CHANNEL_ID,
            REMINDER_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(channel)
    }
}

internal const val REMINDER_NOTIFICATION_CHANNEL_ID = "carbura_reminders"
private const val REMINDER_NOTIFICATION_CHANNEL_NAME = "Recordatorios"
private const val NOTIFICATION_HOUR = 9
private const val PAST_DUE_NOTIFICATION_DELAY_MILLIS = 5_000L
