package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import org.junit.Test
import androidx.compose.material3.MaterialTheme as MaterialTheme3
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

class ScreenshotTest {

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
        state: String,
        expected: String,
        content: @Composable ColumnScope.() -> Unit
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
                        content()
                        Text(text = state)
                        Text(text = expected)
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
            state = "Default wavy sliders with no arguments passed",
            expected = "Should be displayed properly"
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(0.5f, {})
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(0.5f, {})
            }
        }
        assert(isPassed)
    }

    @Test
    fun `Test 2`() {
        val isPassed = testApp(
            name = object {}.javaClass.enclosingMethod.name,
            state = "Wave height set to 0",
            expected = "Should be exactly like a regular Slider"
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 2:")
                WavySlider2(0.5f, {}, waveHeight = 0.dp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Material 3:")
                WavySlider3(0.5f, {}, waveHeight = 0.dp)
            }
        }
        assert(isPassed)
    }
}
