plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(projects.core.model)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.koin.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.sqldelight.runtime)
        implementation(libs.supabase.postgrest)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.work.runtime.ktx)
        implementation(libs.koin.android)
        implementation(libs.sqldelight.android.driver)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.sqldelight.sqlite.driver)
    }

    sourceSets.desktopTest.dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.sqldelight.sqlite.driver)
    }

    sourceSets.androidUnitTest.dependencies {
        implementation(libs.robolectric)
    }

    sourceSets.androidInstrumentedTest.dependencies {
        implementation(libs.androidx.test.ext.junit)
        implementation(libs.androidx.test.runner)
    }
}

android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    testOptions.unitTests.isIncludeAndroidResources = true
}

sqldelight {
    databases {
        create("CarburaDatabase") {
            packageName.set("com.asensiodev.carbura.core.data.local")
        }
    }
}
