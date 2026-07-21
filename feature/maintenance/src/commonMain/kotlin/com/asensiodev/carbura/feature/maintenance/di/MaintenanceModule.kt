package com.asensiodev.carbura.feature.maintenance.di

import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.DeleteMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.GetVehicleHistoryUseCase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryViewModel
import org.koin.dsl.module

val maintenanceModule = module {
    factory { CreateMaintenanceRecordUseCase(get()) }
    factory { CreateMaintenanceRecordFromInputUseCase(get()) }
    factory { GetVehicleHistoryUseCase(get()) }
    factory { DeleteMaintenanceRecordUseCase(get()) }
    factory { parameters ->
        MaintenanceHistoryViewModel(
            vehicleId = parameters.get<VehicleId>(),
            familyId = parameters.get<FamilyId>(),
            dispatchers = get(),
            createMaintenanceRecordFromInputUseCase = get(),
            getVehicleHistoryUseCase = get(),
            deleteMaintenanceRecordUseCase = get(),
            syncManager = get(),
        )
    }
}
