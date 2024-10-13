[![Kotlin version]][Kotlin release]
[![Compose Multiplatform version]][Compose Multiplatform release]
[![Latest Maven Central release]][Library on Maven Central]

<br>

<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="asset/demo-dark.png">
    <source media="(prefers-color-scheme: light)" srcset="asset/demo-light.png">
    <img alt="Real-world demo" src="asset/demo-light.png">
  </picture>
</div>

# Wavy slider

Animated [Material](https://material.io) wavy slider and progress/seek bar similar to the one used in [**Android 13** media controls](https://www.xda-developers.com/android-13-beta-1-media-controls-animation/).  
It has curly, wobbly, squiggly, wiggly, jiggly, wriggly, dancing movements.
Some users call it the **sperm**.

The library can be used in [Jetpack Compose](https://developer.android.com/jetpack/compose) and [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) projects like
a regular Material [Slider](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#Slider(kotlin.Float,kotlin.Function1,androidx.compose.ui.Modifier,kotlin.Boolean,kotlin.ranges.ClosedFloatingPointRange,kotlin.Int,kotlin.Function0,androidx.compose.material3.SliderColors,androidx.compose.foundation.interaction.MutableInteractionSource)).  
Supported target platforms are Android, iOS, Desktop (JVM), and JavaScript (Kotlin/JS and Kotlin/Wasm).

## Demo
For a live, interactive Web demo go to https://mahozad.ir/wavy-slider.  
For real-world apps in various platforms using the library, see the [showcase](showcase) directory.

## Getting started

```kotlin
implementation("ir.mahozad.multiplatform:wavy-slider:2.0.0-beta")
```

<details>

<summary>Setup for multiplatform projects</summary>

If you target a subset of the library supported platforms, add the library to your common source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation/* OR api */("ir.mahozad.multiplatform:wavy-slider:2.0.0-beta")
            // ...
        }
// ...
```

If you have targets that are not supported by the library,
add the library separately to each supported target:

```kotlin
kotlin {
    val desktopMain /* OR jvmMain */ by getting {
        dependencies {
            implementation/* OR api */("ir.mahozad.multiplatform:wavy-slider:2.0.0-beta")
            // ...
        }
    }
    androidMain.dependencies {
        implementation/* OR api */("ir.mahozad.multiplatform:wavy-slider:2.0.0-beta")
        // ...
    }
    // Other targets...
```

</details>

Using the WavySlider is much like using the Material Slider
(you can even make it a regular flat Slider):

```kotlin
import ir.mahozad.multiplatform.wavyslider.material/*OR material3*/.WavySlider
import ir.mahozad.multiplatform.wavyslider.WaveDirection.*

@Composable
fun MyComposable() {
    var fraction by remember { mutableStateOf(0.5f) }
    WavySlider(
        value = fraction,
        onValueChange = { fraction = it },
        waveLength = 16.dp,     // Set this to 0.dp to make the Slider flat
        waveHeight = 16.dp,     // Set this to 0.dp to make the Slider flat
        waveVelocity = 15.dp to HEAD, // Speed per second and its direction
        waveThickness = 4.dp,   // Defaults to 4.dp irregardless of variant
        trackThickness = 4.dp,  // Defaults to a thickness based on variant
        incremental = false,    // Whether to gradually increase waveHeight
        // animationSpecs = ... // Customize various animations of the wave
    )
}
```

## Related
  - AOSP native squiggly progress: [Main branch][Android main branch implementation] ❖ [Android 14][Android 14 branch implementation] ❖ [Android 13][Android 13 branch implementation]
  - LinearWavyProgressIndicator (available since [Material 3 v1.4.0-alpha01][Material 3 v1.4.0-alpha01])
  - Squiggly slider (Android-only): https://github.com/saket/squiggly-slider
  - Wave slider (Android-only): https://github.com/galaxygoldfish/waveslider
  - Squiggly seekbar (Flutter): https://github.com/hannesgith/squiggly_slider
  - Sliders with custom styles: https://github.com/krottv/compose-sliders
  - Customizable seeker/slider: https://github.com/2307vivek/Seeker
  - Squiggly text underlines: https://github.com/saket/ExtendedSpans
  - Waveform seekbar: https://github.com/massoudss/waveformSeekBar
  - Colorful sliders: https://github.com/SmartToolFactory/Compose-Colorful-Sliders
  - StackOverflow posts:
    + [How to create the Android 13 squiggly slider using Jetpack Compose?](https://stackoverflow.com/q/77927207/8583692) 
    + [Implement new slider in Android 13 media player via Jetpack Compose](https://stackoverflow.com/q/75268182/8583692)
    + [Wave like seek bar for music player app in android jetpack compose](https://stackoverflow.com/q/77661902/8583692)
    + [Squiggly Seekbar with Animation in Flutter](https://stackoverflow.com/q/75889414/8583692)
    + [Is it possible to make a squiggly line?](https://stackoverflow.com/q/17285514/8583692)

[Kotlin version]: https://img.shields.io/badge/Kotlin-2.0.21-303030.svg?labelColor=303030&logo=data:image/svg+xml;base64,PHN2ZyB2ZXJzaW9uPSIxLjEiIHZpZXdCb3g9IjAgMCAxOC45MyAxOC45MiIgd2lkdGg9IjE4IiBoZWlnaHQ9IjE4IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxyYWRpYWxHcmFkaWVudCBpZD0iZ3JhZGllbnQiIHI9IjIxLjY3OSIgY3g9IjIyLjQzMiIgY3k9IjMuNDkzIiBncmFkaWVudFRyYW5zZm9ybT0ibWF0cml4KDEgMCAwIDEgLTQuMTMgLTIuNzE4KSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPgogICAgPHN0b3Agc3RvcC1jb2xvcj0iI2U0NDg1NyIgb2Zmc2V0PSIuMDAzIi8+CiAgICA8c3RvcCBzdG9wLWNvbG9yPSIjYzcxMWUxIiBvZmZzZXQ9Ii40NjkiLz4KICAgIDxzdG9wIHN0b3AtY29sb3I9IiM3ZjUyZmYiIG9mZnNldD0iMSIvPgogIDwvcmFkaWFsR3JhZGllbnQ+CiAgPHBhdGggZmlsbD0idXJsKCNncmFkaWVudCkiIGQ9Ik0gMTguOTMsMTguOTIgSCAwIFYgMCBIIDE4LjkzIEwgOS4yNyw5LjMyIFoiLz4KPC9zdmc+Cg==
[Compose Multiplatform version]: https://img.shields.io/badge/Compose_Multiplatform-1.7.0%E2%80%93rc01-303030.svg?labelColor=303030&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAA7DAAAOwwHHb6hkAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAAj5JREFUOI2Vk0FIVFEUhv9znBllplBIF7loK1jtJKhFNG/EVtYicNkmKghCMpJGq0HoPcWQVi2KUMqdixaJi0KdXVBILQojs4wCaTGC4LyX+N47fwtFpnEKOnDh3p//fudeDr+QRK3KukGHCscAwCjXi4PphVo+qQZkhzaa61J6m8RhAfpisS01HQOwZin0F29kftYEdDxCsqnkX6HgIonR+YHM00pjzg26oXRBPrNw30ixgM1dgDMcnFFyyIAphpn7xQI2Tw6XW5LQO0L+isPQKxaa1rNDaJCkf02BHhMpzOfTzxUA1GyCxEcFxjcOIu50/b4kZQnkZQJ9mkwuOV5wqaUdYSIhTwBZFto4AOj2R+S7qEwZMNtU8lcoGAPximZHDegAsCjgw7XP/rJFnDHBhEB+AABIIueW35FEdsQ/67hl5jz/AklUrpxX7nfcMp27wYnKO/rHCAwhANDkffW4DPJhZxtV6lpt/N+qCRCND+3RDHs0AEhUHii6KIxXSZnq9PxJTUhetrQ+VrsH4TlAvlgUfd3zAgMau0aD1uLNhm8WBm0CjBDoiSN8ijReJHBaRAYtTB8pFvaXukaDVgMadwFC6bWIM47n54GWaHYgM5CwunaASwBe1yXQNptPewDgeH7eIs4IpXcXMDeYnl5vzhxTINCUv+B4/vkXtxpWQEwK8Phlf3o15wbdmvLfCFgfh5njc4Pp6e3mVWHqHN44AOidnTC9NVpJRE+BKP0zTNW1HWc8IMxIvfq3OP8GvjkzgYHHZZMAAAAASUVORK5CYII=
[Latest Maven Central release]: https://img.shields.io/maven-central/v/ir.mahozad.multiplatform/wavy-slider?label=Maven%20Central&labelColor=303030&logo=data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZlcnNpb249IjEuMSIgdmlld0JveD0iMCAwIDE2IDE2IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxkZWZzPgogICAgPHN0eWxlPi5he2ZpbGw6bm9uZTt9LmJ7Y2xpcC1wYXRoOnVybCgjYSk7fS5je2ZpbGw6I2ZmZjt9PC9zdHlsZT4KICAgIDxjbGlwUGF0aCBpZD0iYSI+CiAgICAgIDxyZWN0IGNsYXNzPSJhIiB4PSIxNC43IiB5PSIxMSIgd2lkdGg9IjE3MSIgaGVpZ2h0PSIxNTEiLz4KICAgIDwvY2xpcFBhdGg+CiAgICA8Y2xpcFBhdGggaWQ9ImNsaXBQYXRoMTMiPgogICAgICA8cmVjdCBjbGFzcz0iYSIgeD0iMTQuNyIgeT0iMTEiIHdpZHRoPSIxNzEiIGhlaWdodD0iMTUxIi8+CiAgICA8L2NsaXBQYXRoPgogIDwvZGVmcz4KICA8cGF0aCBjbGFzcz0iYyIgdHJhbnNmb3JtPSJtYXRyaXgoLjE2NCAwIDAgLjE2NCAtOC4zNyAtMS44MSkiIGQ9Im0xMDAgMTEtNDIuMyAyNC40djQ4LjlsNDIuMyAyNC40IDQyLjMtMjQuNHYtNDguOXptMzAuMiA2Ni4zLTMwLjIgMTcuNC0zMC4yLTE3LjR2LTM0LjlsMzAuMi0xNy40IDMwLjIgMTcuNHoiIGNsaXAtcGF0aD0idXJsKCNjbGlwUGF0aDEzKSIvPgo8L3N2Zz4K
[Kotlin release]: https://github.com/JetBrains/kotlin/releases/tag/v2.0.21
[Compose Multiplatform release]: https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.7.0-rc01
[Library on Maven Central]: https://repo1.maven.org/maven2/ir/mahozad/multiplatform/wavy-slider/2.0.0-beta/
[Android main branch implementation]: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/packages/SystemUI/src/com/android/systemui/media/controls/ui/drawable/SquigglyProgress.kt
[Android 14 branch implementation]: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android14-release/packages/SystemUI/src/com/android/systemui/media/controls/ui/SquigglyProgress.kt
[Android 13 branch implementation]: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/packages/SystemUI/src/com/android/systemui/media/SquigglyProgress.kt
[Material 3 v1.4.0-alpha01]: https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/WavyProgressIndicator.kt#L132
