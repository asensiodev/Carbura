pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "Carbura"

include(":app:android")
include(":app:shared")
include(":app:desktop")

include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:auth")
include(":core:designsystem")
include(":core:string-resources")
include(":core:testing")

include(":feature:onboarding")
include(":feature:garage")
include(":feature:maintenance")
include(":feature:reminders")

include(":quality:architecture")
