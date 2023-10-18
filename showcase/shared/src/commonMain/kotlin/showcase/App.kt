package showcase

import androidx.compose.runtime.*
import ir.mahozad.multiplatform.material.WavySlider

@Composable
fun App() {
    var value by remember { mutableFloatStateOf(0.67f) }
    WavySlider(
        value = value,
        onValueChange = { value = it }
    )
}
