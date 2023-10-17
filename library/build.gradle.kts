import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask
import java.util.*

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

buildscript {
    dependencies {
        val dokkaVersion = properties["dokka.version"]
        classpath("org.jetbrains.dokka:dokka-base:$dokkaVersion")
    }
}

group = "ir.mahozad.multiplatform"
version = "0.0.1"

kotlin {
    // See https://central.sonatype.com/namespace/org.jetbrains.compose.material
    // for the targets that Compose Multiplatform supports

    androidTarget() { publishLibraryVariants("release") }
    // Windows, Linux, macOS (with Java runtime)
    jvm(name = "desktop" /* Renames jvm to desktop */)
    js(compiler = IR) {
        nodejs()
        browser()
    }

    //// Native targets:
    // macosX64()
    // macosArm64()
    //// Building for IOS target requires a machine running macOS
    //// See https://kotlinlang.org/docs/multiplatform-mobile-understand-project-structure.html#ios-framework
    // ios {
    //     binaries {
    //         framework {
    //             baseName = "wavy-slider"
    //         }
    //     }
    // }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.foundation)
                api(compose.material)
                api(compose.runtime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {}
        val androidUnitTest by getting {}
        val desktopMain by getting {}
        val desktopTest by getting {}
        val jsMain by getting {}
        val jsTest by getting {}
        // See above
        // val macosArm64Main by getting {}
        // val macosX64Main by getting {}
        // val iosMain by getting {}
        // val iosTest by getting {}
    }
}

android {
    namespace = "ir.mahozad.multiplatform"
    compileSdk = 34
    defaultConfig {
        minSdk = 21
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap(DokkaTask::outputDirectory))
    archiveClassifier = "javadoc"
}

tasks.dokkaHtml {
    // outputDirectory = buildDir.resolve("dokka")
    offlineMode = false
    moduleName = "Wavy Slider"

    // See the buildscript block above and also
    // https://github.com/Kotlin/dokka/issues/2406
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("../assets/logo-icon.svg"))
        customStyleSheets = listOf(file("../assets/logo-styles.css"))
        separateInheritedMembers = true
    }

    dokkaSourceSets {
        configureEach {
            reportUndocumented = true
            noAndroidSdkLink = false
            noStdlibLink = false
            noJdkLink = false
            jdkVersion = JavaVersion.VERSION_17.majorVersion.toInt()
            // sourceLink {
            //     // Unix based directory relative path to the root of the project (where you execute gradle respectively).
            //     // localDirectory.set(file("src/main/kotlin"))
            //     // URL showing where the source code can be accessed through the web browser
            //     // remoteUrl = uri("https://github.com/mahozad/${project.name}/blob/main/${project.name}/src/main/kotlin").toURL()
            //     // Suffix which is used to append the line number to the URL. Use #L for GitHub
            //     remoteLineSuffix = "#L"
            // }
        }
    }
}

fun error(propertyName: String): Nothing = error("Property $propertyName not found")
val properties = Properties().apply { load(rootProject.file("local.properties").reader()) }
extra["ossrhUsername"] = properties["ossrhUsername"] as? String ?: error("ossrhUsername")
extra["ossrhPassword"] = properties["ossrhPassword"] as? String ?: error("ossrhPassword")
extra["githubUsername"] = properties["github.username"] as? String ?: error("githubUsername")
extra["githubPassword"] = properties["github.token"] as? String ?: error("githubPassword")
extra["signingKeyId"] = properties["signing.keyId"] as? String ?: error("signingKeyId")
extra["signingPassword"] = properties["signing.password"] as? String ?: error("signingPassword")
extra["signingSecretKeyRingFile"] = properties["signing.secretKeyRingFile"] as? String ?: error("signingSecretKeyRingFile")

publishing {
    repositories {
        maven {
            name = "CustomLocal"
            url = uri("file://${buildDir}/local-repository")
        }
        maven {
            name = "MavenCentral"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = extra["ossrhUsername"]?.toString()
                password = extra["ossrhPassword"]?.toString()
            }
        }
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mahozad/${project.name}")
            credentials {
                username = extra["githubUsername"]?.toString()
                password = extra["githubPassword"]?.toString()
            }
        }
    }
    publications.withType<MavenPublication> {
        artifact(javadocJar) // Required a workaround. See below
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

// TODO: Remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
//  Thanks to KSoup repository for this code snippet
tasks.withType(AbstractPublishToMaven::class).configureEach {
    dependsOn(tasks.withType(Sign::class))
}

// Uses signing.* properties defined in gradle.properties in home/.gradle/
// See https://stackoverflow.com/a/67115705
signing {
    sign(publishing.publications)
}
