import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.model)
    implementation(projects.core.stringResources)
    implementation(projects.feature.garage)
    implementation(projects.feature.maintenance)
    implementation(projects.feature.reminders)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqldelight.sqlite.driver)
}

compose.resources {
    packageOfResClass = "com.asensiodev.carbura.desktop.resources"
}

compose.desktop {
    application {
        mainClass = "com.asensiodev.carbura.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "Carbura"
            packageVersion = "1.0.0"
            description = "Vehicle maintenance and reminder companion"
            vendor = "Carbura"

            macOS {
                bundleID = "com.asensiodev.carbura.desktop"
            }
        }
    }
}
