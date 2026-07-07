package com.asensiodev.carbura.feature.garage.presentation

import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featuregarage.R

internal fun CarburaString.garageStringRes(): Int = when (this) {
    CarburaString.VehicleCreatedMessage -> R.string.vehicle_created_message
    CarburaString.VehicleDeletedMessage -> R.string.vehicle_deleted_message
    CarburaString.ValidationBlankVehicleName -> R.string.validation_blank_vehicle_name
    CarburaString.ValidationNegativeVehicleOdometer -> R.string.validation_negative_vehicle_odometer
    CarburaString.ValidationGeneric -> R.string.validation_generic
    else -> R.string.validation_generic
}
