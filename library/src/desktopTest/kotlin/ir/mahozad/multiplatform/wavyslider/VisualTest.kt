package ir.mahozad.multiplatform.wavyslider

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ir.mahozad.multiplatform.wavyslider.WaveDirection.*
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.skiko.FPSCounter
import org.junit.AssumptionViolatedException
import org.junit.Test
import kotlin.streams.asSequence
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.material.Slider as Slider2
import androidx.compose.material3.MaterialTheme as MaterialTheme3
import androidx.compose.material3.Slider as Slider3
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

class VisualTest {

    @Test
    fun `Test 1`() = runVisualTest(
        provided = "Default wavy sliders",
        expected = "Should be displayed properly",
        wavySlider2 = { value, onChange -> WavySlider2(value, onChange) },
        wavySlider3 = { value, onChange -> WavySlider3(value, onChange) }
    )

    @Test
    fun `Test 2`() = runVisualTest(
        provided = "Default wavy sliders (RTL container layout)",
        expected = "Should be a mirror of the LTR one"
    ) { value, onChange ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange)
            }
        }
    }

    @Test
    fun `Test 3`() = runVisualTest(
        provided = "When enabled is false",
        expected = "Should not be able to drag the thumb",
        wavySlider2 = { value, onChange -> WavySlider2(value, onChange, enabled = false) },
        wavySlider3 = { value, onChange -> WavySlider3(value, onChange, enabled = false) }
    )

    @Test
    fun `Test 4`() = runVisualTest(
        provided = "When enabled is toggled",
        expected = "Should not have wave horizontal shift restart (should smoothly continue its movement)"
    ) { value, onChange ->
        var isEnabled by remember { mutableStateOf(true) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, enabled = isEnabled)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, enabled = isEnabled)
        }
        Button(onClick = { isEnabled = !isEnabled }) {
            Text(text = "Toggle isEnabled")
        }
    }

    @Test
    fun `Test 5`() = runVisualTest(
        provided = "When waveHeight is toggled",
        expected = """
            Should not have wave horizontal shift restart (should smoothly continue its movement)
            Also, should have its wave height change with its default animation spec
        """.trimIndent()
    ) { value, onChange ->
        var waveHeight by remember { mutableStateOf(16.dp) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, waveHeight = waveHeight)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, waveHeight = waveHeight)
        }
        Button(onClick = { waveHeight = if (waveHeight == 16.dp) 44.dp else 16.dp }) {
            Text(text = "Toggle waveHeight")
        }
    }

    @Test
    fun `Test 6`() = runVisualTest(
        provided = "When waveHeight is toggled between 0 and a positive value",
        expected = """
            Should not have wave horizontal shift restart (should smoothly continue its movement)
            Also, the waveHeight should be animated smoothly
        """.trimIndent(),
    ) { value, onChange ->
        var waveHeight by remember { mutableStateOf(16.dp) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, waveHeight = waveHeight)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, waveHeight = waveHeight)
        }
        Button(onClick = { waveHeight = if (waveHeight == 16.dp) 0.dp else 16.dp }) {
            Text(text = "Toggle waveHeight")
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun `Test 7`() {
        val spec1 = tween<Dp>(durationMillis = 1300, easing = EaseOutBounce)
        val spec2 = tween<Dp>(durationMillis = 150, easing = LinearEasing)
        runVisualTest(
            provided = "When using different animationSpecs for waveHeight when dragging vs when toggling waveHeight",
            expected = "Should have the proper animationSpec applied"
        ) { value, onChange ->
            var spec: AnimationSpec<Dp> by remember { mutableStateOf(spec1) }
            var waveHeight by remember { mutableStateOf(16.dp) }
            val interactionSource = remember { MutableInteractionSource() }
            var isPressed by remember { mutableStateOf(false) }
            val isDragged by interactionSource.collectIsDraggedAsState()
            LaunchedEffect(isPressed, isDragged) {
                waveHeight = if (isPressed || isDragged) 0.dp else 16.dp
                spec = if (isPressed || isDragged) {
                    spec2
                } else {
                    delay(spec2.durationMillis.milliseconds)
                    spec1
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(
                    value,
                    onChange,
                    waveHeight = waveHeight,
                    interactionSource = interactionSource,
                    animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(waveHeightAnimationSpec = spec),
                    modifier = Modifier
                        .onPointerEvent(PointerEventType.Press) { isPressed = true }
                        .onPointerEvent(PointerEventType.Release) { isPressed = false }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(
                    value,
                    onChange,
                    waveHeight = waveHeight,
                    interactionSource = interactionSource,
                    animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(waveHeightAnimationSpec = spec),
                    modifier = Modifier
                        .onPointerEvent(PointerEventType.Press) { isPressed = true }
                        .onPointerEvent(PointerEventType.Release) { isPressed = false }
                )
            }
            Button(onClick = { waveHeight = if (waveHeight == 0.dp) 16.dp else 0.dp }) {
                Text(text = "Toggle waveHeight")
            }
        }
    }

    @Test
    fun `Test 8`() = runVisualTest(
        provided = "When incremental is toggled",
        expected = "Should not have wave horizontal shift restart (should smoothly continue its movement)"
    ) { value, onChange ->
        var isIncremental by remember { mutableStateOf(false) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, incremental = isIncremental)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, incremental = isIncremental)
        }
        Button(onClick = { isIncremental = !isIncremental }) {
            Text(text = "Toggle incremental")
        }
    }

    @Test
    fun `Test 9`() = runVisualTest(
        provided = "When waveLength is toggled",
        expected = "Should not have unexpected behaviour"
    ) { value, onChange ->
        var waveLength by remember { mutableStateOf(18.dp) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, waveLength = waveLength)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, waveLength = waveLength)
        }
        Button(onClick = { waveLength = if (waveLength == 33.dp) 18.dp else 33.dp }) {
            Text(text = "Toggle waveLength")
        }
    }

    @Test
    fun `Test 10`() = runVisualTest(
        provided = "When waveThickness is toggled",
        expected = "Should not have wave horizontal shift restart (should smoothly continue its movement)"
    ) { value, onChange ->
        var waveThickness by remember { mutableStateOf(4.dp) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, waveThickness = waveThickness)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, waveThickness = waveThickness)
        }
        Button(onClick = { waveThickness = if (waveThickness == 4.dp) 10.dp else 4.dp }) {
            Text(text = "Toggle waveThickness")
        }
    }

    @Test
    fun `Test 11`() = runVisualTest(
        provided = "When waveThickness is toggled between 0 and a positive value",
        expected = "Should not have wave horizontal shift restart (should smoothly continue its movement)"
    ) { value, onChange ->
        var waveThickness by remember { mutableStateOf(4.dp) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, waveThickness = waveThickness)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, waveThickness = waveThickness)
        }
        Button(onClick = { waveThickness = if (waveThickness == 4.dp) 0.dp else 4.dp }) {
            Text(text = "Toggle waveThickness")
        }
    }

    @Test
    fun `Test 12`() = runVisualTest(
        provided = "When container layout is LTR and direction of waveVelocity is RIGHT",
        expected = "Should shift to right"
    ) { value, onChange ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 16.dp to RIGHT)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 16.dp to RIGHT)
            }
        }
    }

    @Test
    fun `Test 13`() = runVisualTest(
        provided = "When container layout is RTL and direction of waveVelocity is RIGHT",
        expected = "Should shift to right"
    ) { value, onChange ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 16.dp to RIGHT)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 16.dp to RIGHT)
            }
        }
    }

    @Test
    fun `Test 14`() = runVisualTest(
        provided = "When container layout is LTR and direction of waveVelocity is LEFT",
        expected = "Should shift to left"
    ) { value, onChange ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 16.dp to LEFT)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 16.dp to LEFT)
            }
        }
    }

    @Test
    fun `Test 15`() = runVisualTest(
        provided = "When container layout is RTL and direction of waveVelocity is LEFT",
        expected = "Should shift to left"
    ) { value, onChange ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 16.dp to LEFT)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 16.dp to LEFT)
            }
        }
    }

    @Test
    fun `Test 16`() = runVisualTest(
        provided = "When container layout is LTR and direction of waveVelocity is HEAD",
        expected = "Should shift to right"
    ) { value, onChange ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 16.dp to HEAD)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 16.dp to HEAD)
            }
        }
    }

    @Test
    fun `Test 17`() = runVisualTest(
        provided = "When container layout is RTL and direction of waveVelocity is HEAD",
        expected = "Should shift to left"
    ) { value, onChange ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 16.dp to HEAD)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 16.dp to HEAD)
            }
        }
    }

    @Test
    fun `Test 18`() = runVisualTest(
        provided = "When container layout is LTR and direction of waveVelocity is TAIL",
        expected = "Should shift to left"
    ) { value, onChange ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 16.dp to TAIL)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 16.dp to TAIL)
            }
        }
    }

    @Test
    fun `Test 19`() = runVisualTest(
        provided = "When container layout is RTL and direction of waveVelocity is TAIL",
        expected = "Should shift to right"
    ) { value, onChange ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 16.dp to TAIL)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 16.dp to TAIL)
            }
        }
    }

    @Test
    fun `Test 20`() = runVisualTest(
        provided = "When container layout is LTR and direction of waveVelocity is toggled",
        expected = "Should smoothly change movement direction"
    ) { value, onChange ->
        var waveDirection by remember { mutableStateOf(TAIL) }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 14.dp to waveDirection)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 14.dp to waveDirection)
            }
            Button(onClick = { waveDirection = if (waveDirection == TAIL) HEAD else TAIL }) {
                Text(text = "Toggle direction")
            }
        }
    }

    @Test
    fun `Test 21`() = runVisualTest(
        provided = "When container layout is RTL and direction of waveVelocity is changed",
        expected = "Should smoothly change movement direction"
    ) { value, onChange ->
        var direction by remember { mutableStateOf(TAIL) }
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 16.dp to direction)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 16.dp to direction)
            }
            Button(onClick = { direction = if (direction == TAIL) HEAD else TAIL }) {
                Text(text = "Toggle direction")
            }
        }
    }

    @Test
    fun `Test 22`() = runVisualTest(
        provided = "When speed of waveVelocity is set to 0",
        expected = "Should stop the wave horizontal shift (movement)"
    ) { value, onChange ->
        var speed by remember { mutableStateOf(16.dp) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, waveVelocity = speed to TAIL)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, waveVelocity = speed to TAIL)
        }
        Button(onClick = { speed = if (speed == 16.dp) 0.dp else 16.dp }) {
            Text(text = "Toggle speed")
        }
    }

    @Test
    fun `Test 23`() = runVisualTest(
        provided = "When speed of waveVelocity is set to a large quantity",
        expected = "Should have fast speed"
    ) { value, onChange ->
        var speed by remember { mutableStateOf(16.dp) }
        val animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(waveVelocityAnimationSpec = snap())
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, waveVelocity = speed to TAIL, animationSpecs = animationSpecs)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, waveVelocity = speed to TAIL, animationSpecs = animationSpecs)
        }
        Button(onClick = { speed = if (speed == 16.dp) 500.dp else 16.dp }) {
            Text(text = "Toggle speed")
        }
    }

    @Test
    fun `Test 24`() = runVisualTest(
        provided = "When speed of waveVelocity is set to a fraction of a 1 dp",
        expected = "Should have very low speed"
    ) { value, onChange ->
        var speed by remember { mutableStateOf(16.dp) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, waveVelocity = speed to TAIL)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, waveVelocity = speed to TAIL)
        }
        Button(onClick = { speed = if (speed == 16.dp) 0.1.dp else 16.dp }) {
            Text(text = "Toggle speed")
        }
    }

    @Test
    fun `Test 25`() = runVisualTest(
        provided = "When speed of waveVelocity is set to a negative value",
        expected = "Should behave the same as if the speed was zero"
    ) { value, onChange ->
        var speed by remember { mutableStateOf(16.dp) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, waveVelocity = speed to TAIL)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, waveVelocity = speed to TAIL)
        }
        Button(onClick = { speed = if (speed == 16.dp) (-10).dp else 16.dp }) {
            Text(text = "Toggle speed")
        }
    }

    @Test
    fun `Test 26`() = runVisualTest(
        provided = "When speed of waveVelocity is toggled",
        expected = "Should smoothly continue its horizontal shift without any glitch"
    ) { value, onChange ->
        var speed by remember { mutableStateOf(23.dp) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange, waveVelocity = speed to TAIL)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange, waveVelocity = speed to TAIL)
        }
        Button(onClick = { speed = if (speed == 23.dp) 10.dp else 23.dp }) {
            Text(text = "Toggle speed")
        }
    }

    @Test
    fun `Test 27`() = runVisualTest(
        provided = "When a custom animationSpec is set for waveVelocity",
        expected = "Should change wave velocity according to the animation spec"
    ) { value, onChange ->
        var direction by remember { mutableStateOf(HEAD) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(
                value,
                onChange,
                waveVelocity = 16.dp to direction,
                animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
                    waveVelocityAnimationSpec = tween(durationMillis = 4000, easing = EaseOutBounce)
                )
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(
                value,
                onChange,
                waveVelocity = 16.dp to direction,
                animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
                    waveVelocityAnimationSpec = tween(durationMillis = 4000, easing = EaseOutBounce)
                )
            )
        }
        Button(onClick = { direction = if (direction == HEAD) TAIL else HEAD }) {
            Text(text = "Toggle direction")
        }
    }

    /**
     * Also see [ScreenshotTest.`Should be able to create a horizontal static, fixed, still, not-animated wavy 'divider' (same code as in the related FAQ in README)`].
     */
    @Test
    fun `Test 28`() = runVisualTest(
        provided = """
            When the appearance (aka composition) animationSpec is set to snap()
            (in other words, the appearance animation is disabled)
        """.trimIndent(),
        expected = "Should have the wave appear immediately and instantly in its full final shape"
    ) { value, onChange ->
        var isShown by remember { mutableStateOf(false) }
        Button(onClick = { isShown = !isShown }) { Text(text = "Toggle appearance") }
        val animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(waveAppearanceAnimationSpec = snap())
        if (isShown) {
            WavySlider2(value, onChange, animationSpecs = animationSpecs)
            WavySlider3(value, onChange, animationSpecs = animationSpecs)
        }
    }

    @Test
    fun `Test 29`() = runVisualTest(
        provided = "When direction of waveVelocity is relative (HEAD or TAIL) and container layout is toggled",
        expected = "Should change wave direction properly"
    ) { value, onChange ->
        var layoutDirection by remember { mutableStateOf(LayoutDirection.Ltr) }
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveVelocity = 16.dp to TAIL)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveVelocity = 16.dp to TAIL)
            }
        }
        Button(onClick = {
            layoutDirection = if (layoutDirection == LayoutDirection.Rtl) {
                LayoutDirection.Ltr
            } else {
                LayoutDirection.Rtl
            }
        }) {
            Text(text = "Toggle layout direction")
        }
    }

    @Test
    fun `Test 30`() = runVisualTest(
        provided = "When the width of the container of the component is changed",
        expected = "Should not have wave horizontal shift restart (should smoothly continue its movement)"
    ) { value, onChange ->
        var containerWidth by remember { mutableStateOf(500.dp) }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(containerWidth)) {
            Text(text = "Material 2:")
            WavySlider2(value, onChange)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(containerWidth)) {
            Text(text = "Material 3:")
            WavySlider3(value, onChange)
        }
        Button(onClick = { containerWidth = if (containerWidth == 500.dp) 200.dp else 500.dp }) {
            Text(text = "Toggle container width")
        }
    }

    @Test
    fun `Test 31`() = runVisualTest(
        provided = "When a custom valueRange (4f..20f) is set",
        expected = "Should have proper behaviour"
    ) { _, _ ->
        var value by remember { mutableFloatStateOf(4f) }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 2:")
            WavySlider2(value, { value = it }, valueRange = 4f..20f)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Material 3:")
            WavySlider3(value, { value = it }, valueRange = 4f..20f)
        }
        Text(text = "Value: $value")
    }

    /**
     * The start and end offset of wave for Material 3 variant are adjusted (± waveThickness.toPx() / 2)
     * to account for the round cap of the path (cap = StrokeCap.Round) which fixes the excess length of
     * the tips of the wave and also more importantly, prevents a sudden change in position of path tip
     * at the moment the wave completely flattens and becomes a line (when toggling the waveHeight to zero).
     *
     * The above adjustment, in turn, results in a glitch visible on some fractional screen densities (like 1.25)
     * which caused the start of the wave to constantly become a little longer and shorter.
     * Here is the description of the reason:
     * To draw the wave, the x is progressed and the y is calculated based on it.
     * In a half a wavelength, the y should only change from increasing to decreasing or vice versa
     * at most once (at the peek or at the trough) but when printing
     * `lineTo(x.toFloat(), y.toFloat())` pairs, it was found that the y sometimes changed slope sign
     * more than once and this probably messes with drawing.
     *
     * Calling roundToInt().toFloat() on the result of the above adjustment seems to have fixed the problem.
     * Also, another less visually-perfect workaround would be to set the path join to StrokeJoin.Bevel for the wavyPath.
     */
    @Test
    fun `Test 32`() = runVisualTest(
        provided = "When the screen density is some fractional value",
        expected = """
            Should have no glitch at the start tip of the wave
            Also, when toggling the height, at the moment the wave completely flattens,
            there should be almost no abrupt change in where the line starts (almost invisible)
        """.trimIndent()
    ) { _, _ ->
        var density by remember { mutableStateOf(1.20f) }
        var waveHeight by remember { mutableStateOf(10.dp) }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                WavySlider3(
                    value = 0.03f,
                    onValueChange = {},
                    waveLength = 24.dp,
                    waveHeight = waveHeight,
                    waveVelocity = 10.dp to HEAD,
                    animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(waveAppearanceAnimationSpec = snap())
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Button(onClick = { waveHeight = if (waveHeight == 0.dp) 10.dp else 0.dp }) {
                        Text("Toggle height")
                    }
                    Text("Height: $waveHeight")
                }
                Column(modifier = Modifier.weight(4f)) {
                    Slider(value = density, onValueChange = { density = it }, valueRange = 1.20f..1.50f, steps = 9)
                    Text("Density: $density")
                }
            }
        }
    }

    @Test
    fun `Test 33`() = runVisualTest(
        provided = "When sliders appear on the screen (composition)",
        expected = "The sliders in RTL container should have the same animation speed as those in LTR container",
        showRegularSliders = false,
        windowSize = DpSize(800.dp, 800.dp)
    ) { value, onChange ->
        var isShown by remember { mutableStateOf(false) }
        Button(onClick = { isShown = !isShown }) { Text(text = "Toggle appearance") }
        if (isShown) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WavySlider2(value, onChange, waveVelocity = 10.dp to TAIL)
                WavySlider3(value, onChange, waveVelocity = 10.dp to TAIL)
                WavySlider2(value, onChange, waveVelocity = 10.dp to HEAD)
                WavySlider3(value, onChange, waveVelocity = 10.dp to HEAD)
            }
            WavySlider2(value, onChange, waveVelocity = 10.dp to TAIL)
            WavySlider3(value, onChange, waveVelocity = 10.dp to TAIL)
            WavySlider2(value, onChange, waveVelocity = 10.dp to HEAD)
            WavySlider3(value, onChange, waveVelocity = 10.dp to HEAD)
        }
    }

    // See https://github.com/JetBrains/compose-multiplatform/issues/4199
    @Test
    fun `Test 34`() = runVisualTest(
        provided = "Measure FPS of the app",
        showRegularSliders = false
    ) { value, onChange ->
        var fps by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            val fpsCounter = FPSCounter(logOnTick = false, periodSeconds = 2.0)
            while (isActive) {
                withFrameNanos {
                    fps = fpsCounter.average
                    fpsCounter.tick()
                }
            }
        }
        Text(text = "FPS: $fps")
        WavySlider2(value, onChange)
        WavySlider3(value, onChange)
    }

    @Test
    fun `Test 35`() = runVisualTest(
        provided = "When there are many wavy sliders",
        showRegularSliders = false,
        windowSize = DpSize(800.dp, 800.dp)
    ) { value, onChange ->
        val size = DpSize(125.dp, 13.dp)
        repeat(22) { row ->
            Row {
                repeat(3) { column ->
                    WavySlider2(
                        value = value,
                        onValueChange = onChange,
                        waveLength = 10.dp + (row + column).dp,
                        incremental = (row + column) % 2 == 0,
                        modifier = Modifier.size(size)
                    )
                    WavySlider3(
                        value = value,
                        onValueChange = onChange,
                        waveLength = 10.dp + (row + column).dp,
                        incremental = (row + column) % 2 == 0,
                        modifier = Modifier.size(size)
                    )
                }
            }
        }
    }

    private fun runVisualTest(
        provided: String,
        expected: String? = null,
        showRegularSliders: Boolean = true,
        windowSize: DpSize = DpSize(800.dp, 600.dp),
        wavySlider2: (@Composable ColumnScope.(value: Float, onChange: (Float) -> Unit) -> Unit)? = null,
        wavySlider3: (@Composable ColumnScope.(value: Float, onChange: (Float) -> Unit) -> Unit)? = null,
        content:     (@Composable ColumnScope.(value: Float, onChange: (Float) -> Unit) -> Unit)? = null
    ) {
        // If the name is passed as a parameter from the caller function,
        // the caller function can use object {}.javaClass.enclosingMethod.name as argument.
        // The first 2 elements in below line are for the internal frame and this current function.
        val nameOfTestFunction = StackWalker.getInstance().walk { it.asSequence().elementAt(index = 2) }.methodName
        var isTestPassed: Boolean? = null
        val green = Color(0xff539c05)
        val red = Color(0xffb11406)
        // TODO: Replace with singleWindowApplication() when https://youtrack.jetbrains.com/issue/CMP-9235 is fixed
        application(exitProcessOnExit = false) {
            Window(
                title = nameOfTestFunction,
                state = rememberWindowState(
                    size = windowSize,
                    position = WindowPosition(Alignment.Center)
                ),
                resizable = false,
                onCloseRequest = ::exitApplication
            ) {
                MaterialTheme3 {
                    Column(
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxHeight().padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            var value by remember { mutableStateOf(0.5f) }
                            if (showRegularSliders) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Slider 2:", modifier = Modifier.width(110.dp))
                                    Slider2(value, { value = it })
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Slider 3:", modifier = Modifier.width(110.dp))
                                    Slider3(value, { value = it })
                                }
                            }
                            if (content == null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Wavy slider 2:", modifier = Modifier.width(110.dp))
                                    wavySlider2?.invoke(this@Column, value) { value = it }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Wavy slider 3:", modifier = Modifier.width(110.dp))
                                    wavySlider3?.invoke(this@Column, value) { value = it }
                                }
                            } else {
                                content(value) { value = it }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = provided,
                                color = Color(0xFF4d4000),
                                modifier = Modifier
                                    .background(Color(0xFFfffcf0), RoundedCornerShape(5.dp))
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            expected?.let {
                                Text(
                                    text = it,
                                    color = Color(0xFF00204d),
                                    modifier = Modifier
                                        .background(Color(0xFFf0f6ff), RoundedCornerShape(5.dp))
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { isTestPassed = false; exitApplication() },
                                    border = BorderStroke(1.dp, red),
                                    colors = ButtonDefaults.buttonColors(containerColor = red.copy(alpha = 0.08f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "FAIL", color = red)
                                }
                                OutlinedButton(
                                    onClick = { isTestPassed = true; exitApplication() },
                                    border = BorderStroke(1.dp, green),
                                    colors = ButtonDefaults.buttonColors(containerColor = green.copy(alpha = 0.08f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "PASS", color = green)
                                }
                            }
                        }
                    }
                }
            }
        }
        when (isTestPassed) {
            false -> throw AssertionError("'$nameOfTestFunction' was decided as failed.")
            null -> throw AssumptionViolatedException("Instead of clicking FAIL or PASS, window was closed.")
            true -> Unit // PASS: Returns cleanly
        }
    }
}
