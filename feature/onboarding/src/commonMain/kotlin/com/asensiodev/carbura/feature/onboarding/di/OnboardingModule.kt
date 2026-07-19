package com.asensiodev.carbura.feature.onboarding.di

import com.asensiodev.carbura.feature.onboarding.presentation.OnboardingViewModel
import org.koin.dsl.module

val onboardingModule =
    module {
        factory { OnboardingViewModel(get(), get(), get(), get()) }
    }
