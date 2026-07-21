plugins {
    alias(libs.plugins.convention.kmp.compose)
}

kotlin {
    sourceSets.androidMain.dependencies {
        implementation(project.dependencies.platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.ui)
    }
}
