rootProject.name = "wavy-slider-project"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":library")
project(":library").name = "wavy-slider"
include(":showcase:shared")
include(":showcase:androidApp")
include(":showcase:desktopApp")
include(":showcase:wasmApp")
include(":showcase:jsApp")
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
