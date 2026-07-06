import java.util.Properties

plugins {
    alias(libs.plugins.convention.android.application.compose)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun localProperty(name: String): String = localProperties.getProperty(name).orEmpty()

android {
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"${localProperty("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperty("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"${localProperty("GOOGLE_CLIENT_ID")}\"")
    }
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.core.designsystem)
    implementation(projects.core.auth)
    implementation(projects.feature.onboarding)
    implementation(projects.feature.garage)
    implementation(projects.feature.maintenance)
    implementation(projects.feature.reminders)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.koin.android)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    debugImplementation(libs.bundles.compose.debug)
}
