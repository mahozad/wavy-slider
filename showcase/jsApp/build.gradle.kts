plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }
    sourceSets {
        jsMain.dependencies {
            implementation(projects.showcase.shared)
        }
    }
}
