plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(projects.core.model)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.koin.core)
    }
}
