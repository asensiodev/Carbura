package com.asensiodev.carbura.feature.onboarding.presentation

data class OnboardingUiState(
    val isInitializing: Boolean = true,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val familyId: String? = null,
    val familyName: String? = null,
    val errorMessage: String? = null,
) {
    val canSubmitLogin: Boolean = !isInitializing && !isLoading
}
