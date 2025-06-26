/*
 *
 *
 * Based on https://github.com/JetBrains/compose-multiplatform-core/blob/v1.8.2/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Slider.kt
 *
 *
 */

@file:Suppress("UnusedReceiverParameter")

package ir.mahozad.multiplatform.wavyslider.material3

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.fastFirst
import ir.mahozad.multiplatform.wavyslider.*
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

/*
 * Instead of directly exposing the following defaults as public properties,
 * we want to provide them in the SliderDefaults object so the user can access all the defaults
 * using that namespace. But SliderDefaults object is in Material library, and we cannot modify it.
 * So, we provide the defaults as extension properties of SliderDefaults object.
 */

/**
 * Default wave length
 */
val SliderDefaults.WaveLength: Dp get() = defaultWaveLength
/**
 * Default wave height
 */
val SliderDefaults.WaveHeight: Dp get() = defaultWaveHeight
/**
 * Default wave velocity (speed and direction)
 */
val SliderDefaults.WaveVelocity: WaveVelocity get() = defaultWaveVelocity
/**
 * Default wave thickness
 */
val SliderDefaults.WaveThickness: Dp get() = 4.dp
/**
 * Default track thickness
 */
val SliderDefaults.TrackThickness: Dp get() = 16.dp
/**
 * Default progression of wave height (whether gradual or not)
 */
val SliderDefaults.Incremental: Boolean get() = defaultIncremental
/**
 * Default animation configurations for various properties of the wave
 */
val SliderDefaults.WaveAnimationSpecs: WaveAnimationSpecs get() = defaultWaveAnimationSpecs

private val TrackInsideCornerSize = 2.dp
private val TrackHeight = @Suppress("INVISIBLE_REFERENCE")
            androidx.compose.material3.tokens.SliderTokens.InactiveTrackHeight
private val ThumbTrackGapSize = @Suppress("INVISIBLE_REFERENCE")
            androidx.compose.material3.tokens.SliderTokens.ActiveHandleLeadingSpace
private val ThumbWidth = @Suppress("INVISIBLE_REFERENCE")
            androidx.compose.material3.tokens.SliderTokens.HandleWidth
private val ThumbHeight = @Suppress("INVISIBLE_REFERENCE")
            androidx.compose.material3.tokens.SliderTokens.HandleHeight
private val ThumbSize = DpSize(ThumbWidth, ThumbHeight)

/**
 * The Default track for [WavySlider].
 *
 * @param sliderState [SliderState] which is used to obtain the current active track.
 * @param modifier the [Modifier] to be applied to the track.
 * @param colors [SliderColors] that will be used to resolve the colors used for this track in
 *   different states. See [SliderDefaults.colors].
 * @param enabled controls the enabled state of this slider. When `false`, this component will
 *   not respond to user input, and it will appear visually disabled and disabled to
 *   accessibility services.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 *   The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 *   Setting speed to `0.dp` or less stops the movement.
 * @param waveThickness the thickness of the active line (whether animated or not).
 * @param trackThickness the thickness of the inactive line.
 * @param incremental whether to gradually increase height from zero at start to [waveHeight] at thumb.
 * @param animationSpecs animation configurations used for various properties of the wave.
 */
@Deprecated(
    message = "Use the overload that takes `thumbTrackGapSize`, `trackInsideCornerSize` and `drawStopIndicator`",
    replaceWith = ReplaceWith(
        "Track(sliderState, modifier, colors, enabled, thumbTrackGapSize, trackInsideCornerSize, " +
                "drawStopIndicator, waveLength, waveHeight, waveVelocity, waveThickness, trackThickness, incremental, animationSpecs)"
    ),
    level = DeprecationLevel.HIDDEN
)
@Composable
@ExperimentalMaterial3Api
fun SliderDefaults.Track(
    sliderState: SliderState,
    modifier: Modifier = Modifier,
    colors: SliderColors = colors(),
    enabled: Boolean = true,
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs
) {
    Track(
        sliderState = sliderState,
        modifier = modifier,
        colors = colors,
        enabled = enabled,
        thumbTrackGapSize = ThumbTrackGapSize,
        trackInsideCornerSize = TrackInsideCornerSize,
        /////////////////
        /////////////////
        /////////////////
        waveLength = waveLength,
        waveHeight = waveHeight,
        waveVelocity = waveVelocity,
        waveThickness = waveThickness,
        trackThickness = trackThickness,
        incremental = incremental,
        animationSpecs = animationSpecs
    )
}

/**
 * The Default track for [WavySlider].
 *
 * @param sliderState [SliderState] which is used to obtain the current active track.
 * @param modifier the [Modifier] to be applied to the track.
 * @param colors [SliderColors] that will be used to resolve the colors used for this track in
 *   different states. See [SliderDefaults.colors].
 * @param enabled controls the enabled state of this slider. When `false`, this component will
 *   not respond to user input, and it will appear visually disabled and disabled to
 *   accessibility services.
 * @param thumbTrackGapSize size of the gap between the thumb and the track.
 * @param trackInsideCornerSize size of the corners towards the thumb when a gap is set.
 * @param drawStopIndicator lambda that will be called to draw the stop indicator at the end of
 *   the track.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 *   The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 *   Setting speed to `0.dp` or less stops the movement.
 * @param waveThickness the thickness of the active line (whether animated or not).
 * @param trackThickness the thickness of the inactive line.
 * @param incremental whether to gradually increase height from zero at start to [waveHeight] at thumb.
 * @param animationSpecs animation configurations used for various properties of the wave.
 */
@Composable
@ExperimentalMaterial3Api
fun SliderDefaults.Track(
    sliderState: SliderState,
    modifier: Modifier = Modifier,
    colors: SliderColors = colors(),
    enabled: Boolean = true,
    thumbTrackGapSize: Dp = ThumbTrackGapSize,
    trackInsideCornerSize: Dp = TrackInsideCornerSize,
    drawStopIndicator: (DrawScope.(Offset) -> Unit)? = {
        drawStopIndicator(
            offset = it,
            color = @Suppress("INVISIBLE_REFERENCE") colors.trackColor(enabled, active = true),
            size = TrackStopIndicatorSize
        )
    },
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs
) {
    // @Suppress("INVISIBLE_REFERENCE") is required to be able to access and use
    // trackColor() function which is marked internal in Material library
    // See https://stackoverflow.com/q/62500464/8583692
    val inactiveTrackColor = @Suppress("INVISIBLE_REFERENCE") colors.trackColor(enabled, active = false)
    val activeTrackColor = @Suppress("INVISIBLE_REFERENCE") colors.trackColor(enabled, active = true)
    val waveSpreadAnimated by animateWaveSpread(animationSpecs.waveAppearanceAnimationSpec)
    val waveHeightAnimated by animateWaveHeight(waveHeight, animationSpecs.waveHeightAnimationSpec)
    val waveShiftAnimated by animateWaveShift(waveVelocity, animationSpecs.waveVelocityAnimationSpec)
    val trackHeight = max(waveThickness + waveHeight.value.absoluteValue.dp, ThumbSize.height)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .rotate(if (LocalLayoutDirection.current == LayoutDirection.Rtl) 180f else 0f)
    ) {
        val sliderStart = Offset(0f, center.y)
        val sliderEnd = Offset(size.width, center.y)
        val sliderValueFraction = @Suppress("INVISIBLE_REFERENCE") sliderState.coercedValueAsFraction
        val sliderValueOffset = Offset(sliderStart.x + (sliderEnd.x - sliderStart.x) * sliderValueFraction, center.y)
        drawTrack(
            waveLength = waveLength,
            waveHeight = waveHeightAnimated,
            waveSpread = waveSpreadAnimated,
            waveShift = waveShiftAnimated,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            sliderValueOffset = sliderValueOffset,
            sliderStart = sliderStart,
            sliderEnd = sliderEnd,
            incremental = incremental,
            inactiveTrackColor = inactiveTrackColor,
            activeTrackColor = activeTrackColor,

            thumbWidth = @Suppress("INVISIBLE_REFERENCE") sliderState.thumbWidth.toDp(),
            thumbTrackGapSize = thumbTrackGapSize,
            trackInsideCornerSize = trackInsideCornerSize,
            drawStopIndicator = drawStopIndicator
        )
    }
}

/**
 * The Default track for [WavySlider].
 *
 * @param sliderPositions [SliderPositions] which is used to obtain the current active track.
 * @param modifier the [Modifier] to be applied to the track.
 * @param colors [SliderColors] that will be used to resolve the colors used for this track in
 *   different states. See [SliderDefaults.colors].
 * @param enabled controls the enabled state of this WavySlider. When `false`, this component will
 *   not respond to user input, and it will appear visually disabled and disabled to
 *   accessibility services.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 *   The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 *   Setting speed to `0.dp` or less stops the movement.
 * @param waveThickness the thickness of the active line (whether animated or not).
 * @param trackThickness the thickness of the inactive line.
 * @param incremental whether to gradually increase height from zero at start to [waveHeight] at thumb.
 * @param animationSpecs animation configurations used for various properties of the wave.
 */
@Composable
@Deprecated("Use the variant that supports SliderState")
fun SliderDefaults.Track(
    sliderPositions: SliderPositions,
    modifier: Modifier = Modifier,
    colors: SliderColors = colors(),
    enabled: Boolean = true,
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs
) {
    // @Suppress("INVISIBLE_MEMBER") is required to be able to access and use
    // trackColor() function which is marked internal in Material library
    // See https://stackoverflow.com/q/62500464/8583692
    // and https://youtrack.jetbrains.com/issue/KT-60304
    val inactiveTrackColor = @Suppress("INVISIBLE_REFERENCE") colors.trackColor(enabled, active = false)
    val activeTrackColor = @Suppress("INVISIBLE_REFERENCE") colors.trackColor(enabled, active = true)
    val waveSpreadAnimated by animateWaveSpread(animationSpecs.waveAppearanceAnimationSpec)
    val waveHeightAnimated by animateWaveHeight(waveHeight, animationSpecs.waveHeightAnimationSpec)
    val waveShiftAnimated by animateWaveShift(waveVelocity, animationSpecs.waveVelocityAnimationSpec)
    val trackHeight = max(waveThickness + waveHeight.value.absoluteValue.dp, ThumbSize.height)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .rotate(if (LocalLayoutDirection.current == LayoutDirection.Rtl) 180f else 0f)
    ) {
        val sliderStart = Offset(0f, center.y)
        val sliderEnd = Offset(size.width, center.y)
        val sliderValueOffset = Offset(sliderStart.x + (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.endInclusive, center.y)
        drawTrack(
            waveLength = waveLength,
            waveHeight = waveHeightAnimated,
            waveSpread = waveSpreadAnimated,
            waveShift = waveShiftAnimated,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            sliderValueOffset = sliderValueOffset,
            sliderStart = sliderStart,
            sliderEnd = sliderEnd,
            incremental = incremental,
            inactiveTrackColor = inactiveTrackColor,
            activeTrackColor = activeTrackColor,

            thumbWidth = 0.dp,
            thumbTrackGapSize = 0.dp,
            trackInsideCornerSize = 0.dp,
            drawStopIndicator = null
        )
    }
}

/**
 * A wavy slider much like the [Material Design 3 Slider](https://m3.material.io/components/sliders).
 *
 * Setting [waveHeight] or [waveLength] to `0.dp` results in a regular [Slider].
 *
 * Note that range sliders do not make sense for the WavySlider.
 * So, there is no RangeWavySlider counterpart.
 *
 * It uses [SliderDefaults.Thumb] and [SliderDefaults.Track] as the thumb and track.
 *
 * @param value current value of the WavySlider. Will be coerced to [valueRange].
 * @param onValueChange callback in which value should be updated.
 * @param modifier the [Modifier] to be applied to this WavySlider.
 * @param enabled controls the enabled state of this WavySlider. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility services.
 * @param valueRange range of values that this WavySlider can take. The passed [value] will be coerced
 *   to this range.
 * @param onValueChangeFinished called when value change has ended. This should not be used to
 *   update the WavySlider value (use [onValueChange] instead), but rather to know when the user has
 *   completed selecting a new value by ending a drag or a click.
 * @param colors [SliderColors] that will be used to resolve the colors used for this WavySlider in
 *   different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 *   for this WavySlider. You can create and pass in your own `remember`ed instance to observe
 *   [Interaction]s and customize the appearance / behavior of this slider in different states.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 *   The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 *   Setting speed to `0.dp` or less stops the movement.
 * @param waveThickness the thickness of the active line (whether animated or not).
 * @param trackThickness the thickness of the inactive line.
 * @param incremental whether to gradually increase height from zero at start to [waveHeight] at thumb.
 * @param animationSpecs animation configurations used for various properties of the wave.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs
) {
    WavySlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource,
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = colors,
                enabled = enabled
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                colors = colors,
                enabled = enabled,
                sliderState = sliderState,
                /////////////////
                /////////////////
                /////////////////
                waveLength = waveLength,
                waveHeight = waveHeight,
                waveVelocity = waveVelocity,
                waveThickness = waveThickness,
                trackThickness = trackThickness,
                incremental = incremental,
                animationSpecs = animationSpecs
            )
        },
        valueRange = valueRange
    )
}

/**
 * A wavy slider much like the [Material Design 3 Slider](https://m3.material.io/components/sliders).
 *
 * Setting [waveHeight] or [waveLength] to `0.dp` results in a regular [Slider].
 *
 * Note that range sliders do not make sense for the WavySlider.
 * So, there is no RangeWavySlider counterpart.
 *
 * @param value current value of the WavySlider. Will be coerced to [valueRange].
 * @param onValueChange callback in which value should be updated.
 * @param modifier the [Modifier] to be applied to this WavySlider.
 * @param enabled controls the enabled state of this WavySlider. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility services.
 * @param valueRange range of values that this WavySlider can take. The passed [value] will be coerced
 *   to this range.
 * @param onValueChangeFinished called when value change has ended. This should not be used to
 *   update the WavySlider value (use [onValueChange] instead), but rather to know when the user has
 *   completed selecting a new value by ending a drag or a click.
 * @param colors [SliderColors] that will be used to resolve the colors used for this WavySlider in
 *   different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 *   for this WavySlider. You can create and pass in your own `remember`ed instance to observe
 *   [Interaction]s and customize the appearance / behavior of this slider in different states.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 *   The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 *   Setting speed to `0.dp` or less stops the movement.
 * @param waveThickness the thickness of the active line (whether animated or not).
 * @param trackThickness the thickness of the inactive line.
 * @param incremental whether to gradually increase height from zero at start to [waveHeight] at thumb.
 * @param animationSpecs animation configurations used for various properties of the wave.
 * @param thumb the thumb to be displayed on the WavySlider, it is placed on top of the track. The lambda
 *   receives a [SliderPositions] which is used to obtain the current active track.
 * @param track the track to be displayed on the WavySlider, it is placed underneath the thumb. The lambda
 *   receives a [SliderPositions] which is used to obtain the current active track.
 */
@Composable
@ExperimentalMaterial3Api
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs,
    /////////////////
    /////////////////
    /////////////////
    thumb: @Composable (SliderState) -> Unit = {
        SliderDefaults.Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled
        )
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            sliderState = sliderState,
            /////////////////
            /////////////////
            /////////////////
            waveLength = waveLength,
            waveHeight = waveHeight,
            waveVelocity = waveVelocity,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            incremental = incremental,
            animationSpecs= animationSpecs
        )
    }
) {
    val state = remember(valueRange) {
        SliderState(value, 0, onValueChangeFinished, valueRange)
    }
    @Suppress("INVISIBLE_REFERENCE")
    state.onValueChangeFinished = onValueChangeFinished
    @Suppress("INVISIBLE_REFERENCE")
    state.onValueChange = onValueChange
    state.value = value
    WavySlider(
        state = state,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        thumb = thumb,
        track = track,
        /////////////////
        /////////////////
        /////////////////
        waveLength = waveLength,
        waveHeight = waveHeight,
        waveVelocity = waveVelocity,
        waveThickness = waveThickness,
        trackThickness = trackThickness,
        incremental = incremental,
        animationSpecs = animationSpecs,
    )
}

/**
 * A wavy slider much like the [Material Design 3 Slider](https://m3.material.io/components/sliders).
 *
 * Setting [waveHeight] or [waveLength] to `0.dp` results in a regular [Slider].
 *
 * Note that range sliders do not make sense for the WavySlider.
 * So, there is no RangeWavySlider counterpart.
 *
 * @param state [SliderState] which contains the slider's current value.
 * @param modifier the [Modifier] to be applied to this WavySlider.
 * @param enabled controls the enabled state of this WavySlider. When `false`, this component will not
 *   respond to user input, and it will appear visually disabled and disabled to accessibility services.
 * @param colors [SliderColors] that will be used to resolve the colors used for this WavySlider in
 *   different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 *   for this WavySlider. You can create and pass in your own `remember`ed instance to observe
 *   [Interaction]s and customize the appearance / behavior of this slider in different states.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 *   The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 *   Setting speed to `0.dp` or less stops the movement.
 * @param waveThickness the thickness of the active line (whether animated or not).
 * @param trackThickness the thickness of the inactive line.
 * @param incremental whether to gradually increase height from zero at start to [waveHeight] at thumb.
 * @param animationSpecs animation configurations used for various properties of the wave.
 * @param thumb the thumb to be displayed on the WavySlider, it is placed on top of the track. The lambda
 *   receives a [SliderPositions] which is used to obtain the current active track.
 * @param track the track to be displayed on the WavySlider, it is placed underneath the thumb. The lambda
 *   receives a [SliderPositions] which is used to obtain the current active track.
 */
@Composable
@ExperimentalMaterial3Api
fun WavySlider(
    state: SliderState,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: WaveVelocity = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs,
    /////////////////
    /////////////////
    /////////////////
    thumb: @Composable (SliderState) -> Unit = {
        SliderDefaults.Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled
        )
    },
    track: @Composable (SliderState) -> Unit = { sliderState ->
        SliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            sliderState = sliderState,
            /////////////////
            /////////////////
            /////////////////
            waveLength = waveLength,
            waveHeight = waveHeight,
            waveVelocity = waveVelocity,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            incremental = incremental,
            animationSpecs= animationSpecs
        )
    }
) {
    WavySliderImpl(
        state = state,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        thumb = thumb,
        track = track
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WavySliderImpl(
    state: SliderState,
    enabled: Boolean,
    modifier: Modifier,
    interactionSource: MutableInteractionSource,
    thumb: @Composable (SliderState) -> Unit,
    track: @Composable (SliderState) -> Unit
) {
    @Suppress("INVISIBLE_REFERENCE")
    state.isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val press = Modifier.sliderTapModifier(state, interactionSource, enabled)
    val drag = Modifier.draggable(
        orientation = Orientation.Horizontal,
        reverseDirection = @Suppress("INVISIBLE_REFERENCE") state.isRtl,
        enabled = enabled,
        interactionSource = interactionSource,
        onDragStopped = { @Suppress("INVISIBLE_REFERENCE") state.gestureEndAction.invoke() },
        startDragImmediately = @Suppress("INVISIBLE_REFERENCE") state.isDragging,
        state = state
    )

    Layout(
        content = {
            Box(
                modifier = Modifier
                    .layoutId(SliderComponents.THUMB)
                    .wrapContentWidth()
                    .onSizeChanged {
                        @Suppress("INVISIBLE_REFERENCE")
                        state.thumbWidth = it.width.toFloat()
                    }
            ) {
                thumb(state)
            }
            Box(modifier = Modifier.layoutId(SliderComponents.TRACK)) { track(state) }
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(minWidth = ThumbWidth, minHeight = TrackHeight)
            .sliderSemantics(state, enabled)
            .focusable(enabled, interactionSource)
            .then(press)
            .then(drag)
    ) { measurables, constraints ->
        val thumbPlaceable = measurables.fastFirst { it.layoutId == SliderComponents.THUMB }.measure(constraints)
        val trackPlaceable = measurables
                .fastFirst { it.layoutId == SliderComponents.TRACK }
                .measure(constraints.offset(horizontal = -thumbPlaceable.width).copy(minHeight = 0))

        val sliderWidth = thumbPlaceable.width + trackPlaceable.width
        val sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)

        @Suppress("INVISIBLE_REFERENCE")
        state.updateDimensions(trackPlaceable.height.toFloat(), sliderWidth)

        val trackOffsetX = thumbPlaceable.width / 2
        val thumbOffsetX = ((trackPlaceable.width) * @Suppress("INVISIBLE_REFERENCE") state.coercedValueAsFraction).roundToInt()
        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
        val thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2

        layout(sliderWidth, sliderHeight) {
            trackPlaceable.placeRelative(trackOffsetX, trackOffsetY)
            thumbPlaceable.placeRelative(thumbOffsetX, thumbOffsetY)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
// No need to name it wavySliderSemantics
private fun Modifier.sliderSemantics(state: SliderState, enabled: Boolean): Modifier {
    return semantics {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                var newValue = targetValue.coerceIn(
                    state.valueRange.start,
                    state.valueRange.endInclusive
                )
                val originalVal = newValue
                val resolvedValue = if (state.steps > 0) {
                    var distance: Float = newValue
                    for (i in 0..state.steps + 1) {
                        val stepValue = lerp(
                            state.valueRange.start,
                            state.valueRange.endInclusive,
                            i.toFloat() / (state.steps + 1)
                        )
                        if (abs(stepValue - originalVal) <= distance) {
                            distance = abs(stepValue - originalVal)
                            newValue = stepValue
                        }
                    }
                    newValue
                } else {
                    newValue
                }

                // This is to keep it consistent with AbsSeekbar.java: return false if no
                // change from current.
                if (resolvedValue == state.value) {
                    false
                } else {
                    if (resolvedValue != state.value) {
                        if (@Suppress("INVISIBLE_REFERENCE") state.onValueChange != null) {
                            @Suppress("INVISIBLE_REFERENCE") state.onValueChange?.let {
                                it(resolvedValue)
                            }
                        } else {
                            state.value = resolvedValue
                        }
                    }
                    state.onValueChangeFinished?.invoke()
                    true
                }
            }
        )
    }
        .then(
            @Suppress("INVISIBLE_REFERENCE")
            androidx.compose.material3.internal.IncreaseHorizontalSemanticsBounds
        )
        .progressSemantics(
            state.value,
            state.valueRange.start..state.valueRange.endInclusive,
            state.steps
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Stable
// No need to name it wavySliderTapModifier
private fun Modifier.sliderTapModifier(
    state: SliderState,
    interactionSource: MutableInteractionSource,
    enabled: Boolean
) = if (enabled) {
    pointerInput(state, interactionSource) {
        detectTapGestures(
            onPress = { @Suppress("INVISIBLE_REFERENCE") state.onPress(it) },
            onTap = {
                state.dispatchRawDelta(0f)
                @Suppress("INVISIBLE_REFERENCE")
                state.gestureEndAction.invoke()
            }
        )
    }
} else {
    this
}

// No need to name it WavySliderComponents
private enum class SliderComponents {
    THUMB,
    TRACK
}

private fun DrawScope.drawStopIndicator(
    offset: Offset,
    size: Dp,
    color: Color
) {
    drawCircle(
        color = color,
        center = offset,
        radius = size.toPx() / 2f
    )
}

// This is named trackPath in the original compose-multiplatform-core code
private val inactiveTrackPath = Path()

private inline fun DrawScope.drawTrack(
    sliderStart: Offset,
    sliderValueOffset: Offset,
    sliderEnd: Offset,
    waveLength: Dp,
    waveHeight: Dp,
    waveSpread: Float,
    waveShift: Dp,
    waveThickness: Dp,
    trackThickness: Dp,
    incremental: Boolean,
    inactiveTrackColor: Color,
    activeTrackColor: Color,

    thumbWidth: Dp,
    thumbTrackGapSize: Dp,
    trackInsideCornerSize: Dp,
    noinline drawStopIndicator: (DrawScope.(Offset) -> Unit)?
) {
    drawTrackActivePart(
        startOffset = sliderStart,
        valueOffset = sliderValueOffset,
        waveLength = waveLength,
        waveHeight = waveHeight,
        waveSpread = waveSpread,
        waveShift = waveShift,
        waveThickness = waveThickness,
        incremental = incremental,
        color = activeTrackColor,
        thumbWidth = thumbWidth,
        thumbTrackGapSize = thumbTrackGapSize,
        trackInsideCornerSize = trackInsideCornerSize
    )
    drawTrackInactivePart(
        color = inactiveTrackColor,
        thickness = trackThickness,
        startOffset = sliderValueOffset,
        endOffset = sliderEnd,
        thumbWidth = thumbWidth,
        thumbTrackGapSize = thumbTrackGapSize,
        trackInsideCornerSize = trackInsideCornerSize,
        drawStopIndicator = drawStopIndicator
    )
}

private inline fun DrawScope.drawTrackInactivePart(
    color: Color,
    thickness: Dp,
    startOffset: Offset,
    endOffset: Offset,
    thumbWidth: Dp,
    thumbTrackGapSize: Dp,
    trackInsideCornerSize: Dp,
    noinline drawStopIndicator: (DrawScope.(Offset) -> Unit)?
) {
    if (thickness <= 0.dp) return
    val cornerSize = thickness.toPx() / 2
    val insideCornerSize = trackInsideCornerSize.toPx()
    var endGap = if (thumbTrackGapSize > 0.dp) {
        (thumbWidth.toPx() / 2) + thumbTrackGapSize.toPx()
    } else {
        0f
    }
    val sliderEnd = endOffset.x - endGap
    if (startOffset.x < sliderEnd - cornerSize) {
        val start = startOffset.x + endGap
        val end = sliderEnd
        // Below code was actually in the drawTrackPath() function in the original CMP Slider
        /////////////////////////////////
        val startCorner = CornerRadius(insideCornerSize, insideCornerSize)
        val endCorner = CornerRadius(cornerSize, cornerSize)
        val track = RoundRect(
            rect = Rect(
                offset = Offset(start, center.y - thickness.toPx() / 2),
                size = Size(end - startOffset.x, thickness.toPx())
            ),
            topLeft = startCorner,
            topRight = endCorner,
            bottomRight = endCorner,
            bottomLeft = startCorner
        )
        inactiveTrackPath.addRoundRect(track)
        drawPath(inactiveTrackPath, color)
        inactiveTrackPath.rewind()
        /////////////////////////////////
        drawStopIndicator?.invoke(this, Offset(endOffset.x - cornerSize, center.y))
    }
}

private inline fun DrawScope.drawTrackActivePart(
    startOffset: Offset,
    valueOffset: Offset,
    waveLength: Dp,
    waveHeight: Dp,
    waveSpread: Float,
    waveShift: Dp,
    waveThickness: Dp,
    incremental: Boolean,
    color: Color,
    thumbWidth: Dp,
    thumbTrackGapSize: Dp,
    trackInsideCornerSize: Dp
) {
    if (waveThickness <= 0.dp) return
    val startCornerRadius = waveThickness.toPx() / 2
    var endGap = if (thumbTrackGapSize > 0.dp) {
        (thumbWidth.toPx() / 2) + thumbTrackGapSize.toPx()
    } else {
        0f
    }
    val endOffset = Offset(x = valueOffset.x - endGap, 0f)
    if (endOffset.x - startOffset.x > startCornerRadius) {
        if (waveLength <= 0.dp || waveHeight == 0.dp) {
            drawPath(
                path = createFlatPath(
                    startOffset,
                    endOffset,
                    waveThickness.toPx(),
                    startCornerRadius,
                    trackInsideCornerSize.toPx()
                ),
                color = color
            )
        } else {
            drawPath(
                path = createWavyPath(
                    // See the visual test #41 and its KDoc for more information.
                    startOffset.copy(x = (startOffset.x + waveThickness.toPx() / 2).roundToInt().toFloat()),
                    endOffset.copy(x = endOffset.x - waveThickness.toPx() / 2),
                    waveLength,
                    waveHeight,
                    waveSpread,
                    waveShift,
                    incremental
                ),
                color = color,
                style = Stroke(
                    width = waveThickness.toPx(),
                    join = StrokeJoin.Round,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

private inline fun DrawScope.createFlatPath(
    startOffset: Offset,
    valueOffset: Offset,
    thickness: Float,
    startCornerSize: Float,
    insideCornerSize: Float
): Path = Path().apply {
    val endCorner = CornerRadius(insideCornerSize, insideCornerSize)
    val startCorner = CornerRadius(startCornerSize, startCornerSize)
    addRoundRect(
        RoundRect(
            left = startOffset.x,
            right = valueOffset.x,
            top = center.y - thickness / 2,
            bottom = center.y + thickness / 2,
            topLeftCornerRadius = startCorner,
            topRightCornerRadius = endCorner,
            bottomRightCornerRadius = endCorner,
            bottomLeftCornerRadius = startCorner
        )
    )
}
