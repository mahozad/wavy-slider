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
            // Needed because of build errors; TODO: remove if the app runs successfully
            implementation(compose.desktop.windows_x64)
        }
    }
}
