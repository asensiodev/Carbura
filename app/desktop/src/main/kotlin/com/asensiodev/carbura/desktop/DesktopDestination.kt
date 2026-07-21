package com.asensiodev.carbura.desktop

import com.asensiodev.carbura.desktop.resources.Res
import com.asensiodev.carbura.desktop.resources.shell_account_description
import com.asensiodev.carbura.desktop.resources.shell_account_eyebrow
import com.asensiodev.carbura.desktop.resources.shell_account_headline
import com.asensiodev.carbura.desktop.resources.shell_destination_account
import com.asensiodev.carbura.desktop.resources.shell_destination_garage
import com.asensiodev.carbura.desktop.resources.shell_destination_maintenance
import com.asensiodev.carbura.desktop.resources.shell_destination_reminders
import com.asensiodev.carbura.desktop.resources.shell_garage_description
import com.asensiodev.carbura.desktop.resources.shell_garage_eyebrow
import com.asensiodev.carbura.desktop.resources.shell_garage_headline
import com.asensiodev.carbura.desktop.resources.shell_maintenance_description
import com.asensiodev.carbura.desktop.resources.shell_maintenance_eyebrow
import com.asensiodev.carbura.desktop.resources.shell_maintenance_headline
import com.asensiodev.carbura.desktop.resources.shell_reminders_description
import com.asensiodev.carbura.desktop.resources.shell_reminders_eyebrow
import com.asensiodev.carbura.desktop.resources.shell_reminders_headline
import org.jetbrains.compose.resources.StringResource

enum class DesktopDestination(
    val label: StringResource,
    val eyebrow: StringResource,
    val headline: StringResource,
    val description: StringResource,
) {
    Garage(
        label = Res.string.shell_destination_garage,
        eyebrow = Res.string.shell_garage_eyebrow,
        headline = Res.string.shell_garage_headline,
        description = Res.string.shell_garage_description,
    ),
    Reminders(
        label = Res.string.shell_destination_reminders,
        eyebrow = Res.string.shell_reminders_eyebrow,
        headline = Res.string.shell_reminders_headline,
        description = Res.string.shell_reminders_description,
    ),
    Maintenance(
        label = Res.string.shell_destination_maintenance,
        eyebrow = Res.string.shell_maintenance_eyebrow,
        headline = Res.string.shell_maintenance_headline,
        description = Res.string.shell_maintenance_description,
    ),
    Account(
        label = Res.string.shell_destination_account,
        eyebrow = Res.string.shell_account_eyebrow,
        headline = Res.string.shell_account_headline,
        description = Res.string.shell_account_description,
    ),
}

internal const val COMPACT_NAVIGATION_THRESHOLD_DP = 900f

internal fun usesCompactNavigation(widthDp: Float): Boolean = widthDp < COMPACT_NAVIGATION_THRESHOLD_DP
