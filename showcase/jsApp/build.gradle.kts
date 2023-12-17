plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
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
            implementation(project(":showcase:shared"))
        }
    }
}

compose.experimental {
    web.application {}
}
