plugins {
    alias(libs.plugins.convention.kmp.compose)
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(projects.core.model)
        implementation(projects.core.stringResources)
        api(libs.androidx.lifecycle.viewmodel)
        implementation(libs.compose.multiplatform.runtime)
        implementation(libs.koin.core)
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets.commonTest.dependencies {
        implementation(projects.core.testing)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.turbine)
    }

    sourceSets.androidMain.dependencies {
        implementation(projects.core.designsystem)
        implementation(project.dependencies.platform(libs.androidx.compose.bom))
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.lifecycle.runtime.compose)
    }

    sourceSets.androidInstrumentedTest.dependencies {
        implementation(project.dependencies.platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.ui.test.junit4)
        implementation(libs.androidx.compose.ui.test.manifest)
        implementation(libs.androidx.test.espresso.core)
        implementation(libs.androidx.test.runner)
    }
}
