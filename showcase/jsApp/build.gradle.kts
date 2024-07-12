plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js(compiler = IR) {
        browser()
        nodejs()
        binaries.executable()
    }
    sourceSets {
        jsMain.dependencies {
            implementation(compose.html.core)
            implementation(compose.runtime)
            implementation(projects.showcase.shared)
        }
    }
}
