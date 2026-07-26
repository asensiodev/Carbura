package com.asensiodev.carbura.desktop

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopFormDialogTest {
    @Test
    fun formDialogUsesConstrainedDesktopDimensions() {
        assertEquals(640.dp, DesktopFormDialogMaxWidth)
        assertEquals(24.dp, DesktopFormDialogMargin)
        assertEquals(712.dp, desktopFormDialogMaxHeight(760.dp))
    }

    @Test
    fun formDialogHeightNeverBecomesNegative() {
        assertEquals(0.dp, desktopFormDialogMaxHeight(40.dp))
    }

    @Test
    fun pairedFieldsStackAtConstrainedDialogWidths() {
        assertTrue(useStackedDesktopFields(479.dp))
        assertFalse(useStackedDesktopFields(480.dp))
    }
}
