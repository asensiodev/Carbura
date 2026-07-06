plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.feature.garage)
        implementation(projects.feature.maintenance)
        implementation(projects.feature.onboarding)
        implementation(projects.core.auth)
        implementation(projects.core.data)
        implementation(projects.core.domain)
        implementation(projects.core.model)
        implementation(libs.koin.core)
        implementation(libs.androidx.navigation3.runtime)
        implementation(libs.kotlinx.serialization.json)
    }
}
