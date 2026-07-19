package com.asensiodev.carbura.feature.reminders.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import com.asensiodev.carbura.core.designsystem.CarburaTheme
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.Reminder
import com.asensiodev.carbura.core.model.ReminderId
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RemindersScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadFailureOffersRetry() {
        var retried = false
        composeRule.setRemindersContent(
            state = RemindersUiState(isLoading = false, hasLoadError = true),
            onRetry = { retried = true },
        )

        composeRule.onNodeWithText("Reintentar").performClick()

        assertTrue(retried)
    }

    @Test
    fun noVehicleStateNavigatesToGarage() {
        var navigated = false
        composeRule.setRemindersContent(
            state = RemindersUiState(isLoading = false),
            onNavigateToGarage = { navigated = true },
        )

        composeRule.onNodeWithText("Ir al garaje").performClick()

        assertTrue(navigated)
    }

    @Test
    fun permanentPermissionDenialGuidanceDoesNotHideCrud() {
        composeRule.setRemindersContent(
            state = loadedState(reminders = listOf(reminder())),
            notificationPermissionState = NotificationPermissionState.PermanentlyDenied,
        )

        composeRule.onNodeWithText("Abrir ajustes").assertIsDisplayed()
        composeRule.onNodeWithText("Completar").assertIsDisplayed()
    }

    @Test
    fun unavailableVehicleAndCompleteActionAreAccessible() {
        val reminder = reminder(title = "ITV familiar")
        composeRule.setRemindersContent(
            state = loadedState(reminders = listOf(reminder), vehicles = emptyList()),
        )

        composeRule.onNodeWithText("Vehículo no disponible").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Completar recordatorio ITV familiar").assertIsDisplayed()
        composeRule.onNodeWithTag("reminder_card_${reminder.id.value}").assertIsDisplayed()
    }

    @Test
    fun compactNonEmptyListHasReachableLabeledFab() {
        composeRule.setRemindersContent(state = loadedState(reminders = listOf(reminder())))

        composeRule.onNodeWithText("Añadir recordatorio", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("add_reminder_fab").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Nuevo recordatorio").assertIsDisplayed()
    }

    @Test
    fun compactFabDoesNotObscureFinalListItem() {
        val reminders = (1..8).map { reminder(id = "reminder-$it", title = "Recordatorio $it") }
        composeRule.setRemindersContent(state = loadedState(reminders = reminders))

        composeRule.onNodeWithTag("reminders_content").performScrollToIndex(8)
        composeRule.onNodeWithTag("reminder_card_reminder-8").assertIsDisplayed()
        composeRule.onNodeWithText("Recordatorios").assertIsDisplayed()
        val finalItemBounds = composeRule.onNodeWithTag("reminder_card_reminder-8").getBoundsInRoot()
        val fabBounds = composeRule.onNodeWithTag("add_reminder_fab").getBoundsInRoot()
        assertTrue(finalItemBounds.bottom <= fabBounds.top)
    }

    @Test
    fun deleteDispatchesOnlyAfterItemSpecificConfirmation() {
        val reminder = reminder(title = "Seguro familiar")
        var deletedReminder: Reminder? = null
        composeRule.setRemindersContent(
            state = loadedState(reminders = listOf(reminder)),
            onDeleteReminder = { deletedReminder = it },
        )

        composeRule.onNodeWithTag("reminder_card_${reminder.id.value}").performTouchInput { swipeLeft() }

        composeRule.runOnIdle { assertNull(deletedReminder) }
        composeRule.onAllNodesWithText("Seguro familiar").assertCountEquals(2)
        composeRule.onNodeWithText("Borrar").performClick()
        composeRule.runOnIdle { assertTrue(deletedReminder === reminder) }
    }

    @Test
    fun manyVehicleSelectorIsSingleChoiceAndSaveRemainsReachable() {
        val vehicles = (1..20).map { vehicle("vehicle-$it", "Vehículo $it") }
        composeRule.setRemindersContent(state = loadedState(vehicles = vehicles))

        composeRule.onNodeWithText("Añadir recordatorio").performClick()

        composeRule.onNodeWithText("Vehículo 1").assertIsSelected()
        composeRule.onNodeWithText("Guardar recordatorio").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun expandedContentUsesReadableMaximumWidth() {
        composeRule.setContent {
            CarburaTheme {
                Box(Modifier.requiredWidth(1_000.dp).height(800.dp)) {
                    remindersScreen(state = loadedState())
                }
            }
        }

        composeRule.onNodeWithTag("reminders_content").assertWidthIsEqualTo(720.dp)
    }

    @Test
    fun remindersTitleAlignsWithFilterContent() {
        val vehicle = vehicle("vehicle-1", "Coche familiar")
        composeRule.setRemindersContent(
            state = loadedState(reminders = listOf(reminder(vehicleId = vehicle.id)), vehicles = listOf(vehicle)),
        )

        val titleLeft = composeRule.onNodeWithTag("reminders_title").getBoundsInRoot().left
        val filtersLeft = composeRule.onNodeWithTag("reminder_vehicle_filters").getBoundsInRoot().left

        assertTrue(kotlin.math.abs((titleLeft - filtersLeft).value) < 1f)
    }

    @Test
    fun vehicleFiltersAllowMultipleSelectionAndAllClearsThem() {
        val firstVehicle = vehicle("vehicle-1", "Coche familiar")
        val secondVehicle = vehicle("vehicle-2", "Moto")
        var state by
            mutableStateOf(
                loadedState(
                    reminders = listOf(reminder(vehicleId = firstVehicle.id), reminder("reminder-2", vehicleId = secondVehicle.id)),
                    vehicles = listOf(firstVehicle, secondVehicle),
                ),
            )
        composeRule.setContent {
            CarburaTheme {
                remindersScreen(
                    state = state,
                    onVehicleFilterToggled = { selectedVehicle ->
                        state =
                            state.copy(
                                selectedFilterVehicleIds =
                                    if (selectedVehicle.id in state.selectedFilterVehicleIds) {
                                        state.selectedFilterVehicleIds - selectedVehicle.id
                                    } else {
                                        state.selectedFilterVehicleIds + selectedVehicle.id
                                    },
                            )
                    },
                    onVehicleFiltersCleared = { state = state.copy(selectedFilterVehicleIds = emptySet()) },
                )
            }
        }

        composeRule.onNodeWithTag("reminder_filter_all").assertIsSelected()
        composeRule.onNodeWithTag("reminder_filter_vehicle_vehicle-1").performClick()
        composeRule.onNodeWithTag("reminder_filter_vehicle_vehicle-2").performClick()

        composeRule.onNodeWithTag("reminder_filter_all").assertIsNotSelected()
        composeRule.onNodeWithTag("reminder_filter_vehicle_vehicle-1").assertIsSelected()
        composeRule.onNodeWithTag("reminder_filter_vehicle_vehicle-2").assertIsSelected()

        composeRule.onNodeWithTag("reminder_filter_all").performClick().assertIsSelected()
        composeRule.onNodeWithTag("reminder_filter_vehicle_vehicle-1").assertIsNotSelected()
        composeRule.onNodeWithTag("reminder_filter_vehicle_vehicle-2").assertIsNotSelected()
    }

    @Test
    fun vehicleFiltersScrollHorizontallyToLastVehicle() {
        val vehicles = (1..20).map { vehicle("vehicle-$it", "Vehículo $it") }
        composeRule.setRemindersContent(
            state = loadedState(reminders = listOf(reminder(vehicleId = vehicles.first().id)), vehicles = vehicles),
        )

        composeRule.onNodeWithTag("reminder_vehicle_filters").assertIsDisplayed()
        composeRule.onNodeWithTag("reminder_vehicle_filters").performScrollToIndex(20)
        composeRule.onNodeWithText("Vehículo 20").assertIsDisplayed()
    }

    @Test
    fun filteredEmptyStateDiffersFromFamilyEmptyState() {
        val firstVehicle = vehicle("vehicle-1", "Coche familiar")
        val secondVehicle = vehicle("vehicle-2", "Moto")
        composeRule.setRemindersContent(
            state =
                loadedState(
                    reminders = listOf(reminder(vehicleId = firstVehicle.id)),
                    vehicles = listOf(firstVehicle, secondVehicle),
                ).copy(selectedFilterVehicleIds = setOf(secondVehicle.id)),
        )

        composeRule.onNodeWithTag("no_matching_reminders").assertIsDisplayed()
        composeRule.onNodeWithText("No hay recordatorios para estos vehículos").assertIsDisplayed()
        composeRule.onNodeWithTag("reminder_card_reminder-1").assertDoesNotExist()
    }

    private fun ComposeContentTestRule.setRemindersContent(
        state: RemindersUiState,
        notificationPermissionState: NotificationPermissionState = NotificationPermissionState.Granted,
        onRetry: () -> Unit = {},
        onNavigateToGarage: () -> Unit = {},
        onDeleteReminder: (Reminder) -> Unit = {},
        onVehicleFilterToggled: (Vehicle) -> Unit = {},
        onVehicleFiltersCleared: () -> Unit = {},
    ) {
        setContent {
            CarburaTheme {
                remindersScreen(
                    state = state,
                    notificationPermissionState = notificationPermissionState,
                    onRetry = onRetry,
                    onNavigateToGarage = onNavigateToGarage,
                    onDeleteReminder = onDeleteReminder,
                    onVehicleFilterToggled = onVehicleFilterToggled,
                    onVehicleFiltersCleared = onVehicleFiltersCleared,
                )
            }
        }
    }

    @Composable
    private fun remindersScreen(
        state: RemindersUiState,
        notificationPermissionState: NotificationPermissionState = NotificationPermissionState.Granted,
        onRetry: () -> Unit = {},
        onNavigateToGarage: () -> Unit = {},
        onDeleteReminder: (Reminder) -> Unit = {},
        onVehicleFilterToggled: (Vehicle) -> Unit = {},
        onVehicleFiltersCleared: () -> Unit = {},
    ) {
        RemindersScreen(
            state = state,
            effectMessage = null,
            reminderCreatedSignal = 0,
            reminderSuccessSignal = 0,
            onTitleChange = {},
            onVehicleSelected = {},
            onVehicleFilterToggled = onVehicleFilterToggled,
            onVehicleFiltersCleared = onVehicleFiltersCleared,
            onDueDateChange = {},
            onDueOdometerChange = {},
            onSubmitReminder = {},
            onCompleteReminder = {},
            onDeleteReminder = onDeleteReminder,
            onRetry = onRetry,
            onNavigateToGarage = onNavigateToGarage,
            notificationPermissionState = notificationPermissionState,
            onRequestNotificationPermission = {},
            onOpenNotificationSettings = {},
        )
    }

    private fun loadedState(
        reminders: List<Reminder> = emptyList(),
        vehicles: List<Vehicle> = listOf(vehicle("vehicle-1", "Coche familiar")),
    ) = RemindersUiState(
        isLoading = false,
        reminders = reminders,
        vehicles = vehicles,
        selectedVehicleId = vehicles.firstOrNull()?.id,
    )

    private fun reminder(
        id: String = "reminder-1",
        title: String = "Pasar ITV",
        vehicleId: VehicleId = VehicleId("missing-vehicle"),
    ) = Reminder(
        id = ReminderId(id),
        familyId = FamilyId("family-1"),
        vehicleId = vehicleId,
        maintenanceTypeId = null,
        title = title,
        dueDate = CalendarDate("2026-08-01"),
    )

    private fun vehicle(
        id: String,
        name: String,
    ) = Vehicle(
        id = VehicleId(id),
        familyId = FamilyId("family-1"),
        name = name,
        type = VehicleType.Car,
        currentOdometerKm = 10_000,
    )
}
