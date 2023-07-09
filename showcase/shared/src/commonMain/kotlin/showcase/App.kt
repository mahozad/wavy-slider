package showcase

import androidx.compose.runtime.*
import ir.mahozad.multiplatform.WavySlider

@Composable
fun App() {
    var value by remember { mutableStateOf(0.67f) }
    WavySlider(
        value = value,
        onValueChange = { value = it }
    )
}
