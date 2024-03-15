package ir.mahozad.multiplatform.wavyslider

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.WaveDirection.TAIL
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sin

/**
 * The horizontal movement (shift) of the whole wave.
 */
enum class WaveDirection(internal inline val factor: (LayoutDirection) -> Float) {
    /**
     * Always shift toward left (regardless of layout direction).
     */
    LEFT({ 1f }),
    /**
     * Always shift toward right (regardless of layout direction).
     */
    RIGHT({ -1f }),
    /**
     * Shift toward the start (depends on layout direction).
     */
    TAIL({ if (it == LayoutDirection.Ltr) 1f else -1f }),
    /**
     * Shift toward the thumb (depends on layout direction).
     */
    HEAD({ if (it == LayoutDirection.Ltr) -1f else 1f })
}

/**
 * Custom animation configurations for various properties of the wave.
 *
 * @param waveHeightAnimationSpec used for **changes** in wave height.
 * @param waveVelocityAnimationSpec used for **changes** in wave velocity (whether in speed or direction).
 */
/*
 * This class enables clients of library to specify custom animation specs for changes in certain properties.
 * This way, they do not have to animate those properties themselves (for example, using animateDpAsState()).
 * More importantly, it enables us to provide a default animation for changes of those properties even
 * if client of library is not aware or does not care about making the changes animated/graceful/gradual.
 * If they want to make the changes immediate/abrupt/sudden, they can simply pass a snap() animation spec.
 */
data class WaveAnimationSpecs(
    /**
     * Used for **changes** in wave height.
     */
    val waveHeightAnimationSpec: AnimationSpec<Dp>,
    /**
     * Used for **changes** in wave velocity (whether in speed or direction).
     */
    val waveVelocityAnimationSpec: AnimationSpec<Dp>
)

/**
 * A better and more clear type name for wave velocity.
 *
 * This improves code readability for function parameters or variable declarations.
 * For example:
 * ```kotlin
 * import ir.mahozad.multiplatform.wavyslider.WaveVelocity
 *
 * fun doSomething(
 *  // velocity: Pair<Dp, WaveDirection>
 *     velocity: WaveVelocity
 * ) {
 *  // var newVelocity: Pair<Dp, WaveDirection>? = null
 *     var newVelocity: WaveVelocity? = null
 *     newVelocity = 10.dp to TAIL // OR WaveVelocity(10.dp, TAIL)
 * }
 * ```
 */
/*
 * For wave velocity, the existing "Pair" class of Kotlin stdlib is used along with the below alias for it.
 * As an example, Ktor has also done this kind of thing pervasively:
 *   https://github.com/search?q=repo%3Aktorio%2Fktor%20public%20typealias&type=code
 * A benefit of using "Pair" is that in Kotlin, any object (including "Dp") has the infix extension function "to"
 * which makes it easy and more readable to create "Pair"s (including creating instances of our wave velocity).
 * Another alternative implementation would be the following:
 *   data class WaveVelocity(val speed: Dp, val direction: WaveDirection)
 *   infix fun Dp.to/* OR toward */(that: WaveDirection) = WaveVelocity(this, that)
 * Advantages would be that instead of waveVelocity.first/.second, waveVelocity.speed/.direction could be used,
 * and it would probably make it easier to change only one property of the default wave velocity (using copy method).
 * Downsides would be that in addition to importing ir.mahozad.multiplatform.wavyslider.WaveVelocity,
 * the client of library would also have to import ir.mahozad.multiplatform.wavyslider.to/toward
 * and the IDE completion on "Dp" would be polluted by our new "to/toward" function
 * even if the client wanted to access something else on "Dp".
 * Note that we could have just omitted declaring this. In that case, if the clients wanted to
 * improve their code readability, they could have introduced this alias themselves.
 * But, our own code has also used this alias. So, keeping it seems a good idea.
 */
typealias WaveVelocity = Pair<Dp, WaveDirection>

internal val defaultIncremental = false
internal val defaultTrackThickness = 4.dp
internal val defaultWaveLength = 20.dp
internal val defaultWaveHeight = 6.dp
internal val defaultWaveVelocity = 10.dp to TAIL
internal val defaultWaveAnimationSpecs = WaveAnimationSpecs(
    waveHeightAnimationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
    waveVelocityAnimationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing),
)

internal expect val KeyEvent.isDirectionUp: Boolean
internal expect val KeyEvent.isDirectionDown: Boolean
internal expect val KeyEvent.isDirectionRight: Boolean
internal expect val KeyEvent.isDirectionLeft: Boolean
internal expect val KeyEvent.isHome: Boolean
internal expect val KeyEvent.isMoveEnd: Boolean
internal expect val KeyEvent.isPgUp: Boolean
internal expect val KeyEvent.isPgDn: Boolean

@Composable
internal inline fun animateWaveShift(
    waveVelocity: WaveVelocity,
    animationSpec: AnimationSpec<Dp>
): State<Dp> {
    val shift = remember { mutableStateOf(0.dp) }
    val speed = waveVelocity.first.coerceAtLeast(0.dp)
    val factor = waveVelocity.second.factor(LocalLayoutDirection.current)
    val amount by animateDpAsState(speed * factor, animationSpec)
    LaunchedEffect(waveVelocity, LocalLayoutDirection.current) {
        val startShift = shift.value
        val startTime = withFrameNanos { it }
        while (true /* Android itself uses true instead of isActive */) {
            val playTime = (withFrameNanos { it } - startTime) / 1_000_000_000f
            shift.value = startShift + (amount * playTime)
        }
    }
    return shift
}

@Composable
internal inline fun animateWaveHeight(
    waveHeight: Dp,
    animationSpec: AnimationSpec<Dp>
): State<Dp> = animateDpAsState(
    targetValue = waveHeight,
    animationSpec = animationSpec
)

internal inline fun DrawScope.drawTrack(
    sliderStart: Offset,
    sliderValueOffset: Offset,
    sliderEnd: Offset,
    waveLength: Dp,
    waveHeight: Dp,
    waveShift: Dp,
    waveThickness: Dp,
    trackThickness: Dp,
    incremental: Boolean,
    inactiveTrackColor: Color,
    activeTrackColor: Color
) {
    drawTrackActivePart(
        startOffset = sliderStart,
        valueOffset = sliderValueOffset,
        waveLength = waveLength,
        waveHeight = waveHeight,
        waveShift = waveShift,
        waveThickness = waveThickness,
        incremental = incremental,
        color = activeTrackColor
    )
    drawTrackInactivePart(
        color = inactiveTrackColor,
        thickness = trackThickness,
        startOffset = sliderValueOffset,
        endOffset = sliderEnd,
    )
}

private inline fun DrawScope.drawTrackInactivePart(
    color: Color,
    thickness: Dp,
    startOffset: Offset,
    endOffset: Offset
) {
    if (thickness <= 0.dp) return
    drawLine(
        strokeWidth = thickness.toPx(),
        color = color,
        start = startOffset,
        end = endOffset,
        cap = StrokeCap.Round
    )
}

private inline fun DrawScope.drawTrackActivePart(
    startOffset: Offset,
    valueOffset: Offset,
    waveLength: Dp,
    waveHeight: Dp,
    waveShift: Dp,
    waveThickness: Dp,
    incremental: Boolean,
    color: Color
) {
    if (waveThickness <= 0.dp) return
    val path = if (waveLength <= 0.dp || waveHeight == 0.dp) {
        createFlatPath(startOffset, valueOffset)
    } else {
        createWavyPath(startOffset, valueOffset, waveLength, waveHeight, waveShift, incremental)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = waveThickness.toPx(),
            join = StrokeJoin.Round,
            cap = StrokeCap.Round
        )
    )
}

private inline fun DrawScope.createFlatPath(
    startOffset: Offset,
    valueOffset: Offset
): Path = Path().apply {
    moveTo(startOffset.x, center.y)
    lineTo(valueOffset.x, center.y)
}

private inline fun DrawScope.createWavyPath(
    startOffset: Offset,
    valueOffset: Offset,
    waveLength: Dp,
    waveHeight: Dp,
    waveShift: Dp,
    incremental: Boolean
): Path = Path().apply {
    val waveShiftPx = waveShift.toPx()
    val waveLengthPx = waveLength.toPx()
    val waveHeightPx = waveHeight.toPx().absoluteValue
    val startHeightFactor = if (incremental) 0f else 1f
    val startRadians = (startOffset.x + waveShiftPx) / waveLengthPx * (2 * PI)
    val startY = (sin(startRadians) * startHeightFactor * waveHeightPx + size.height) / 2
    moveTo(startOffset.x, startY.toFloat())
    val range = if (layoutDirection == LayoutDirection.Rtl) {
        startOffset.x.toInt() downTo valueOffset.x.toInt()
    } else {
        startOffset.x.toInt()..valueOffset.x.toInt()
    }
    for (x in range) {
        val heightFactor = if (incremental) (x - range.first).toFloat() / (range.last - range.first) else 1f
        val radians = (x + waveShiftPx) / waveLengthPx * (2 * PI)
        val y = (sin(radians) * heightFactor * waveHeightPx + size.height) / 2
        lineTo(x.toFloat(), y.toFloat())
    }
}

// Scale x1 from a1..b1 range to a2..b2 range
internal inline fun scale(a1: Float, b1: Float, x1: Float, a2: Float, b2: Float) =
    lerp(a2, b2, calcFraction(a1, b1, x1))

internal inline fun lerp(start: Float, stop: Float, fraction: Float) =
    (start * (1 - fraction) + stop * fraction)

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
internal inline fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

/*
For links to source code of the original squiggly progress in Android OS, see the main README file.

Also, for source code of default Android apps (for example Music app),
see https://android.googlesource.com/platform/packages/apps/Music/

And, for source code of everything visible in Android that's not an app,
see https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/packages/SystemUI/

Here is the implementation in Android as of 2024-02-02:

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
*/
