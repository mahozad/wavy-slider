plugins {
    // This is necessary to avoid the plugins to be loaded
    // multiple times in each subproject's classloader
    kotlin("multiplatform").apply(false)
    id("org.jetbrains.compose").apply(false)
    id("com.android.application").apply(false)
    id("com.android.library").apply(false)
    id("org.jetbrains.dokka").apply(false)
}

tasks.wrapper {
    gradleVersion = properties["gradle.version"] as String
    networkTimeout = 60_000 // milliseconds
    distributionType = Wrapper.DistributionType.ALL
    validateDistributionUrl = false
}
