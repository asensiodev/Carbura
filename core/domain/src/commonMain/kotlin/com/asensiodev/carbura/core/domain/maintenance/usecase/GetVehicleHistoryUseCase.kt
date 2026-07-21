package com.asensiodev.carbura.core.domain.maintenance.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.maintenance.repository.MaintenanceRecordRepository
import com.asensiodev.carbura.core.model.MaintenanceRecord
import com.asensiodev.carbura.core.model.VehicleId

class GetVehicleHistoryUseCase(
    private val repository: MaintenanceRecordRepository,
) : SuspendUseCase<VehicleId, List<MaintenanceRecord>> {
    override suspend fun invoke(params: VehicleId): List<MaintenanceRecord> =
        repository.getVehicleHistory(params).sortedByDescending { it.performedOn }
}
