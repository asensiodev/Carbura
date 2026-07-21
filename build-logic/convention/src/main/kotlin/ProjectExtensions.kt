import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.versionInt(name: String): Int =
    libs.findVersion(name).get().requiredVersion.toInt()

internal fun Project.versionString(name: String): String =
    libs.findVersion(name).get().requiredVersion

internal fun Project.generatedNamespace(): String =
    "com.asensiodev.carbura" + path
        .split(":")
        .filter { it.isNotBlank() }
        .joinToString(separator = "", prefix = ".") { segment ->
            segment.replace("-", "")
        }

internal fun Project.configureKotlinJvmTarget() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(versionString("java")))
        }
    }
}

internal fun Project.configureAndroidApplication() {
    extensions.configure<ApplicationExtension> {
        namespace = "com.asensiodev.carbura"
        compileSdk = versionInt("compileSdk")

        defaultConfig {
            applicationId = "com.asensiodev.carbura"
            minSdk = versionInt("minSdk")
            targetSdk = versionInt("targetSdk")
            versionCode = 1
            versionName = "0.1.0"
        }

        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(versionString("java"))
            targetCompatibility = JavaVersion.toVersion(versionString("java"))
        }
    }
}

internal fun Project.configureAndroidLibrary() {
    extensions.configure<LibraryExtension> {
        namespace = generatedNamespace()
        compileSdk = versionInt("compileSdk")

        defaultConfig {
            minSdk = versionInt("minSdk")
        }

        compileOptions {
            sourceCompatibility = JavaVersion.toVersion(versionString("java"))
            targetCompatibility = JavaVersion.toVersion(versionString("java"))
        }
    }
}
