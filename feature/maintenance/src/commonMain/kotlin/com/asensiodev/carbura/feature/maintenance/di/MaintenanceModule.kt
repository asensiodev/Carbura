package com.asensiodev.carbura.feature.maintenance.di

import com.asensiodev.carbura.core.domain.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.GetVehicleHistoryUseCase
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryViewModel
import org.koin.dsl.module

val maintenanceModule = module {
    factory { CreateMaintenanceRecordUseCase(get()) }
    factory { GetVehicleHistoryUseCase(get()) }
    factory { parameters ->
        MaintenanceHistoryViewModel(
            vehicleId = parameters.get<VehicleId>(),
            familyId = get(),
            dispatchers = get(),
            createMaintenanceRecordUseCase = get(),
            getVehicleHistoryUseCase = get(),
        )
    }
}
