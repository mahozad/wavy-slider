package ir.mahozad.multiplatform.wavyslider

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.math.ceil
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
    waveThicknessPx: Float,
    wavePosition: Float,
    waveHeightAnimated: Float,
    trackThicknessPx: Float,
    inactiveTrackColor: Color,
    sliderValueOffset: Offset,
    sliderStart: Offset,
    sliderEnd: Offset,
    sliderRight: Offset,
    sliderLeft: Offset,
    waveLengthPx: Float,
    activeTrackColor: Color,
    shouldFlatten: Boolean
) {
    val isRtl = layoutDirection == LayoutDirection.Rtl
    if (trackThicknessPx > 0f) {
        drawLine(
            strokeWidth = trackThicknessPx,
            color = inactiveTrackColor,
            start = sliderValueOffset,
            end = sliderEnd,
            cap = StrokeCap.Round
        )
    }
    if (waveThicknessPx <= 0f) return
    val wave = Path().apply {
        val startX = sliderStart.x + /* Two extra required padding waves at the start */ (2 * waveLengthPx) * if (isRtl) 1 else -1
        val length = (sliderValueOffset.x - startX).absoluteValue + /* Two extra required padding waves at the end */ (2 * waveLengthPx)
        val totalWaveCount = if (waveLengthPx == 0f) 0 else ceil(length / waveLengthPx).toInt()
        val heightFactors = if (shouldFlatten) {
            generateHeightFactors(totalWaveCount)
        } else {
            FloatArray(totalWaveCount)
        }
        moveTo(startX, center.y)
        if (totalWaveCount == 0) lineTo(sliderValueOffset.x, center.y)
        for (i in 0 ..< totalWaveCount) {
            relativeCubicTo(
                /* Control 1: */ waveLengthPx / 2 * if (isRtl) -1 else 1,
                (waveHeightAnimated / 2) * if (shouldFlatten) heightFactors[i] else 1f,
                /* Control 2: */
                waveLengthPx / 2 * if (isRtl) -1 else 1,
                (-waveHeightAnimated / 2) * if (shouldFlatten) heightFactors[i] else 1f,
                /* End point: */
                waveLengthPx * if (isRtl) -1 else 1,
                0f
            )
        }
    }
    // Could also have used .clipToBounds() on Canvas modifier
    clipRect(
        left = if (isRtl) sliderValueOffset.x else sliderLeft.x - (/* To match the size of material slider as it has round cap */ waveThicknessPx / 2),
        right = if (isRtl) sliderRight.x + (/* To match the size of material slider as it has round cap */ waveThicknessPx / 2) else sliderValueOffset.x
    ) {
        translate(left = wavePosition) {
            drawPath(
                path = wave,
                color = activeTrackColor,
                style = Stroke(waveThicknessPx)
            )
        }
    }
}
