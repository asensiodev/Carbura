package com.asensiodev.carbura

import androidx.navigation3.runtime.NavKey
import com.asensiodev.carbura.app.shared.CarburaRoute
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AppShellStateTest {
    @Test
    fun normalLaunchStartsAtGarage() {
        val stack = mutableListOf<NavKey>(CarburaRoute.Garage)

        assertEquals(listOf<NavKey>(CarburaRoute.Garage), stack)
    }

    @Test
    fun notificationLaunchAppendsRemindersAboveGarage() {
        val stack = mutableListOf<NavKey>(CarburaRoute.Garage)

        stack.navigateToTopLevel(CarburaRoute.Reminders)

        assertEquals(listOf<NavKey>(CarburaRoute.Garage, CarburaRoute.Reminders), stack)
    }

    @Test
    fun selectingGarageRemovesTopLevelAndDetailDestinations() {
        val stack =
            mutableListOf<NavKey>(
                CarburaRoute.Garage,
                CarburaRoute.VehicleDetail("vehicle-1"),
                CarburaRoute.User,
            )

        stack.navigateToTopLevel(CarburaRoute.Garage)

        assertEquals(listOf<NavKey>(CarburaRoute.Garage), stack)
    }

    @Test
    fun signOutClearsProtectedDestinations() {
        val stack = mutableListOf<NavKey>(CarburaRoute.Garage, CarburaRoute.Reminders)

        stack.clearProtectedDestinations()

        assertEquals(emptyList<NavKey>(), stack)
    }

    @Test
    fun syncFailureIsShownOnceUntilSuccessAndCanBeShownAgain() {
        val tracker = SyncFeedbackTracker()
        val failure = SyncStatus(lastErrorMessage = "timeout")

        assertIs<SyncFeedbackEvent.ShowFailure>(tracker.update(failure))
        assertEquals(SyncFeedbackEvent.None, tracker.update(failure))
        tracker.retryRequested()
        assertEquals(SyncFeedbackEvent.None, tracker.update(SyncStatus(isSyncing = true)))
        assertIs<SyncFeedbackEvent.ShowFailure>(tracker.update(failure))
        assertEquals(SyncFeedbackEvent.Clear, tracker.update(SyncStatus(lastSyncedAtMillis = 10L)))
        assertIs<SyncFeedbackEvent.ShowFailure>(tracker.update(failure))
    }
}
