package com.asensiodev.carbura.feature.maintenance.presentation

import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featuremaintenance.R

internal fun CarburaString.maintenanceStringRes(): Int =
    when (this) {
        CarburaString.MaintenanceCreatedMessage -> R.string.maintenance_created_message
        CarburaString.MaintenanceDeletedMessage -> R.string.maintenance_deleted_message
        CarburaString.ValidationBlankMaintenanceType -> R.string.validation_blank_maintenance_type
        CarburaString.ValidationInvalidMaintenanceDate -> R.string.validation_invalid_maintenance_date
        CarburaString.ValidationNegativeMaintenanceOdometer -> R.string.validation_negative_maintenance_odometer
        CarburaString.ValidationNegativeMaintenanceCost -> R.string.validation_negative_maintenance_cost
        CarburaString.ValidationGeneric -> R.string.validation_generic
        else -> R.string.validation_generic
    }
