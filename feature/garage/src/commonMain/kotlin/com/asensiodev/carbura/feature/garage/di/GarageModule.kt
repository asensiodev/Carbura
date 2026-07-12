package com.asensiodev.carbura.feature.garage.di

import com.asensiodev.carbura.core.domain.vehicle.usecase.CreateVehicleUseCase
import com.asensiodev.carbura.core.domain.vehicle.usecase.DeleteVehicleUseCase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.feature.garage.presentation.GarageViewModel
import org.koin.dsl.module

val garageModule =
    module {
        factory { CreateVehicleUseCase(get()) }
        factory { DeleteVehicleUseCase(get(), get(), get()) }
        factory { parameters ->
            GarageViewModel(
                familyId = parameters.get<FamilyId>(),
                vehicleRepository = get(),
                dispatchers = get(),
                createVehicleUseCase = get(),
                deleteVehicleUseCase = get(),
                syncManager = get(),
            )
        }
    }
