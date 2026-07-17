plugins {
    alias(libs.plugins.convention.kmp.compose)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(projects.core.model)
        implementation(projects.core.stringResources)
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
        implementation(projects.core.auth)
        implementation(projects.core.designsystem)
        implementation(project.dependencies.platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.lifecycle.runtime.compose)
        implementation(libs.androidx.credentials)
        implementation(libs.androidx.credentials.playauth)
        implementation(libs.googleid)
    }

    sourceSets.androidInstrumentedTest.dependencies {
        implementation(project.dependencies.platform(libs.androidx.compose.bom))
        implementation("androidx.compose.ui:ui-test-junit4")
        implementation("androidx.test:runner:1.7.0")
        implementation("androidx.test.espresso:espresso-core:3.7.0")
        implementation("androidx.test.ext:junit:1.3.0")
    }
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
