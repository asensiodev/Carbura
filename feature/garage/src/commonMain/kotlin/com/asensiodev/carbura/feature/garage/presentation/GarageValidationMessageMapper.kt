package com.asensiodev.carbura.feature.garage.presentation

import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.stringresources.CarburaString

internal fun ValidationFailure.toGarageMessage(): CarburaString = when (this) {
    ValidationFailure.BlankVehicleName -> CarburaString.ValidationBlankVehicleName
    ValidationFailure.NegativeVehicleOdometer -> CarburaString.ValidationNegativeVehicleOdometer
    ValidationFailure.NegativeMaintenanceOdometer,
    ValidationFailure.NegativeMaintenanceCost,
    ValidationFailure.BlankReminderTitle,
    ValidationFailure.MissingReminderVehicle,
    ValidationFailure.MissingReminderDueTarget,
    ValidationFailure.NegativeReminderDueOdometer,
    -> CarburaString.ValidationGeneric
}
