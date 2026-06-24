plugins {
    alias(libs.plugins.convention.android.application.compose)
}

dependencies {
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

    debugImplementation(libs.bundles.compose.debug)
}
