package com.asensiodev.carbura.feature.garage.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.asensiodev.carbura.core.designsystem.CarburaTheme
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageLoadState
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewUiState
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleEditMode
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormUiState
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
        var deleteRequested = false
        val vehicle = vehicle(name).copy(licensePlate = "1234 ABC")
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density = 1f, fontScale = 2f)) {
                CarburaTheme {
                    VehicleCard(
                        vehicle = vehicle,
                        actionsEnabled = true,
                        deleting = false,
                        onSelectVehicle = {},
                        onDeleteVehicle = { deleteRequested = true },
                        onEditVehicle = {},
                        onQuickOdometerUpdate = {},
                    )
                }
            }
        }

        composeRule.onNodeWithText(name).assertIsDisplayed()
        composeRule.onNodeWithText("Furgoneta").assertIsDisplayed()
        composeRule.onNodeWithText("1234 ABC").assertIsDisplayed()
        composeRule.onNodeWithText("Kilometraje actual").assertIsDisplayed()
        composeRule.onNodeWithText("120000 km").assertIsDisplayed()
        composeRule.onNodeWithText("Ver vehículo e historial").assertIsDisplayed()
        composeRule.onNodeWithText("Actualizar km").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Editar $name").assertIsDisplayed().assertHasClickAction()
        composeRule.onNodeWithTag("vehicle_card_${vehicle.id.value}").performTouchInput { swipeLeft() }
        composeRule.runOnIdle { check(deleteRequested) }
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
        composeRule.runOnIdle { check(!confirmed) }
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

    @Test
    fun compactFullEditUsesFullScreenAndKeepsSaveReachableWithLongContent() {
        var submitted = false
        composeRule.setContent {
            CompactGarage {
                TestGarageScreen(
                    formState = fullEditState(),
                    onSubmitEdit = { submitted = true },
                )
            }
        }

        composeRule.onNodeWithTag("full_screen_vehicle_editor").assertIsDisplayed()
        composeRule.onNodeWithText("Editar vehículo").assertIsDisplayed()
        composeRule.onNodeWithText("Datos del vehículo").assertIsDisplayed()
        val serviceOdometer = composeRule.onNodeWithTag("vehicle_edit_next_service_odometer")
        serviceOdometer.performScrollTo().performClick().performTextReplacement("130000")
        serviceOdometer.assertIsFocused().assertIsDisplayed()
        val save = composeRule.onNodeWithTag("full_screen_vehicle_save").assertIsDisplayed()
        save.performClick()
        composeRule.runOnIdle { assert(submitted) }
    }

    @Test
    fun dirtyFullEditRequiresDiscardConfirmationAndRetainsInput() {
        var dismissed = false
        composeRule.setContent {
            CompactGarage {
                var state by remember { mutableStateOf(fullEditState()) }
                TestGarageScreen(
                    formState = state,
                    onEditNameChange = { state = state.copy(editName = it, isEditDirty = true) },
                    onDismissEdit = { dismissed = true },
                )
            }
        }

        composeRule.onNodeWithText("Nombre").performTextReplacement("Nombre sin guardar")
        composeRule.onNodeWithContentDescription("Cerrar edición del vehículo").performClick()
        composeRule.onNodeWithText("¿Descartar cambios?").assertIsDisplayed()
        composeRule.onNodeWithText("Seguir editando").performClick()
        composeRule
            .onNodeWithTag("vehicle_edit_name")
            .assertIsDisplayed()
            .assertTextContains("Nombre sin guardar")
        composeRule.runOnIdle { assert(!dismissed) }

        composeRule.onNodeWithContentDescription("Cerrar edición del vehículo").performClick()
        composeRule.onNodeWithText("Descartar").performClick()
        composeRule.runOnIdle { assert(dismissed) }
    }

    @Test
    fun quickOdometerEditRemainsFocusedInDialog() {
        composeRule.setContent {
            CompactGarage {
                TestGarageScreen(
                    formState =
                        fullEditState().copy(
                            editMode = VehicleEditMode.Odometer,
                            isEditDirty = false,
                        ),
                )
            }
        }

        composeRule.onNodeWithText("Actualizar kilómetros").assertIsDisplayed()
        composeRule.onNodeWithText("Kilómetros actuales").assertIsDisplayed()
        composeRule.onNodeWithTag("full_screen_vehicle_editor").assertDoesNotExist()
        composeRule.onNodeWithText("Matrícula (opcional)").assertDoesNotExist()
    }

    @Test
    fun compactNonEmptyGarageUsesLabeledFabAndLeavesFinalCardUnobscured() {
        val vehicles = (1..8).map { vehicle("Vehículo $it", "vehicle-$it") }
        composeRule.setContent {
            CompactGarage {
                TestGarageScreen(
                    overviewState = GarageOverviewUiState(vehicles, GarageLoadState.Loaded),
                )
            }
        }

        composeRule.onNodeWithTag("garage_vehicle_list").performScrollToIndex(7)
        val finalCard = composeRule.onNodeWithTag("vehicle_card_vehicle-8").assertIsDisplayed()
        composeRule.onNodeWithText("Garaje").assertIsDisplayed()
        val fab = composeRule.onNodeWithTag("garage_add_vehicle_fab").assertIsDisplayed().assertHasClickAction()
        fab.assertContentDescriptionEquals("Añadir vehículo")
        composeRule.onNodeWithTag("garage_vehicle_list").performTouchInput { swipeUp() }
        assert(finalCard.getUnclippedBoundsInRoot().bottom <= fab.getUnclippedBoundsInRoot().top)
    }

    @Test
    fun compactGarageTitleAlignsWithVehicleContent() {
        val vehicle = vehicle("Coche familiar", "vehicle-1")
        composeRule.setContent {
            CompactGarage {
                TestGarageScreen(
                    overviewState = GarageOverviewUiState(listOf(vehicle), GarageLoadState.Loaded),
                )
            }
        }

        val titleLeft = composeRule.onNodeWithTag("garage_title").getUnclippedBoundsInRoot().left
        val cardLeft = composeRule.onNodeWithTag("vehicle_card_vehicle-1").getUnclippedBoundsInRoot().left

        assert(kotlin.math.abs((titleLeft - cardLeft).value) < 1f)
    }

    private fun vehicle(
        name: String,
        id: String = "vehicle-long",
    ) = Vehicle(
        id = VehicleId(id),
        familyId = FamilyId("family-test"),
        name = name,
        type = VehicleType.Van,
        currentOdometerKm = 120_000,
    )

    private fun fullEditState() =
        VehicleFormUiState(
            editMode = VehicleEditMode.Full,
            editingVehicleId = VehicleId("vehicle-edit"),
            editName = "Furgoneta familiar para viajes muy largos",
            editLicensePlate = "1234 ABC",
            editOdometerKm = "120000",
            editType = VehicleType.Van,
            editNextItvDate = "2027-05-10",
            editInsuranceRenewalDate = "2027-07-20",
            editNextServiceOdometerKm = "130000",
        )
}

@Composable
private fun CompactGarage(content: @Composable () -> Unit) {
    val configuration = Configuration(LocalConfiguration.current).apply { screenWidthDp = 360 }
    CompositionLocalProvider(LocalConfiguration provides configuration) {
        CarburaTheme { content() }
    }
}

@Composable
private fun TestGarageScreen(
    overviewState: GarageOverviewUiState = GarageOverviewUiState(loadState = GarageLoadState.Loaded),
    formState: VehicleFormUiState = VehicleFormUiState(),
    onEditNameChange: (String) -> Unit = {},
    onSubmitEdit: () -> Unit = {},
    onDismissEdit: () -> Unit = {},
) {
    GarageScreen(
        overviewState = overviewState,
        formState = formState,
        effectMessage = null,
        vehicleCreatedSignal = 0,
        vehicleSuccessSignal = 0,
        onNameChange = {},
        onOdometerChange = {},
        onTypeSelected = {},
        onNextItvDateChange = {},
        onInsuranceRenewalDateChange = {},
        onNextServiceOdometerChange = {},
        onCreateVehicle = {},
        onSelectVehicle = {},
        onDeleteVehicle = {},
        onEditVehicle = {},
        onQuickOdometerUpdate = {},
        onEditNameChange = onEditNameChange,
        onEditLicensePlateChange = {},
        onEditOdometerChange = {},
        onEditTypeSelected = {},
        onEditNextItvDateChange = {},
        onEditInsuranceRenewalDateChange = {},
        onEditNextServiceOdometerChange = {},
        onSubmitEdit = onSubmitEdit,
        onDismissEdit = onDismissEdit,
        onConfirmOdometerDecrease = {},
        onCancelOdometerDecrease = {},
        onConfirmReminderSuggestions = {},
        onDeclineReminderSuggestions = {},
        onRetry = {},
    )
}
