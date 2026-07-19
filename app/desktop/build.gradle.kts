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
    implementation(libs.koin.core)
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    testImplementation(kotlin("test"))
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
