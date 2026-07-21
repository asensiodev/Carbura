package com.asensiodev.carbura.desktop

enum class DesktopDestination(
    val label: String,
    val eyebrow: String,
    val headline: String,
    val description: String,
) {
    Garage(
        label = "Garage",
        eyebrow = "YOUR VEHICLES",
        headline = "Everything important, parked in one place.",
        description = "Vehicle cards and editing are the next shared UI migration. Your KMP data layer is already connected.",
    ),
    Reminders(
        label = "Reminders",
        eyebrow = "STAY AHEAD",
        headline = "Maintenance should never be a surprise.",
        description = "Reliable reminder storage is ready. Desktop scheduling and notification controls arrive in the next increment.",
    ),
    Maintenance(
        label = "Maintenance",
        eyebrow = "SERVICE HISTORY",
        headline = "A clearer record of every kilometre.",
        description = "Shared maintenance models and persistence are available while the desktop history editor is migrated.",
    ),
    Account(
        label = "Account",
        eyebrow = "LOCAL ACCOUNT",
        headline = "Your data, on this device.",
        description = "Review local mode and the application storage used by Carbura Desktop.",
    ),
}

internal const val COMPACT_NAVIGATION_THRESHOLD_DP = 900f

internal fun usesCompactNavigation(widthDp: Float): Boolean = widthDp < COMPACT_NAVIGATION_THRESHOLD_DP
