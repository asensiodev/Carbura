package com.asensiodev.carbura.feature.onboarding.presentation

data class OnboardingUiState(
    val isInitializing: Boolean = true,
    val isLoading: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val isAuthenticated: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val familyId: String? = null,
    val familyName: String? = null,
    val error: OnboardingError? = null,
    val errorDiagnostic: String? = null,
) {
    val canSubmitLogin: Boolean = !isInitializing && !isLoading
}

enum class OnboardingError {
    SessionUnavailable,
    ProfileUnavailable,
    SignInFailed,
    ProfileCreationFailed,
    SignOutFailed,
}
