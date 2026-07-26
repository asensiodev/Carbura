package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.family.FamilyScoped
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.VehicleId

class GetVehicleHistoryUseCase(
    private val repository: MaintenanceRecordRepository,
) : SuspendUseCase<FamilyScoped<VehicleId>, List<MaintenanceRecord>> {
    override suspend fun invoke(params: FamilyScoped<VehicleId>): List<MaintenanceRecord> =
        repository.getVehicleHistory(params.scope, params.value).sortedByDescending { it.performedOn }
}
