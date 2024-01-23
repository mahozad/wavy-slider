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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The horizontal movement of the whole wave.
 */
enum class WaveMovement(internal inline val factor: (LayoutDirection) -> Int) {
    /**
     * Always move from right to left (regardless of layout direction).
     */
    RTL({ 1 }),
    /**
     * Always move from left to right (regardless of layout direction).
     */
    LTR({ -1 }),
    /**
     * Move away from the thumb (depends on layout direction).
     */
    BACKWARD({ if (it == LayoutDirection.Ltr) 1 else -1 }),
    /**
     * Move toward the thumb (depends on layout direction).
     */
    FORWARD({ if (it == LayoutDirection.Ltr) -1 else 1 }),
    /**
     * Do not move.
     */
    STOPPED({ 0 })
}

/**
 * Custom animation configurations for various properties of the wave.
 *
 * @param waveHeightAnimationSpec used for changes in wave height.
 */
data class WaveAnimationSpecs(
    val waveHeightAnimationSpec: AnimationSpec<Float>
)

internal val defaultIncremental = false
internal val defaultWaveMovement = WaveMovement.BACKWARD
internal val defaultTrackThickness = 4.dp
internal val defaultWaveLength = 20.dp
internal val defaultWaveHeight = 6.dp
internal val defaultWavePeriod = 2.seconds
internal val defaultWaveAnimationSpecs = WaveAnimationSpecs(
    waveHeightAnimationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
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
internal inline fun animatePhaseShiftPx(
    waveLengthPx: Float,
    wavePeriod: Duration,
    waveMovement: WaveMovement
): State<Float> {
    val shift = waveLengthPx * waveMovement.factor(LocalLayoutDirection.current)
    val phaseShiftPxAnimated = remember { mutableFloatStateOf(0f) }
    val phaseShiftPxAnimation = remember(shift, wavePeriod) {
        val wavePeriodAdjusted = wavePeriod.toAdjustedMilliseconds()
        val shiftAdjusted = if (wavePeriodAdjusted == Int.MAX_VALUE) 0f else shift
        TargetBasedAnimation(
            animationSpec = infiniteRepeatable(
                animation = tween(wavePeriodAdjusted, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            typeConverter = Float.VectorConverter,
            // Instead of simply 0 and shift, they are added to current phaseShiftPxAnimated to
            // smoothly continue the wave shift when wavePeriod or waveMovement is changed
            initialValue =             0 + phaseShiftPxAnimated.value,
            targetValue  = shiftAdjusted + phaseShiftPxAnimated.value
        )
    }
    var playTime by remember { mutableStateOf(0L) }
    LaunchedEffect(phaseShiftPxAnimation) {
        val startTime = withFrameNanos { it }
        while (isActive) {
            playTime = withFrameNanos { it } - startTime
            phaseShiftPxAnimated.value = phaseShiftPxAnimation.getValueFromNanos(playTime)
        }
    }
    return phaseShiftPxAnimated
}

private inline fun Duration.toAdjustedMilliseconds() = this
    .absoluteValue
    .inWholeMilliseconds
    .coerceAtMost(Int.MAX_VALUE.toLong())
    .toInt() // Do not call before coercion
    .takeIf { it != 0 }
    ?: Int.MAX_VALUE

@Composable
internal inline fun animateWaveHeightPx(
    waveHeightPx: Float,
    animationSpec: AnimationSpec<Float>
): State<Float> = animateFloatAsState(
    targetValue = waveHeightPx,
    animationSpec = animationSpec
)

internal inline fun DrawScope.drawTrack(
    sliderStart: Offset,
    sliderValueOffset: Offset,
    sliderEnd: Offset,
    waveLengthPx: Float,
    waveHeightPx: Float,
    waveThicknessPx: Float,
    trackThicknessPx: Float,
    phaseShiftPx: Float,
    incremental: Boolean,
    inactiveTrackColor: Color,
    activeTrackColor: Color
) {
    drawTrackActivePart(
        startOffset = sliderStart,
        valueOffset = sliderValueOffset,
        waveLengthPx = waveLengthPx,
        waveHeightPx = waveHeightPx,
        waveThicknessPx = waveThicknessPx,
        phaseShiftPx = phaseShiftPx,
        incremental = incremental,
        color = activeTrackColor
    )
    drawTrackInactivePart(
        color = inactiveTrackColor,
        thicknessPx = trackThicknessPx,
        startOffset = sliderValueOffset,
        endOffset = sliderEnd,
    )
}

private inline fun DrawScope.drawTrackInactivePart(
    color: Color,
    thicknessPx: Float,
    startOffset: Offset,
    endOffset: Offset
) {
    if (thicknessPx <= 0f) return
    drawLine(
        strokeWidth = thicknessPx,
        color = color,
        start = startOffset,
        end = endOffset,
        cap = StrokeCap.Round
    )
}

private inline fun DrawScope.drawTrackActivePart(
    startOffset: Offset,
    valueOffset: Offset,
    waveLengthPx: Float,
    waveHeightPx: Float,
    waveThicknessPx: Float,
    phaseShiftPx: Float,
    incremental: Boolean,
    color: Color
) {
    if (waveThicknessPx <= 0f) return
    val wave = Path().apply {
        if (waveLengthPx == 0f || waveHeightPx == 0f) {
            moveTo(startOffset.x, center.y)
            lineTo(valueOffset.x, center.y)
            return@apply
        }
        val startHeightFactor = if (incremental) 0f else 1f
        val startRadians = (startOffset.x + phaseShiftPx) % waveLengthPx / waveLengthPx * (2 * PI)
        val startY = (sin(startRadians) * startHeightFactor * (waveHeightPx / 2)) + (size.height / 2)
        moveTo(startOffset.x, startY.toFloat())
        val range = if (layoutDirection == LayoutDirection.Rtl) {
            startOffset.x.toInt() downTo valueOffset.x.toInt()
        } else {
            startOffset.x.toInt()..valueOffset.x.toInt()
        }
        for (x in range) {
            val heightFactor = if (incremental) (x - range.first).toFloat() / (range.last - range.first) else 1f
            val radians = (x + phaseShiftPx) % waveLengthPx / waveLengthPx * (2 * PI)
            val y = (sin(radians) * heightFactor * (waveHeightPx / 2)) + (size.height / 2)
            lineTo(x.toFloat(), y.toFloat())
        }
    }
    drawPath(
        path = wave,
        color = color,
        style = Stroke(
            width = waveThicknessPx,
            join = StrokeJoin.Round,
            cap = StrokeCap.Round
        )
    )
}

internal inline fun snapValueToTick(
    current: Float,
    tickFractions: FloatArray,
    minPx: Float,
    maxPx: Float
): Float {
    // target is a closest anchor to the `current`, if exists
    return tickFractions
        .minByOrNull { abs(lerp(minPx, maxPx, it) - current) }
        ?.run { lerp(minPx, maxPx, this) }
        ?: current
}

// Scale x1 from a1..b1 range to a2..b2 range
internal inline fun scale(a1: Float, b1: Float, x1: Float, a2: Float, b2: Float) =
    lerp(a2, b2, calcFraction(a1, b1, x1))

internal inline fun lerp(start: Float, stop: Float, fraction: Float) =
    (start * (1 - fraction) + stop * fraction)

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
internal inline fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)
