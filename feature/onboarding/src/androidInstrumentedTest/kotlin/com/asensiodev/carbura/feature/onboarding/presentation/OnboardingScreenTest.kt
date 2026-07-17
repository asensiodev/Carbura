package com.asensiodev.carbura.feature.onboarding.presentation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun initializationExposesHeadingAndLoadingStatus() {
        composeRule.setContent {
            MaterialTheme {
                OnboardingScreen(state = OnboardingUiState(), onGoogleSignIn = {})
            }
        }

        composeRule.onNodeWithText("Carbura").assert(isHeading())
        composeRule
            .onNode(
                hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate) and
                    hasStateDescription("Comprobando tu sesión..."),
            ).assertExists()
        composeRule.onNodeWithText("Comprobando tu sesión...").assertIsDisplayed()
    }

    @Test
    fun regularPhoneCentersAccessPanel() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1f)) {
                MaterialTheme {
                    OnboardingScreen(
                        state = OnboardingUiState(isInitializing = false),
                        onGoogleSignIn = {},
                        modifier = Modifier.size(width = 360.dp, height = 720.dp),
                    )
                }
            }
        }

        val screenCenter =
            composeRule
                .onNodeWithTag("onboarding_screen")
                .fetchSemanticsNode()
                .boundsInRoot.center.y
        val contentCenter =
            composeRule
                .onNodeWithTag("onboarding_content")
                .fetchSemanticsNode()
                .boundsInRoot.center.y

        assertEquals(screenCenter, contentCenter, 1f)
    }

    @Test
    fun compactLandscapeKeepsInitializationStatusReachableByScrolling() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 1f)) {
                MaterialTheme {
                    OnboardingScreen(
                        state = OnboardingUiState(),
                        onGoogleSignIn = {},
                        modifier = Modifier.size(width = 640.dp, height = 180.dp),
                    )
                }
            }
        }

        composeRule.onNodeWithText("Continuar con Google").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Comprobando tu sesión...").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun longErrorKeepsRetryReachableAtLargeTextAndCompactHeight() {
        var retryClicked = false
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                MaterialTheme {
                    OnboardingScreen(
                        state =
                            OnboardingUiState(
                                isInitializing = false,
                                error = OnboardingError.SignInFailed,
                            ),
                        onGoogleSignIn = { retryClicked = true },
                        modifier = Modifier.size(width = 360.dp, height = 240.dp),
                    )
                }
            }
        }

        composeRule
            .onNode(hasText("No pudimos completar el acceso con Google", substring = true))
            .assertExists()
        composeRule
            .onNode(hasText("Reintentar con Google") and hasClickAction())
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        assertTrue(retryClicked)
    }

    @Test
    fun primaryCopyDescribesBenefitsWithoutBackendVendorName() {
        composeRule.setContent {
            MaterialTheme {
                OnboardingScreen(
                    state = OnboardingUiState(isInitializing = false),
                    onGoogleSignIn = {},
                )
            }
        }

        composeRule
            .onNodeWithText(
                "Gestiona los vehículos de tu familia, su mantenimiento y sus recordatorios en un solo lugar.",
            ).assertIsDisplayed()
        composeRule.onAllNodesWithText("Supabase", substring = true).assertCountEquals(0)
    }

    @Test
    fun expandedContentUsesReadableMaximumWidth() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 0.5f, fontScale = 1f)) {
                MaterialTheme {
                    OnboardingScreen(
                        state = OnboardingUiState(isInitializing = false),
                        onGoogleSignIn = {},
                        modifier = Modifier.size(width = 1000.dp, height = 800.dp),
                    )
                }
            }
        }

        composeRule.onNodeWithTag("onboarding_content").assertWidthIsEqualTo(560.dp)
        composeRule.onNodeWithTag("onboarding_content").assertHeightIsAtLeast(1.dp)
    }
}
