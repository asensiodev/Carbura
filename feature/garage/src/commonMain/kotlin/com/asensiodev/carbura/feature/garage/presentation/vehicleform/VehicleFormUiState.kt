package com.asensiodev.carbura.feature.garage.presentation.vehicleform

import com.asensiodev.carbura.core.domain.reminder.usecase.VehicleReminderSuggestion
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.core.model.VehicleType
import com.asensiodev.carbura.core.stringresources.CarburaString

data class VehicleFormUiState(
    val name: String = "",
    val odometerKm: String = "0",
    val selectedType: VehicleType = VehicleType.Car,
    val createValidationError: CarburaString? = null,
    val persistenceError: Boolean = false,
    val activeMutation: VehicleFormMutation? = null,
    val nextItvDate: String = "",
    val insuranceRenewalDate: String = "",
    val nextServiceOdometerKm: String = "",
    val editMode: VehicleEditMode? = null,
    val editingVehicleId: VehicleId? = null,
    val editName: String = "",
    val editLicensePlate: String = "",
    val editOdometerKm: String = "",
    val editType: VehicleType = VehicleType.Car,
    val editValidationError: CarburaString? = null,
    val editNextItvDate: String = "",
    val editInsuranceRenewalDate: String = "",
    val editNextServiceOdometerKm: String = "",
    val isEditDirty: Boolean = false,
    val odometerDecreaseConfirmation: OdometerDecreaseConfirmation? = null,
    val reminderSuggestions: List<VehicleReminderSuggestion> = emptyList(),
    val reminderConfirmationMode: VehicleSaveMode? = null,
)

sealed interface VehicleFormMutation {
    data object Creating : VehicleFormMutation

    data class Updating(
        val vehicleId: VehicleId,
    ) : VehicleFormMutation
}

enum class VehicleSaveMode { Create, Edit }

enum class VehicleEditMode { Full, Odometer }

data class OdometerDecreaseConfirmation(
    val currentOdometerKm: Int,
    val proposedOdometerKm: Int,
)
