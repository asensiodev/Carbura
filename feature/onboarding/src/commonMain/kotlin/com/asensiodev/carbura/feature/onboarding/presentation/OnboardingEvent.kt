package com.asensiodev.carbura.feature.onboarding.presentation

sealed interface OnboardingEvent {
    data object Started : OnboardingEvent
    data object GoogleSignInClicked : OnboardingEvent
    data object SignOutClicked : OnboardingEvent
    data object ErrorDismissed : OnboardingEvent
}
