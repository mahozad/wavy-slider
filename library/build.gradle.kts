import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compatibility)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven.publish)
}

group = "ir.mahozad.multiplatform"
version = "2.1.0-rc"

kotlin {
    androidTarget { publishLibraryVariants("release") }
    jvm(name = "desktop") // Windows, Linux, macOS (with Java runtime)
    js(compiler = IR) { // Kotlin/JS drawing to a canvas
        nodejs()
        browser()
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { // Kotlin/Wasm drawing to a canvas
        nodejs()
        browser()
        binaries.executable()
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "library"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(compose.foundation)
            api(compose.material3)
            api(compose.material)
            api(compose.runtime)
        }
        @Suppress("unused")
        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
                // Needed because of build errors; TODO: remove if tests run
                implementation(compose.desktop.windows_x64)
            }
        }
    }
}

android {
    namespace = "ir.mahozad.multiplatform.wavyslider"
    defaultConfig {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    }
}

dokka {
    moduleName = "Wavy Slider"
    // TODO: Remove this after https://github.com/Kotlin/dokka/issues/3885 is resolved
    dokkaGeneratorIsolation = ClassLoaderIsolation()
    dokkaSourceSets.configureEach {
        reportUndocumented = true
        enableJdkDocumentationLink = true
        enableAndroidDocumentationLink = true
        enableKotlinStdLibDocumentationLink = true
        jdkVersion = libs.versions.java.get().toInt()
        sourceLink {
            localDirectory = projectDir.resolve("src")
            // URL showing where the source code can be accessed through the web browser
            remoteUrl = uri("https://github.com/mahozad/${project.name}/tree/v$version/library/src")
            // Is used to append the line number to the URL
            remoteLineSuffix = "#L"
        }
    }

    dokkaPublications.html {
        outputDirectory = layout.buildDirectory.get().dir("dokka")
    }

    pluginsConfiguration.html {
        customAssets.from("../asset/logo-icon.svg")
        customStyleSheets.from("../asset/logo-styles.css")
        separateInheritedMembers = true
        footerMessage = "© Wavy slider"
    }
}

publishing {
    repositories {
        maven {
            name = "CustomLocal"
            url = uri("file://${layout.buildDirectory.get()}/local-repository")
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mahozad/${project.name}")
            // username and password/token should be specified as `GitHubPackagesUsername` and `GitHubPackagesPassword`
            // in gradle.properties in ~/.gradle/ or project root or from CLI like ./gradlew task -PexampleProperty=...
            // or with environment variables as `ORG_GRADLE_PROJECT_GitHubPackagesUsername` and `ORG_GRADLE_PROJECT_GitHubPackagesPassword`
            credentials(credentialsType = PasswordCredentials::class)
        }
        // Maven Central Portal is defined below
    }
}

mavenPublishing {
    // GitHub and other Maven repos are defined above
    // Should set Gradle mavenCentralUsername and mavenCentralPassword gradle properties
    publishToMavenCentral(
        host = SonatypeHost.CENTRAL_PORTAL,
        automaticRelease = false
    )

    // For information about signing.* properties, see the gradle.properties file
    signAllPublications()

    coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString()
    )

    pom {
        url = "https://mahozad.ir/${project.name}"
        name = project.name
        description = """
            Animated wavy Material Slider and progress/seek bar similar to the one used in Android 13 media controls.  
            It has curly, wobbly, squiggly, wiggly, jiggly, wriggly, dancing movements.
            Some users call it the sperm. Visit the project on GitHub at https://github.com/mahozad/wavy-slider to learn more.
        """.trimIndent()
        inceptionYear = "2023"
        licenses {
            license {
                name = "Apache-2.0 License"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "mahozad"
                name = "Mahdi Hosseinzadeh"
                url = "https://mahozad.ir/"
                email = ""
                roles = listOf("Lead Developer")
                timezone = "GMT+4:30"
            }
        }
        contributors {
            // contributor {}
        }
        scm {
            tag = "HEAD"
            url = "https://github.com/mahozad/${project.name}"
            connection = "scm:git:github.com/mahozad/${project.name}.git"
            developerConnection = "scm:git:ssh://github.com/mahozad/${project.name}.git"
        }
        issueManagement {
            system = "GitHub"
            url = "https://github.com/mahozad/${project.name}/issues"
        }
        ciManagement {
            system = "GitHub Actions"
            url = "https://github.com/mahozad/${project.name}/actions"
        }
    }
}
