package com.asensiodev.carbura.core.domain.reminder.notification

interface NotificationOutboxRecovery {
    fun request()
}

object NoOpNotificationOutboxRecovery : NotificationOutboxRecovery {
    override fun request() = Unit
}

class NotificationRecoveryTrigger(
    private val recovery: NotificationOutboxRecovery,
    private val foregroundThrottleMillis: Long,
) {
    private var lastRequestAtMillis: Long? = null

    fun onAuthenticatedStartup(nowMillis: Long) {
        lastRequestAtMillis = nowMillis
        recovery.request()
    }

    fun onForeground(nowMillis: Long) {
        val lastRequest = lastRequestAtMillis
        if (lastRequest == null || nowMillis - lastRequest >= foregroundThrottleMillis) {
            lastRequestAtMillis = nowMillis
            recovery.request()
        }
    }
}
