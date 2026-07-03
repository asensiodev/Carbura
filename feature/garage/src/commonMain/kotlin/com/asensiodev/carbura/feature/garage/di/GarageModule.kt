package com.asensiodev.carbura.feature.garage.di

import com.asensiodev.carbura.core.domain.CreateVehicleUseCase
import com.asensiodev.carbura.core.domain.VehicleRepository
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.feature.garage.data.InMemoryVehicleRepository
import com.asensiodev.carbura.feature.garage.presentation.GarageViewModel
import org.koin.dsl.module

val garageModule = module {
    single { FamilyId("local-family") }
    single<VehicleRepository> { InMemoryVehicleRepository() }
    factory { CreateVehicleUseCase(get()) }
    factory { GarageViewModel(get(), get(), get(), get()) }
}
