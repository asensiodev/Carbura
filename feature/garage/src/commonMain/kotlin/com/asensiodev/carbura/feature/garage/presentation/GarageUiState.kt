package com.asensiodev.carbura.feature.garage.presentation

import com.asensiodev.carbura.core.domain.reminder.usecase.VehicleReminderSuggestion
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString

data class GarageUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val name: String = "",
    val odometerKm: String = "0",
    val selectedType: VehicleType = VehicleType.Car,
    val errorMessage: CarburaString? = null,
    val isLoading: Boolean = false,
    val nextItvDate: String = "",
    val insuranceRenewalDate: String = "",
    val nextServiceOdometerKm: String = "",
    val editMode: VehicleEditMode? = null,
    val editingVehicleId: VehicleId? = null,
    val editName: String = "",
    val editLicensePlate: String = "",
    val editOdometerKm: String = "",
    val editType: VehicleType = VehicleType.Car,
    val editErrorMessage: CarburaString? = null,
    val editNextItvDate: String = "",
    val editInsuranceRenewalDate: String = "",
    val editNextServiceOdometerKm: String = "",
    val odometerDecreaseConfirmation: OdometerDecreaseConfirmation? = null,
    val reminderSuggestions: List<VehicleReminderSuggestion> = emptyList(),
    val reminderConfirmationMode: VehicleSaveMode? = null,
) {
    val isEmpty: Boolean = vehicles.isEmpty() && !isLoading
}

enum class VehicleSaveMode { Create, Edit }

enum class VehicleEditMode {
    Full,
    Odometer,
}

data class OdometerDecreaseConfirmation(
    val currentOdometerKm: Int,
    val proposedOdometerKm: Int,
)
