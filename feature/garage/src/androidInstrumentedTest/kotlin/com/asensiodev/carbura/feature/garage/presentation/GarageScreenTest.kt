package com.asensiodev.carbura.feature.garage.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.asensiodev.carbura.core.designsystem.CarburaTheme
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString
import org.junit.Rule
import org.junit.Test

class GarageScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun vehicleTypeSelectorLocalizesEveryTypeAndExposesSelection() {
        composeRule.setContent {
            CarburaTheme {
                VehicleTypeSelector(selectedType = VehicleType.Van, onTypeSelected = {})
            }
        }

        composeRule.onNodeWithText("Coche").assertIsDisplayed()
        composeRule.onNodeWithText("Moto").assertIsDisplayed()
        composeRule.onNodeWithText("Furgoneta").assertIsDisplayed().assertIsSelected()
        composeRule.onNodeWithText("Otro").assertIsDisplayed()
    }

    @Test
    fun longVehicleCardHasItemSpecificActionsAtLargeText() {
        val name = "Furgoneta familiar con un nombre especialmente largo"
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                CarburaTheme {
                    VehicleCard(
                        vehicle = vehicle(name),
                        actionsEnabled = true,
                        deleting = false,
                        onSelectVehicle = {},
                        onDeleteVehicle = {},
                        onEditVehicle = {},
                        onQuickOdometerUpdate = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText(name).assertIsDisplayed()
        composeRule.onNodeWithText("Ver vehículo e historial").assertIsDisplayed()
        composeRule.onNodeWithText("Actualizar km").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Editar $name").assertIsDisplayed().assertHasClickAction()
        composeRule.onNodeWithContentDescription("Borrar $name").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun deleteDialogNamesVehicleAndRequiresConfirmation() {
        val name = "Coche familiar"
        var confirmed = false
        composeRule.setContent {
            CarburaTheme {
                VehicleDeleteDialog(
                    vehicle = vehicle(name),
                    onConfirm = { confirmed = true },
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText(name).assertIsDisplayed()
        composeRule.onNodeWithText("Borrar vehículo").assertIsDisplayed()
        composeRule.onNodeWithText("Borrar").performClick()
        composeRule.runOnIdle { assert(confirmed) }
    }

    @Test
    fun compactCreateFormScrollsToFieldErrorAndSaveAction() {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                CarburaTheme {
                    Box(modifier = Modifier.size(360.dp, 320.dp)) {
                        VehicleForm(
                            title = "Añade tu primer vehículo",
                            name = "",
                            odometer = "0",
                            selectedType = VehicleType.Car,
                            nextItvDate = "",
                            insuranceRenewalDate = "",
                            nextServiceOdometer = "",
                            errorMessage = CarburaString.ValidationBlankVehicleName,
                            persistenceError = false,
                            isSaving = false,
                            onNameChange = {},
                            onOdometerChange = {},
                            onTypeSelected = {},
                            onNextItvDateChange = {},
                            onInsuranceRenewalDateChange = {},
                            onNextServiceOdometerChange = {},
                            onCreateVehicle = {},
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        composeRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
            .assertIsDisplayed()
        composeRule.onNodeWithText("Guardar vehículo").performScrollTo().assertIsDisplayed()
    }

    private fun vehicle(name: String) =
        Vehicle(
            id = VehicleId("vehicle-long"),
            familyId = FamilyId("family-test"),
            name = name,
            type = VehicleType.Van,
            currentOdometerKm = 120_000,
        )
}
