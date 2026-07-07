package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.stringresources.CarburaString

internal fun ValidationFailure.toMaintenanceMessage(): CarburaString = when (this) {
    ValidationFailure.NegativeMaintenanceOdometer -> CarburaString.ValidationNegativeMaintenanceOdometer
    ValidationFailure.NegativeMaintenanceCost -> CarburaString.ValidationNegativeMaintenanceCost
    ValidationFailure.BlankVehicleName,
    ValidationFailure.NegativeVehicleOdometer,
    ValidationFailure.BlankReminderTitle,
    ValidationFailure.MissingReminderVehicle,
    ValidationFailure.MissingReminderDueTarget,
    ValidationFailure.NegativeReminderDueOdometer,
    -> CarburaString.ValidationGeneric
}
