rootProject.name = "wavy-slider-project"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":library")
// We want the project name to be "wavy-slider" but its directory/folder name to be "library".
// If the project name were not set here, it would be published with its directory/folder name ("library")
// (suffixed with proper platform type added by Kotlin Multiplatform).
project(":library").name = "wavy-slider"
include(":website")
// :showcase:iosApp is managed by Xcode and its build system
// See https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html#root-project

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
