import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm(name = "desktop")
    sourceSets {
        @Suppress("unused")
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(projects.showcase.shared)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "showcase.MainKt"
        buildTypes.release.proguard.isEnabled = false
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "WavySliderShowcase"
            packageVersion = "1.0.0"
        }
    }
}
