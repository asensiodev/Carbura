plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(projects.core.auth)
        implementation(projects.core.model)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.koin.core)
        implementation(libs.sqldelight.runtime)
        implementation(libs.supabase.postgrest)
    }

    sourceSets.androidMain.dependencies {
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
}

sqldelight {
    databases {
        create("CarburaDatabase") {
            packageName.set("com.asensiodev.carbura.core.data.local")
        }
    }
}
