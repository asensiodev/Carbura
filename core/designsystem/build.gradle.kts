plugins {
    alias(libs.plugins.convention.kmp.compose)
}

kotlin {
    sourceSets.androidMain.dependencies {
        implementation(project.dependencies.platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.text.google.fonts)
    }

    sourceSets.androidInstrumentedTest.dependencies {
        implementation(project.dependencies.platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.ui.test.junit4)
        implementation(libs.androidx.compose.ui.test.manifest)
        implementation(libs.androidx.test.espresso.core)
    }
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}
