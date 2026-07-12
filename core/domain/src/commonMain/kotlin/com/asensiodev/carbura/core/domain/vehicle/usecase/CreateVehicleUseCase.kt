package com.asensiodev.carbura.core.domain.vehicle.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.Vehicle

class CreateVehicleUseCase(
    private val repository: VehicleRepository,
) : SuspendUseCase<Vehicle, DomainResult<Vehicle>> {
    override suspend fun invoke(params: Vehicle): DomainResult<Vehicle> {
        if (params.name.isBlank()) {
            return DomainResult.ValidationError(ValidationFailure.BlankVehicleName)
        }

        if (params.currentOdometerKm < 0) {
            return DomainResult.ValidationError(ValidationFailure.NegativeVehicleOdometer)
        }
        if ((params.nextServiceOdometerKm ?: 0) < 0) {
            return DomainResult.ValidationError(ValidationFailure.NegativeVehicleServiceOdometer)
        }

        repository.saveVehicle(params)
        return DomainResult.Success(params)
    }
}
