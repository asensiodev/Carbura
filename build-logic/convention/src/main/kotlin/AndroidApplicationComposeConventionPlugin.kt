import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.asensiodev.carbura.convention.android-application")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    }
}
