package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ir.mahozad.multiplatform.wavyslider.WaveDirection.TAIL
import ir.mahozad.multiplatform.wavyslider.material.WavySlider
import ir.mahozad.multiplatform.wavyslider.material3.Track
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider

// See the README in the <PROJECT ROOT>/asset directory
fun main() {
    System.setProperty("skiko.renderApi", "OPENGL")
    application(exitProcessOnExit = false) {
        Window(
            title = "WavySliderDemo",
            undecorated = true,
            transparent = true,
            resizable = false,
            state = rememberWindowState(
                size = DpSize(640.dp, Dp.Companion.Unspecified),
                position = WindowPosition(Alignment.Center)
            ),
            onCloseRequest = ::exitApplication
        ) {
            MaterialTheme {
                var value by remember { mutableStateOf(0.5f) }
                val onValueChange: (Float) -> Unit = remember { { value = it } }
                val isDark = true
                val colorsLightM3 = SliderDefaults.colors(
                    thumbColor = Color(0xff727d1a), // Primary
                    activeTrackColor = Color(0xff727d1a), // Primary
                    inactiveTrackColor = Color(0xffe4e3d2) // Light SurfaceVariant
                )
                val colorsDarkM3 = SliderDefaults.colors(
                    thumbColor = Color(0xff727d1a), // Primary
                    activeTrackColor = Color(0xff727d1a), // Primary
                    inactiveTrackColor = Color(0xff47483b) // Dark SurfaceVariant
                )
                val colorsLightM2 = androidx.compose.material.SliderDefaults.colors(
                    thumbColor = Color(0xff727d1a), // Primary
                    activeTrackColor = Color(0xff727d1a), // Primary
                    inactiveTrackColor = Color(0xffe4e3d2) // Light SurfaceVariant
                )
                val colorsDarkM2 = androidx.compose.material.SliderDefaults.colors(
                    thumbColor = Color(0xff727d1a), // Primary
                    activeTrackColor = Color(0xff727d1a), // Primary
                    inactiveTrackColor = Color(0xff47483b) // Dark SurfaceVariant
                )
                val colorsM2 = if (isDark) colorsDarkM2 else colorsLightM2
                val colorsM3 = if (isDark) colorsDarkM3 else colorsLightM3
                CompositionLocalProvider(LocalDensity provides Density(1.5f)) {
                    Column {
                        WavySlider(value, onValueChange, colors = colorsM2, trackThickness = 2.dp)
                        WavySlider(
                            value,
                            onValueChange,
                            colors = colorsM2,
                            waveLength = 30.dp,
                            waveVelocity = 15.dp to WaveDirection.HEAD,
                            trackThickness = 4.dp
                        )
                        @OptIn(ExperimentalMaterial3Api::class)
                        (WavySlider(
                            value,
                            onValueChange,
                            track = {
                                SliderDefaults.Track(
                                    it,
                                    colors = colorsM3,
                                    waveHeight = 14.dp,
                                    waveVelocity = 20.dp to TAIL,
                                    waveThickness = 3.dp,
                                    trackThickness = 12.dp,
                                    incremental = true,
                                    thumbTrackGapSize = 8.dp
                                )
                            },
                            thumb = { Box(Modifier.size(18.dp).rotate(45f).background(Color(0xff727d1a))) }
                        ))
                        WavySlider(value, onValueChange, colors = colorsM3, waveHeight = 0.dp)
                    }
                }
            }
        }
    }
}
