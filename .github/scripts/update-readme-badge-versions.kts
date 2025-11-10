#!/usr/bin/env kotlin

import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.io.path.writeLines

val mutableReadmeLines = Path("README.md").readLines().toMutableList()
val libsVersionsLines = Path("gradle/libs.versions.toml").readLines()
val newKotlinVersion = libsVersionsLines.first { "kotlin = " in it }.split('"')[1]
val newCmpVersion = libsVersionsLines.first { "compose-multiplatform = " in it }.split('"')[1]
val kotlinBadge = mutableReadmeLines.single { it.startsWith("[Kotlin badge]:") }
val kotlinLink = mutableReadmeLines.single { it.startsWith("[Kotlin release]:") }
val cmpBadge = mutableReadmeLines.single { it.startsWith("[Compose Multiplatform badge]:") }
val cmpLink = mutableReadmeLines.single { it.startsWith("[Compose Multiplatform release]:") }
val newKotlinBadge = kotlinBadge.replace(Regex("""-.+-"""), "-$newKotlinVersion-")
val newKotlinLink = kotlinLink.replace(Regex("""v.+"""), "v$newKotlinVersion")
val newCmpBadge = cmpBadge.replace(Regex("""-.+-"""), "-$newCmpVersion-")
val newCmpLink = cmpLink.replace(Regex("""v.+"""), "v$newCmpVersion")

mutableReadmeLines[mutableReadmeLines.indexOf(kotlinBadge)] = newKotlinBadge
mutableReadmeLines[mutableReadmeLines.indexOf(kotlinLink)] = newKotlinLink
mutableReadmeLines[mutableReadmeLines.indexOf(cmpBadge)] = newCmpBadge
mutableReadmeLines[mutableReadmeLines.indexOf(cmpLink)] = newCmpLink

Path("README.md").writeLines(mutableReadmeLines)
