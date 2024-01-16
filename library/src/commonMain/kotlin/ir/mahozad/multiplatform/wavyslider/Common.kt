package ir.mahozad.multiplatform.wavyslider

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * The direction of wave movement.
 *
 * By default, and also when set to [WaveAnimationDirection.UNSPECIFIED],
 * it moves from right to left on LTR layouts and from left to right on RTL layouts.
 */
enum class WaveAnimationDirection {
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
    UNSPECIFIED
}

internal val defaultTrackThickness = 4.dp
internal val defaultWaveSize = 16.dp
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

internal fun DrawScope.drawTrack(
    sliderStart: Offset,
    sliderValueOffset: Offset,
    sliderEnd: Offset,
    waveLengthPx: Float,
    waveHeightPx: Float,
    waveThicknessPx: Float,
    trackThicknessPx: Float,
    componentHeightPx: Float,
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
        componentHeightPx = componentHeightPx,
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
    componentHeightPx: Float,
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
        val heightFactor = if (shouldFlatten) 0f else 1f
        val rad = (startOffset.x + phaseShiftPx) % waveLengthPx / waveLengthPx * (2 * PI)
        val y = if (waveHeightPx == 0f) {
            center.y
        } else {
            (sin(rad) * heightFactor * (waveHeightPx / 2)) + (componentHeightPx / 2)
        }
        moveTo(startOffset.x, y.toFloat())
        val range = if (layoutDirection == LayoutDirection.Rtl) {
            startOffset.x.toInt() downTo valueOffset.x.toInt()
        } else {
            startOffset.x.toInt()..valueOffset.x.toInt()
        }
        for (x in range) {
            val heightFactor = if (shouldFlatten) (x - range.first).toFloat() / (range.last - range.first) else 1f
            val rad = (x + phaseShiftPx) % waveLengthPx / waveLengthPx * (2 * PI)
            val y = if (waveHeightPx == 0f) {
                center.y
            } else {
                (sin(rad) * heightFactor * (waveHeightPx / 2)) + (componentHeightPx / 2)
            }
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
