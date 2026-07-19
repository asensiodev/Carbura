package com.asensiodev.carbura

import androidx.navigation3.runtime.NavKey
import com.asensiodev.carbura.app.shared.CarburaRoute
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun successfulSignOutResetsToNonEmptyRootDestination() {
        val stack = mutableListOf<NavKey>(CarburaRoute.Garage, CarburaRoute.Reminders)

        stack.resetAfterSignOut()

        assertEquals(listOf<NavKey>(CarburaRoute.Garage), stack)
    }

    @Test
    fun syncFailureIsShownOnceUntilSuccessAndCanBeShownAgain() {
        val tracker = SyncFeedbackTracker()
        val failure = SyncStatus(lastErrorMessage = "timeout", failureId = 1L)

        assertEquals(SyncFeedbackEvent.ShowFailure(1L, "timeout"), tracker.update(failure))
        assertEquals(SyncFeedbackEvent.None, tracker.update(SyncStatus(isSyncing = true)))
        assertEquals(SyncFeedbackEvent.ShowFailure(1L, "timeout"), tracker.update(failure))
        assertEquals(
            SyncFeedbackEvent.ShowFailure(2L, "timeout"),
            tracker.update(failure.copy(failureId = 2L)),
        )
    }

    @Test
    fun acknowledgedFailureIsNotReplayedToRecreatedTracker() {
        val acknowledgedFailure = SyncStatus(lastErrorMessage = "timeout", failureId = 1L, acknowledgedFailureId = 1L)

        assertEquals(SyncFeedbackEvent.None, SyncFeedbackTracker().update(acknowledgedFailure))
    }
}
