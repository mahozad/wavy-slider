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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The horizontal movement of the whole wave.
 */
enum class WaveMovement(internal inline val factor: (LayoutDirection) -> Float) {
    /**
     * Always move from right to left (regardless of layout direction).
     */
    RTL({ 1f }),
    /**
     * Always move from left to right (regardless of layout direction).
     */
    LTR({ -1f }),
    /**
     * Move away from the thumb (depends on layout direction).
     */
    BACKWARD({ if (it == LayoutDirection.Ltr) 1f else -1f }),
    /**
     * Move toward the thumb (depends on layout direction).
     */
    FORWARD({ if (it == LayoutDirection.Ltr) -1f else 1f }),
    /**
     * Do not move.
     */
    STOPPED({ 0f })
}

/**
 * Custom animation configurations for various properties of the wave.
 *
 * @param waveHeightAnimationSpec used for changes in wave height.
 */
data class WaveAnimationSpecs(
    val waveHeightAnimationSpec: AnimationSpec<Dp>
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
internal inline fun animatePhaseShift(
    waveLength: Dp,
    wavePeriod: Duration,
    waveMovement: WaveMovement
): State<Dp> {
    val shift = waveLength * waveMovement.factor(LocalLayoutDirection.current)
    val phaseShiftAnimated = remember { mutableStateOf(0.dp) }
    val phaseShiftAnimation = remember(shift, wavePeriod) {
        val wavePeriodAdjusted = wavePeriod.toAdjustedMilliseconds()
        val shiftAdjusted = if (wavePeriodAdjusted == Int.MAX_VALUE) 0.dp else shift
        TargetBasedAnimation(
            animationSpec = infiniteRepeatable(
                animation = tween(wavePeriodAdjusted, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            typeConverter = Dp.VectorConverter,
            // Instead of simply 0 and shift, they are added to current phaseShiftAnimated to
            // smoothly continue the wave shift when wavePeriod or waveMovement is changed
            initialValue =          0.dp + phaseShiftAnimated.value,
            targetValue  = shiftAdjusted + phaseShiftAnimated.value
        )
    }
    var playTime by remember { mutableStateOf(0L) }
    LaunchedEffect(phaseShiftAnimation) {
        val startTime = withFrameNanos { it }
        while (isActive) {
            playTime = withFrameNanos { it } - startTime
            phaseShiftAnimated.value = phaseShiftAnimation.getValueFromNanos(playTime)
        }
    }
    return phaseShiftAnimated
}

private inline fun Duration.toAdjustedMilliseconds() = this
    .absoluteValue
    .inWholeMilliseconds
    .coerceAtMost(Int.MAX_VALUE.toLong())
    .toInt() // Do not call this before coercion
    .takeIf { it != 0 }
    ?: Int.MAX_VALUE

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
    waveThickness: Dp,
    trackThickness: Dp,
    phaseShift: Dp,
    incremental: Boolean,
    inactiveTrackColor: Color,
    activeTrackColor: Color
) {
    drawTrackActivePart(
        startOffset = sliderStart,
        valueOffset = sliderValueOffset,
        waveLength = waveLength,
        waveHeight = waveHeight,
        waveThickness = waveThickness,
        phaseShift = phaseShift,
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
    waveThickness: Dp,
    phaseShift: Dp,
    incremental: Boolean,
    color: Color
) {
    if (waveThickness <= 0.dp) return
    val wave = Path().apply {
        if (waveLength <= 0.dp || waveHeight == 0.dp) {
            moveTo(startOffset.x, center.y)
            lineTo(valueOffset.x, center.y)
            return@apply
        }
        val phaseShiftPx = phaseShift.toPx()
        val waveLengthPx = waveLength.toPx()
        val waveHeightPx = waveHeight.toPx().absoluteValue
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
            width = waveThickness.toPx(),
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
