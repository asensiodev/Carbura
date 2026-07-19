package com.asensiodev.carbura.core.data

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxProcessor
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class AndroidNotificationOutboxRecovery(
    private val context: Context,
    private val processor: NotificationOutboxProcessor,
    private val reconciler: NotificationOutboxReconciler,
    private val applicationScope: CoroutineScope,
) : NotificationOutboxRecovery {
    override fun request() {
        applicationScope.launch {
            reconciler.reconcileExistingReminders()
            processor.drain()
        }
        WorkManager.getInstance(context).enqueueUniqueWork(
            NOTIFICATION_OUTBOX_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<NotificationOutboxWorker>().build(),
        )
    }
}

internal const val NOTIFICATION_OUTBOX_WORK_NAME = "notification-outbox-recovery"
