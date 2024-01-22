package ir.mahozad.multiplatform.wavyslider

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import ir.mahozad.multiplatform.wavyslider.WaveMovement.*
import kotlinx.coroutines.delay
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
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
        content: (@Composable ColumnScope.(value: Float, onChange: (Float) -> Unit) -> Unit)? = null
    ): Boolean {
        var passed = false
        application(exitProcessOnExit = false) {
            Window(
                title = name,
                state = WindowState(
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { passed = false; exitApplication() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.05f))
                            ) {
                                Text(text = "Fail", color = Color.Red)
                            }
                            OutlinedButton(
                                onClick = { passed = true; exitApplication() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.05f))
                            ) {
                                Text(text = "Pass", color = Color.Green)
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
            given = "Default wavy sliders with no arguments passed",
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
            given = "Wave height set to 0",
            expected = "Should be exactly like a regular Slider",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveHeight = 0.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveHeight = 0.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 3`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Disabled",
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
            given = "LTR animation",
            expected = "Should move from left to right",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveMovement = LTR) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveMovement = LTR) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 5`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Wave height set to more than thumb height",
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
    fun `Test 6`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Incremental",
            expected = "Should have its height increase gradually",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, incremental = true) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, incremental = true) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 7`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Wave height set to negative value",
            expected = "Should have the same behaviour as if the size was positive\n" +
                       "Except that the phase is shifted",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveHeight = (-48).dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveHeight = (-48).dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 8`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Wave length set to a large value",
            expected = "Should have proper wave length",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveLength = 128.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveLength = 128.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 9`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Wave length set to 0",
            expected = "Should be exactly like a regular Slider",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveLength = 0.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveLength = 0.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 10`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Wave length set to a negative value",
            expected = "Should have the same behaviour as if the size was 0",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveLength = (-48).dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveLength = (-48).dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 11`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Wave thickness set to a large value",
            expected = "Should have proper wave thickness",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveThickness = 8.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveThickness = 8.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 12`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Wave thickness set to 0",
            expected = "Should have the wave disappeared",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveThickness = 0.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveThickness = 0.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 13`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Wave thickness set to a negative value",
            expected = "Should have the same behaviour as if the thickness was 0",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveThickness = (-10).dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveThickness = (-10).dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 14`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Track thickness set to a large value",
            expected = "Should have proper track thickness",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, trackThickness = 18.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, trackThickness = 18.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 15`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Track thickness set to 0",
            expected = "Should have the track disappeared",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, trackThickness = 0.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, trackThickness = 0.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 16`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Track thickness set to a negative value",
            expected = "Should have the same behaviour as if the thickness was 0",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, trackThickness = (-10).dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, trackThickness = (-10).dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 17`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Container layout direction set to RTL",
            expected = "Should be reversed"
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
    fun `Test 18`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "enabled" is toggled""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)"
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
    fun `Test 19`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "incremental" is toggled""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)"
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
    fun `Test 20`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "waveThickness" is changed""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)"
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
    fun `Test 21`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "waveHeight" is changed""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)\n" +
                       "Also, should have its wave height change with a smooth animation"
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
    fun `Test 22`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "LTR animation",
            expected = "The wave start (tail of the slider) should be long enough all the time\n" +
                       "(keep looking at the slider start for a few seconds and ensure the tail does not shrink)",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveHeight = 0.dp, waveMovement = LTR) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveHeight = 0.dp, waveMovement = LTR) }
        )
        assert(isPassed)
    }

    @Test
    @OptIn(ExperimentalMaterial3Api::class)
    fun `Test 23`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "A custom thumb",
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
    fun `Test 24`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "A wave length larger than slider total length",
            expected = "The wave should be displayed properly",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, modifier = Modifier.width(400.dp), waveHeight = 24.dp, waveLength = 500.dp) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, modifier = Modifier.width(400.dp), waveHeight = 24.dp, waveLength = 500.dp) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 25`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When the width of the container of the component is changed",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)"
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
    fun `Test 26`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "waveThickness" is toggled between 0 and a positive value""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)"
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
    fun `Test 27`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When container layout direction is LTR and the "waveMovement" is set to "BACKWARD"""",
            expected = "Should animate from right to left"
        ) { value, onChange ->
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, waveMovement = BACKWARD)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange, waveMovement = BACKWARD)
                }
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 28`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When container layout direction is RTL and the "waveMovement" is set to "BACKWARD"""",
            expected = "Should animate from left to right"
        ) { value, onChange ->
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, waveMovement = BACKWARD)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange, waveMovement = BACKWARD)
                }
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 29`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Many wavy sliders",
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

    @Test
    fun `Test 30`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When container layout direction set to RTL and the "incremental" is set to "true"""",
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
    fun `Test 31`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "waveHeight" is toggled between 0 and a positive value""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)\n" +
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

    @Test
    fun `Test 32`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "wavePeriod" is toggled""",
            expected = "Should change speed and smoothly continue its horizontal shift without any glitch"
        ) { value, onChange ->
            var wavePeriod by remember { mutableStateOf(1.seconds) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, wavePeriod = wavePeriod)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, wavePeriod = wavePeriod)
            }
            Button(onClick = { wavePeriod = if (wavePeriod == 1.seconds) 3079.milliseconds else 1.seconds }) {
                Text(text = "Toggle wavePeriod")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 33`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "waveMovement" is toggled""",
            expected = "Should smoothly change movement direction"
        ) { value, onChange ->
            var waveMovement by remember { mutableStateOf(RTL) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value, onChange, waveMovement = waveMovement)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value, onChange, waveMovement = waveMovement)
            }
            Button(onClick = { waveMovement = if (waveMovement == RTL) LTR else RTL }) {
                Text(text = "Toggle waveMovement")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 34`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When container layout is RTL and "waveMovement" is toggled""",
            expected = "Should smoothly change movement direction"
        ) { value, onChange ->
            var waveMovement by remember { mutableStateOf(LTR) }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, waveMovement = waveMovement)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange, waveMovement = waveMovement)
                }
                Button(onClick = { waveMovement = if (waveMovement == RTL) LTR else RTL }) {
                    Text(text = "Toggle waveMovement")
                }
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 35`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "wavePeriod" is set to 0""",
            expected = "Should stop the wave horizontal movement"
        ) { value, onChange ->
            var wavePeriod by remember { mutableStateOf(1.seconds) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, wavePeriod = wavePeriod)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange, wavePeriod = wavePeriod)
                }
                Button(onClick = { wavePeriod = if (wavePeriod == 1.seconds) Duration.ZERO else 1.seconds }) {
                    Text(text = "Toggle wavePeriod")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 36`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "wavePeriod" is set to >= Int.MAX_VALUE milliseconds""",
            expected = "Should have the same behaviour as if the period was 0\n" +
                       "This is because the animationSpec gets its duration argument as an integer"
        ) { value, onChange ->
            var wavePeriod by remember { mutableStateOf(1.seconds) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, wavePeriod = wavePeriod)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange, wavePeriod = wavePeriod)
                }
                Button(onClick = { wavePeriod = if (wavePeriod == 1.seconds) 50.days else 1.seconds }) {
                    Text(text = "Toggle wavePeriod")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 37`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "wavePeriod" is set to <= Int.MIN_VALUE milliseconds""",
            expected = "Should have the same behaviour as if the period was 0\n" +
                       "This is because the animationSpec gets its duration argument as an integer"
        ) { value, onChange ->
            var wavePeriod by remember { mutableStateOf(1.seconds) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, wavePeriod = wavePeriod)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange, wavePeriod = wavePeriod)
                }
                Button(onClick = { wavePeriod = if (wavePeriod == 1.seconds) (-50).days else 1.seconds }) {
                    Text(text = "Toggle wavePeriod")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 38`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "wavePeriod" is set to fraction of a millisecond""",
            expected = "Should have the same behaviour as if the period was 0\n" +
                       "This is because the animationSpec gets its duration argument as an integer milliseconds"
        ) { value, onChange ->
            var wavePeriod by remember { mutableStateOf(1.seconds) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, wavePeriod = wavePeriod)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange, wavePeriod = wavePeriod)
                }
                Button(onClick = { wavePeriod = if (wavePeriod == 1.seconds) 5.nanoseconds else 1.seconds }) {
                    Text(text = "Toggle wavePeriod")
            }
        }
        assert(isPassed)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun `Test 39`() {
        val spec1 = tween<Float>(durationMillis = 1300, easing = EaseOutBounce)
        val spec2 = tween<Float>(durationMillis = 150, easing = LinearEasing)
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Different animationSpecs for wave height when dragging vs when toggling wave height",
            expected = "Should stop the wave horizontal movement"
        ) { value, onChange ->
            var spec: AnimationSpec<Float> by remember { mutableStateOf(spec1) }
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
                    animationSpecs = WaveAnimationSpecs(spec),
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
                    animationSpecs = WaveAnimationSpecs(spec),
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
    fun `Test 40`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When container layout direction is LTR and the "waveMovement" is set to "FORWARD"""",
            expected = "Should animate from left to right"
        ) { value, onChange ->
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, waveMovement = FORWARD)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange, waveMovement = FORWARD)
                }
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 41`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When container layout direction is RTL and the "waveMovement" is set to "FORWARD"""",
            expected = "Should animate from right to left"
        ) { value, onChange ->
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value, onChange, waveMovement = FORWARD)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value, onChange, waveMovement = FORWARD)
                }
            }
        }
        assert(isPassed)
    }
}
