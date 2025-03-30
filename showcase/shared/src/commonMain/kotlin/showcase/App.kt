package showcase

import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.WaveAnimationSpecs
import ir.mahozad.multiplatform.wavyslider.WaveDirection.RIGHT
import ir.mahozad.multiplatform.wavyslider.material3.Track
import androidx.compose.material.Slider as Slider2
import androidx.compose.material.SliderDefaults as SliderDefaults2
import androidx.compose.material3.Slider as Slider3
import androidx.compose.material3.SliderDefaults as SliderDefaults3
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val state = remember { SliderState(value = 0.67f) }
    Column {
        Slider2(value = state.value, onValueChange = { state.value = it })
        WavySlider2(
            value = state.value,
            onValueChange = { state.value = it }
        )
        WavySlider2(
            value = state.value,
            onValueChange = { state.value = it },
            waveLength = 12.dp,
            waveHeight = 24.dp,
            waveVelocity = 14.dp to RIGHT,
            waveThickness = 1.dp,
            trackThickness = 5.dp,
            incremental = true,
            colors = SliderDefaults2.colors(activeTrackColor = Color.Red)
        )
        WavySlider2(
            value = state.value,
            onValueChange = { state.value = it },
            waveHeight = 0.dp
        )

        WavyDivider()

        // FIXME: Some of the sliders have broken layout because all the sliders are using the same state instance
        //  Is it a bug at all or is it intended?
        //  It happens because there are `state.something = ...` assignments in the material3.WavySlider

        Slider3(state)
        Slider3(
            state = state,
            thumb = { Box(modifier = Modifier.size(64.dp).background(Color.Yellow)) }
        )
        WavySlider3(
            state = state,
            thumb = { Box(modifier = Modifier.size(64.dp).background(Color.Red)) }
        )
        WavySlider3(state,
            track = {
                SliderDefaults3.Track(
                    sliderState = state,
                    waveLength = 20.dp,
                    thumbTrackGapSize = 0.dp
                )
            }
        )
        WavySlider3(
            state = state,
            waveLength = 12.dp,
            waveHeight = 24.dp,
            waveVelocity = 14.dp to RIGHT,
            waveThickness = 1.dp,
            trackThickness = 2.dp,
            incremental = true,
            colors = SliderDefaults3.colors(activeTrackColor = Color.Red)
        )
        WavySlider3(state = state, waveHeight = 0.dp)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WavyDivider() {
    WavySlider3(
        value = 1f,
        onValueChange = {},
        thumb = {},
        track = {
            SliderDefaults.Track(
                it,
                enabled = false,
                thumbTrackGapSize = 0.dp,
                waveThickness = 1.dp,
                waveVelocity = 0.dp to RIGHT,
                animationSpecs = WaveAnimationSpecs(
                    waveAppearanceAnimationSpec = snap(),
                    waveVelocityAnimationSpec = snap(),
                    waveHeightAnimationSpec = snap()
                )
            )
        }
    )
}
