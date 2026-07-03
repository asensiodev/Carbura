plugins {
    alias(libs.plugins.convention.kmp.compose)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(projects.core.model)
        api(libs.androidx.lifecycle.viewmodel)
        implementation(libs.koin.core)
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.commonTest.dependencies {
        implementation(projects.core.testing)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.turbine)
    }

    sourceSets.androidMain.dependencies {
        implementation(project.dependencies.platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.ui)
    }
}
