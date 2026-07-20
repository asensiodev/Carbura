package com.asensiodev.carbura.desktop

import com.asensiodev.carbura.core.stringresources.CarburaString

internal fun CarburaString.desktopMessage(): String =
    when (this) {
        CarburaString.ValidationBlankVehicleName -> "Enter a vehicle name."
        CarburaString.ValidationNegativeVehicleOdometer -> "Odometer must be zero or greater."
        CarburaString.ValidationBlankReminderTitle -> "Enter a reminder title."
        CarburaString.ValidationMissingReminderVehicle -> "Select a vehicle."
        CarburaString.ValidationMissingReminderDueTarget -> "Enter a due date or target odometer."
        CarburaString.ValidationNegativeReminderDueOdometer -> "Target odometer must be zero or greater."
        CarburaString.ValidationInvalidReminderDate -> "Use a valid date in YYYY-MM-DD format."
        CarburaString.ValidationBlankMaintenanceType -> "Enter a custom maintenance type."
        CarburaString.ValidationInvalidMaintenanceDate -> "Use a valid maintenance date in YYYY-MM-DD format."
        CarburaString.ValidationNegativeMaintenanceOdometer -> "Maintenance odometer must be zero or greater."
        CarburaString.ValidationNegativeMaintenanceCost -> "Enter a valid cost that is zero or greater."
        else -> "Check the details and try again."
    }
