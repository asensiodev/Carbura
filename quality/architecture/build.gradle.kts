plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(libs.konsist)
}

tasks.withType<Test>().configureEach {
    workingDir = rootProject.projectDir
}
