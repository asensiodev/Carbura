import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

dependencies {
    implementation(projects.core.auth)
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

fun kotlinString(value: String): String =
    value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")

val generateDesktopPublicConfig by tasks.registering {
    val outputDirectory = layout.buildDirectory.dir("generated/desktopPublicConfig")
    val desktopPublicConfigFile = rootProject.layout.projectDirectory.file("local.properties")
    inputs.file(desktopPublicConfigFile).withPropertyName("desktopPublicConfigFile").optional()
    outputs.dir(outputDirectory)
    doLast {
        val desktopLocalProperties =
            Properties().apply {
                val file = desktopPublicConfigFile.asFile
                if (file.exists()) file.inputStream().use(::load)
            }
        val packageDirectory = outputDirectory.get().asFile.resolve("com/asensiodev/carbura/desktop")
        packageDirectory.mkdirs()
        packageDirectory.resolve("DesktopPublicConfig.kt").writeText(
            """
            package com.asensiodev.carbura.desktop

            internal object DesktopPublicConfig {
                const val supabaseUrl: String = "${kotlinString(desktopLocalProperties.getProperty("SUPABASE_URL").orEmpty())}"
                const val supabaseAnonKey: String = "${kotlinString(desktopLocalProperties.getProperty("SUPABASE_ANON_KEY").orEmpty())}"
            }
            """.trimIndent() + "\n",
        )
    }
}

sourceSets.main {
    kotlin.srcDir(generateDesktopPublicConfig)
}

compose.resources {
    packageOfResClass = "com.asensiodev.carbura.desktop.resources"
}

compose.desktop {
    application {
        mainClass = "com.asensiodev.carbura.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            modules(
                "java.instrument",
                "java.management",
                "java.net.http",
                "java.prefs",
                "java.sql",
                "jdk.unsupported",
            )
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
