package com.asensiodev.carbura.core.domain

import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.Vehicle
import com.asensiodev.carbura.core.model.VehicleId

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

        repository.saveVehicle(params)
        return DomainResult.Success(params)
    }
}

class DeleteVehicleUseCase(
    private val repository: VehicleRepository,
) : SuspendUseCase<VehicleId, Unit> {
    override suspend fun invoke(params: VehicleId) {
        repository.deleteVehicle(params)
    }
}

class GetVehicleHistoryUseCase(
    private val repository: MaintenanceRecordRepository,
) : SuspendUseCase<VehicleId, List<MaintenanceRecord>> {
    override suspend fun invoke(params: VehicleId): List<MaintenanceRecord> =
        repository.getVehicleHistory(params).sortedByDescending { it.performedOn }
}
