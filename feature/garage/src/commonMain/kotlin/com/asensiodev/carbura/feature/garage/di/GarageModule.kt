package com.asensiodev.carbura.feature.garage.di

import com.asensiodev.carbura.core.domain.CreateVehicleUseCase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.feature.garage.presentation.GarageViewModel
import org.koin.dsl.module

val garageModule = module {
    single { FamilyId("local-family") }
    factory { CreateVehicleUseCase(get()) }
    factory {
        GarageViewModel(
            familyId = get(),
            vehicleRepository = get(),
            dispatchers = get(),
            createVehicleUseCase = get(),
            syncManager = get(),
        )
    }
}
