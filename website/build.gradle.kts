import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "app"
        binaries.executable()
        browser {
            commonWebpackConfig {
                outputFileName = "app.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    // Serves source files to be able to debug inside browser
                    static(project.projectDir.path)
                }
            }
        }
    }
    sourceSets {
        wasmJsMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.components.resources)
            implementation(projects.wavySlider)
        }
    }
}

compose.resources {
    generateResClass = always
    packageOfResClass = "ir.mahozad.wavyslider"
}
