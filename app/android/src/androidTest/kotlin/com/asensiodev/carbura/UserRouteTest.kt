package com.asensiodev.carbura

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.asensiodev.carbura.core.designsystem.CarburaTheme
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import org.junit.Rule
import org.junit.Test

class UserRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun accountDeletionRequiresConfirmationAndCancellationHasNoEffect() {
        var deletionRequests = 0
        composeRule.setContent {
            CarburaTheme {
                UserRoute(
                    displayName = "Angela",
                    email = "angela@example.com",
                    familyName = "Familia de Angela",
                    syncStatus = SyncStatus(),
                    isDeletingAccount = false,
                    onSyncNow = {},
                    onSignOut = {},
                    onDeleteAccount = { deletionRequests += 1 },
                )
            }
        }

        composeRule
            .onNodeWithTag("delete_account_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText("¿Eliminar tu cuenta?").assertIsDisplayed()
        composeRule.runOnIdle { check(deletionRequests == 0) }

        composeRule.onNodeWithText("Cancelar").performClick()
        composeRule.runOnIdle { check(deletionRequests == 0) }

        composeRule.onNodeWithTag("delete_account_button").performClick()
        composeRule.onNodeWithTag("confirm_delete_account_button").performClick()
        composeRule.runOnIdle { check(deletionRequests == 1) }
    }
}
