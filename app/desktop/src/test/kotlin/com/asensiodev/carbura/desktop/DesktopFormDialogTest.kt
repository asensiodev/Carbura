package com.asensiodev.carbura.desktop

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
