package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.WaveDirection.HEAD
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.junit.Test
import kotlin.io.path.*
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

@OptIn(ExperimentalTestApi::class)
class ScreenshotTest {

//    @get:Rule
//    val composeTestRule = createComposeRule()
//
//    @Test
//    fun readAndCompareScreenshots() {
//        composeTestRule.mainClock.autoAdvance = false // Pauses animations
//        composeTestRule.setContent {
//            Column(Modifier.width(400.dp)) {
//                WavySlider2(value = 0.5f, onValueChange = { }, waveHeight = 0.dp)
//                WavySlider3(value = 0.5f, onValueChange = { }, waveHeight = 0.dp)
//            }
//        }
//        val node = composeTestRule.onRoot()
//        val screenshot = Image.makeFromBitmap(node.captureToImage().asSkiaBitmap())
//        val actualPath = Path("screenshot.png")
//        val actualData = screenshot.encodeToData(EncodedImageFormat.PNG)
//            ?: error("Could not encode image as png")
//        actualPath.writeBytes(actualData.bytes)
//        val reference = ClassLoader.getSystemResource("reference-1.png")
//        assert(actualData.bytes.contentEquals(reference.readBytes()))
//    }

    @Test
    fun `When waveHeight is 0, should be exactly like a regular Slider`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-1.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveHeight = 0.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveHeight = 0.dp)
    }

    @Test
    fun `When waveLength is 0, should be exactly like a regular Slider`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-1.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveLength = 0.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveLength = 0.dp)
    }

    @Test
    fun `When waveLength is negative, should have the same behaviour as if waveLength was 0`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-1.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveLength = (-48).dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveLength = (-48).dp)
    }

    @Test
    fun `When waveThickness is 0, should have the wave disappeared`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-2.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveThickness = 0.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveThickness = 0.dp)
    }

    @Test
    fun `When waveThickness is negative, should have the same behaviour as if the thickness was 0`() =
        runScreenshotTest(
            windowSize = IntSize(width = 400, height = 100),
            referenceName = "reference-2.png"
        ) {
            WavySlider2(value = 0.5f, onValueChange = {}, waveThickness = (-10).dp)
            WavySlider3(value = 0.5f, onValueChange = {}, waveThickness = (-10).dp)
        }

    @Test
    fun `When waveThickness is a large value, should have proper wave thickness`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-3.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, waveThickness = 9.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, waveThickness = 9.dp)
    }

    @Test
    fun `When trackThickness is 0, should have the track disappeared`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-4.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, trackThickness = 0.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, trackThickness = 0.dp)
    }


    @Test
    fun `When trackThickness is negative, should have the same behaviour as if the thickness was 0`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-4.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, trackThickness = (-10).dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, trackThickness = (-10).dp)
    }

    @Test
    fun `When trackThickness is a large value, should have proper track thickness`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-5.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, trackThickness = 18.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, trackThickness = 18.dp)
    }

    @OptIn(InternalComposeUiApi::class)
    private fun runScreenshotTest(
        windowSize: IntSize,
        referenceName: String,
        content: @Composable ColumnScope.() -> Unit
    ) {
        runDesktopComposeUiTest(windowSize.width, windowSize.height) {
            val tempDirectory = createTempDirectory()
            scene.density = Density(density = 1f, fontScale = 1f)
            mainClock.autoAdvance = false
            setContent { Column(content = content) }
            mainClock.advanceTimeBy(milliseconds = 100)
            val screenshot = Image.makeFromBitmap(captureToImage().asSkiaBitmap())
            val screenshotData = screenshot
                .encodeToData(EncodedImageFormat.PNG)
                ?: error("Could not encode image as png")
            val screenshotPath = tempDirectory / "screenshot.png"
            screenshotPath.writeBytes(screenshotData.bytes)
            val reference = ClassLoader.getSystemResource(referenceName)
            assert(screenshotData.bytes.contentEquals(reference.readBytes())) {
                "The screenshot '$screenshotPath' does not match the reference '$reference'"
            }
        }
    }
}
