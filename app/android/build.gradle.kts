plugins {
    alias(libs.plugins.convention.android.application.compose)
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.core.designsystem)
    implementation(projects.core.auth)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.garage)
    implementation(projects.feature.maintenance)
    implementation(projects.feature.reminders)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.koin.android)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    debugImplementation(libs.bundles.compose.debug)
}
