// Based on https://github.com/JetBrains/compose-multiplatform-core/blob/release/1.5.11/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Slider.kt

@file:Suppress("UnusedReceiverParameter")

package ir.mahozad.multiplatform.wavyslider.material3

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.*
import ir.mahozad.multiplatform.wavyslider.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.time.Duration

// Instead of directly exposing the following defaults as public properties,
// we want to provide them in the SliderDefaults object so the user can access all the defaults
// using that namespace. But SliderDefaults object is in Material library, and we cannot modify it.
// So, we provide the defaults as extension properties of SliderDefaults object.

val SliderDefaults.WaveLength: Dp get() = defaultWaveLength
val SliderDefaults.WaveHeight: Dp get() = defaultWaveHeight
val SliderDefaults.WavePeriod: Duration get() = defaultWavePeriod
val SliderDefaults.WaveMovement: WaveMovement get() = defaultWaveMovement
val SliderDefaults.WaveThickness: Dp get() = defaultTrackThickness
val SliderDefaults.TrackThickness: Dp get() = defaultTrackThickness
val SliderDefaults.Incremental: Boolean get() = defaultIncremental
val SliderDefaults.WaveAnimationSpecs: WaveAnimationSpecs get() = defaultWaveAnimationSpecs

private val ThumbWidth = SliderTokens.HandleWidth
private val ThumbHeight = SliderTokens.HandleHeight
private val ThumbSize = DpSize(ThumbWidth, ThumbHeight)

/**
 * The Default track for [WavySlider]
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
 * @param wavePeriod the duration it takes for the wave to move by [waveLength] horizontally.
 * Setting to [Duration.ZERO] or outside `Int.MIN_VALUE..Int.MAX_VALUE` milliseconds stops the movement.
 * @param waveMovement the horizontal movement of the whole wave.
 * @param waveThickness the thickness of the active line (whether animated or not).
 * @param trackThickness the thickness of the inactive line.
 * @param incremental whether to gradually increase height from zero at start to [waveHeight] at thumb.
 * @param animationSpecs animation configurations used for various properties of the wave.
 */
@Composable
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
    wavePeriod: Duration = SliderDefaults.WavePeriod,
    waveMovement: WaveMovement = SliderDefaults.WaveMovement,
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
    val phaseShiftAnimated by animatePhaseShift(waveLength, wavePeriod, waveMovement)
    val waveHeightAnimated by animateWaveHeight(waveHeight, animationSpecs.waveHeightAnimationSpec)
    val trackHeight = max(waveHeight + waveThickness, ThumbSize.height)
    Canvas(modifier = Modifier.fillMaxWidth().height(trackHeight)) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(0f, center.y)
        val sliderRight = Offset(size.width, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight
        val sliderValueOffset = Offset(sliderStart.x + (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.endInclusive, center.y)
        drawTrack(
            waveLength = waveLength,
            waveHeight = waveHeightAnimated,
            phaseShift = phaseShiftAnimated,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            sliderValueOffset = sliderValueOffset,
            sliderStart = sliderStart,
            sliderEnd = sliderEnd,
            incremental = incremental,
            inactiveTrackColor = inactiveTrackColor.value,
            activeTrackColor = activeTrackColor.value
        )
    }
}

/**
 * See the other overloaded Composable for documentations.
 */
@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    wavePeriod: Duration = SliderDefaults.WavePeriod,
    waveMovement: WaveMovement = SliderDefaults.WaveMovement,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs
) {
    WavySliderImpl(
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        value = value,
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = colors,
                enabled = enabled
            )
        },
        track = { sliderPositions ->
            SliderDefaults.Track(
                colors = colors,
                enabled = enabled,
                sliderPositions = sliderPositions,
                /////////////////
                /////////////////
                /////////////////
                waveLength = waveLength,
                waveHeight = waveHeight,
                wavePeriod = wavePeriod,
                waveMovement = waveMovement,
                waveThickness = waveThickness,
                trackThickness = trackThickness,
                incremental = incremental,
                animationSpecs = animationSpecs
            )
        }
    )
}

/**
 * A wavy slider much like the [Material Design 3 slider](https://m3.material.io/components/sliders).
 *
 * Setting [waveHeight] or [waveLength] to `0.dp` results in a regular Material [Slider].
 *
 * This component can also be used as a progress bar.
 *
 * Note that range sliders do not make sense for the wavy slider.
 * So, there is no RangeWavySlider counterpart.
 *
 * @param value current value of the WavySlider. it will be coerced to the range `0f..1f`.
 * @param onValueChange onValueChange callback in which value should be updated
 * @param modifier the [Modifier] to be applied to this WavySlider
 * @param enabled controls the enabled state of this WavySlider. When `false`, this component will not
 * respond to user input, and it will appear visually disabled and disabled to accessibility services.
 * @param onValueChangeFinished called when value change has ended. This should not be used to
 * update the slider value (use [onValueChange] instead), but rather to know when the user has
 * completed selecting a new value by ending a drag or a click.
 * @param colors [SliderColors] that will be used to resolve the colors used for this WavySlider in
 * different states. See [SliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this WavySlider. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this WavySlider in different states.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 * The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param wavePeriod the duration it takes for the wave to move by [waveLength] horizontally.
 * Setting to [Duration.ZERO] or outside `Int.MIN_VALUE..Int.MAX_VALUE` milliseconds stops the movement.
 * @param waveMovement the horizontal movement of the whole wave.
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
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    wavePeriod: Duration = SliderDefaults.WavePeriod,
    waveMovement: WaveMovement = SliderDefaults.WaveMovement,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs,
    /////////////////
    /////////////////
    /////////////////
    thumb: @Composable (SliderPositions) -> Unit = {
        SliderDefaults.Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled
        )
    },
    track: @Composable (SliderPositions) -> Unit = { sliderPositions ->
        SliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            sliderPositions = sliderPositions,
            /////////////////
            /////////////////
            /////////////////
            waveLength = waveLength,
            waveHeight = waveHeight,
            wavePeriod = wavePeriod,
            waveMovement = waveMovement,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            incremental = incremental,
            animationSpecs= animationSpecs
        )
    }
) {
    WavySliderImpl(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
        interactionSource = interactionSource,
        thumb = thumb,
        track = track
    )
}

@Composable
private fun WavySliderImpl(
    modifier: Modifier,
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)?,
    value: Float,
    thumb: @Composable (SliderPositions) -> Unit,
    track: @Composable (SliderPositions) -> Unit
) {
    val onValueChangeState = rememberUpdatedState<(Float) -> Unit> {
        if (it != value) {
            onValueChange(it)
        }
    }

    val tickFractions = remember { floatArrayOf() }

    val thumbWidth = remember { mutableFloatStateOf(ThumbWidth.value) }
    val totalWidth = remember { mutableIntStateOf(0) }

    fun scaleToUserValue(minPx: Float, maxPx: Float, offset: Float) =
        scale(minPx, maxPx, offset, 0f, 1f)

    fun scaleToOffset(minPx: Float, maxPx: Float, userValue: Float) =
        scale(0f, 1f, userValue, minPx, maxPx)

    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val rawOffset = remember { mutableFloatStateOf(scaleToOffset(0f, 0f, value)) }
    val pressOffset = remember { mutableFloatStateOf(0f) }
    val coerced = value.coerceIn(0f, 1f)

    val positionFraction = calcFraction(0f, 1f, coerced)
    val sliderPositions = remember(positionFraction, tickFractions) {
        SliderPositions(0f..positionFraction, tickFractions)
    }

    val draggableState = remember {
        SliderDraggableState {
            val maxPx = max(totalWidth.value - thumbWidth.value / 2, 0f)
            val minPx = min(thumbWidth.value / 2, maxPx)
            rawOffset.value = (rawOffset.value + it + pressOffset.value)
            pressOffset.value = 0f
            val offsetInTrack = snapValueToTick(rawOffset.value, tickFractions, minPx, maxPx)
            onValueChangeState.value.invoke(scaleToUserValue(minPx, maxPx, offsetInTrack))
        }
    }

    val gestureEndAction = rememberUpdatedState {
        if (!draggableState.isDragging) {
            // check isDragging in case the change is still in progress (touch -> drag case)
            onValueChangeFinished?.invoke()
        }
    }

    val press = Modifier.sliderTapModifier(
        draggableState,
        interactionSource,
        totalWidth.value,
        isRtl,
        rawOffset,
        gestureEndAction,
        pressOffset,
        enabled
    )

    val drag = Modifier.draggable(
        orientation = Orientation.Horizontal,
        reverseDirection = isRtl,
        enabled = enabled,
        interactionSource = interactionSource,
        onDragStopped = { _ -> gestureEndAction.value.invoke() },
        startDragImmediately = draggableState.isDragging,
        state = draggableState
    )

    Layout(
        {
            Box(modifier = Modifier.layoutId(SliderComponents.THUMB)) { thumb(sliderPositions) }
            Box(modifier = Modifier.layoutId(SliderComponents.TRACK)) { track(sliderPositions) }
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(
                minWidth = SliderTokens.HandleWidth,
                minHeight = SliderTokens.HandleHeight
            )
            .sliderSemantics(
                value,
                enabled,
                onValueChange,
                onValueChangeFinished,
                0f..1f,
                0
            )
            .focusable(enabled, interactionSource)
            .then(press)
            .then(drag)
    ) { measurables, constraints ->

        val thumbPlaceable = measurables.first {
            it.layoutId == SliderComponents.THUMB
        }.measure(constraints)

        val trackPlaceable = measurables.first {
            it.layoutId == SliderComponents.TRACK
        }.measure(
            constraints.offset(horizontal = - thumbPlaceable.width).copy(minHeight = 0)
        )

        val sliderWidth = thumbPlaceable.width + trackPlaceable.width
        val sliderHeight = max(trackPlaceable.height, thumbPlaceable.height)

        thumbWidth.value = thumbPlaceable.width.toFloat()
        totalWidth.value = sliderWidth

        val trackOffsetX = thumbPlaceable.width / 2
        val thumbOffsetX = ((trackPlaceable.width) * positionFraction).roundToInt()
        val trackOffsetY = (sliderHeight - trackPlaceable.height) / 2
        val thumbOffsetY = (sliderHeight - thumbPlaceable.height) / 2

        layout(
            sliderWidth,
            sliderHeight
        ) {
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

// No need to name it wavySliderSemantics
private fun Modifier.sliderSemantics(
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0
): Modifier {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    return semantics {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                var newValue = targetValue.coerceIn(valueRange.start, valueRange.endInclusive)
                val originalVal = newValue
                val resolvedValue = if (steps > 0) {
                    var distance: Float = newValue
                    for (i in 0..steps + 1) {
                        val stepValue = lerp(
                            valueRange.start,
                            valueRange.endInclusive,
                            i.toFloat() / (steps + 1)
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
                if (resolvedValue == coerced) {
                    false
                } else {
                    onValueChange(resolvedValue)
                    onValueChangeFinished?.invoke()
                    true
                }
            }
        )
    }.progressSemantics(value, valueRange, steps)
}

// No need to name it wavySliderTapModifier
private fun Modifier.sliderTapModifier(
    draggableState: DraggableState,
    interactionSource: MutableInteractionSource,
    maxPx: Int,
    isRtl: Boolean,
    rawOffset: State<Float>,
    gestureEndAction: State<() -> Unit>,
    pressOffset: MutableState<Float>,
    enabled: Boolean
) = composed(
    factory = {
        if (enabled) {
            val scope = rememberCoroutineScope()
            pointerInput(draggableState, interactionSource, maxPx, isRtl) {
                detectTapGestures(
                    onPress = { pos ->
                        val to = if (isRtl) maxPx - pos.x else pos.x
                        pressOffset.value = to - rawOffset.value
                        try {
                            awaitRelease()
                        } catch (_: GestureCancellationException) {
                            pressOffset.value = 0f
                        }
                    },
                    onTap = {
                        scope.launch {
                            draggableState.drag(MutatePriority.UserInput) {
                                // just trigger animation, press offset will be applied
                                dragBy(0f)
                            }
                            gestureEndAction.value.invoke()
                        }
                    }
                )
            }
        } else {
            this
        }
    },
    inspectorInfo = debugInspectorInfo {
        name = "wavySliderTapModifier"
        properties["draggableState"] = draggableState
        properties["interactionSource"] = interactionSource
        properties["maxPx"] = maxPx
        properties["isRtl"] = isRtl
        properties["rawOffset"] = rawOffset
        properties["gestureEndAction"] = gestureEndAction
        properties["pressOffset"] = pressOffset
        properties["enabled"] = enabled
    })

private class SliderDraggableState(
    val onDelta: (Float) -> Unit
) : DraggableState {

    var isDragging by mutableStateOf(false)
        private set

    private val dragScope: DragScope = object : DragScope {
        override fun dragBy(pixels: Float): Unit = onDelta(pixels)
    }

    private val scrollMutex = MutatorMutex()

    override suspend fun drag(
        dragPriority: MutatePriority,
        block: suspend DragScope.() -> Unit
    ): Unit = coroutineScope {
        isDragging = true
        scrollMutex.mutateWith(dragScope, dragPriority, block)
        isDragging = false
    }

    override fun dispatchRawDelta(delta: Float) {
        return onDelta(delta)
    }
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
