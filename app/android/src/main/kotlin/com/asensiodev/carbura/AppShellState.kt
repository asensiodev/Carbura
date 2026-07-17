package com.asensiodev.carbura

import androidx.navigation3.runtime.NavKey
import com.asensiodev.carbura.app.shared.CarburaRoute
import com.asensiodev.carbura.core.domain.sync.SyncStatus

internal fun MutableList<NavKey>.navigateToTopLevel(destination: CarburaRoute) {
    if (firstOrNull() != CarburaRoute.Garage) {
        clear()
        add(CarburaRoute.Garage)
    } else {
        while (size > 1) removeLastOrNull()
    }
    if (destination != CarburaRoute.Garage) add(destination)
}

internal fun MutableList<NavKey>.clearProtectedDestinations() = clear()

internal sealed interface SyncFeedbackEvent {
    data object None : SyncFeedbackEvent

    data object Clear : SyncFeedbackEvent

    data class ShowFailure(
        val message: String,
    ) : SyncFeedbackEvent
}

internal class SyncFeedbackTracker {
    private var shownFailure: String? = null
    private var handledSuccess: Long? = null

    fun retryRequested() {
        shownFailure = null
    }

    fun update(status: SyncStatus): SyncFeedbackEvent {
        val success = status.lastSyncedAtMillis
        if (!status.isSyncing && status.lastErrorMessage == null) {
            if (success != null && success != handledSuccess) {
                handledSuccess = success
                shownFailure = null
                return SyncFeedbackEvent.Clear
            }
        }

        val failure = status.lastErrorMessage
        if (status.isSyncing || failure == null || failure == shownFailure) return SyncFeedbackEvent.None

        shownFailure = failure
        return SyncFeedbackEvent.ShowFailure(failure)
    }
}
