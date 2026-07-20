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

internal fun MutableList<NavKey>.resetAfterSignOut() = navigateToTopLevel(CarburaRoute.Garage)

internal sealed interface SyncFeedbackEvent {
    data object None : SyncFeedbackEvent

    data class ShowFailure(
        val id: Long,
        val message: String,
    ) : SyncFeedbackEvent
}

internal class SyncFeedbackTracker {
    private var deliveredFailureId: Long? = null

    fun update(status: SyncStatus): SyncFeedbackEvent {
        val failure = status.lastErrorMessage
        val failureId = status.failureId
        if (status.isSyncing) return SyncFeedbackEvent.None
        if (failure == null || failureId == null) {
            deliveredFailureId = null
            return SyncFeedbackEvent.None
        }
        if (failureId == status.acknowledgedFailureId || failureId == deliveredFailureId) {
            return SyncFeedbackEvent.None
        }

        deliveredFailureId = failureId
        return SyncFeedbackEvent.ShowFailure(failureId, failure)
    }
}
