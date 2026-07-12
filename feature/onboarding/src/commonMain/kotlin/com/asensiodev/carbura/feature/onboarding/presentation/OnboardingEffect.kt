package com.asensiodev.carbura.feature.onboarding.presentation

sealed interface OnboardingEffect {
    data object NavigateToGarage : OnboardingEffect

    data object NavigateToLogin : OnboardingEffect
}
