package com.asensiodev.carbura.feature.garage.di

import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway
import com.asensiodev.carbura.core.domain.reminder.usecase.DeriveVehicleReminderSuggestionsUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.SaveVehicleWithRemindersUseCase
import com.asensiodev.carbura.core.domain.vehicle.usecase.CreateVehicleUseCase
import com.asensiodev.carbura.core.domain.vehicle.usecase.DeleteVehicleUseCase
import com.asensiodev.carbura.core.domain.vehicle.usecase.UpdateVehicleUseCase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.feature.garage.presentation.overview.GarageOverviewViewModel
import com.asensiodev.carbura.feature.garage.presentation.vehicleform.VehicleFormViewModel
import org.koin.dsl.module

val garageModule =
    module {
        factory { CreateVehicleUseCase(get()) }
        factory { DeleteVehicleUseCase(get(), get(), get()) }
        factory { UpdateVehicleUseCase(get()) }
        factory { DeriveVehicleReminderSuggestionsUseCase() }
        factory { SaveVehicleWithRemindersUseCase(get(), get(), get(), get()) }
        factory { parameters ->
            GarageOverviewViewModel(
                scope = get<ActiveFamilyScopeGateway>().capture(parameters.get<FamilyId>()),
                vehicleRepository = get(),
                dispatchers = get(),
                deleteVehicleUseCase = get(),
                syncManager = get(),
            )
        }
        factory { parameters ->
            VehicleFormViewModel(
                scope = get<ActiveFamilyScopeGateway>().capture(parameters.get<FamilyId>()),
                vehicleRepository = get(),
                dispatchers = get(),
                createVehicleUseCase = get(),
                updateVehicleUseCase = get(),
                deriveVehicleReminderSuggestions = get(),
                saveVehicleWithRemindersUseCase = get(),
                syncManager = get(),
            )
        }
    }
