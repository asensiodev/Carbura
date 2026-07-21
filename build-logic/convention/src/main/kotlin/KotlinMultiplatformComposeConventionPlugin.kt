import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinMultiplatformComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.asensiodev.carbura.convention.kotlin-multiplatform-library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    }
}
