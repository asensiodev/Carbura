package com.asensiodev.carbura.app.shared

import com.asensiodev.carbura.core.data.dataModule
import com.asensiodev.carbura.feature.garage.di.garageModule
import com.asensiodev.carbura.feature.maintenance.di.maintenanceModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) = startKoin {
    config?.invoke(this)
    modules(dataModule, garageModule, maintenanceModule)
}
