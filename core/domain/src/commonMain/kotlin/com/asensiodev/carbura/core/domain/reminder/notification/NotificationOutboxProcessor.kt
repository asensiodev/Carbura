package com.asensiodev.carbura.core.domain.reminder.notification

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NotificationPermissionDeniedException(
    message: String,
) : Exception(message)

class NonRetryableNotificationException(
    message: String,
) : Exception(message)

class NotificationOutboxProcessor(
    private val outbox: NotificationOutbox,
    private val scheduler: ReminderNotificationScheduler,
) {
    private val mutex = Mutex()

    suspend fun drain(): NotificationOutboxDrainResult =
        mutex.withLock {
            var hasRetryableFailure = false
            var hasPermissionFailure = false
            var hasNonRetryableFailure = false

            outbox.pending().forEach { desired ->
                try {
                    when (desired.action) {
                        DesiredNotificationAction.Schedule -> scheduler.schedule(requireNotNull(desired.payload).toPlan(desired.revision))
                        DesiredNotificationAction.Cancel -> scheduler.cancel(desired.reminderId)
                    }
                    outbox.acknowledge(desired.reminderId, desired.revision)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: NotificationPermissionDeniedException) {
                    hasPermissionFailure = true
                } catch (_: NonRetryableNotificationException) {
                    hasNonRetryableFailure = true
                } catch (_: Exception) {
                    hasRetryableFailure = true
                }
            }

            when {
                hasPermissionFailure -> NotificationOutboxDrainResult.PermissionDenied
                hasRetryableFailure -> NotificationOutboxDrainResult.RetryableWorkRemaining
                hasNonRetryableFailure -> NotificationOutboxDrainResult.NonRetryableWorkRemaining
                else -> NotificationOutboxDrainResult.Drained
            }
        }
}
