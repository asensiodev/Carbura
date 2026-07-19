package com.asensiodev.carbura.desktop

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
}
