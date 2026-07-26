package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxDrainResult
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationOutboxWorkerTest {
    @Test
    fun retryableWorkRequestsWorkManagerRetry() {
        assertEquals(
            ListenableWorkerResult.Retry,
            workerResult(NotificationOutboxDrainResult.RetryableWorkRemaining),
        )
    }

    @Test
    fun outcomesThatCannotBenefitFromBackoffFinishSuccessfully() {
        val results =
            listOf(
                NotificationOutboxDrainResult.Drained,
                NotificationOutboxDrainResult.PermissionDenied,
                NotificationOutboxDrainResult.NonRetryableWorkRemaining,
            )

        results.forEach { result ->
            assertEquals(ListenableWorkerResult.Success, workerResult(result))
        }
    }
}
