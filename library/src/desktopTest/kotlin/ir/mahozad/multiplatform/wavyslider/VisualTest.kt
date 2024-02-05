package ir.mahozad.multiplatform.wavyslider

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import androidx.compose.ui.window.*
import ir.mahozad.multiplatform.wavyslider.WaveDirection.*
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import kotlinx.coroutines.delay
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.material.Slider as Slider2
import androidx.compose.material3.MaterialTheme as MaterialTheme3
import androidx.compose.material3.Slider as Slider3
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

class VisualTest {

    // As stated in https://developer.android.com/jetpack/compose/animation/customize#:~:text=animations%20using%20infiniteRepeatable%20are%20not%20run
    // the test rule does not run infiniteRepeatable animations

    // @get:Rule
    // val rule = createComposeRule()

    // @Test
    // fun test() {
    //     var waveHeight by mutableStateOf(48.dp)
    //     rule.mainClock.autoAdvance = false // Pauses animations
    //     rule.setContent {
    //         WavySlider(0.5f, {}, waveHeight = waveHeight)
    //     }

    //     waveHeight = 0.dp
    //     rule.mainClock.advanceTimeByFrame()
    //     rule.mainClock.advanceTimeBy(517L)

    //     // val image = rule.onRoot().captureToImage()
    //     // ImageIO.write(image.toAwtImage(), "PNG", Path("output.png").outputStream())

    //     val referencePath = Path("output.png")
    //     val screenshot = Image.makeFromBitmap(rule.onRoot().captureToImage().asSkiaBitmap())
    //     val actualPath = Path("output1.png")
    //     val actualData = screenshot.encodeToData(EncodedImageFormat.PNG) ?: error("Could not encode image as png")
    //     actualPath.writeBytes(actualData.bytes)

    //     assert(actualPath.readBytes().contentEquals(referencePath.readBytes())) {
    //         "The screenshot '$actualPath' does not match the reference '$referencePath'"
    //     }
    // }

    private fun testApp(
        name: String,
        given: String,
        expected: String? = null,
        showRegularSliders: Boolean = true,
        windowSize: DpSize = DpSize(800.dp, 600.dp),
        wavySlider2: (@Composable ColumnScope.(value: Float, onChange: (Float) -> Unit) -> Unit)? = null,
        wavySlider3: (@Composable ColumnScope.(value: Float, onChange: (Float) -> Unit) -> Unit)? = null,
        content:     (@Composable ColumnScope.(value: Float, onChange: (Float) -> Unit) -> Unit)? = null
    ): Boolean {
        var passed = false
        val green = Color(0xff539c05)
        val red = Color(0xffb11406)
        application(exitProcessOnExit = false) {
            Window(
                title = name,
                state = rememberWindowState(
                    size = windowSize,
                    position = WindowPosition(Alignment.Center)
                ),
                resizable = false,
                onCloseRequest = ::exitApplication
            ) {
                MaterialTheme3 {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
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
                        Text(text = given)
                        expected?.let { Text(text = it) }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { passed = false; exitApplication() },
                                border = BorderStroke(Dp.Hairline, red),
                                colors = ButtonDefaults.buttonColors(containerColor = red.copy(alpha = 0.05f))
                            ) {
                                Text(text = "Fail", color = red)
                            }
                            OutlinedButton(
                                onClick = { passed = true; exitApplication() },
                                border = BorderStroke(Dp.Hairline, green),
                                colors = ButtonDefaults.buttonColors(containerColor = green.copy(alpha = 0.05f))
                            ) {
                                Text(text = "Pass", color = green)
                            }
                        }
                    }
                }
            }
        }
        return passed
    }

    @Test
    fun `Test 1`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Default wavy sliders",
            expected = "Should be displayed properly",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 2`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Default wavy sliders (RTL container layout direction)",
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
        assert(isPassed)
    }

    @Test
    fun `Test 3`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When enabled is false",
            expected = "Should not be able to drag the thumb",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, enabled = false) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, enabled = false) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 4`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When enabled is toggled",
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
            Button(onClick = { isEnabled = !isEnabled }) { Text(text = "Toggle isEnabled") }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 5`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveHeight is 0",
            expected = "Should be exactly like a regular Slider",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveHeight = 0.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveHeight = 0.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 6`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveHeight is more than thumb height",
            expected = "Should take into account the height of wave in component overall height\n" +
                       "Also, the component overall height should be exactly equal to the wave height",
            wavySlider2 = { value, onChange ->
                WavySlider2(value, onChange, waveHeight = 57.dp, modifier = Modifier.border(1.dp, Color.Gray))
            },
            wavySlider3 = { value, onChange ->
                WavySlider3(value, onChange, waveHeight = 57.dp, modifier = Modifier.border(1.dp, Color.Gray))
            }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 7`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveHeight is negative",
            expected = "Should have the same behaviour as if the size was positive\n" +
                       "Except that the phase is shifted",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveHeight = (-57).dp, modifier = Modifier.border(1.dp, Color.Gray)) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveHeight = (-57).dp, modifier = Modifier.border(1.dp, Color.Gray)) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 8`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveHeight is toggled",
            expected = "Should not have wave horizontal shift restart (should smoothly continue its movement)\n" +
                       "Also, should have its wave height change with its default animation spec"
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
        assert(isPassed)
    }

    @Test
    fun `Test 9`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveHeight is toggled between 0 and a positive value",
            expected = "Should not have wave horizontal shift restart (should smoothly continue its movement)\n" +
                       "Also, the waveHeight should be animated smoothly"
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
        assert(isPassed)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun `Test 10`() {
        val spec1 = tween<Dp>(durationMillis = 1300, easing = EaseOutBounce)
        val spec2 = tween<Dp>(durationMillis = 150, easing = LinearEasing)
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When using different animationSpecs for waveHeight when dragging vs when toggling waveHeight",
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
        assert(isPassed)
    }

    @Test
    fun `Test 11`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When container layout direction is LTR and incremental is true",
            expected = "Should have proper gradual height (from the thumb with most height to the tail with least height",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, incremental = true) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, incremental = true) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 12`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When container layout direction is RTL and incremental is true",
            expected = "Should have proper gradual height (from the thumb with most height to the tail with least height"
        ) { value, onChange ->
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, incremental = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange, incremental = true)
                }
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 13`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When incremental is toggled",
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
            Button(onClick = { isIncremental = !isIncremental }) { Text(text = "Toggle incremental") }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 14`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveLength is a large value",
            expected = "Should have proper wave length",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveLength = 128.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveLength = 128.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 15`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveLength is 0",
            expected = "Should be exactly like a regular Slider",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveLength = 0.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveLength = 0.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 16`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveLength is negative",
            expected = "Should have the same behaviour as if the size was 0",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveLength = (-48).dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveLength = (-48).dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 17`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveLength is larger than slider total length",
            expected = "The wave should be displayed properly",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, modifier = Modifier.width(400.dp), waveHeight = 24.dp, waveLength = 500.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, modifier = Modifier.width(400.dp), waveHeight = 24.dp, waveLength = 500.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 18`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveLength is toggled",
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
        assert(isPassed)
    }

    @Test
    fun `Test 19`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveThickness is a large value",
            expected = "Should have proper wave thickness",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveThickness = 8.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveThickness = 8.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 20`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveThickness is 0",
            expected = "Should have the wave disappeared",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveThickness = 0.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveThickness = 0.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 21`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveThickness is negative",
            expected = "Should have the same behaviour as if the thickness was 0",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveThickness = (-10).dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveThickness = (-10).dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 22`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveThickness is toggled",
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
        assert(isPassed)
    }

    @Test
    fun `Test 23`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When waveThickness is toggled between 0 and a positive value",
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
        assert(isPassed)
    }

    @Test
    fun `Test 24`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When trackThickness is a large value",
            expected = "Should have proper track thickness",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, trackThickness = 18.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, trackThickness = 18.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 25`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When trackThickness is 0",
            expected = "Should have the track disappeared",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, trackThickness = 0.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, trackThickness = 0.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 26`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When trackThickness is negative",
            expected = "Should have the same behaviour as if the thickness was 0",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, trackThickness = (-10).dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, trackThickness = (-10).dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 27`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When direction of waveVelocity is RIGHT",
            expected = "Should move from left to right",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveVelocity = 16.dp to RIGHT) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveVelocity = 16.dp to RIGHT) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 28`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When direction of waveVelocity is LEFT",
            expected = "Should move from right to left",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveVelocity = 16.dp to LEFT) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveVelocity = 16.dp to LEFT) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 29`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When direction of waveVelocity is HEAD",
            expected = "The wave start (tail of the slider) should be long enough all the time\n" +
                       "(keep looking at the slider start for a few seconds and ensure the tail does not shrink)",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveHeight = 0.dp, waveVelocity = 16.dp to HEAD) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveHeight = 0.dp, waveVelocity = 16.dp to HEAD) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 30`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When container layout direction is LTR and direction of waveVelocity is TAIL",
            expected = "Should animate from right to left"
        ) { value, onChange ->
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, waveVelocity = 16.dp to TAIL)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange,  waveVelocity = 16.dp to TAIL)
                }
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 31`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When container layout direction is RTL and direction of waveVelocity is TAIL",
            expected = "Should animate from left to right"
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
        assert(isPassed)
    }

    @Test
    fun `Test 32`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When container layout direction is LTR and direction of waveVelocity is HEAD",
            expected = "Should animate from left to right"
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
        assert(isPassed)
    }

    @Test
    fun `Test 33`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When container layout direction is RTL and direction of waveVelocity is HEAD",
            expected = "Should animate from right to left"
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
        assert(isPassed)
    }

    @Test
    fun `Test 34`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When direction of waveVelocity is toggled",
            expected = "Should smoothly change movement direction"
        ) { value, onChange ->
            var waveDirection by remember { mutableStateOf(TAIL) }
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
        assert(isPassed)
    }

    @Test
    fun `Test 35`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When container layout is RTL and direction of waveVelocity is changed",
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
        assert(isPassed)
    }

    @Test
    fun `Test 36`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When speed of waveVelocity is toggled to 0",
            expected = "Should stop the wave horizontal movement"
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
        assert(isPassed)
    }

    @Test
    fun `Test 37`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When speed of waveVelocity is toggled to a very large quantity",
            expected = "Should have fast speed"
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
            Button(onClick = { speed = if (speed == 16.dp) 5_000_000.dp else 16.dp }) {
                Text(text = "Toggle speed")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 38`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When speed of waveVelocity is toggled to a fraction of a 1 dp",
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
        assert(isPassed)
    }

    @Test
    fun `Test 39`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When speed of waveVelocity is toggled to a negative value",
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
        assert(isPassed)
    }

    @Test
    fun `Test 40`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When speed of waveVelocity is toggled",
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
        assert(isPassed)
    }

    @Test
    fun `Test 41`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When a custom animationSpec is set for waveVelocity",
            expected = "Should change wave velocity according to the animation spec"
        ) { value, onChange ->
            var direction by remember { mutableStateOf(HEAD) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(
                    value,
                    onChange,
                    waveVelocity = 16.dp to direction,
                    animationSpecs = SliderDefaults
                        .WaveAnimationSpecs
                        .copy(waveVelocityAnimationSpec = tween(durationMillis = 4000, easing = EaseOutBounce))
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(
                    value,
                    onChange,
                    waveVelocity = 16.dp to direction,
                    animationSpecs = SliderDefaults
                        .WaveAnimationSpecs
                        .copy(waveVelocityAnimationSpec = tween(durationMillis = 4000, easing = EaseOutBounce))
                )
            }
            Button(onClick = { direction = if (direction == HEAD) TAIL else HEAD }) {
                Text(text = "Toggle direction")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 42`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When direction of waveVelocity is relative (HEAD or TAIL) and container layout direction is toggled",
            expected = "Should change wave direction gracefully according to the default animation spec"
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
        assert(isPassed)
    }

    @Test
    @OptIn(ExperimentalMaterial3Api::class)
    fun `Test 43`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When a custom thumb is set",
            expected = "The custom thumb should be displayed and its height be taken into account in overall component height"
        ) { value, onChange ->
            val thumb: @Composable (SliderPositions) -> Unit = @Composable {
                Box(Modifier.width(6.dp).height(128.dp).background(Color.Red))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, thumb = thumb, modifier = Modifier.border(1.dp, Color.Gray))
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 44`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When the width of the container of the component is changed",
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
        assert(isPassed)
    }

    @Test
    fun `Test 45`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When a custom valueRange (4f..20f) is set",
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
        assert(isPassed)
    }

    @Test
    fun `Test 46`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When the screen density is something low",
            expected = "Should have everything scaled proportionally"
        ) { value, onChange ->
            CompositionLocalProvider(LocalDensity provides Density(0.43f)) {
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
        assert(isPassed)
    }

    @Test
    fun `Test 47`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When the screen density is something high",
            expected = "Should have everything scaled proportionally"
        ) { value, onChange ->
            CompositionLocalProvider(LocalDensity provides Density(2.43f)) {
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
        assert(isPassed)
    }

    // See https://github.com/JetBrains/compose-multiplatform/issues/4199
    @Test
    fun `Test 48`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Measure FPS of the app",
            showRegularSliders = false
        ) { value, onChange ->
            @Composable
            fun FPSCounter(onUpdateFPS: (Int) -> Unit) {
                LaunchedEffect(Unit) {
                    val fpsCounter = org.jetbrains.skiko.FPSCounter(logOnTick = true)
                    while (true) {
                        withFrameNanos {
                            onUpdateFPS(fpsCounter.average)
                            fpsCounter.tick()
                        }
                    }
                }
            }
            var fps by remember { mutableIntStateOf(0) }
            FPSCounter { fps = it }
            Text(text = "FPS: $fps")
            WavySlider2(value, onChange)
            WavySlider3(value, onChange)
        }
        assert(isPassed)
    }

    @Test
    fun `Test 49`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When there are many wavy sliders",
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
        assert(isPassed)
    }

    // See the README in the <PROJECT ROOT>/asset directory
    @Test
    fun `Test 50`() {
        application(exitProcessOnExit = false) {
            Window(
                title = "WavySliderDemo",
                undecorated = true,
                transparent = true,
                resizable = false,
                state = rememberWindowState(size = DpSize(640.dp, Dp.Unspecified)),
                onCloseRequest = ::exitApplication
            ) {
                MaterialTheme3 {
                    var value by remember { mutableStateOf(0.5f) }
                    val isDark = false
                    val colorsLight = SliderDefaults.colors(
                        thumbColor = Color(0xff727d1a), // Primary
                        activeTrackColor = Color(0xff727d1a), // Primary
                        inactiveTrackColor = Color(0xffe4e3d2) // Light SurfaceVariant
                    )
                    val colorsDark = SliderDefaults.colors(
                        thumbColor = Color(0xff727d1a), // Primary
                        activeTrackColor = Color(0xff727d1a), // Primary
                        inactiveTrackColor = Color(0xff47483b) // Dark SurfaceVariant
                    )
                    val colors = if (isDark) colorsDark else colorsLight
                    CompositionLocalProvider(LocalDensity provides Density(2f)) {
                        Column {
                            WavySlider3(value, { value = it }, colors = colors)
                            WavySlider3(value, { value = it }, colors = colors, waveLength = 30.dp, waveVelocity = 15.dp to TAIL)
                            WavySlider3(value, { value = it }, colors = colors, waveHeight = 12.dp, waveVelocity = 20.dp to TAIL, incremental = true)
                            WavySlider3(value, { value = it }, colors = colors, waveHeight = 0.dp)
                        }
                    }
                }
            }
        }
    }
}
