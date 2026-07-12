package com.asensiodev.carbura.core.domain.vehicle.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.CalendarDate
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleType

data class UpdateVehicleParams(
    val currentVehicle: Vehicle,
    val name: String,
    val type: VehicleType,
    val licensePlate: String?,
    val odometerKm: Int,
    val nextItvDate: CalendarDate? = currentVehicle.nextItvDate,
    val insuranceRenewalDate: CalendarDate? = currentVehicle.insuranceRenewalDate,
    val nextServiceOdometerKm: Int? = currentVehicle.nextServiceOdometerKm,
    val allowOdometerDecrease: Boolean = false,
)

sealed interface UpdateVehicleResult {
    data class Success(
        val vehicle: Vehicle,
    ) : UpdateVehicleResult

    data class ValidationError(
        val reason: ValidationFailure,
    ) : UpdateVehicleResult

    data class OdometerDecreaseConfirmationRequired(
        val currentOdometerKm: Int,
        val proposedOdometerKm: Int,
    ) : UpdateVehicleResult
}

class UpdateVehicleUseCase(
    private val repository: VehicleRepository,
) : SuspendUseCase<UpdateVehicleParams, UpdateVehicleResult> {
    override suspend fun invoke(params: UpdateVehicleParams): UpdateVehicleResult {
        if (params.name.isBlank()) {
            return UpdateVehicleResult.ValidationError(ValidationFailure.BlankVehicleName)
        }
        if (params.odometerKm < 0) {
            return UpdateVehicleResult.ValidationError(ValidationFailure.NegativeVehicleOdometer)
        }
        if (params.nextServiceOdometerKm != null && params.nextServiceOdometerKm < 0) {
            return UpdateVehicleResult.ValidationError(ValidationFailure.NegativeVehicleServiceOdometer)
        }
        if (
            params.odometerKm < params.currentVehicle.currentOdometerKm &&
            !params.allowOdometerDecrease
        ) {
            return UpdateVehicleResult.OdometerDecreaseConfirmationRequired(
                currentOdometerKm = params.currentVehicle.currentOdometerKm,
                proposedOdometerKm = params.odometerKm,
            )
        }

        val updatedVehicle =
            params.currentVehicle.copy(
                name = params.name.trim(),
                type = params.type,
                licensePlate = params.licensePlate?.trim()?.ifBlank { null },
                currentOdometerKm = params.odometerKm,
                nextItvDate = params.nextItvDate,
                insuranceRenewalDate = params.insuranceRenewalDate,
                nextServiceOdometerKm = params.nextServiceOdometerKm,
            )
        repository.saveVehicle(updatedVehicle)
        return UpdateVehicleResult.Success(updatedVehicle)
    }
}
