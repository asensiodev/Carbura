package com.asensiodev.carbura.feature.garage.presentation.vehicleform

import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleType

sealed interface VehicleFormEvent {
    data class NameChanged(
        val value: String,
    ) : VehicleFormEvent

    data class OdometerChanged(
        val value: String,
    ) : VehicleFormEvent

    data class TypeSelected(
        val value: VehicleType,
    ) : VehicleFormEvent

    data class NextItvDateChanged(
        val value: String,
    ) : VehicleFormEvent

    data class InsuranceRenewalDateChanged(
        val value: String,
    ) : VehicleFormEvent

    data class NextServiceOdometerChanged(
        val value: String,
    ) : VehicleFormEvent

    data class EditVehicleRequested(
        val vehicle: Vehicle,
    ) : VehicleFormEvent

    data class QuickOdometerUpdateRequested(
        val vehicle: Vehicle,
    ) : VehicleFormEvent

    data class EditNameChanged(
        val value: String,
    ) : VehicleFormEvent

    data class EditLicensePlateChanged(
        val value: String,
    ) : VehicleFormEvent

    data class EditOdometerChanged(
        val value: String,
    ) : VehicleFormEvent

    data class EditTypeSelected(
        val value: VehicleType,
    ) : VehicleFormEvent

    data class EditNextItvDateChanged(
        val value: String,
    ) : VehicleFormEvent

    data class EditInsuranceRenewalDateChanged(
        val value: String,
    ) : VehicleFormEvent

    data class EditNextServiceOdometerChanged(
        val value: String,
    ) : VehicleFormEvent

    data object SubmitVehicle : VehicleFormEvent

    data object SubmitVehicleEdit : VehicleFormEvent

    data object ConfirmOdometerDecrease : VehicleFormEvent

    data object CancelOdometerDecrease : VehicleFormEvent

    data object DismissVehicleEdit : VehicleFormEvent

    data object ConfirmReminderSuggestions : VehicleFormEvent

    data object DeclineReminderSuggestions : VehicleFormEvent
}
