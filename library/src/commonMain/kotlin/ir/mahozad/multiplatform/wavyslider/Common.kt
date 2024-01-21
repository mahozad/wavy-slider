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
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * The horizontal movement of the whole wave.
 */
enum class WaveMovement {
    /**
     * Always move from right to left regardless of the layout direction.
     */
    RTL,
    /**
     * Always move from left to right regardless of the layout direction.
     */
    LTR,
    /**
     * Based on layout direction; on LTR move from right to left and on RTL move from left to right.
     */
    AUTO
}

internal val defaultWaveMovement = WaveMovement.AUTO
internal val defaultShouldFlatten = false
internal val defaultTrackThickness = 4.dp
internal val defaultWaveLength = 20.dp
internal val defaultWaveHeight = 6.dp
internal val defaultWavePeriod = 2.seconds
internal val defaultWaveHeightChangeDuration = 300.milliseconds

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
    val delta = if (waveMovement == WaveMovement.LTR) {
        -waveLengthPx
    } else if (waveMovement == WaveMovement.RTL) {
        waveLengthPx
    } else if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
        -waveLengthPx
    } else {
        waveLengthPx
    }
    val phaseShiftPxAnimated = remember { mutableFloatStateOf(0f) }
    val phaseShiftPxAnimation = remember(delta, wavePeriod) {
        val wavePeriodAdjusted = wavePeriod.toAdjustedMilliseconds()
        val deltaAdjusted = if (wavePeriodAdjusted == Int.MAX_VALUE) 0f else delta
        TargetBasedAnimation(
            animationSpec = infiniteRepeatable(
                animation = tween(wavePeriodAdjusted, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            typeConverter = Float.VectorConverter,
            // Instead of simply 0 and delta, they are added to current phaseShiftPxAnimated to
            // smoothly continue the wave shift when wavePeriod or waveMovement is changed
            initialValue =             0 + phaseShiftPxAnimated.value,
            targetValue  = deltaAdjusted + phaseShiftPxAnimated.value
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
internal inline fun animateWaveHeightPx(waveHeightPx: Float): State<Float> {
    return animateFloatAsState(
        waveHeightPx,
        tween(defaultWaveHeightChangeDuration.inWholeMilliseconds.toInt(), easing = FastOutSlowInEasing)
    )
}

internal fun DrawScope.drawTrack(
    sliderStart: Offset,
    sliderValueOffset: Offset,
    sliderEnd: Offset,
    waveLengthPx: Float,
    waveHeightPx: Float,
    waveThicknessPx: Float,
    trackThicknessPx: Float,
    phaseShiftPx: Float,
    shouldFlatten: Boolean,
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
        shouldFlatten = shouldFlatten,
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
    if (thicknessPx.toInt() <= 0) return
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
    shouldFlatten: Boolean,
    color: Color
) {
    if (waveThicknessPx.toInt() <= 0) return
    val wave = Path().apply {
        if (waveLengthPx.toInt() == 0) {
            moveTo(startOffset.x, center.y)
            lineTo(valueOffset.x, center.y)
            return@apply
        }
        val startHeightFactor = if (shouldFlatten) 0f else 1f
        val startRadians = (startOffset.x + phaseShiftPx) % waveLengthPx / waveLengthPx * (2 * PI)
        val startY = (sin(startRadians) * startHeightFactor * (waveHeightPx / 2)) + (size.height / 2)
        moveTo(startOffset.x, startY.toFloat())
        val range = if (layoutDirection == LayoutDirection.Rtl) {
            startOffset.x.toInt() downTo valueOffset.x.toInt()
        } else {
            startOffset.x.toInt()..valueOffset.x.toInt()
        }
        for (x in range) {
            val heightFactor = if (shouldFlatten) (x - range.first).toFloat() / (range.last - range.first) else 1f
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
