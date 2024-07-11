// Based on https://github.com/JetBrains/compose-multiplatform-core/blob/release/1.7.0-alpha01/compose/material/material/src/commonMain/kotlin/androidx/compose/material/Slider.kt

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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import ir.mahozad.multiplatform.wavyslider.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

private val ThumbRadius = 10.dp
private val ThumbRippleRadius = 24.dp
private val ThumbDefaultElevation = 1.dp
private val ThumbPressedElevation = 6.dp
private val SliderMinWidth = 144.dp // TODO from compose-multiplatform-core: clarify min width
private val DefaultSliderConstraints = Modifier.widthIn(min = SliderMinWidth)

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
val SliderDefaults.WaveThickness: Dp get() = SliderDefaults.TrackThickness
/**
 * Default track thickness
 */
val SliderDefaults.TrackThickness: Dp get() = 4.dp
/**
 * Default progression of wave height (whether gradual or not)
 */
val SliderDefaults.Incremental: Boolean get() = defaultIncremental
/**
 * Default animation configurations for various properties of the wave
 */
val SliderDefaults.WaveAnimationSpecs: WaveAnimationSpecs get() = defaultWaveAnimationSpecs

/**
 * A wavy slider much like the [Material Design 2 Slider](https://m2.material.io/components/sliders).
 *
 * Setting [waveHeight] or [waveLength] to `0.dp` results in a regular [Slider].
 *
 * Note that range sliders do not make sense for the WavySlider.
 * So, there is no RangeWavySlider counterpart.
 *
 * @param value current value of the WavySlider. Will be coerced to [valueRange].
 * @param onValueChange lambda in which value should be updated.
 * @param modifier modifiers for the WavySlider layout.
 * @param enabled whether or not component is enabled and can be interacted with or not.
 * @param valueRange range of values that WavySlider value can take. Passed [value] will be coerced to
 *   this range.
 * @param onValueChangeFinished lambda to be invoked when value change has ended. This callback
 *   shouldn't be used to update the WavySlider value (use [onValueChange] for that), but rather to
 *   know when the user has completed selecting a new value by ending a drag or a click.
 * @param interactionSource an optional hoisted [MutableInteractionSource] for observing and
 *   emitting [Interaction]s for this WavySlider. You can use this to change the WavySlider's
 *   appearance or preview the WavySlider in different states. Note that if `null` is provided,
 *   interactions will still happen internally.
 * @param colors [SliderColors] that will be used to determine the color of the WavySlider parts in
 *   different state. See [SliderDefaults.colors] to customize.
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
fun WavySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    colors: SliderColors = SliderDefaults.colors(),
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
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
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
        WavySliderImpl(
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

// TODO from compose-multiplatform-core: Edge case - losing focus on slider while key is pressed will end up with onValueChangeFinished not being invoked
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
private fun WavySliderImpl(
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
    waveVelocity: WaveVelocity,
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
            modifier,
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
    waveVelocity: WaveVelocity,
    waveThickness: Dp,
    trackThickness: Dp,
    incremental: Boolean,
    animationSpecs: WaveAnimationSpecs
) {
    val activeTrackColor by colors.trackColor(enabled, active = true)
    val inactiveTrackColor by colors.trackColor(enabled, active = false)
    val waveSpreadAnimated by animateWaveSpread(animationSpecs.waveStartSpreadAnimationSpec)
    val waveHeightAnimated by animateWaveHeight(waveHeight, animationSpecs.waveHeightAnimationSpec)
    val waveShiftAnimated by animateWaveShift(waveVelocity, animationSpecs.waveVelocityAnimationSpec)
    val trackHeight = max(waveThickness + waveHeight.value.absoluteValue.dp, ThumbRadius * 2)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .rotate(if (LocalLayoutDirection.current == LayoutDirection.Rtl) 180f else 0f)
    ) {
        val sliderStart = Offset(thumbPx, center.y)
        val sliderEnd = Offset(size.width - thumbPx, center.y)
        val sliderValueOffset = Offset(sliderStart.x + (sliderEnd.x - sliderStart.x) * positionFractionEnd, center.y)
        drawTrackActivePart(
            startOffset = sliderStart,
            valueOffset = sliderValueOffset,
            waveLength = waveLength,
            waveHeight = waveHeightAnimated,
            waveSpread = waveSpreadAnimated,
            waveShift = waveShiftAnimated,
            waveThickness = waveThickness,
            incremental = incremental,
            color = activeTrackColor
        )
        drawTrackInactivePart(
            color = inactiveTrackColor,
            thickness = trackThickness,
            startOffset = sliderValueOffset,
            endOffset = sliderEnd
        )
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
    color: Color
) {
    if (waveThickness <= 0.dp) return
    val path = if (waveLength <= 0.dp || waveHeight == 0.dp) {
        createFlatPath(
            startOffset,
            valueOffset
        )
    } else {
        createWavyPath(
            startOffset,
            valueOffset,
            waveLength,
            waveHeight,
            waveSpread,
            waveShift,
            incremental
        )
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = waveThickness.toPx(),
            join = StrokeJoin.Round,
            cap = StrokeCap.Round
        )
    )
}

private inline fun DrawScope.createFlatPath(
    startOffset: Offset,
    valueOffset: Offset
): Path = Path().apply {
    moveTo(startOffset.x, center.y)
    lineTo(valueOffset.x, center.y)
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
                    indication =
                        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                        rippleOrFallbackImplementation(
                            bounded = false,
                            radius = ThumbRippleRadius
                        )
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
