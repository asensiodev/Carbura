package com.asensiodev.carbura.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ScreenPrimitivesTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun constrainedScreenUsesCompactPadding() {
        setConstrainedContent(width = 400, height = 600)

        composeRule
            .onNodeWithTag(CONTENT_TAG)
            .assertLeftPositionInRootIsEqualTo(24.dp)
            .assertWidthIsEqualTo(352.dp)
    }

    @Test
    fun constrainedScreenKeepsCompactPaddingInLandscape() {
        setConstrainedContent(width = 580, height = 320)

        composeRule
            .onNodeWithTag(CONTENT_TAG)
            .assertLeftPositionInRootIsEqualTo(24.dp)
            .assertWidthIsEqualTo(532.dp)
    }

    @Test
    fun constrainedScreenUsesExpandedPaddingAndMaximumWidth() {
        setConstrainedContent(width = 1_000, height = 600)

        composeRule
            .onNodeWithTag(CONTENT_TAG)
            .assertLeftPositionInRootIsEqualTo(80.dp)
            .assertWidthIsEqualTo(840.dp)
    }

    @Test
    fun loadingStatePresentsProgressAndFeatureCopy() {
        composeRule.setContent {
            CarburaTheme {
                LoadingState(message = "Cargando vehiculos")
            }
        }

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
        composeRule.onNodeWithText("Cargando vehiculos").assertIsDisplayed()
    }

    @Test
    fun emptyStatePresentsFeatureCopy() {
        composeRule.setContent {
            CarburaTheme {
                EmptyState(
                    title = "Sin vehiculos",
                    description = "Anade tu primer vehiculo",
                )
            }
        }

        composeRule
            .onNodeWithText("Sin vehiculos")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .assertIsDisplayed()
        composeRule.onNodeWithText("Anade tu primer vehiculo").assertIsDisplayed()
    }

    @Test
    fun retryStateExposesErrorAndInvokesRetryAtLargeFontScale() {
        var retries = 0
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                CarburaTheme {
                    RetryState(
                        title = "No se pudieron cargar los datos",
                        description = "Tus datos locales siguen disponibles",
                        retryLabel = "Reintentar",
                        onRetry = { retries += 1 },
                    )
                }
            }
        }

        composeRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
            .assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(1, retries) }
    }

    @Test
    fun swipeToDeleteExposesAccessibleActionAndRequestsDeletion() {
        var deleteRequests = 0
        composeRule.setContent {
            CarburaTheme {
                SwipeToDeleteContainer(
                    actionLabel = "Borrar",
                    accessibilityLabel = "Borrar vehiculo",
                    enabled = true,
                    onDeleteRequest = { deleteRequests += 1 },
                    modifier = Modifier.size(300.dp, 100.dp).testTag("swipe-row"),
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }

        composeRule.onNodeWithTag("swipe-row").assert(SemanticsMatcher.keyIsDefined(SemanticsActions.CustomActions))
        composeRule.onNodeWithTag("swipe-row").performTouchInput { swipeLeft() }
        composeRule.runOnIdle { assertEquals(1, deleteRequests) }
    }

    private fun setConstrainedContent(
        width: Int,
        height: Int,
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f)) {
                Box(modifier = Modifier.size(width.dp, height.dp)) {
                    ConstrainedScreen {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .testTag(CONTENT_TAG),
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val CONTENT_TAG = "constrained-content"
    }
}
