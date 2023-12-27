plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
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
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            // api("ir.mahozad.multiplatform:wavy-slider:x.y.z")
            implementation(project(":wavy-slider"))
        }
    }
}

compose.experimental {
    web.application {}
}
