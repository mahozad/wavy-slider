plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvm(name = "desktop")
    sourceSets {
        commonMain.dependencies {
            implementation(projects.wavySlider)
            implementation(compose.desktop.currentOs)
        }
    }
}
