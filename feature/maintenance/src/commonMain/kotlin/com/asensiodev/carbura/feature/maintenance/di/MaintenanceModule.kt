package com.asensiodev.carbura.feature.maintenance.di

import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderFromInputUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.CreateMaintenanceWithReminderUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.DeleteMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.GetVehicleHistoryUseCase
import com.asensiodev.carbura.core.domain.maintenance.usecase.UpdateMaintenanceRecordUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateAutomaticReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreatePlannedMaintenanceReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.RemovePlannedMaintenanceReminderUseCase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.VehicleId
import com.asensiodev.carbura.feature.maintenance.presentation.MaintenanceHistoryViewModel
import org.koin.dsl.module

val maintenanceModule =
    module {
        factory { CreateMaintenanceRecordUseCase(get()) }
        factory { CreateAutomaticReminderUseCase(get(), get()) }
        factory { CreatePlannedMaintenanceReminderUseCase(get(), get()) }
        factory { RemovePlannedMaintenanceReminderUseCase(get(), get()) }
        factory { CreateMaintenanceWithReminderUseCase(get(), get()) }
        factory { CreateMaintenanceWithReminderFromInputUseCase(get()) }
        factory { GetVehicleHistoryUseCase(get()) }
        factory { DeleteMaintenanceRecordUseCase(get(), get(), get()) }
        factory { UpdateMaintenanceRecordUseCase(get(), get()) }
        factory { parameters ->
            MaintenanceHistoryViewModel(
                vehicleId = parameters.get<VehicleId>(),
                scope = get<ActiveFamilyScopeGateway>().capture(parameters.get<FamilyId>()),
                dispatchers = get(),
                createMaintenanceWithReminderFromInputUseCase = get(),
                createPlannedMaintenanceReminderUseCase = get(),
                removePlannedMaintenanceReminderUseCase = get(),
                getVehicleHistoryUseCase = get(),
                deleteMaintenanceRecordUseCase = get(),
                updateMaintenanceRecordUseCase = get(),
                vehicleRepository = get(),
                syncManager = get(),
            )
        }
    }
