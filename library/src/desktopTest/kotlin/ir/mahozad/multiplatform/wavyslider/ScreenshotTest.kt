package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.WaveDirection.HEAD
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.junit.Test
import kotlin.io.path.createTempDirectory
import kotlin.io.path.div
import kotlin.io.path.writeBytes
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

class ScreenshotTest {

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
        WavySlider2(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, waveHeight = 0.dp, waveThickness = 9.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, waveHeight = 0.dp, waveThickness = 9.dp)
    }

    @Test
    fun `When trackThickness is 0, should have the track disappeared`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-4.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, waveHeight = 0.dp, trackThickness = 0.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, waveHeight = 0.dp, trackThickness = 0.dp)
    }

    @Test
    fun `When trackThickness is negative, should have the same behaviour as if the thickness was 0`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-4.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, waveHeight = 0.dp, trackThickness = (-10).dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, waveHeight = 0.dp, trackThickness = (-10).dp)
    }

    @Test
    fun `When trackThickness is a large value, should have proper track thickness`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-5.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, trackThickness = 18.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveVelocity = 0.dp to HEAD, trackThickness = 18.dp)
    }

    @Test
    fun `When waveHeight is more than thumb height, should take into account the height of wave in component overall height and also the component overall height should be exactly equal to the wave height`() =
        runScreenshotTest(
            windowSize = IntSize(width = 400, height = 126),
            referenceName = "reference-6.png"
        ) {
            WavySlider2(value = 0.5f, onValueChange = {}, waveHeight = 57.dp, modifier = Modifier.border(1.dp, Color.Red))
            Spacer(Modifier.height(2.dp))
            WavySlider3(value = 0.5f, onValueChange = {}, waveHeight = 57.dp, modifier = Modifier.border(1.dp, Color.Green))
        }

    @Test
    fun `When waveHeight is negative, Should have the same behaviour as if the size was positive (except that the phase may be shifted)`() =
        runScreenshotTest(
            windowSize = IntSize(width = 400, height = 126),
            referenceName = "reference-6.png"
        ) {
            WavySlider2(value = 0.5f, onValueChange = {}, waveHeight = (-57).dp, modifier = Modifier.border(1.dp, Color.Red))
            Spacer(Modifier.height(2.dp))
            WavySlider3(value = 0.5f, onValueChange = {}, waveHeight = (-57).dp, modifier = Modifier.border(1.dp, Color.Green))
        }

    @Test
    fun `When container layout direction is LTR and incremental is true, should have proper gradual height (from the thumb with most height to the tail with least height`() =
        runScreenshotTest(
            windowSize = IntSize(width = 400, height = 100),
            referenceName = "reference-7.png"
        ) {
            WavySlider2(value = 0.7f, onValueChange = {}, waveHeight = 16.dp, incremental = true)
            WavySlider3(value = 0.7f, onValueChange = {}, waveHeight = 16.dp, incremental = true)
        }

    @Test
    fun `When container layout direction is RTL and incremental is true, should have proper gradual height (from the thumb with most height to the tail with least height`() =
        runScreenshotTest(
            windowSize = IntSize(width = 400, height = 100),
            referenceName = "reference-8.png"
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WavySlider2(value = 0.7f, onValueChange = {}, waveHeight = 16.dp, incremental = true)
                WavySlider3(value = 0.7f, onValueChange = {}, waveHeight = 16.dp, incremental = true)
            }
        }

    @Test
    fun `When waveLength is a large value, should have proper wave length`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-9.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, waveLength = 128.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, waveLength = 128.dp)
    }

    @Test
    fun `When waveLength is larger than slider total length, the wave should be displayed properly`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 100),
        referenceName = "reference-10.png"
    ) {
        WavySlider2(value = 0.5f, onValueChange = {}, modifier = Modifier.width(400.dp), waveHeight = 24.dp, waveLength = 500.dp)
        WavySlider3(value = 0.5f, onValueChange = {}, modifier = Modifier.width(400.dp), waveHeight = 24.dp, waveLength = 500.dp)
    }

    @Test
    fun `When waveThickness is the default value of Material 3 Slider, the wave should be like that of the Material 3 Slider`() =
        runScreenshotTest(
            windowSize = IntSize(width = 400, height = 100),
            referenceName = "reference-11.png"
        ) {
            Slider(value = 0.5f, onValueChange = {})
            WavySlider3(value = 0.5f, onValueChange = {}, waveThickness = 16.dp, waveHeight = 0.dp)
        }

    @Test
    @OptIn(ExperimentalMaterial3Api::class)
    fun `When a custom thumb is set, the custom thumb should be displayed and its height be taken into account in overall component height`() =
        runScreenshotTest(
            windowSize = IntSize(width = 400, height = 130),
            referenceName = "reference-12.png"
        ) {
            val thumb: @Composable (SliderState) -> Unit = @Composable {
                Box(Modifier.width(6.dp).height(128.dp).background(Color.Red))
            }
            WavySlider3(value = 0.5f, onValueChange = {}, thumb = thumb, modifier = Modifier.border(1.dp, Color.Green))
        }

    @Test
    fun `When the screen density is something low, should have everything scaled proportionally`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 50),
        referenceName = "reference-13.png"
    ) {
        CompositionLocalProvider(LocalDensity provides Density(0.43f)) {
            WavySlider2(value = 0.5f, onValueChange = {})
            WavySlider3(value = 0.5f, onValueChange = {})
        }
    }

    @Test
    fun `When the screen density is something high, should have everything scaled proportionally`() = runScreenshotTest(
        windowSize = IntSize(width = 400, height = 250),
        referenceName = "reference-14.png"
    ) {
        CompositionLocalProvider(LocalDensity provides Density(2.43f)) {
            WavySlider2(value = 0.5f, onValueChange = {})
            WavySlider3(value = 0.5f, onValueChange = {})
        }
    }

    @OptIn(InternalComposeUiApi::class, ExperimentalTestApi::class)
    private fun runScreenshotTest(
        windowSize: IntSize,
        referenceName: String,
        content: @Composable ColumnScope.() -> Unit
    ) = runDesktopComposeUiTest(windowSize.width, windowSize.height) {
        val tempDirectory = createTempDirectory()
        scene.density = Density(density = 1f, fontScale = 1f)
        mainClock.autoAdvance = false
        setContent { Column(content = content) }
        mainClock.advanceTimeBy(milliseconds = 10_000)
        val screenshot = Image.makeFromBitmap(captureToImage().asSkiaBitmap())
        val screenshotData = screenshot
            .encodeToData(EncodedImageFormat.PNG)
            ?: error("Could not encode image as png")
        val screenshotPath = tempDirectory / "screenshot.png"
        screenshotPath.writeBytes(screenshotData.bytes)
        val reference = ClassLoader.getSystemResource(referenceName)
        assert(screenshotData.bytes contentEquals reference.readBytes()) {
            "The screenshot '$screenshotPath' does not match the reference '$reference'"
        }
    }
}
