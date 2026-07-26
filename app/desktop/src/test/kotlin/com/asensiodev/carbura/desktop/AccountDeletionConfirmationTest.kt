package com.asensiodev.carbura.desktop

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccountDeletionConfirmationTest {
    @Test
    fun cancellationDoesNotDispatchAndConfirmationDispatchesOnce() {
        val confirmation = AccountDeletionConfirmation()
        var deletionCalls = 0

        confirmation.request()
        assertTrue(confirmation.isVisible)
        confirmation.cancel()
        assertFalse(confirmation.isVisible)
        assertEquals(0, deletionCalls)

        confirmation.request()
        confirmation.confirm { deletionCalls += 1 }
        confirmation.confirm { deletionCalls += 1 }

        assertFalse(confirmation.isVisible)
        assertEquals(1, deletionCalls)
    }

    @Test
    fun accountCardsStackOnlyWhenTheirActualContentWidthIsConstrained() {
        assertTrue(useStackedAccountCards(759.dp))
        assertFalse(useStackedAccountCards(760.dp))
    }

    @Test
    fun storagePathsAreHiddenUntilDetailsAreRequested() {
        val details = AccountStorageDetails()

        assertFalse(details.isVisible)
        details.toggle()
        assertTrue(details.isVisible)
        details.toggle()
        assertFalse(details.isVisible)
    }
}
