package com.asensiodev.carbura.desktop

import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewEffect
import com.asensiodev.carbura.feature.reminders.presentation.RemindersEffect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopShellTest {
    @Test
    fun allProductDestinationsAreAvailableFromTheShell() {
        assertEquals(
            listOf("Garage", "Reminders", "Maintenance", "Account"),
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
        assertEquals("LOCAL ACCOUNT", DesktopDestination.Account.eyebrow)
        assertEquals("Your data, on this device.", DesktopDestination.Account.headline)
        assertTrue(DesktopDestination.Account.description.contains("local mode", ignoreCase = true))
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
