// Based on https://github.com/JetBrains/compose-multiplatform-core/blob/release/1.5.11/compose/material/material/src/commonMain/kotlin/androidx/compose/material/Slider.kt

@file:Suppress("UnusedReceiverParameter")

package ir.mahozad.multiplatform.wavyslider.material

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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

private val ThumbRadius = 10.dp
private val ThumbRippleRadius = 24.dp
private val ThumbDefaultElevation = 1.dp
private val ThumbPressedElevation = 6.dp
private val SliderMinWidth = 144.dp // TODO: clarify min width
private val DefaultSliderConstraints = Modifier.widthIn(min = SliderMinWidth)

// Instead of directly exposing the following defaults as public properties,
// we want to provide them in the SliderDefaults object so the user can access all the defaults
// using that namespace. But SliderDefaults object is in Material library, and we cannot modify it.
// So, we provide the defaults as extension properties of SliderDefaults object.

val SliderDefaults.WaveLength: Dp get() = defaultWaveLength
val SliderDefaults.WaveHeight: Dp get() = defaultWaveHeight
val SliderDefaults.WaveVelocity: Pair<Dp, WaveDirection> get() = defaultWaveVelocity
val SliderDefaults.WaveThickness: Dp get() = defaultTrackThickness
val SliderDefaults.WaveAnimationSpecs: WaveAnimationSpecs get() = defaultWaveAnimationSpecs
val SliderDefaults.TrackThickness: Dp get() = defaultTrackThickness
val SliderDefaults.Incremental: Boolean get() = defaultIncremental

/**
 * A wavy slider much like the [Material Design 2 slider](https://m2.material.io/components/sliders).
 *
 * Setting [waveHeight] or [waveLength] to `0.dp` results in a regular Material [Slider].
 *
 * This component can also be used as a progress bar.
 *
 * Note that range sliders do not make sense for the wavy slider.
 * So, there is no RangeWavySlider counterpart.
 *
 * @param value current value of the WavySlider. Will be coerced to [valueRange].
 * @param onValueChange lambda in which value should be updated
 * @param modifier modifiers for the WavySlider layout
 * @param enabled whether or not component is enabled and can be interacted with or not
 * @param valueRange range of values that WavySlider value can take. Passed [value] will be coerced to
 * this range.
 * @param onValueChangeFinished lambda to be invoked when value change has ended. This callback
 * shouldn't be used to update the wavy slider value (use [onValueChange] for that), but rather to
 * know when the user has completed selecting a new value by ending a drag or a click.
 * @param interactionSource the [MutableInteractionSource] representing the stream of
 * [Interaction]s for this WavySlider. You can create and pass in your own remembered
 * [MutableInteractionSource] if you want to observe [Interaction]s and customize the
 * appearance / behavior of this WavySlider in different [Interaction]s.
 * @param colors [SliderColors] that will be used to determine the color of the WavySlider parts in
 * different state. See [SliderDefaults.colors] to customize.
 *
 *
 *
 * @param waveLength the distance over which the wave's shape repeats.
 * @param waveHeight the total height of the wave (from crest to trough i.e. amplitude * 2).
 * The final rendered height of the wave will be [waveHeight] + [waveThickness].
 * @param waveVelocity the speed per second and movement direction of the whole wave.
 * Setting speed to `0.dp` or less stops the movement.
 * @param waveThickness the thickness of the active line (whether animated or not).
 * @param trackThickness the thickness of the inactive line.
 * @param incremental whether to gradually increase height from zero at start to [waveHeight] at thumb.
 * @param animationSpecs animation configurations used for various properties of the wave.
 */
@Composable
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: SliderColors = SliderDefaults.colors(),
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = SliderDefaults.WaveLength,
    waveHeight: Dp = SliderDefaults.WaveHeight,
    waveVelocity: Pair<Dp, WaveDirection> = SliderDefaults.WaveVelocity,
    waveThickness: Dp = SliderDefaults.WaveThickness,
    trackThickness: Dp = SliderDefaults.TrackThickness,
    incremental: Boolean = SliderDefaults.Incremental,
    animationSpecs: WaveAnimationSpecs = SliderDefaults.WaveAnimationSpecs
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val onValueChangeState = rememberUpdatedState(onValueChange)
    val onValueChangeFinishedState = rememberUpdatedState(onValueChangeFinished)
    val focusRequester = remember { FocusRequester() }
    BoxWithConstraints(
        modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(minWidth = ThumbRadius * 2, minHeight = ThumbRadius * 2)
            .sliderSemantics(
                value,
                enabled,
                onValueChange,
                onValueChangeFinished,
                valueRange
            )
            .focusRequester(focusRequester)
            .focusable(enabled, interactionSource)
            .slideOnKeyEvents(enabled, valueRange, value, isRtl, onValueChangeState, onValueChangeFinishedState)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val maxPx: Float
        val minPx: Float

        with(LocalDensity.current) {
            maxPx = max(widthPx - ThumbRadius.toPx(), 0f)
            minPx = min(ThumbRadius.toPx(), maxPx)
        }

        fun scaleToUserValue(offset: Float) =
            scale(minPx, maxPx, offset, valueRange.start, valueRange.endInclusive)

        fun scaleToOffset(userValue: Float) =
            scale(valueRange.start, valueRange.endInclusive, userValue, minPx, maxPx)

        val rawOffset = remember { mutableFloatStateOf(scaleToOffset(value)) }
        val pressOffset = remember { mutableFloatStateOf(0f) }

        val draggableState = remember(minPx, maxPx, valueRange) {
            SliderDraggableState {
                rawOffset.floatValue = (rawOffset.floatValue + it + pressOffset.floatValue)
                pressOffset.floatValue = 0f
                val offsetInTrack = rawOffset.floatValue.coerceIn(minPx, maxPx)
                onValueChangeState.value.invoke(scaleToUserValue(offsetInTrack))
            }
        }

        CorrectValueSideEffect(::scaleToOffset, valueRange, minPx..maxPx, rawOffset, value)

        val gestureEndAction = rememberUpdatedState { _: Float ->
            focusRequester.requestFocus()
            if (!draggableState.isDragging) {
                // check ifDragging in case the change is still in progress (touch -> drag case)
                onValueChangeFinished?.invoke()
            }
        }
        val press = Modifier.sliderTapModifier(
            draggableState,
            interactionSource,
            widthPx,
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
            onDragStopped = { velocity -> gestureEndAction.value.invoke(velocity) },
            startDragImmediately = draggableState.isDragging,
            state = draggableState
        )

        val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
        val fraction = calcFraction(valueRange.start, valueRange.endInclusive, coerced)
        SliderImpl(
            enabled,
            fraction,
            colors,
            maxPx - minPx,
            interactionSource,
            modifier = press.then(drag),
            /////////////////
            /////////////////
            /////////////////
            waveLength,
            waveHeight,
            waveVelocity,
            waveThickness,
            trackThickness,
            incremental,
            animationSpecs
        )
    }
}

// TODO: Edge case - losing focus on slider while key is pressed will end up with onValueChangeFinished not being invoked
private fun Modifier.slideOnKeyEvents(
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    value: Float,
    isRtl: Boolean,
    onValueChangeState: State<(Float) -> Unit>,
    onValueChangeFinishedState: State<(() -> Unit)?>
): Modifier {
    return this.onKeyEvent {
        if (!enabled) return@onKeyEvent false

        when (it.type) {
            KeyEventType.KeyDown -> {
                val rangeLength = abs(valueRange.endInclusive - valueRange.start)
                // A user is not limited by a step length (delta) when using touch or mouse.
                // But it is not possible to adjust the value continuously when using keyboard buttons -
                // the delta has to be discrete. In this case, 1% of the valueRange seems to make sense.
                val delta = rangeLength / 100
                when {
                    it.isDirectionUp -> {
                        onValueChangeState.value((value + delta).coerceIn(valueRange))
                        true
                    }

                    it.isDirectionDown -> {
                        onValueChangeState.value((value - delta).coerceIn(valueRange))
                        true
                    }

                    it.isDirectionRight -> {
                        val sign = if (isRtl) -1 else 1
                        onValueChangeState.value((value + sign * delta).coerceIn(valueRange))
                        true
                    }

                    it.isDirectionLeft -> {
                        val sign = if (isRtl) -1 else 1
                        onValueChangeState.value((value - sign * delta).coerceIn(valueRange))
                        true
                    }

                    it.isHome -> {
                        onValueChangeState.value(valueRange.start)
                        true
                    }

                    it.isMoveEnd -> {
                        onValueChangeState.value(valueRange.endInclusive)
                        true
                    }

                    it.isPgUp -> {
                        val page = 10
                        onValueChangeState.value((value - page * delta).coerceIn(valueRange))
                        true
                    }

                    it.isPgDn -> {
                        val page = 10
                        onValueChangeState.value((value + page * delta).coerceIn(valueRange))
                        true
                    }

                    else -> false
                }
            }

            KeyEventType.KeyUp -> {
                if (it.isDirectionDown || it.isDirectionUp || it.isDirectionRight
                    || it.isDirectionLeft || it.isHome || it.isMoveEnd || it.isPgUp || it.isPgDn
                ) {
                    onValueChangeFinishedState.value?.invoke()
                    true
                } else {
                    false
                }
            }

            else -> false
        }
    }
}

@Composable
private fun SliderImpl(
    enabled: Boolean,
    positionFraction: Float,
    colors: SliderColors,
    width: Float,
    interactionSource: MutableInteractionSource,
    modifier: Modifier,
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp,
    waveHeight: Dp,
    waveVelocity:  Pair<Dp, WaveDirection>,
    waveThickness: Dp,
    trackThickness: Dp,
    incremental: Boolean,
    animationSpecs: WaveAnimationSpecs
) {
    Box(modifier.then(DefaultSliderConstraints)) {
        val thumbPx: Float
        val widthDp: Dp
        with(LocalDensity.current) {
            thumbPx = ThumbRadius.toPx()
            widthDp = width.toDp()
        }

        val thumbSize = ThumbRadius * 2
        val offset = widthDp * positionFraction

        Track(
            Modifier.fillMaxSize(),
            colors,
            enabled,
            positionFraction,
            thumbPx,
            /////////////////
            /////////////////
            /////////////////
            waveLength,
            waveHeight,
            waveVelocity,
            waveThickness,
            trackThickness,
            incremental,
            animationSpecs
        )
        SliderThumb(Modifier, offset, interactionSource, colors, enabled, thumbSize)
    }
}

@Composable
private fun Track(
    modifier: Modifier,
    colors: SliderColors,
    enabled: Boolean,
    positionFractionEnd: Float,
    thumbPx: Float,
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp,
    waveHeight: Dp,
    waveVelocity:  Pair<Dp, WaveDirection>,
    waveThickness: Dp,
    trackThickness: Dp,
    incremental: Boolean,
    animationSpecs: WaveAnimationSpecs
) {
    val inactiveTrackColor = colors.trackColor(enabled, active = false)
    val activeTrackColor = colors.trackColor(enabled, active = true)
    val phaseShiftAnimated by animatePhaseShift(waveVelocity, animationSpecs.waveVelocityAnimationSpec)
    val waveHeightAnimated by animateWaveHeight(waveHeight, animationSpecs.waveHeightAnimationSpec)
    val trackHeight = max(waveHeight + waveThickness, ThumbRadius * 2)
    Canvas(modifier = Modifier.fillMaxWidth().height(trackHeight)) {
        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(thumbPx, center.y)
        val sliderRight = Offset(size.width - thumbPx, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight
        val sliderValueOffset = Offset(sliderStart.x + (sliderEnd.x - sliderStart.x) * positionFractionEnd, center.y)
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

@Composable
private fun BoxScope.SliderThumb(
    modifier: Modifier,
    offset: Dp,
    interactionSource: MutableInteractionSource,
    colors: SliderColors,
    enabled: Boolean,
    thumbSize: Dp
) {
    Box(
        Modifier
            .padding(start = offset)
            .align(Alignment.CenterStart)) {
        val interactions = remember { mutableStateListOf<Interaction>() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> interactions.add(interaction)
                    is PressInteraction.Release -> interactions.remove(interaction.press)
                    is PressInteraction.Cancel -> interactions.remove(interaction.press)
                    is DragInteraction.Start -> interactions.add(interaction)
                    is DragInteraction.Stop -> interactions.remove(interaction.start)
                    is DragInteraction.Cancel -> interactions.remove(interaction.start)
                }
            }
        }

        val elevation = if (interactions.isNotEmpty()) {
            ThumbPressedElevation
        } else {
            ThumbDefaultElevation
        }
        Spacer(
            modifier
                .size(thumbSize, thumbSize)
                .indication(
                    interactionSource = interactionSource,
                    indication = rememberRipple(bounded = false, radius = ThumbRippleRadius)
                )
                .hoverable(interactionSource = interactionSource)
                .shadow(if (enabled) elevation else 0.dp, CircleShape, clip = false)
                .background(colors.thumbColor(enabled).value, CircleShape)
        )
    }
}

@Composable
private fun CorrectValueSideEffect(
    scaleToOffset: (Float) -> Float,
    valueRange: ClosedFloatingPointRange<Float>,
    trackRange: ClosedFloatingPointRange<Float>,
    valueState: MutableState<Float>,
    value: Float
) {
    SideEffect {
        val error = (valueRange.endInclusive - valueRange.start) / 1000
        val newOffset = scaleToOffset(value)
        if (abs(newOffset - valueState.value) > error) {
            if (valueState.value in trackRange) {
                valueState.value = newOffset
            }
        }
    }
}

private fun Modifier.sliderSemantics(
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
): Modifier {
    val coerced = value.coerceIn(valueRange.start, valueRange.endInclusive)
    return semantics {
        if (!enabled) disabled()
        setProgress(
            action = { targetValue ->
                val newValue = targetValue.coerceIn(valueRange.start, valueRange.endInclusive)
                // This is to keep it consistent with AbsSeekbar.java: return false if no
                // change from current.
                if (newValue == coerced) {
                    false
                } else {
                    onValueChange(newValue)
                    onValueChangeFinished?.invoke()
                    true
                }
            }
        )
    }.progressSemantics(value, valueRange, 0)
}

private fun Modifier.sliderTapModifier(
    draggableState: DraggableState,
    interactionSource: MutableInteractionSource,
    maxPx: Float,
    isRtl: Boolean,
    rawOffset: State<Float>,
    gestureEndAction: State<(Float) -> Unit>,
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
                            gestureEndAction.value.invoke(0f)
                        }
                    }
                )
            }
        } else {
            this
        }
    },
    inspectorInfo = debugInspectorInfo {
        name = "sliderTapModifier"
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
