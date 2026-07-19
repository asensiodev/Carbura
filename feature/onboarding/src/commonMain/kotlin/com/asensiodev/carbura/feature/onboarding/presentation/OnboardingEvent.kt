package com.asensiodev.carbura.feature.onboarding.presentation

sealed interface OnboardingEvent {
    data object Started : OnboardingEvent

    data object GoogleSignInClicked : OnboardingEvent

    data object GoogleCredentialRequestStarted : OnboardingEvent

    data object GoogleCredentialRequestCancelled : OnboardingEvent

    data class GoogleIdTokenReceived(
        val idToken: String,
    ) : OnboardingEvent

    data class GoogleSignInError(
        val diagnostic: String,
    ) : OnboardingEvent

    data object SignOutClicked : OnboardingEvent

    data object DeleteAccountConfirmed : OnboardingEvent
}
