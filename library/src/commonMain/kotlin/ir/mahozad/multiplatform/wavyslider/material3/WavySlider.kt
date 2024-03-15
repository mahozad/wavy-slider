// Based on https://github.com/JetBrains/compose-multiplatform-core/blob/release/1.6.0/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Slider.kt

@file:Suppress("UnusedReceiverParameter")

package ir.mahozad.multiplatform.wavyslider.material3

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
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
val SliderDefaults.WaveThickness: Dp get() = defaultTrackThickness
/**
 * Default track thickness
 */
val SliderDefaults.TrackThickness: Dp get() = defaultTrackThickness
/**
 * Default progression of wave height (whether gradual or not)
 */
val SliderDefaults.Incremental: Boolean get() = defaultIncremental
/**
 * Default animation configurations for various properties of the wave
 */
val SliderDefaults.WaveAnimationSpecs: WaveAnimationSpecs get() = defaultWaveAnimationSpecs

private val ThumbWidth = SliderTokens.HandleWidth
private val ThumbHeight = SliderTokens.HandleHeight
private val ThumbSize = DpSize(ThumbWidth, ThumbHeight)

/**
 * The Default track for [WavySlider].
 *
 * @param sliderState [SliderState] which is used to obtain the current active track.
 * @param modifier the [Modifier] to be applied to the track.
 * @param colors [SliderColors] that will be used to resolve the colors used for this track in
 * different states. See [SliderDefaults.colors].
 * @param enabled controls the enabled state of this slider. When `false`, this component will
 * not respond to user input, and it will appear visually disabled and disabled to
 * accessibility services.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 * The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 * Setting speed to `0.dp` or less stops the movement.
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
    val inactiveTrackColor = @Suppress("INVISIBLE_MEMBER") colors.trackColor(enabled, active = false)
    val activeTrackColor = @Suppress("INVISIBLE_MEMBER") colors.trackColor(enabled, active = true)
    val waveSpreadAnimated by animateWaveSpread(animationSpecs.waveStartSpreadAnimationSpec)
    val waveHeightAnimated by animateWaveHeight(waveHeight, animationSpecs.waveHeightAnimationSpec)
    val waveShiftAnimated by animateWaveShift(waveVelocity, animationSpecs.waveVelocityAnimationSpec)
    val trackHeight = max(waveThickness + waveHeight.value.absoluteValue.dp, ThumbSize.height)
    Canvas(modifier = modifier.fillMaxWidth().height(trackHeight)) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(0f, center.y)
        val sliderRight = Offset(size.width, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight
        val sliderValueFraction = @Suppress("INVISIBLE_MEMBER") sliderState.coercedValueAsFraction
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
            activeTrackColor = activeTrackColor
        )
    }
}

/**
 * The Default track for [WavySlider].
 *
 * @param sliderPositions [SliderPositions] which is used to obtain the current active track.
 * @param modifier the [Modifier] to be applied to the track.
 * @param colors [SliderColors] that will be used to resolve the colors used for this track in
 * different states. See [SliderDefaults.colors].
 * @param enabled controls the enabled state of this WavySlider. When `false`, this component will
 * not respond to user input, and it will appear visually disabled and disabled to
 * accessibility services.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 * The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 * Setting speed to `0.dp` or less stops the movement.
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
    val inactiveTrackColor = @Suppress("INVISIBLE_MEMBER") colors.trackColor(enabled, active = false)
    val activeTrackColor = @Suppress("INVISIBLE_MEMBER") colors.trackColor(enabled, active = true)
    val waveSpreadAnimated by animateWaveSpread(animationSpecs.waveStartSpreadAnimationSpec)
    val waveHeightAnimated by animateWaveHeight(waveHeight, animationSpecs.waveHeightAnimationSpec)
    val waveShiftAnimated by animateWaveShift(waveVelocity, animationSpecs.waveVelocityAnimationSpec)
    val trackHeight = max(waveThickness + waveHeight.value.absoluteValue.dp, ThumbSize.height)
    Canvas(modifier = modifier.fillMaxWidth().height(trackHeight)) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(0f, center.y)
        val sliderRight = Offset(size.width, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight
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
            activeTrackColor = activeTrackColor
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
 * respond to user input, and it will appear visually disabled and disabled to accessibility services.
 * @param valueRange range of values that this WavySlider can take. The passed [value] will be coerced
 * to this range.
 * @param onValueChangeFinished called when value change has ended. This should not be used to
 * update the WavySlider value (use [onValueChange] instead), but rather to know when the user has
 * completed selecting a new value by ending a drag or a click.
 * @param colors [SliderColors] that will be used to resolve the colors used for this WavySlider in
 * different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this WavySlider. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this slider in different states.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 * The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 * Setting speed to `0.dp` or less stops the movement.
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
 * respond to user input, and it will appear visually disabled and disabled to accessibility services.
 * @param valueRange range of values that this WavySlider can take. The passed [value] will be coerced
 * to this range.
 * @param onValueChangeFinished called when value change has ended. This should not be used to
 * update the WavySlider value (use [onValueChange] instead), but rather to know when the user has
 * completed selecting a new value by ending a drag or a click.
 * @param colors [SliderColors] that will be used to resolve the colors used for this WavySlider in
 * different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this WavySlider. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this slider in different states.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 * The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 * Setting speed to `0.dp` or less stops the movement.
 * @param waveThickness the thickness of the active line (whether animated or not).
 * @param trackThickness the thickness of the inactive line.
 * @param incremental whether to gradually increase height from zero at start to [waveHeight] at thumb.
 * @param animationSpecs animation configurations used for various properties of the wave.
 * @param thumb the thumb to be displayed on the WavySlider, it is placed on top of the track. The lambda
 * receives a [SliderPositions] which is used to obtain the current active track.
 * @param track the track to be displayed on the WavySlider, it is placed underneath the thumb. The lambda
 * receives a [SliderPositions] which is used to obtain the current active track.
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
    val state = remember(valueRange, onValueChangeFinished) {
        SliderState(value, 0, onValueChangeFinished, valueRange)
    }
    @Suppress("INVISIBLE_MEMBER")
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
 * respond to user input, and it will appear visually disabled and disabled to accessibility services.
 * @param colors [SliderColors] that will be used to resolve the colors used for this WavySlider in
 * different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this WavySlider. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this slider in different states.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 * The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the horizontal movement (speed per second and direction) of the whole wave (aka phase shift).
 * Setting speed to `0.dp` or less stops the movement.
 * @param waveThickness the thickness of the active line (whether animated or not).
 * @param trackThickness the thickness of the inactive line.
 * @param incremental whether to gradually increase height from zero at start to [waveHeight] at thumb.
 * @param animationSpecs animation configurations used for various properties of the wave.
 * @param thumb the thumb to be displayed on the WavySlider, it is placed on top of the track. The lambda
 * receives a [SliderPositions] which is used to obtain the current active track.
 * @param track the track to be displayed on the WavySlider, it is placed underneath the thumb. The lambda
 * receives a [SliderPositions] which is used to obtain the current active track.
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
    @Suppress("INVISIBLE_MEMBER")
    state.isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val press = Modifier.sliderTapModifier(
        state,
        interactionSource,
        enabled
    )
    val drag = Modifier.draggable(
        orientation = Orientation.Horizontal,
        reverseDirection = @Suppress("INVISIBLE_MEMBER") state.isRtl,
        enabled = enabled,
        interactionSource = interactionSource,
        onDragStopped = { @Suppress("INVISIBLE_MEMBER") state.gestureEndAction.invoke() },
        startDragImmediately = @Suppress("INVISIBLE_MEMBER") state.isDragging,
        state = state
    )

    Layout(
        content = {
            Box(modifier = Modifier.layoutId(SliderComponents.THUMB)) { thumb(state) }
            Box(modifier = Modifier.layoutId(SliderComponents.TRACK)) { track(state) }
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(
                minWidth = SliderTokens.HandleWidth,
                minHeight = SliderTokens.HandleHeight
            )
            .sliderSemantics(state, enabled)
            .focusable(enabled, interactionSource)
            .then(press)
            .then(drag)
    ) { measurables, constraints ->

        val thumbPlaceable = measurables.fastFirst { it.layoutId == SliderComponents.THUMB }.measure(constraints)
        val trackPlaceable = measurables.fastFirst { it.layoutId == SliderComponents.TRACK }.measure(
            constraints.offset(horizontal = - thumbPlaceable.width).copy(minHeight = 0)
        )

        val sliderWidth = thumbPlaceable.width + trackPlaceable.width
        val sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)

        @Suppress("INVISIBLE_MEMBER")
        state.updateDimensions(
            thumbPlaceable.width.toFloat(),
            sliderWidth
        )

        val trackOffsetX = thumbPlaceable.width / 2
        val thumbOffsetX = ((trackPlaceable.width) * @Suppress("INVISIBLE_MEMBER") state.coercedValueAsFraction).roundToInt()
        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
        val thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2

        layout(sliderWidth, sliderHeight) {
            trackPlaceable.placeRelative(
                trackOffsetX,
                trackOffsetY
            )
            thumbPlaceable.placeRelative(
                thumbOffsetX,
                thumbOffsetY
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
// No need to name it wavySliderSemantics
private fun Modifier.sliderSemantics(
    state: SliderState,
    enabled: Boolean
): Modifier {
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
                        val stepValue = androidx.compose.ui.util.lerp(
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
                        if (@Suppress("INVISIBLE_MEMBER") state.onValueChange != null) {
                            @Suppress("INVISIBLE_MEMBER") state.onValueChange?.let {
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
    }.progressSemantics(
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
            onPress = { @Suppress("INVISIBLE_MEMBER") state.onPress(it) },
            onTap = {
                state.dispatchRawDelta(0f)
                @Suppress("INVISIBLE_MEMBER")
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

// No need to name it WavySliderTokens
internal object SliderTokens {
    val HandleHeight = 20.0.dp
    val HandleWidth = 20.0.dp
}
