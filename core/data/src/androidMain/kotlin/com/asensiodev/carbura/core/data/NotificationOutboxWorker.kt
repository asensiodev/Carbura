package com.asensiodev.carbura.core.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxDrainResult
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxProcessor
import org.koin.core.context.GlobalContext

internal class NotificationOutboxWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val koin = GlobalContext.get()
        koin.get<NotificationOutboxReconciler>().reconcileExistingReminders()
        return when (workerResult(koin.get<NotificationOutboxProcessor>().drain())) {
            ListenableWorkerResult.Success -> Result.success()
            ListenableWorkerResult.Retry -> Result.retry()
        }
    }
}

internal fun workerResult(result: NotificationOutboxDrainResult): ListenableWorkerResult =
    when (result) {
        NotificationOutboxDrainResult.Drained -> ListenableWorkerResult.Success
        NotificationOutboxDrainResult.RetryableWorkRemaining -> ListenableWorkerResult.Retry
        NotificationOutboxDrainResult.PermissionDenied,
        NotificationOutboxDrainResult.NonRetryableWorkRemaining,
        -> ListenableWorkerResult.Success
    }

internal enum class ListenableWorkerResult {
    Success,
    Retry,
}
