package com.asensiodev.carbura.core.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.asensiodev.carbura.core.domain.reminder.notification.NotificationOutboxRecovery
import org.koin.core.context.GlobalContext

class NotificationOutboxRecoveryReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action in RECOVERY_ACTIONS) {
            GlobalContext.get().get<NotificationOutboxRecovery>().request()
        }
    }

    private companion object {
        val RECOVERY_ACTIONS =
            setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
            )
    }
}
