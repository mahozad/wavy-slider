package showcase

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
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
        WavySlider3(
            value = value,
            onValueChange = { value = it }
        )
    }
}
