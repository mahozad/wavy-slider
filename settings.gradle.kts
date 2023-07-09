rootProject.name = "wavy-slider"

include(":library")
project(":library").name = "wavy-slider"
include(":showcase:shared")
include(":showcase:androidApp")
include(":showcase:desktopApp")
include(":showcase:jsApp")
// :showcase:iosApp is managed by Xcode and its build system
// See https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html#root-project

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        val composeVersion = extra["compose.version"] as String
        val agpVersion = extra["agp.version"] as String
        val dokkaVersion = extra["dokka.version"] as String

        kotlin("multiplatform").version(kotlinVersion)
        id("org.jetbrains.compose").version(composeVersion)
        id("com.android.application").version(agpVersion)
        id("com.android.library").version(agpVersion)
        id("org.jetbrains.dokka").version(dokkaVersion)
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
