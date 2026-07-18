package com.asensiodev.carbura.feature.maintenance.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.asensiodev.carbura.core.designsystem.CarburaTheme
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.MaintenanceRecordId
import com.asensiodev.carbura.core.model.MaintenanceTypeCode
import com.asensiodev.carbura.core.model.MaintenanceTypeId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaintenanceHistoryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectedVehicleAndRecoverableErrorRemainVisible() {
        var retried = false
        setScreen(state = state(loadState = MaintenanceLoadState.Error), onRetry = { retried = true })

        composeRule.onNodeWithText("Coche familiar").assertIsDisplayed()
        composeRule.onNodeWithText("Reintentar").assertIsEnabled().performClick()
        composeRule.runOnIdle { check(retried) }
    }

    @Test
    fun longRecordShowsLocalizedDateCostAndSwipeAction() {
        val record = record()
        setScreen(state = state(records = listOf(record)))

        composeRule.onNodeWithText(record.displayType()).assertIsDisplayed()
        composeRule.onNodeWithText(record.performedOn.localizedDate()).assertIsDisplayed()
        composeRule.onNodeWithText(8_950.localizedCost("EUR")).assertIsDisplayed()
        composeRule.onNodeWithTag("maintenance_card_${record.id.value}").assertIsDisplayed()
    }

    @Test
    fun deleteDispatchesOnlyAfterItemSpecificConfirmation() {
        val record = record()
        var deletedRecord: MaintenanceRecord? = null
        setScreen(
            state = state(records = listOf(record)),
            onDeleteMaintenance = { deletedRecord = it },
        )

        composeRule.onNodeWithTag("maintenance_card_${record.id.value}").performTouchInput { swipeLeft() }

        composeRule.runOnIdle { check(deletedRecord == null) }
        composeRule.onAllNodesWithText(record.displayType()).assertCountEquals(2)
        composeRule.onNodeWithText("Borrar").performClick()
        composeRule.runOnIdle { check(deletedRecord === record) }
    }

    @Test
    fun fieldValidationAndSaveRemainVisibleAtLargeText() {
        setScreen(
            state =
                state(validationError = CarburaString.ValidationBlankMaintenanceType).copy(
                    maintenanceTypeCode = MaintenanceTypeCode.Custom,
                ),
            fontScale = 2f,
        )

        composeRule.onNodeWithTag("add_maintenance_fab").performClick()
        composeRule.onNodeWithTag("full_screen_maintenance_form").assertIsDisplayed()
        composeRule
            .onNodeWithText("Introduce un nombre para el mantenimiento personalizado.")
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag("save_maintenance_button").assertIsDisplayed()
    }

    @Test
    fun canonicalTypeShowsOnlyItsConditionalFields() {
        setScreen(state = state().copy(maintenanceTypeCode = MaintenanceTypeCode.Itv))

        composeRule.onNodeWithTag("add_maintenance_fab").performClick()
        composeRule.onNodeWithText("Próxima fecha de ITV (opcional)").assertIsDisplayed()
        composeRule.onAllNodesWithText("Especifica el mantenimiento").assertCountEquals(0)
    }

    @Test
    fun customTypeShowsLabelAndHidesNextDate() {
        setScreen(state = state().copy(maintenanceTypeCode = MaintenanceTypeCode.Custom))

        composeRule.onNodeWithTag("add_maintenance_fab").performClick()
        composeRule.onNodeWithText("Especifica el mantenimiento").assertIsDisplayed()
        composeRule.onAllNodesWithText("Próxima fecha de ITV (opcional)").assertCountEquals(0)
    }

    @Test
    fun typeDropdownRespectsFormPaddingAndDispatchesCustomSelection() {
        var selectedType: MaintenanceTypeCode? = null
        setScreen(
            state = state(),
            onTypeSelected = { selectedType = it },
        )

        composeRule.onNodeWithTag("add_maintenance_fab").performClick()
        val dropdown = composeRule.onNodeWithTag("maintenance_type_dropdown").assertIsDisplayed()
        val dropdownBounds = dropdown.getBoundsInRoot()
        check(dropdownBounds.left >= 24.dp)

        composeRule.onAllNodesWithText("Otro").assertCountEquals(0)
        dropdown.performClick()
        composeRule.onNodeWithText("Otro").assertIsDisplayed().performClick()
        composeRule.runOnIdle { check(selectedType == MaintenanceTypeCode.Custom) }
    }

    @Test
    fun keyboardKeepsFocusedFieldAndSaveActionVisible() {
        setScreen(state = state())

        composeRule.onNodeWithTag("add_maintenance_fab").performClick()
        val workshop = composeRule.onNodeWithTag("maintenance_workshop_input")
        workshop.performScrollTo().performClick().performTextInput("Taller")
        workshop.assertIsFocused().assertIsDisplayed()
        composeRule.onNodeWithTag("save_maintenance_button").assertIsDisplayed()
    }

    private fun setScreen(
        state: MaintenanceHistoryUiState,
        onRetry: () -> Unit = {},
        onDeleteMaintenance: (MaintenanceRecord) -> Unit = {},
        onTypeSelected: (MaintenanceTypeCode) -> Unit = {},
        fontScale: Float = 1f,
    ) {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                CarburaTheme {
                    MaintenanceHistoryScreen(
                        state = state,
                        effectMessage = null,
                        maintenanceCreatedSignal = 0,
                        maintenanceSuccessSignal = 0,
                        onBack = {},
                        onTypeSelected = onTypeSelected,
                        onCustomTypeLabelChange = {},
                        onPerformedOnChange = {},
                        onNextDueDateChange = {},
                        onOdometerChange = {},
                        onCostChange = {},
                        onWorkshopChange = {},
                        onNotesChange = {},
                        onSubmitMaintenance = {},
                        onDeleteMaintenance = onDeleteMaintenance,
                        onRetry = onRetry,
                    )
                }
            }
        }
    }

    private fun state(
        records: List<MaintenanceRecord> = emptyList(),
        loadState: MaintenanceLoadState = MaintenanceLoadState.Content,
        validationError: CarburaString? = null,
    ) = MaintenanceHistoryUiState(
        vehicle = vehicle,
        records = records,
        performedOn = "2026-07-17",
        loadState = loadState,
        validationError = validationError,
    )

    private fun record() =
        MaintenanceRecord(
            id = MaintenanceRecordId("record-1"),
            familyId = familyId,
            vehicleId = vehicle.id,
            maintenanceTypeId = MaintenanceTypeId("type-revisión-general-extraordinariamente-larga"),
            maintenanceTypeCode = MaintenanceTypeCode.Custom,
            performedOn = CalendarDate("2026-07-17"),
            odometerKm = 12_300,
            costCents = 8_950,
        )

    private companion object {
        val familyId = FamilyId("family-test")
        val vehicle =
            Vehicle(
                id = VehicleId("vehicle-test"),
                familyId = familyId,
                name = "Coche familiar",
                type = VehicleType.Car,
                brand = "Seat",
                model = "León",
                currentOdometerKm = 12_300,
            )
    }
}
