package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationRecoveryTrigger
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationRecoveryTriggerTest {
    @Test
    fun authenticatedStartupRequestsRecoveryImmediately() {
        val recovery = RecordingRecovery()
        val trigger = NotificationRecoveryTrigger(recovery, foregroundThrottleMillis = 1_000)

        trigger.onAuthenticatedStartup(nowMillis = 100)

        assertEquals(1, recovery.requests)
    }

    @Test
    fun foregroundRecoveryIsThrottled() {
        val recovery = RecordingRecovery()
        val trigger = NotificationRecoveryTrigger(recovery, foregroundThrottleMillis = 1_000)
        trigger.onAuthenticatedStartup(nowMillis = 100)

        trigger.onForeground(nowMillis = 1_099)
        trigger.onForeground(nowMillis = 1_100)
        trigger.onForeground(nowMillis = 1_101)

        assertEquals(2, recovery.requests)
    }

    private class RecordingRecovery : NotificationOutboxRecovery {
        var requests = 0

        override fun request() {
            requests += 1
        }
    }
}
