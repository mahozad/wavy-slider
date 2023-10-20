package showcase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SliderDefaults
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.WaveAnimationDirection
import ir.mahozad.multiplatform.wavyslider.material3.WavySliderDefaults
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

@Composable
fun App() {
    var value by remember { mutableFloatStateOf(0.67f) }
    Column {
        WavySlider2(
            value = value,
            onValueChange = { value = it }
        )
        WavySlider2(
            value = value,
            onValueChange = { value = it },
            waveLength = 12.dp,
            waveHeight = 24.dp,
            colors = SliderDefaults.colors(activeTrackColor = Color.Red),
            waveThickness = 1.dp,
            trackThickness = 5.dp,
            animationDirection = WaveAnimationDirection.LTR,
            shouldFlatten = true
        )
        WavySlider2(
            value = value,
            onValueChange = { value = it },
            waveHeight = 0.dp
        )

        Divider(Modifier.padding(vertical = 32.dp))

        WavySlider3(
            value = value,
            onValueChange = { value = it }
        )
        WavySlider3(
            value = value,
            onValueChange = { value = it },
            waveLength = 12.dp,
            waveHeight = 24.dp,
            colors = WavySliderDefaults.colors(activeTrackColor = Color.Red),
            waveThickness = 1.dp,
            trackThickness = 5.dp,
            animationDirection = WaveAnimationDirection.LTR,
            shouldFlatten = true
        )
        WavySlider3(
            value = value,
            onValueChange = { value = it },
            waveHeight = 0.dp
        )
    }
}
