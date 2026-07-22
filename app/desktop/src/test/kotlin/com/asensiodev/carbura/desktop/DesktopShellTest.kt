package com.asensiodev.carbura.desktop

import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.desktop.resources.Res
import com.asensiodev.carbura.desktop.resources.shell_account_description
import com.asensiodev.carbura.desktop.resources.shell_account_eyebrow
import com.asensiodev.carbura.desktop.resources.shell_account_headline
import com.asensiodev.carbura.desktop.resources.shell_destination_account
import com.asensiodev.carbura.desktop.resources.shell_destination_garage
import com.asensiodev.carbura.desktop.resources.shell_destination_maintenance
import com.asensiodev.carbura.desktop.resources.shell_destination_reminders
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewEffect
import com.asensiodev.carbura.feature.reminders.presentation.RemindersEffect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopShellTest {
    @Test
    fun allProductDestinationsAreAvailableFromTheShell() {
        assertEquals(
            listOf(
                Res.string.shell_destination_garage,
                Res.string.shell_destination_reminders,
                Res.string.shell_destination_maintenance,
                Res.string.shell_destination_account,
            ),
            DesktopDestination.entries.map { it.label },
        )
    }

    @Test
    fun navigationCompactsBelowDesktopThreshold() {
        assertTrue(usesCompactNavigation(899f))
        assertFalse(usesCompactNavigation(900f))
    }

    @Test
    fun accountDestinationDescribesTheDedicatedLocalWorkspace() {
        assertEquals(Res.string.shell_account_eyebrow, DesktopDestination.Account.eyebrow)
        assertEquals(Res.string.shell_account_headline, DesktopDestination.Account.headline)
        assertEquals(Res.string.shell_account_description, DesktopDestination.Account.description)
    }

    @Test
    fun sidebarMaintenanceNavigationClearsRoutedVehicleContext() {
        val vehicleId = VehicleId("vehicle-route")

        assertNull(maintenanceVehicleAfterSidebarNavigation(DesktopDestination.Maintenance, vehicleId))
        assertEquals(vehicleId, maintenanceVehicleAfterSidebarNavigation(DesktopDestination.Garage, vehicleId))
    }

    @Test
    fun maintenanceSelectionRequiresCurrentOrRoutedVehicleContext() {
        val currentVehicleId = VehicleId("vehicle-current")
        val routedVehicleId = VehicleId("vehicle-routed")
        val availableVehicleIds = setOf(currentVehicleId, routedVehicleId)

        assertNull(resolveMaintenanceVehicleSelection(null, null, availableVehicleIds))
        assertEquals(routedVehicleId, resolveMaintenanceVehicleSelection(null, routedVehicleId, availableVehicleIds))
        assertEquals(currentVehicleId, resolveMaintenanceVehicleSelection(currentVehicleId, routedVehicleId, availableVehicleIds))
        assertNull(resolveMaintenanceVehicleSelection(VehicleId("missing"), null, availableVehicleIds))
    }

    @Test
    fun reminderGarageRequestTargetsGarageInTheExistingShell() {
        assertEquals(
            DesktopDestination.Garage,
            reminderNavigationDestination(RemindersEffect.NavigateToGarage),
        )
        assertEquals(
            null,
            reminderNavigationDestination(RemindersEffect.ReminderCreated("Inspection")),
        )
    }

    @Test
    fun garageHistoryRequestCarriesVehicleIntoMaintenance() {
        val vehicleId = VehicleId("vehicle-history")

        assertEquals(
            vehicleId,
            maintenanceVehicleForGarageEffect(GarageOverviewEffect.NavigateToVehicleHistory(vehicleId)),
        )
        assertEquals(
            null,
            maintenanceVehicleForGarageEffect(GarageOverviewEffect.VehicleDeleted("Car")),
        )
    }
}
