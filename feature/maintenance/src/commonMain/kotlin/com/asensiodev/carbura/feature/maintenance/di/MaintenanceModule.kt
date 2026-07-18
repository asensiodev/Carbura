package com.asensiodev.carbura.feature.maintenance.di

import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.DeleteMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.GetVehicleHistoryUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateAutomaticReminderUseCase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryViewModel
import org.koin.dsl.module

val maintenanceModule =
    module {
        factory { CreateMaintenanceRecordUseCase(get()) }
        factory { CreateAutomaticReminderUseCase(get(), get()) }
        factory { CreateMaintenanceWithReminderUseCase(get(), get()) }
        factory { CreateMaintenanceWithReminderFromInputUseCase(get()) }
        factory { GetVehicleHistoryUseCase(get()) }
        factory { DeleteMaintenanceRecordUseCase(get(), get(), get()) }
        factory { parameters ->
            MaintenanceHistoryViewModel(
                vehicleId = parameters.get<VehicleId>(),
                familyId = parameters.get<FamilyId>(),
                dispatchers = get(),
                createMaintenanceWithReminderFromInputUseCase = get(),
                getVehicleHistoryUseCase = get(),
                deleteMaintenanceRecordUseCase = get(),
                vehicleRepository = get(),
                syncManager = get(),
            )
        }
    }
