[![Kotlin version]][Kotlin release]
[![Compose Multiplatform version]][Compose Multiplatform release]
[![Latest Maven Central release]][Library on Maven Central]

<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="asset/demo-dark.png">
    <source media="(prefers-color-scheme: light)" srcset="asset/demo-light.png">
    <img alt="Real-world demo" src="asset/demo-light.png">
  </picture>
</div>

# Wavy slider

This is an animated [Material](https://material.io) wavy slider and progress bar similar to the one used in [**Android 13** media controls](https://www.xda-developers.com/android-13-beta-1-media-controls-animation/).  
It has curly, wobbly, squiggly, wiggly, jiggly, wriggly, dancing movements.
Some users call it the **sperm**.

The library can be used in [Jetpack Compose](https://developer.android.com/jetpack/compose) and [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) projects like
a regular Material [Slider](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#Slider(kotlin.Float,kotlin.Function1,androidx.compose.ui.Modifier,kotlin.Boolean,kotlin.ranges.ClosedFloatingPointRange,kotlin.Int,kotlin.Function0,androidx.compose.material3.SliderColors,androidx.compose.foundation.interaction.MutableInteractionSource)).  
Supported target platforms are Android, iOS, Desktop (JVM), and JavaScript (Kotlin/JS).

## Demo
For a live, interactive Web demo go to https://mahozad.ir/wavy-slider.  
For real-world apps in various platforms using the library, see the [showcase](showcase) directory.

## Getting started

For a single-platform project (Android or iOS or Desktop or JS):

```kotlin
dependencies {
    implementation/* OR api */("ir.mahozad.multiplatform:wavy-slider:1.0.0-rc")
}
```

For a multiplatform project (if you target a subset of the library supported platforms):

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation/* OR api */("ir.mahozad.multiplatform:wavy-slider:1.0.0-rc")
        }
// ...
```

If you have targets that are not supported by the library,
add the library separately to each supported target:

```kotlin
kotlin {
    val desktopMain /* OR jvmMain */ by getting {
        dependencies {
            implementation/* OR api */("ir.mahozad.multiplatform:wavy-slider:1.0.0-rc")
        }
    }
    androidMain.dependencies {
        implementation/* OR api */("ir.mahozad.multiplatform:wavy-slider:1.0.0-rc")
    }
    // etc.
```

Using the WavySlider is much like using the Material Slider
(you can even make it a regular flat slider):

```kotlin
import ir.mahozad.multiplatform.wavyslider.material/*OR material3*/.WavySlider
import ir.mahozad.multiplatform.wavyslider.WaveDirection.*

@Composable
fun MyComposable() {
    var fraction by remember { mutableStateOf(0.5f) }
    WavySlider(
        value = fraction,
        onValueChange = { fraction = it },
        waveLength = 16.dp,     // Set this to 0.dp to get a regular Slider
        waveHeight = 16.dp,     // Set this to 0.dp to get a regular Slider
        waveVelocity = 15.dp to HEAD, // Speed per second and direction
        waveThickness = 4.dp,   // Defaults to the track thickness
        trackThickness = 4.dp,  // Defaults to 4.dp, same as regular Slider
        incremental = false,    // Whether to gradually increase waveHeight
        // animationSpecs = ... // Customize animations used for properties 
    )
}
```

## Related
  - Android squiggly progress:
      + <details>
        <summary>Current implementation (as of 2024-02-02)</summary>
        
        ```kotlin
        /*
         * Copyright (C) 2022 The Android Open Source Project
         *
         * Licensed under the Apache License, Version 2.0 (the "License");
         * you may not use this file except in compliance with the License.
         * You may obtain a copy of the License at
         *
         *      http://www.apache.org/licenses/LICENSE-2.0
         *
         * Unless required by applicable law or agreed to in writing, software
         * distributed under the License is distributed on an "AS IS" BASIS,
         * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
         * See the License for the specific language governing permissions and
         * limitations under the License.
         */
        
        package com.android.systemui.media.controls.ui
        
        import android.animation.Animator
        import android.animation.AnimatorListenerAdapter
        import android.animation.ValueAnimator
        import android.content.res.ColorStateList
        import android.graphics.Canvas
        import android.graphics.ColorFilter
        import android.graphics.Paint
        import android.graphics.Path
        import android.graphics.PixelFormat
        import android.graphics.drawable.Drawable
        import android.os.SystemClock
        import android.util.MathUtils.lerp
        import android.util.MathUtils.lerpInv
        import android.util.MathUtils.lerpInvSat
        import androidx.annotation.VisibleForTesting
        import com.android.app.animation.Interpolators
        import com.android.internal.graphics.ColorUtils
        import kotlin.math.abs
        import kotlin.math.cos
        
        private const val TAG = "Squiggly"
        
        private const val TWO_PI = (Math.PI * 2f).toFloat()
        @VisibleForTesting internal const val DISABLED_ALPHA = 77
        
        class SquigglyProgress : Drawable() {
        
            private val wavePaint = Paint()
            private val linePaint = Paint()
            private val path = Path()
            private var heightFraction = 0f
            private var heightAnimator: ValueAnimator? = null
            private var phaseOffset = 0f
            private var lastFrameTime = -1L
        
            /* distance over which amplitude drops to zero, measured in wavelengths */
            private val transitionPeriods = 1.5f
            /* wave endpoint as percentage of bar when play position is zero */
            private val minWaveEndpoint = 0.2f
            /* wave endpoint as percentage of bar when play position matches wave endpoint */
            private val matchedWaveEndpoint = 0.6f
        
            // Horizontal length of the sine wave
            var waveLength = 0f
            // Height of each peak of the sine wave
            var lineAmplitude = 0f
            // Line speed in px per second
            var phaseSpeed = 0f
            // Progress stroke width, both for wave and solid line
            var strokeWidth = 0f
                set(value) {
                    if (field == value) {
                        return
                    }
                    field = value
                    wavePaint.strokeWidth = value
                    linePaint.strokeWidth = value
                }
        
            // Enables a transition region where the amplitude
            // of the wave is reduced linearly across it.
            var transitionEnabled = true
                set(value) {
                    field = value
                    invalidateSelf()
                }
        
            init {
                wavePaint.strokeCap = Paint.Cap.ROUND
                linePaint.strokeCap = Paint.Cap.ROUND
                linePaint.style = Paint.Style.STROKE
                wavePaint.style = Paint.Style.STROKE
                linePaint.alpha = DISABLED_ALPHA
            }
        
            var animate: Boolean = false
                set(value) {
                    if (field == value) {
                        return
                    }
                    field = value
                    if (field) {
                        lastFrameTime = SystemClock.uptimeMillis()
                    }
                    heightAnimator?.cancel()
                    heightAnimator =
                        ValueAnimator.ofFloat(heightFraction, if (animate) 1f else 0f).apply {
                            if (animate) {
                                startDelay = 60
                                duration = 800
                                interpolator = Interpolators.EMPHASIZED_DECELERATE
                            } else {
                                duration = 550
                                interpolator = Interpolators.STANDARD_DECELERATE
                            }
                            addUpdateListener {
                                heightFraction = it.animatedValue as Float
                                invalidateSelf()
                            }
                            addListener(
                                object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        heightAnimator = null
                                    }
                                }
                            )
                            start()
                        }
                }
        
            override fun draw(canvas: Canvas) {
                if (animate) {
                    invalidateSelf()
                    val now = SystemClock.uptimeMillis()
                    phaseOffset += (now - lastFrameTime) / 1000f * phaseSpeed
                    phaseOffset %= waveLength
                    lastFrameTime = now
                }
        
                val progress = level / 10_000f
                val totalWidth = bounds.width().toFloat()
                val totalProgressPx = totalWidth * progress
                val waveProgressPx =
                    totalWidth *
                        (if (!transitionEnabled || progress > matchedWaveEndpoint) progress
                        else
                            lerp(
                                minWaveEndpoint,
                                matchedWaveEndpoint,
                                lerpInv(0f, matchedWaveEndpoint, progress)
                            ))
        
                // Build Wiggly Path
                val waveStart = -phaseOffset - waveLength / 2f
                val waveEnd = if (transitionEnabled) totalWidth else waveProgressPx
        
                // helper function, computes amplitude for wave segment
                val computeAmplitude: (Float, Float) -> Float = { x, sign ->
                    if (transitionEnabled) {
                        val length = transitionPeriods * waveLength
                        val coeff =
                            lerpInvSat(waveProgressPx + length / 2f, waveProgressPx - length / 2f, x)
                        sign * heightFraction * lineAmplitude * coeff
                    } else {
                        sign * heightFraction * lineAmplitude
                    }
                }
        
                // Reset path object to the start
                path.rewind()
                path.moveTo(waveStart, 0f)
        
                // Build the wave, incrementing by half the wavelength each time
                var currentX = waveStart
                var waveSign = 1f
                var currentAmp = computeAmplitude(currentX, waveSign)
                val dist = waveLength / 2f
                while (currentX < waveEnd) {
                    waveSign = -waveSign
                    val nextX = currentX + dist
                    val midX = currentX + dist / 2
                    val nextAmp = computeAmplitude(nextX, waveSign)
                    path.cubicTo(midX, currentAmp, midX, nextAmp, nextX, nextAmp)
                    currentAmp = nextAmp
                    currentX = nextX
                }
        
                // translate to the start position of the progress bar for all draw commands
                val clipTop = lineAmplitude + strokeWidth
                canvas.save()
                canvas.translate(bounds.left.toFloat(), bounds.centerY().toFloat())
        
                // Draw path up to progress position
                canvas.save()
                canvas.clipRect(0f, -1f * clipTop, totalProgressPx, clipTop)
                canvas.drawPath(path, wavePaint)
                canvas.restore()
        
                if (transitionEnabled) {
                    // If there's a smooth transition, we draw the rest of the
                    // path in a different color (using different clip params)
                    canvas.save()
                    canvas.clipRect(totalProgressPx, -1f * clipTop, totalWidth, clipTop)
                    canvas.drawPath(path, linePaint)
                    canvas.restore()
                } else {
                    // No transition, just draw a flat line to the end of the region.
                    // The discontinuity is hidden by the progress bar thumb shape.
                    canvas.drawLine(totalProgressPx, 0f, totalWidth, 0f, linePaint)
                }
        
                // Draw round line cap at the beginning of the wave
                val startAmp = cos(abs(waveStart) / waveLength * TWO_PI)
                canvas.drawPoint(0f, startAmp * lineAmplitude * heightFraction, wavePaint)
        
                canvas.restore()
            }
        
            override fun getOpacity(): Int {
                return PixelFormat.TRANSLUCENT
            }
        
            override fun setColorFilter(colorFilter: ColorFilter?) {
                wavePaint.colorFilter = colorFilter
                linePaint.colorFilter = colorFilter
            }
        
            override fun setAlpha(alpha: Int) {
                updateColors(wavePaint.color, alpha)
            }
        
            override fun getAlpha(): Int {
                return wavePaint.alpha
            }
        
            override fun setTint(tintColor: Int) {
                updateColors(tintColor, alpha)
            }
        
            override fun onLevelChange(level: Int): Boolean {
                return animate
            }
        
            override fun setTintList(tint: ColorStateList?) {
                if (tint == null) {
                    return
                }
                updateColors(tint.defaultColor, alpha)
            }
        
            private fun updateColors(tintColor: Int, alpha: Int) {
                wavePaint.color = ColorUtils.setAlphaComponent(tintColor, alpha)
                linePaint.color =
                    ColorUtils.setAlphaComponent(tintColor, (DISABLED_ALPHA * (alpha / 255f)).toInt())
            }
        }
        ```
        </details>
      + [Main branch](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/packages/SystemUI/src/com/android/systemui/media/controls/ui/SquigglyProgress.kt)
      + [Android 14](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android14-release/packages/SystemUI/src/com/android/systemui/media/controls/ui/SquigglyProgress.kt)
      + [Android 13](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/packages/SystemUI/src/com/android/systemui/media/SquigglyProgress.kt)
      + [Android Music app](https://android.googlesource.com/platform/packages/apps/Music/)
      + [Everything you see in Android that's not an app](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/packages/SystemUI/)
  - Wave slider (Android-only): https://github.com/galaxygoldfish/waveslider
  - Squiggly seekbar (Flutter): https://github.com/hannesgith/squiggly_slider
  - Sliders with custom styles: https://github.com/krottv/compose-sliders
  - Squiggly text underlines: https://github.com/saket/ExtendedSpans
  - StackOverflow posts:
    + [How to create the Android 13 squiggly slider using Jetpack Compose?](https://stackoverflow.com/q/77927207/8583692) 
    + [Squiggly Seekbar with Animation in Flutter](https://stackoverflow.com/q/75889414/8583692)
    + [Is it possible to make a squiggly line?](https://stackoverflow.com/q/17285514/8583692)

[Kotlin version]: https://img.shields.io/badge/Kotlin-1.9.22-303030.svg?labelColor=303030&logo=data:image/svg+xml;base64,PHN2ZyB2ZXJzaW9uPSIxLjEiIHZpZXdCb3g9IjAgMCAxOC45MyAxOC45MiIgd2lkdGg9IjE4IiBoZWlnaHQ9IjE4IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxyYWRpYWxHcmFkaWVudCBpZD0iZ3JhZGllbnQiIHI9IjIxLjY3OSIgY3g9IjIyLjQzMiIgY3k9IjMuNDkzIiBncmFkaWVudFRyYW5zZm9ybT0ibWF0cml4KDEgMCAwIDEgLTQuMTMgLTIuNzE4KSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiPgogICAgPHN0b3Agc3RvcC1jb2xvcj0iI2U0NDg1NyIgb2Zmc2V0PSIuMDAzIi8+CiAgICA8c3RvcCBzdG9wLWNvbG9yPSIjYzcxMWUxIiBvZmZzZXQ9Ii40NjkiLz4KICAgIDxzdG9wIHN0b3AtY29sb3I9IiM3ZjUyZmYiIG9mZnNldD0iMSIvPgogIDwvcmFkaWFsR3JhZGllbnQ+CiAgPHBhdGggZmlsbD0idXJsKCNncmFkaWVudCkiIGQ9Ik0gMTguOTMsMTguOTIgSCAwIFYgMCBIIDE4LjkzIEwgOS4yNyw5LjMyIFoiLz4KPC9zdmc+Cg==
[Compose Multiplatform version]: https://img.shields.io/badge/Compose_Multiplatform-1.5.12-303030.svg?labelColor=303030&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAA7DAAAOwwHHb6hkAAAAGXRFWHRTb2Z0d2FyZQB3d3cuaW5rc2NhcGUub3Jnm+48GgAAAj5JREFUOI2Vk0FIVFEUhv9znBllplBIF7loK1jtJKhFNG/EVtYicNkmKghCMpJGq0HoPcWQVi2KUMqdixaJi0KdXVBILQojs4wCaTGC4LyX+N47fwtFpnEKOnDh3p//fudeDr+QRK3KukGHCscAwCjXi4PphVo+qQZkhzaa61J6m8RhAfpisS01HQOwZin0F29kftYEdDxCsqnkX6HgIonR+YHM00pjzg26oXRBPrNw30ixgM1dgDMcnFFyyIAphpn7xQI2Tw6XW5LQO0L+isPQKxaa1rNDaJCkf02BHhMpzOfTzxUA1GyCxEcFxjcOIu50/b4kZQnkZQJ9mkwuOV5wqaUdYSIhTwBZFto4AOj2R+S7qEwZMNtU8lcoGAPximZHDegAsCjgw7XP/rJFnDHBhEB+AABIIueW35FEdsQ/67hl5jz/AklUrpxX7nfcMp27wYnKO/rHCAwhANDkffW4DPJhZxtV6lpt/N+qCRCND+3RDHs0AEhUHii6KIxXSZnq9PxJTUhetrQ+VrsH4TlAvlgUfd3zAgMau0aD1uLNhm8WBm0CjBDoiSN8ijReJHBaRAYtTB8pFvaXukaDVgMadwFC6bWIM47n54GWaHYgM5CwunaASwBe1yXQNptPewDgeH7eIs4IpXcXMDeYnl5vzhxTINCUv+B4/vkXtxpWQEwK8Phlf3o15wbdmvLfCFgfh5njc4Pp6e3mVWHqHN44AOidnTC9NVpJRE+BKP0zTNW1HWc8IMxIvfq3OP8GvjkzgYHHZZMAAAAASUVORK5CYII=
[Latest Maven Central release]: https://img.shields.io/maven-central/v/ir.mahozad.multiplatform/wavy-slider?label=Maven%20Central&labelColor=303030&logo=data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZlcnNpb249IjEuMSIgdmlld0JveD0iMCAwIDE2IDE2IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciPgogIDxkZWZzPgogICAgPHN0eWxlPi5he2ZpbGw6bm9uZTt9LmJ7Y2xpcC1wYXRoOnVybCgjYSk7fS5je2ZpbGw6I2ZmZjt9PC9zdHlsZT4KICAgIDxjbGlwUGF0aCBpZD0iYSI+CiAgICAgIDxyZWN0IGNsYXNzPSJhIiB4PSIxNC43IiB5PSIxMSIgd2lkdGg9IjE3MSIgaGVpZ2h0PSIxNTEiLz4KICAgIDwvY2xpcFBhdGg+CiAgICA8Y2xpcFBhdGggaWQ9ImNsaXBQYXRoMTMiPgogICAgICA8cmVjdCBjbGFzcz0iYSIgeD0iMTQuNyIgeT0iMTEiIHdpZHRoPSIxNzEiIGhlaWdodD0iMTUxIi8+CiAgICA8L2NsaXBQYXRoPgogIDwvZGVmcz4KICA8cGF0aCBjbGFzcz0iYyIgdHJhbnNmb3JtPSJtYXRyaXgoLjE2NCAwIDAgLjE2NCAtOC4zNyAtMS44MSkiIGQ9Im0xMDAgMTEtNDIuMyAyNC40djQ4LjlsNDIuMyAyNC40IDQyLjMtMjQuNHYtNDguOXptMzAuMiA2Ni4zLTMwLjIgMTcuNC0zMC4yLTE3LjR2LTM0LjlsMzAuMi0xNy40IDMwLjIgMTcuNHoiIGNsaXAtcGF0aD0idXJsKCNjbGlwUGF0aDEzKSIvPgo8L3N2Zz4K
[Kotlin release]: https://github.com/JetBrains/kotlin/releases/tag/v1.9.22
[Compose Multiplatform release]: https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.5.12
[Library on Maven Central]: https://repo1.maven.org/maven2/ir/mahozad/multiplatform/wavy-slider/1.0.0-rc/
