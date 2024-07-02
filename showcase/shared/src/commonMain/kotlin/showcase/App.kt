package showcase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider as Slider2
import androidx.compose.material3.Slider as Slider3
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.WaveDirection.RIGHT
import androidx.compose.material.SliderDefaults as SliderDefaults2
import androidx.compose.material3.SliderDefaults as SliderDefaults3
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val state = remember { SliderState(0.67f) }
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

        Divider(Modifier.padding(vertical = 32.dp))

        Slider3(state)
        WavySlider3(state)
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
