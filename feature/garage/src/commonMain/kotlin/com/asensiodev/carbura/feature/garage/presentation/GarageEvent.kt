package com.asensiodev.carbura.feature.garage.presentation

import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType

sealed interface GarageEvent {
    data object Started : GarageEvent

    data class NameChanged(
        val value: String,
    ) : GarageEvent

    data class OdometerChanged(
        val value: String,
    ) : GarageEvent

    data class TypeSelected(
        val value: VehicleType,
    ) : GarageEvent

    data class NextItvDateChanged(
        val value: String,
    ) : GarageEvent

    data class InsuranceRenewalDateChanged(
        val value: String,
    ) : GarageEvent

    data class NextServiceOdometerChanged(
        val value: String,
    ) : GarageEvent

    data class VehicleSelected(
        val vehicleId: VehicleId,
    ) : GarageEvent

    data class DeleteVehicleConfirmed(
        val vehicleId: VehicleId,
    ) : GarageEvent

    data class EditVehicleRequested(
        val vehicleId: VehicleId,
    ) : GarageEvent

    data class QuickOdometerUpdateRequested(
        val vehicleId: VehicleId,
    ) : GarageEvent

    data class EditNameChanged(
        val value: String,
    ) : GarageEvent

    data class EditLicensePlateChanged(
        val value: String,
    ) : GarageEvent

    data class EditOdometerChanged(
        val value: String,
    ) : GarageEvent

    data class EditTypeSelected(
        val value: VehicleType,
    ) : GarageEvent

    data class EditNextItvDateChanged(
        val value: String,
    ) : GarageEvent

    data class EditInsuranceRenewalDateChanged(
        val value: String,
    ) : GarageEvent

    data class EditNextServiceOdometerChanged(
        val value: String,
    ) : GarageEvent

    data object SubmitVehicle : GarageEvent

    data object SubmitVehicleEdit : GarageEvent

    data object ConfirmOdometerDecrease : GarageEvent

    data object CancelOdometerDecrease : GarageEvent

    data object DismissVehicleEdit : GarageEvent

    data object ConfirmReminderSuggestions : GarageEvent

    data object DeclineReminderSuggestions : GarageEvent
}
