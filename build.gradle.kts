import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.HTML)
        }
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
            exclude { fileTreeElement ->
                fileTreeElement.file.invariantSeparatorsPath.contains("/build/generated/")
            }
        }
    }

    tasks.withType<BaseKtLintCheckTask>().configureEach {
        exclude { fileTreeElement ->
            fileTreeElement.file.invariantSeparatorsPath.contains("/build/generated/")
        }
    }

    if (project.path == ":core:data") {
        tasks.matching { task ->
            task.name == "ktlintCommonMainSourceSetCheck" ||
                task.name == "runKtlintCheckOverCommonMainSourceSet" ||
                task.name == "ktlintCommonMainSourceSetFormat" ||
                task.name == "runKtlintFormatOverCommonMainSourceSet"
        }.configureEach {
            enabled = false
        }
    }

    configure<DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        source.setFrom(files("src"))
    }

    tasks.withType<Detekt>().configureEach {
        include("**/*.kt", "**/*.kts")
        exclude("**/build/**", "**/generated/**")
        reports {
            xml.required.set(false)
            html.required.set(true)
            txt.required.set(true)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("qualityCheck") {
    subprojects.forEach { subproject ->
        dependsOn(
            subproject.tasks.matching { task ->
                task.name == "ktlintCheck" || task.name == "detekt"
            },
        )
    }
    dependsOn(":quality:architecture:test")
}

tasks.register("copyGitHooks") {
    doLast {
        val sourceDir = file("hooks")
        val targetDir = file(".git/hooks")

        if (!sourceDir.exists() || !targetDir.exists()) {
            return@doLast
        }

        sourceDir
            .listFiles()
            ?.filter { it.isFile }
            ?.forEach { sourceFile ->
                val targetFile = File(targetDir, sourceFile.name)
                if (!targetFile.exists() || Files.mismatch(sourceFile.toPath(), targetFile.toPath()) != -1L) {
                    Files.copy(
                        sourceFile.toPath(),
                        targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                    )
                    logger.lifecycle("> Copied hook: ${sourceFile.name}")
                }

                try {
                    Files.setPosixFilePermissions(
                        targetFile.toPath(),
                        PosixFilePermissions.fromString("rwxr-xr-x"),
                    )
                } catch (_: UnsupportedOperationException) {
                    logger.warn("Unable to set POSIX permissions on ${targetFile.name}.")
                }
            }
    }
}
