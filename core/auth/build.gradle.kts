plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.kotlin.serialization)
}

kotlin {
    sourceSets.commonMain.dependencies {
        implementation(projects.core.domain)
        implementation(libs.koin.core)
        implementation(libs.supabase.auth)
        implementation(libs.supabase.postgrest)
    }

    sourceSets.androidMain.dependencies {
        implementation(libs.ktor.client.okhttp)
    }

    sourceSets.desktopMain.dependencies {
        implementation(libs.jna)
        implementation(libs.jna.platform)
        implementation(libs.ktor.client.java)
    }

    sourceSets.commonTest.dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
    }
}
