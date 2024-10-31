import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.io.File
import java.util.*

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compatibility)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
}

group = "ir.mahozad.multiplatform"
version = "2.0.0-beta"

// See https://central.sonatype.com/namespace/org.jetbrains.compose.material
// for the targets that Compose Multiplatform supports
kotlin {
    // Publishes source files; for javadoc/kdoc/dokka see the publications block
    withSourcesJar(publish = true)

    androidTarget { publishLibraryVariants("release") }
    // Windows, Linux, macOS (with Java runtime)
    jvm(name = "desktop")
    // Kotlin/JS drawing to a canvas
    js(compiler = IR) {
        nodejs()
        browser()
        binaries.executable()
    }
    // Kotlin/Wasm drawing to a canvas
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        browser()
        binaries.executable()
    }
    // Building and publishing for iOS target requires a machine running macOS;
    // otherwise, the .klib will not be produced and the compiler warns about that.
    // See https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html#ios-framework
    listOf(
        // By declaring these targets, the iosMain source set will be created automatically
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
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        val desktopMain by getting {}
        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
                // Needed because of build errors; remove if tests run
                implementation(compose.desktop.windows_x64)
            }
        }
        androidMain {}
        androidUnitTest {}
        jsMain {}
        jsTest {}
        iosX64Main {}
        iosArm64Main {}
        iosSimulatorArm64Main {}
    }
}

android {
    namespace = "ir.mahozad.multiplatform.wavyslider"

    defaultConfig {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.get())
    }
}

tasks.withType<PublishToMavenRepository> {
    val isMac = getCurrentOperatingSystem().isMacOsX
    onlyIf {
        isMac.also {
            if (!isMac) logger.error(
                """
                    Publishing the library requires macOS to be able to generate iOS artifacts.
                    Run the task on a mac or use the project GitHub workflows for publication and release.
                """
            )
        }
    }
}

// Custom javadoc that contains Dokka HTML instead of traditional Java HTML
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaGenerate)
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier = "javadoc"
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
        // sourceLink {
        //     // Unix based directory relative path to the root of the project (where you execute gradle respectively)
        //     localDirectory = file("src/commonMain/kotlin/")
        //     // URL showing where the source code can be accessed through the web browser
        //     remoteUrl = uri("https://github.com/mahozad/${project.name}/blob/main/${project.name}/src/main/kotlin")
        //     // Suffix which is used to append the line number to the URL. Use #L for GitHub
        //     remoteLineSuffix = "#L"
        // }
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

val localProperties = Properties().apply {
    rootProject
        .runCatching { file("local.properties") }
        .getOrNull()
        ?.takeIf(File::exists)
        ?.reader()
        ?.use(::load)
}
// For information about signing.* properties,
// see comments on signing { ... } block below
extra["ossrhUsername"] = localProperties["ossrh.username"] as? String
    ?: properties["ossrh.username"] as? String // From gradle.properties in ~/.gradle/ or project root
    ?: System.getenv("OSSRH_USERNAME")
    ?: ""
extra["ossrhPassword"] = localProperties["ossrh.password"] as? String
    ?: properties["ossrh.password"] as? String // From gradle.properties in ~/.gradle/ or project root
    ?: System.getenv("OSSRH_PASSWORD")
    ?: ""
extra["githubToken"] = localProperties["github.token"] as? String
    ?: properties["github.token"] as? String // From gradle.properties in ~/.gradle/ or project root
    ?: System.getenv("GITHUB_TOKEN")
    ?: ""

publishing {
    repositories {
        maven {
            name = "CustomLocal"
            url = uri("file://${layout.buildDirectory.get()}/local-repository")
        }
        maven {
            name = "MavenCentral"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = extra["ossrhUsername"]?.toString()
                password = extra["ossrhPassword"]?.toString()
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mahozad/${project.name}")
            credentials {
                username = "mahozad"
                password = extra["githubToken"]?.toString()
            }
        }
    }
    publications.withType<MavenPublication> {
        // Publishes javadoc/kdoc/dokka; for sources see the kotlin block
        artifact(javadocJar) // Required a workaround. See the below TODO
        pom {
            url = "https://mahozad.ir/${project.name}"
            name = project.name
            description = """
                Animated Material wavy slider and progress bar similar to the one introduced in Android 13 media player.  
                It has curly, wobbly, squiggly, wiggly, jiggly, wriggly, dancing movements.
                Some users call it the sperm. Visit the project on GitHub to learn more.
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
}

// TODO: Remove after https://github.com/gradle/gradle/issues/26091 is fixed
//  Thanks to KSoup repository for this code snippet
tasks.withType(AbstractPublishToMaven::class).configureEach {
    dependsOn(tasks.withType(Sign::class))
}

/*
 * Uses signing.* properties defined in gradle.properties in ~/.gradle/ or project root
 * Can also pass from command line like below:
 * ./gradlew task -Psigning.secretKeyRingFile=... -Psigning.password=... -Psigning.keyId=...
 * See https://docs.gradle.org/current/userguide/signing_plugin.html
 * and https://stackoverflow.com/a/67115705
 */
signing {
    sign(publishing.publications)
}
