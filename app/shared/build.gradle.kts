plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.core.model)
        implementation(libs.androidx.navigation3.runtime)
        implementation(libs.kotlinx.serialization.json)
    }
}
