package com.asensiodev.carbura.feature.garage.presentation

import com.asensiodev.carbura.core.stringresources.CarburaString
import com.asensiodev.carbura.featuregarage.R

internal fun CarburaString.garageStringRes(): Int =
    when (this) {
        CarburaString.VehicleCreatedMessage -> R.string.vehicle_created_message
        CarburaString.VehicleDeletedMessage -> R.string.vehicle_deleted_message
        CarburaString.VehicleUpdatedMessage -> R.string.vehicle_updated_message
        CarburaString.ValidationBlankVehicleName -> R.string.validation_blank_vehicle_name
        CarburaString.ValidationInvalidVehicleOdometer -> R.string.validation_invalid_vehicle_odometer
        CarburaString.ValidationInvalidVehicleServiceOdometer -> R.string.validation_invalid_vehicle_odometer
        CarburaString.ValidationNegativeVehicleServiceOdometer -> R.string.validation_negative_vehicle_odometer
        CarburaString.ValidationInvalidVehicleItvDate -> R.string.validation_invalid_vehicle_date
        CarburaString.ValidationInvalidVehicleInsuranceDate -> R.string.validation_invalid_vehicle_date
        CarburaString.ValidationNegativeVehicleOdometer -> R.string.validation_negative_vehicle_odometer
        CarburaString.ValidationGeneric -> R.string.validation_generic
        else -> R.string.validation_generic
    }
