package com.asensiodev.carbura.core.data

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.asensiodev.carbura.core.domain.reminder.notification.ReminderAlertKind
import com.asensiodev.carbura.coredata.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale

class AndroidReminderNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID).orEmpty()
        val alertKind = intent.getStringExtra(EXTRA_ALERT_KIND)?.let { value -> ReminderAlertKind.entries.firstOrNull { it.name == value } }
        val dueDate = intent.getStringExtra(EXTRA_DUE_DATE).orEmpty()
        val fallbackTitle =
            intent.getStringExtra(EXTRA_REMINDER_TITLE).orEmpty().ifBlank {
                context.getString(R.string.reminder_notification_default_title)
            }
        val (title, body) = notificationCopy(context, alertKind, dueDate, fallbackTitle)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentIntent =
            launchIntent?.let {
                it.putExtra(EXTRA_START_ROUTE, START_ROUTE_REMINDERS)
                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                PendingIntent.getActivity(
                    context,
                    reminderId.hashCode(),
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            }

        val notification =
            NotificationCompat
                .Builder(context, REMINDER_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        val notificationIdentity = "$reminderId:${alertKind?.name.orEmpty()}"
        NotificationManagerCompat.from(context).notify(stableAlertIntIdentity(notificationIdentity), notification)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_ALERT_KIND = "extra_alert_kind"
        const val EXTRA_DUE_DATE = "extra_due_date"
        private const val EXTRA_START_ROUTE = "com.asensiodev.carbura.START_ROUTE"
        private const val START_ROUTE_REMINDERS = "reminders"
    }
}

internal fun notificationCopy(
    context: Context,
    alertKind: ReminderAlertKind?,
    dueDate: String,
    fallbackTitle: String,
): Pair<String, String> {
    val localizedDueDate =
        localizedExpirationDate(dueDate, context.resources.configuration.locales[0])
            ?: context.getString(R.string.reminder_notification_unknown_date)
    return when (alertKind) {
        ReminderAlertKind.Itv60Days,
        ReminderAlertKind.Itv30Days,
        ReminderAlertKind.Itv7Days,
        ->
            context.getString(R.string.itv_reminder_notification_title) to
                context.getString(R.string.itv_reminder_notification_body, localizedDueDate)
        ReminderAlertKind.Insurance45Days ->
            context.getString(R.string.insurance_reminder_notification_title) to
                context.getString(R.string.insurance_reminder_notification_early_body, localizedDueDate)
        ReminderAlertKind.Insurance37Days ->
            context.getString(R.string.insurance_reminder_notification_title) to
                context.getString(R.string.insurance_reminder_notification_notice_body, localizedDueDate)
        ReminderAlertKind.Insurance7Days ->
            context.getString(R.string.insurance_reminder_notification_title) to
                context.getString(R.string.insurance_reminder_notification_final_body, localizedDueDate)
        ReminderAlertKind.Manual,
        null,
        -> fallbackTitle to context.getString(R.string.reminder_notification_default_body)
    }
}

internal fun localizedExpirationDate(
    iso8601: String,
    locale: Locale,
): String? =
    try {
        LocalDate.parse(iso8601).format(
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale),
        )
    } catch (_: DateTimeParseException) {
        null
    }
