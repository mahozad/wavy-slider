package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import ir.mahozad.multiplatform.wavyslider.WaveAnimationDirection.LTR
import ir.mahozad.multiplatform.wavyslider.WaveAnimationDirection.UNSPECIFIED
import org.junit.Test
import androidx.compose.material.Slider as Slider2
import androidx.compose.material3.MaterialTheme as MaterialTheme3
import androidx.compose.material3.Slider as Slider3
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

class VisualTest {

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
        wavySlider2: (@Composable ColumnScope.(value: Float, onChange: (Float) -> Unit) -> Unit)? = null,
        wavySlider3: (@Composable ColumnScope.(value: Float, onChange: (Float) -> Unit) -> Unit)? = null,
        content: (@Composable ColumnScope.() -> Unit)? = null
    ): Boolean {
        var passed = false
        application(exitProcessOnExit = false) {
            Window(
                title = name,
                state = WindowState(position = WindowPosition(Alignment.Center)),
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
                                Slider2(value, onValueChange = { value = it })
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Slider 3:", modifier = Modifier.width(110.dp))
                                Slider3(value, onValueChange = { value = it })
                            }
                        }
                        content?.invoke(this@Column) ?: run {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Wavy slider 2:", modifier = Modifier.width(110.dp))
                                wavySlider2?.invoke(this@Column, value) { value = it }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Wavy slider 3:", modifier = Modifier.width(110.dp))
                                wavySlider3?.invoke(this@Column, value) { value = it }
                            }
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
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, animationDirection = LTR) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, animationDirection = LTR) }
        )
        assert(isPassed)
    }

    // FIXME: The rendered wave height does not correspond exactly to the specified wave height (it's a little bit smaller)
    //  because it is a bezier curve and the curve height is not exactly equal to the y-coordinate of the control point
    //  So, should place the control points a little bit farther than the specified wave height (maybe there is a formula for that?)
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
            given = "Flattened",
            expected = "Should be flattened",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, shouldFlatten = true) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, shouldFlatten = true) }
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
            given = "Track thickness set to null",
            expected = "Should have the same behaviour as if the thickness was 0",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, trackThickness = null) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, trackThickness = null) }
        )
        assert(isPassed)
    }

    @Test
    fun `Test 18`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Container layout direction set to RTL",
            expected = "Should be reversed"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value = value, onValueChange = { value = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value = value, onValueChange = { value = it })
                }
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 19`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "enabled" is toggled""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            var isEnabled by remember { mutableStateOf(true) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value = value, onValueChange = { value = it }, enabled = isEnabled)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value = value, onValueChange = { value = it }, enabled = isEnabled)
            }
            Button(onClick = { isEnabled = !isEnabled }) { Text(text = "Toggle isEnabled") }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 20`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "shouldFlatten" is toggled""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            var isFlattened by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value = value, onValueChange = { value = it }, shouldFlatten = isFlattened)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value = value, onValueChange = { value = it }, shouldFlatten = isFlattened)
            }
            Button(onClick = { isFlattened = !isFlattened }) { Text(text = "Toggle shouldFlatten") }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 21`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "waveThickness" is changed""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            var waveThickness by remember { mutableStateOf(4.dp) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value = value, onValueChange = { value = it }, waveThickness = waveThickness)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value = value, onValueChange = { value = it }, waveThickness = waveThickness)
            }
            Button(onClick = { waveThickness = if (waveThickness == 4.dp) 10.dp else 4.dp }) {
                Text(text = "Toggle waveThickness")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 22`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "waveHeight" is changed""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)\n" +
                       "Also, should have its wave height change with a smooth animation"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            var waveHeight by remember { mutableStateOf(16.dp) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value = value, onValueChange = { value = it }, waveHeight = waveHeight)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value = value, onValueChange = { value = it }, waveHeight = waveHeight)
            }
            Button(onClick = { waveHeight = if (waveHeight == 16.dp) 48.dp else 16.dp }) {
                Text(text = "Toggle waveHeight")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 23`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "LTR animation",
            expected = "The wave start (tail of the slider) should be long enough all the time\n" +
                       "(keep looking at the slider start for a few seconds and ensure the tail does not shrink)",
            wavySlider2 = { value, onChange -> WavySlider2(value, onChange, waveHeight = 0.dp, animationDirection = LTR) },
            wavySlider3 = { value, onChange -> WavySlider3(value, onChange, waveHeight = 0.dp, animationDirection = LTR) }
        )
        assert(isPassed)
    }

    @Test
    @OptIn(ExperimentalMaterial3Api::class)
    fun `Test 24`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "A custom thumb",
            expected = "The custom thumb should be displayed and its height be taken into account in overall component height"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            val thumb: @Composable (SliderPositions) -> Unit = @Composable {
                Box(Modifier.width(6.dp).height(128.dp).background(Color.Red))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value = value, onValueChange = { value = it }, thumb = thumb, modifier = Modifier.border(1.dp, Color.Gray))
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 25`() {
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
    fun `Test 26`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "When the width of the container of the component is changed",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            var containerWidth by remember { mutableStateOf(500.dp) }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(containerWidth)) {
                Text(text = "Material 2:")
                WavySlider2(value = value, onValueChange = { value = it })
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(containerWidth)) {
                Text(text = "Material 3:")
                WavySlider3(value = value, onValueChange = { value = it })
            }
            Button(onClick = { containerWidth = if (containerWidth == 500.dp) 200.dp else 500.dp }) {
                Text(text = "Toggle container width")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 27`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When "waveThickness" is toggled between 0 and a positive value""",
            expected = "Should not have its wave animation restart (should smoothly continue its animation)"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            var waveThickness by remember { mutableStateOf(4.dp) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(value = value, onValueChange = { value = it }, waveThickness = waveThickness)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(value = value, onValueChange = { value = it }, waveThickness = waveThickness)
            }
            Button(onClick = { waveThickness = if (waveThickness == 4.dp) 0.dp else 4.dp }) {
                Text(text = "Toggle waveThickness")
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 28`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When container layout direction is LTR and the "animationDirection" is set to "unspecified"""",
            expected = "Should animate from right to left"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value = value, onValueChange = { value = it }, animationDirection = UNSPECIFIED)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value = value, onValueChange = { value = it }, animationDirection = UNSPECIFIED)
                }
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 29`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When container layout direction is RTL and the "animationDirection" is set to "unspecified"""",
            expected = "Should animate from left to right"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value = value, onValueChange = { value = it }, animationDirection = UNSPECIFIED)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value = value, onValueChange = { value = it }, animationDirection = UNSPECIFIED)
                }
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 30`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = "Many wavy sliders",
            showRegularSliders = false
        ) {
            var value by remember { mutableStateOf(1f) }
            repeat(10) { row ->
                Row {
                    repeat(3) { column ->
                        WavySlider2(
                            value = value,
                            onValueChange = { value = it },
                            waveLength = 10.dp + (row + column).dp,
                            shouldFlatten = (row + column) % 2 == 0,
                            modifier = Modifier.width(125.dp).height(28.dp)
                        )
                        WavySlider3(
                            value = value,
                            onValueChange = { value = it },
                            waveLength = 10.dp + (row + column).dp,
                            shouldFlatten = (row + column) % 2 == 0,
                            modifier = Modifier.width(125.dp).height(28.dp)
                        )
                    }
                }
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 31`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            given = """When container layout direction set to RTL and the "shouldFlatten" is set to "true"""",
            expected = "Should be flattened properly (from the thumb with most height to the tail with least height"
        ) {
            var value by remember { mutableStateOf(0.5f) }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 2:")
                    WavySlider2(value = value, onValueChange = { value = it }, shouldFlatten = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Material 3:")
                    WavySlider3(value = value, onValueChange = { value = it }, shouldFlatten = true)
                }
            }
        }
        assert(isPassed)
    }
}
