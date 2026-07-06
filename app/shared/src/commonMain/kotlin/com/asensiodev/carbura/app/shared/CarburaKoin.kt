package com.asensiodev.carbura.app.shared

import com.asensiodev.carbura.core.data.dataModule
import com.asensiodev.carbura.core.auth.authModule
import com.asensiodev.carbura.feature.garage.di.garageModule
import com.asensiodev.carbura.feature.maintenance.di.maintenanceModule
import com.asensiodev.carbura.feature.onboarding.di.onboardingModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) = startKoin {
    config?.invoke(this)
    modules(authModule, dataModule, onboardingModule, garageModule, maintenanceModule)
}
