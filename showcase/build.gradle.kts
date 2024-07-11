plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js(compiler = IR) {
        moduleName = "app"
        browser {
            commonWebpackConfig {
                outputFileName = "app.js"
            }
        }
        nodejs()
        binaries.executable()
    }
    sourceSets {
        jsMain.dependencies {
            implementation(compose.html.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.components.resources)
            implementation(project(":wavy-slider"))
        }
    }
}

compose.resources {
    generateResClass = always
    packageOfResClass = "ir.mahozad.wavyslider"
}
