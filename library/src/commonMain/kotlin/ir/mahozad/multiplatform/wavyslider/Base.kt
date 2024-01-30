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
import kotlin.math.*

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

internal val defaultIncremental = false
internal val defaultTrackThickness = 4.dp
internal val defaultWaveLength = 20.dp
internal val defaultWaveHeight = 6.dp
// Instead of providing a dedicated type for this, the existing "Pair" class of Kotlin stdlib is used.
// One benefit is that in Kotlin, any object (including "Dp") has the infix extension function "to"
// which makes it easy and more readable to create "Pair"s (including creating this property).
// In addition, if a dedicated type were provided, the users would have to either pass the velocity argument
// like "WaveVelocity(10.dp, TAIL)" which is not very easy and very readable, or, to make it easier,
// we'd have to also provide an infix extension function on Dp (for example, call it "toward") to
// enable users to create an instance of the velocity like how they do using the "to" for "Pair"s.
// But in that case, the IDE completion on "Dp" would be polluted by our new "toward" function
// even if the user wanted to access something else on "Dp".
internal val defaultWaveVelocity = 10.dp to TAIL
// These are provided so that users of library can specify custom animation specs for the changes in certain properties.
// This way, they do not have to animate those properties themselves (for example, using animateDpAsState()).
// This also enables us to provide a default animation for changes of those properties
// if user of library is not aware or does not care about making the changes animated/graceful/gradual.
// If they want to make the changes immediate/abrupt/sudden, they can simply pass a snap() animation spec.
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
    waveVelocity: Pair<Dp, WaveDirection>,
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
    waveThickness: Dp,
    waveShift: Dp,
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
