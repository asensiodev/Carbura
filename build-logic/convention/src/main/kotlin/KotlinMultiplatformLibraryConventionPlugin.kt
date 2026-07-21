import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KotlinMultiplatformLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")

        configureAndroidLibrary()
        configureKotlinJvmTarget()

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget()
            jvm("desktop")

            sourceSets.commonTest.dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
