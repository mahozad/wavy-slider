package ir.mahozad.multiplatform.wavyslider.material3

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.*
import ir.mahozad.multiplatform.wavyslider.*
import ir.mahozad.multiplatform.wavyslider.lerp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.*

private val ThumbWidth = WavySliderTokens.HandleWidth
private val ThumbHeight = WavySliderTokens.HandleHeight
private val ThumbSize = DpSize(ThumbWidth, ThumbHeight)
private val ThumbDefaultElevation = 1.dp
private val ThumbPressedElevation = 6.dp

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
    colors: WavySliderColors = WavySliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = defaultWaveSize,
    waveHeight: Dp = defaultWaveSize,
    waveThickness: Dp = defaultTrackThickness,
    trackThickness: Dp? = defaultTrackThickness,
    animationDirection: WaveAnimationDirection = WaveAnimationDirection.UNSPECIFIED,
    shouldFlatten: Boolean = false
) {
    WavySliderImpl(
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        value = value,
        thumb = {
            WavySliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = colors,
                enabled = enabled
            )
        },
        track = { sliderPositions ->
            WavySliderDefaults.Track(
                colors = colors,
                enabled = enabled,
                sliderPositions = sliderPositions,
                /////////////////
                /////////////////
                /////////////////
                waveLength = waveLength,
                waveHeight = waveHeight,
                waveThickness = waveThickness,
                trackThickness = trackThickness,
                animationDirection = animationDirection,
                shouldFlatten = shouldFlatten
            )
        }
    )
}

/**
 * A wavy slider much like the [Material Design 3 slider](https://m3.material.io/components/sliders).
 *
 * By setting [waveHeight] to `0.dp` it becomes just a regular Material [Slider].
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
 * @param colors [WavySliderColors] that will be used to resolve the colors used for this WavySlider in
 * different states. See [WavySliderDefaults.colors].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this WavySlider. You can create and pass in your own `remember`ed instance to observe
 * [Interaction]s and customize the appearance / behavior of this WavySlider in different states.
 * @param waveLength the distance over which the wave's shape repeats (must be > 0.dp)
 * @param waveHeight the total height of the wave (from crest to trough) (in other words, amplitude * 2)
 * @param waveThickness the thickness of the active line (whether animated or not)
 * @param trackThickness the thickness of the inactive line
 * @param animationDirection the direction of wave movement which is, by default,
 * from right to left for LTR layouts and from left to right for RTL layouts.
 * Setting to [WaveAnimationDirection.UNSPECIFIED] also does the same thing
 * @param shouldFlatten whether to decrease the wave height the farther it is from the thumb
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
    colors: WavySliderColors = WavySliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    /////////////////
    /////////////////
    /////////////////
    waveLength: Dp = defaultWaveSize,
    waveHeight: Dp = defaultWaveSize,
    waveThickness: Dp = defaultTrackThickness,
    trackThickness: Dp? = defaultTrackThickness,
    animationDirection: WaveAnimationDirection = WaveAnimationDirection.UNSPECIFIED,
    shouldFlatten: Boolean = false,
    /////////////////
    /////////////////
    /////////////////
    thumb: @Composable (SliderPositions) -> Unit = {
        WavySliderDefaults.Thumb(
            interactionSource = interactionSource,
            colors = colors,
            enabled = enabled
        )
    },
    track: @Composable (SliderPositions) -> Unit = { sliderPositions ->
        WavySliderDefaults.Track(
            colors = colors,
            enabled = enabled,
            sliderPositions = sliderPositions,
            /////////////////
            /////////////////
            /////////////////
            waveLength = waveLength,
            waveHeight = waveHeight,
            waveThickness = waveThickness,
            trackThickness = trackThickness,
            animationDirection = animationDirection,
            shouldFlatten = shouldFlatten
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
        WavySliderDraggableState {
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

    val press = Modifier.wavySliderTapModifier(
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
            Box(modifier = Modifier.layoutId(WavySliderComponents.THUMB)) { thumb(sliderPositions) }
            Box(modifier = Modifier.layoutId(WavySliderComponents.TRACK)) { track(sliderPositions) }
        },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .requiredSizeIn(
                minWidth = WavySliderTokens.HandleWidth,
                minHeight = WavySliderTokens.HandleHeight
            )
            .wavySliderSemantics(
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
            it.layoutId == WavySliderComponents.THUMB
        }.measure(constraints)

        val trackPlaceable = measurables.first {
            it.layoutId == WavySliderComponents.TRACK
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

/**
 * Object to hold defaults used by [WavySlider]
 */
@Stable
object WavySliderDefaults {

    /**
     * Creates a [WavySliderColors] that represents the different colors used in parts of the
     * [WavySlider] in different states.
     *
     * For the name references below the words "active" and "inactive" are used. Active part of
     * the WavySlider is filled with progress, so if slider's progress is 30% out of 100%, left (or
     * right in RTL) 30% of the track will be active, while the rest is inactive.
     *
     * @param thumbColor thumb color when enabled
     * @param activeTrackColor color of the track in the part that is "active", meaning that the
     * thumb is ahead of it
     * @param inactiveTrackColor color of the track in the part that is "inactive", meaning that the
     * thumb is before it
     * @param disabledThumbColor thumb colors when disabled
     * @param disabledActiveTrackColor color of the track in the "active" part when the WavySlider is
     * disabled
     * @param disabledInactiveTrackColor color of the track in the "inactive" part when the
     * Slider is disabled
     */
    @Composable
    fun colors(
        thumbColor: Color = WavySliderTokens.HandleColor.toColor(),
        activeTrackColor: Color = WavySliderTokens.ActiveTrackColor.toColor(),
        inactiveTrackColor: Color = WavySliderTokens.InactiveTrackColor.toColor(),
        disabledThumbColor: Color = WavySliderTokens.DisabledHandleColor
            .toColor()
            .copy(alpha = WavySliderTokens.DisabledHandleOpacity)
            .compositeOver(MaterialTheme.colorScheme.surface),
        disabledActiveTrackColor: Color =
            WavySliderTokens.DisabledActiveTrackColor
                .toColor()
                .copy(alpha = WavySliderTokens.DisabledActiveTrackOpacity),
        disabledInactiveTrackColor: Color =
            WavySliderTokens.DisabledInactiveTrackColor
                .toColor()
                .copy(alpha = WavySliderTokens.DisabledInactiveTrackOpacity)
    ): WavySliderColors = WavySliderColors(
        thumbColor = thumbColor,
        activeTrackColor = activeTrackColor,
        inactiveTrackColor = inactiveTrackColor,
        disabledThumbColor = disabledThumbColor,
        disabledActiveTrackColor = disabledActiveTrackColor,
        disabledInactiveTrackColor = disabledInactiveTrackColor
    )

    /**
     * The Default thumb for [WavySlider]
     *
     * @param interactionSource the [MutableInteractionSource] representing the stream of
     * [Interaction]s for this thumb. You can create and pass in your own `remember`ed
     * instance to observe.
     * @param modifier the [Modifier] to be applied to the thumb.
     * @param colors [WavySliderColors] that will be used to resolve the colors used for this thumb in
     * different states. See [WavySliderDefaults.colors].
     * @param enabled controls the enabled state of this WavySlider. When `false`, this component will
     * not respond to user input, and it will appear visually disabled and disabled to
     * accessibility services.
     */
    @Composable
    fun Thumb(
        interactionSource: MutableInteractionSource,
        modifier: Modifier = Modifier,
        colors: WavySliderColors = colors(),
        enabled: Boolean = true,
        thumbSize: DpSize = ThumbSize
    ) {
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
        val shape = CircleShape

        Spacer(
            modifier
                .size(thumbSize)
                .indication(
                    interactionSource = interactionSource,
                    indication = rememberRipple(
                        bounded = false,
                        radius = WavySliderTokens.StateLayerSize / 2
                    )
                )
                .hoverable(interactionSource = interactionSource)
                .shadow(if (enabled) elevation else 0.dp, shape, clip = false)
                .background(colors.thumbColor(enabled).value, shape)
        )
    }

    /**
     * The Default track for [WavySlider]
     *
     * @param sliderPositions [SliderPositions] which is used to obtain the current active track.
     * @param modifier the [Modifier] to be applied to the track.
     * @param colors [WavySliderColors] that will be used to resolve the colors used for this track in
     * different states. See [WavySliderDefaults.colors].
     * @param enabled controls the enabled state of this WavySlider. When `false`, this component will
     * not respond to user input, and it will appear visually disabled and disabled to
     * accessibility services.
     */
    @Composable
    fun Track(
        sliderPositions: SliderPositions,
        modifier: Modifier = Modifier,
        colors: WavySliderColors = colors(),
        enabled: Boolean = true,
        /////////////////
        /////////////////
        /////////////////
        waveLength: Dp,
        waveHeight: Dp,
        waveThickness: Dp,
        trackThickness: Dp?,
        animationDirection: WaveAnimationDirection,
        shouldFlatten: Boolean
    ) {
        require(waveLength > 0.dp)

        val inactiveTrackColor = colors.trackColor(enabled, active = false)
        val activeTrackColor = colors.trackColor(enabled, active = true)
        val waveLengthPx: Float
        val waveHeightPx: Float
        val waveThicknessPx: Float
        val trackThicknessPx: Float
        with(LocalDensity.current) {
            waveLengthPx = waveLength.toPx()
            waveHeightPx = waveHeight.toPx()
            waveThicknessPx = waveThickness.toPx()
            trackThicknessPx = trackThickness?.toPx() ?: 0f
        }
        val waveHeightAnimated by animateFloatAsState(
            waveHeightPx,
            tween(defaultWaveHeightChangeDuration.inWholeMilliseconds.toInt(), easing = LinearEasing)
        )
        val wavePosition by rememberInfiniteTransition()
            .animateFloat(
                initialValue = 0f,
                targetValue = if (animationDirection == WaveAnimationDirection.LTR) {
                    waveLengthPx
                } else if (animationDirection == WaveAnimationDirection.RTL) {
                    -waveLengthPx
                } else if (LocalLayoutDirection.current == LayoutDirection.Rtl) {
                    waveLengthPx
                } else {
                    -waveLengthPx
                },
                animationSpec = infiniteRepeatable(
                    animation = tween(defaultWavePeriod.inWholeMilliseconds.toInt(), easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                // To take into account the wave height in component overall height,
                // and to prevent the extremes of the wave from being partially clipped,
                // and to make sure proper component layout when waveHeight is 0.dp
                .height(waveHeight.coerceAtLeast(48.dp))
        ) {
            val isRtl = layoutDirection == LayoutDirection.Rtl
            val sliderLeft = Offset(0f, center.y)
            val sliderRight = Offset(size.width, center.y)
            val sliderStart = if (isRtl) sliderRight else sliderLeft
            val sliderEnd = if (isRtl) sliderLeft else sliderRight
            val sliderValueOffset = Offset(
                sliderStart.x + (sliderEnd.x - sliderStart.x) * sliderPositions.activeRange.endInclusive,
                center.y
            )
            if (trackThickness != null && trackThickness > 0.dp) {
                drawLine(
                    strokeWidth = trackThicknessPx,
                    color = inactiveTrackColor.value,
                    start = sliderValueOffset,
                    end = sliderEnd,
                    cap = StrokeCap.Round
                )
            }
            val wave = Path().apply {
                val startX = sliderStart.x + /* One extra required wave at the start */ waveLengthPx * if (isRtl) 1 else -1
                val length = (sliderValueOffset.x - startX).absoluteValue + /* One extra required wave at the end */ waveLengthPx
                val totalWaveCount = ceil(length / waveLengthPx).toInt()
                val heightFactors = if (shouldFlatten) {
                    generateHeightFactors(totalWaveCount)
                } else {
                    FloatArray(totalWaveCount)
                }
                moveTo(startX, center.y)
                for (i in 0 until totalWaveCount) {
                    relativeCubicTo(
                        /* Control 1: */ waveLengthPx / 2 * if (isRtl) -1 else 1, (waveHeightAnimated / 2) * if (shouldFlatten) heightFactors[i] else 1f,
                        /* Control 2: */ waveLengthPx / 2 * if (isRtl) -1 else 1, (-waveHeightAnimated / 2) * if (shouldFlatten) heightFactors[i] else 1f,
                        /* End point: */ waveLengthPx * if (isRtl) -1 else 1, 0f
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
                        color = activeTrackColor.value,
                        style = Stroke(waveThicknessPx)
                    )
                }
            }
        }
    }
}

private fun Modifier.wavySliderSemantics(
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

private fun Modifier.wavySliderTapModifier(
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

@Immutable
class WavySliderColors internal constructor(
    private val thumbColor: Color,
    private val activeTrackColor: Color,
    private val inactiveTrackColor: Color,
    private val disabledThumbColor: Color,
    private val disabledActiveTrackColor: Color,
    private val disabledInactiveTrackColor: Color
) {

    @Composable
    internal fun thumbColor(enabled: Boolean): State<Color> {
        return rememberUpdatedState(if (enabled) thumbColor else disabledThumbColor)
    }

    @Composable
    internal fun trackColor(enabled: Boolean, active: Boolean): State<Color> {
        return rememberUpdatedState(
            if (enabled) {
                if (active) activeTrackColor else inactiveTrackColor
            } else {
                if (active) disabledActiveTrackColor else disabledInactiveTrackColor
            }
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is WavySliderColors) return false

        if (thumbColor != other.thumbColor) return false
        if (activeTrackColor != other.activeTrackColor) return false
        if (inactiveTrackColor != other.inactiveTrackColor) return false
        if (disabledThumbColor != other.disabledThumbColor) return false
        if (disabledActiveTrackColor != other.disabledActiveTrackColor) return false
        if (disabledInactiveTrackColor != other.disabledInactiveTrackColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = thumbColor.hashCode()
        result = 31 * result + activeTrackColor.hashCode()
        result = 31 * result + inactiveTrackColor.hashCode()
        result = 31 * result + disabledThumbColor.hashCode()
        result = 31 * result + disabledActiveTrackColor.hashCode()
        result = 31 * result + disabledInactiveTrackColor.hashCode()
        return result
    }
}

private class WavySliderDraggableState(
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

private enum class WavySliderComponents {
    THUMB,
    TRACK
}

/** Converts a color token key to the local color scheme provided by the theme */
@ReadOnlyComposable
@Composable
internal fun ColorSchemeKeyTokens.toColor(): Color {
    return MaterialTheme.colorScheme.fromToken(this)
}

internal fun ColorScheme.fromToken(value: ColorSchemeKeyTokens): Color {
    return when (value) {
        ColorSchemeKeyTokens.Background -> background
        ColorSchemeKeyTokens.Error -> error
        ColorSchemeKeyTokens.ErrorContainer -> errorContainer
        ColorSchemeKeyTokens.InverseOnSurface -> inverseOnSurface
        ColorSchemeKeyTokens.InversePrimary -> inversePrimary
        ColorSchemeKeyTokens.InverseSurface -> inverseSurface
        ColorSchemeKeyTokens.OnBackground -> onBackground
        ColorSchemeKeyTokens.OnError -> onError
        ColorSchemeKeyTokens.OnErrorContainer -> onErrorContainer
        ColorSchemeKeyTokens.OnPrimary -> onPrimary
        ColorSchemeKeyTokens.OnPrimaryContainer -> onPrimaryContainer
        ColorSchemeKeyTokens.OnSecondary -> onSecondary
        ColorSchemeKeyTokens.OnSecondaryContainer -> onSecondaryContainer
        ColorSchemeKeyTokens.OnSurface -> onSurface
        ColorSchemeKeyTokens.OnSurfaceVariant -> onSurfaceVariant
        ColorSchemeKeyTokens.SurfaceTint -> surfaceTint
        ColorSchemeKeyTokens.OnTertiary -> onTertiary
        ColorSchemeKeyTokens.OnTertiaryContainer -> onTertiaryContainer
        ColorSchemeKeyTokens.Outline -> outline
        ColorSchemeKeyTokens.OutlineVariant -> outlineVariant
        ColorSchemeKeyTokens.Primary -> primary
        ColorSchemeKeyTokens.PrimaryContainer -> primaryContainer
        ColorSchemeKeyTokens.Scrim -> scrim
        ColorSchemeKeyTokens.Secondary -> secondary
        ColorSchemeKeyTokens.SecondaryContainer -> secondaryContainer
        ColorSchemeKeyTokens.Surface -> surface
        ColorSchemeKeyTokens.SurfaceVariant -> surfaceVariant
        ColorSchemeKeyTokens.Tertiary -> tertiary
        ColorSchemeKeyTokens.TertiaryContainer -> tertiaryContainer
    }
}

/** Helper function for component shape tokens. Used to grab the end values of a shape parameter. */
internal fun CornerBasedShape.end(): CornerBasedShape {
    return copy(topStart = CornerSize(0.0.dp), bottomStart = CornerSize(0.0.dp))
}

internal object WavySliderTokens {
    const val DisabledActiveTrackOpacity = 0.38f
    const val DisabledHandleOpacity = 0.38f
    const val DisabledInactiveTrackOpacity = 0.12f
    val ActiveTrackColor = ColorSchemeKeyTokens.Primary
    val DisabledActiveTrackColor = ColorSchemeKeyTokens.OnSurface
    val DisabledHandleColor = ColorSchemeKeyTokens.OnSurface
    val DisabledInactiveTrackColor = ColorSchemeKeyTokens.OnSurface
    val HandleColor = ColorSchemeKeyTokens.Primary
    val HandleHeight = 20.0.dp
    val HandleWidth = 20.0.dp
    val InactiveTrackColor = ColorSchemeKeyTokens.SurfaceVariant
    val StateLayerSize = 40.0.dp
}

internal enum class ColorSchemeKeyTokens {
    Background,
    Error,
    ErrorContainer,
    InverseOnSurface,
    InversePrimary,
    InverseSurface,
    OnBackground,
    OnError,
    OnErrorContainer,
    OnPrimary,
    OnPrimaryContainer,
    OnSecondary,
    OnSecondaryContainer,
    OnSurface,
    OnSurfaceVariant,
    OnTertiary,
    OnTertiaryContainer,
    Outline,
    OutlineVariant,
    Primary,
    PrimaryContainer,
    Scrim,
    Secondary,
    SecondaryContainer,
    Surface,
    SurfaceTint,
    SurfaceVariant,
    Tertiary,
    TertiaryContainer
}
