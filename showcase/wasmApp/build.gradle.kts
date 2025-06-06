import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets {
        @Suppress("unused")
        val wasmJsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(projects.showcase.shared)
            }
        }
    }
}
