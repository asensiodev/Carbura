package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.stringresources.CarburaString

internal fun ValidationFailure.toMaintenanceMessage(): CarburaString =
    when (this) {
        ValidationFailure.BlankMaintenanceType -> CarburaString.ValidationBlankMaintenanceType
        ValidationFailure.InvalidMaintenanceDate -> CarburaString.ValidationInvalidMaintenanceDate
        ValidationFailure.InvalidMaintenancePerformedDate -> CarburaString.ValidationInvalidMaintenancePerformedDate
        ValidationFailure.InvalidMaintenanceNextDueDate -> CarburaString.ValidationInvalidMaintenanceNextDueDate
        ValidationFailure.InvalidMaintenanceCost -> CarburaString.ValidationInvalidMaintenanceCost
        ValidationFailure.InvalidMaintenanceOdometer -> CarburaString.ValidationInvalidMaintenanceOdometer
        ValidationFailure.NegativeMaintenanceOdometer -> CarburaString.ValidationNegativeMaintenanceOdometer
        ValidationFailure.NegativeMaintenanceCost -> CarburaString.ValidationNegativeMaintenanceCost
        ValidationFailure.BlankVehicleName,
        ValidationFailure.InvalidVehicleOdometer,
        ValidationFailure.InvalidVehicleServiceOdometer,
        ValidationFailure.NegativeVehicleOdometer,
        ValidationFailure.NegativeVehicleServiceOdometer,
        ValidationFailure.BlankReminderTitle,
        ValidationFailure.MissingReminderVehicle,
        ValidationFailure.MissingReminderDueTarget,
        ValidationFailure.InvalidReminderDueOdometer,
        ValidationFailure.NegativeReminderDueOdometer,
        -> CarburaString.ValidationGeneric
    }
