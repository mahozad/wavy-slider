plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

kotlin {
    js(compiler = IR) {
        browser()
        nodejs()
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
                implementation(compose.runtime)
                implementation(project(":showcase:shared"))
            }
        }
    }
}

compose.experimental {
    web.application {}
}
