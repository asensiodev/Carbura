package com.asensiodev.carbura.desktop

import java.net.URI
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopPlatformActionsTest {
    @Test
    fun actionFeedbackOnlyReportsFailures() {
        assertFalse(DesktopActionResult.Success.shouldReportFailure())
        assertTrue(DesktopActionResult.Unsupported.shouldReportFailure())
        assertTrue(DesktopActionResult.Failed.shouldReportFailure())
    }

    @Test
    fun projectWebsiteUsesHttps() {
        assertEquals("https", CARBURA_PROJECT_URI.scheme)
        assertEquals("github.com", CARBURA_PROJECT_URI.host)
    }

    @Test
    fun accountActionsDelegateExactDirectoryAndProjectUri() {
        val actions = RecordingDesktopPlatformActions()
        val dataDirectory = Path.of("/tmp/Carbura")

        assertEquals(DesktopActionResult.Success, openAccountDataDirectory(actions, dataDirectory))
        assertEquals(dataDirectory, actions.openedDirectory)
        assertEquals(DesktopActionResult.Success, openAccountProject(actions))
        assertEquals(CARBURA_PROJECT_URI, actions.browsedUri)
    }

    private class RecordingDesktopPlatformActions : DesktopPlatformActions {
        var openedDirectory: Path? = null
        var browsedUri: URI? = null

        override fun openDirectory(path: Path): DesktopActionResult {
            openedDirectory = path
            return DesktopActionResult.Success
        }

        override fun browse(uri: URI): DesktopActionResult {
            browsedUri = uri
            return DesktopActionResult.Success
        }
    }
}
