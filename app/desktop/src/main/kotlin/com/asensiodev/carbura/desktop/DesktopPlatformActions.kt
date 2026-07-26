package com.asensiodev.carbura.desktop

import java.awt.Desktop
import java.net.URI
import java.nio.file.Path

internal interface DesktopPlatformActions {
    fun openDirectory(path: Path): DesktopActionResult

    fun browse(uri: URI): DesktopActionResult
}

internal enum class DesktopActionResult {
    Success,
    Unsupported,
    Failed,
}

internal object AwtDesktopPlatformActions : DesktopPlatformActions {
    override fun openDirectory(path: Path): DesktopActionResult = performDesktopAction(Desktop.Action.OPEN) { open(path.toFile()) }

    override fun browse(uri: URI): DesktopActionResult = performDesktopAction(Desktop.Action.BROWSE) { browse(uri) }

    private fun performDesktopAction(
        action: Desktop.Action,
        operation: Desktop.() -> Unit,
    ): DesktopActionResult {
        return try {
            if (!Desktop.isDesktopSupported()) return DesktopActionResult.Unsupported
            val desktop = Desktop.getDesktop()
            if (!desktop.isSupported(action)) return DesktopActionResult.Unsupported
            desktop.operation()
            DesktopActionResult.Success
        } catch (_: Exception) {
            DesktopActionResult.Failed
        }
    }
}

internal fun DesktopActionResult.shouldReportFailure(): Boolean = this != DesktopActionResult.Success
