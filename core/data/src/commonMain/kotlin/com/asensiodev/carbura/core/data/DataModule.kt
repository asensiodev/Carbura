package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.domain.DispatcherProvider
import org.koin.dsl.module

val dataModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}
