import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.asensiodev.carbura.buildlogic.convention"

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.get()))
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "com.asensiodev.carbura.convention.android-application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "com.asensiodev.carbura.convention.android-application-compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("kotlinMultiplatformLibrary") {
            id = "com.asensiodev.carbura.convention.kotlin-multiplatform-library"
            implementationClass = "KotlinMultiplatformLibraryConventionPlugin"
        }
        register("kotlinMultiplatformCompose") {
            id = "com.asensiodev.carbura.convention.kotlin-multiplatform-compose"
            implementationClass = "KotlinMultiplatformComposeConventionPlugin"
        }
        register("kotlinSerialization") {
            id = "com.asensiodev.carbura.convention.kotlin-serialization"
            implementationClass = "KotlinSerializationConventionPlugin"
        }
    }
}
