package com.asensiodev.carbura.core.domain.vehicle.usecase

import com.asensiodev.carbura.core.domain.DomainResult
import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.ValidationFailure
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.vehicle.repository.VehicleRepository
import com.asensiodev.carbura.core.model.Vehicle

class CreateVehicleUseCase(
    private val repository: VehicleRepository,
) : SuspendUseCase<FamilyScoped<Vehicle>, DomainResult<Vehicle>> {
    override suspend fun invoke(params: FamilyScoped<Vehicle>): DomainResult<Vehicle> {
        val vehicle = params.value
        if (vehicle.name.isBlank()) {
            return DomainResult.ValidationError(ValidationFailure.BlankVehicleName)
        }

        if (vehicle.currentOdometerKm < 0) {
            return DomainResult.ValidationError(ValidationFailure.NegativeVehicleOdometer)
        }
        if ((vehicle.nextServiceOdometerKm ?: 0) < 0) {
            return DomainResult.ValidationError(ValidationFailure.NegativeVehicleServiceOdometer)
        }

        require(vehicle.familyId == params.scope.familyId)
        repository.saveVehicle(params.scope, vehicle)
        return DomainResult.Success(vehicle)
    }
}
