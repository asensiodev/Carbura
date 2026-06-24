plugins {
    alias(libs.plugins.convention.kmp.library)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(projects.core.model)
    }
}
