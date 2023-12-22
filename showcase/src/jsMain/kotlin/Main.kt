import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import ir.mahozad.multiplatform.wavyslider.WaveAnimationDirection
import org.jetbrains.skiko.wasm.onWasmReady
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

/**
 * To generate the website, see the README in the website branch.
 */
fun main() {
    onWasmReady {
        Window(title = "Wavy slider showcase") {
            App()
        }
    }

    // See https://github.com/JetBrains/compose-multiplatform/issues/2186
    // val body = document.getElementsByTagName("body")[0] as HTMLElement
    // renderComposable(rootElementId = "root") {
    //     MaterialTheme {
    //         WavySlider(0.67f, true, {})
    //     }
    // }
}

@Composable
fun App() {
    MaterialTheme(colorScheme = lightScheme) {
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle.Default.copy(fontFamily = FontFamily.SansSerif)
        ) {
            Content()
        }
    }
}

@Composable
fun Content() {
    var value by remember { mutableFloatStateOf(0.5f) }
    var waveLength by remember { mutableStateOf(20.dp) }
    var waveHeight by remember { mutableStateOf(20.dp) }
    var waveThickness by remember { mutableStateOf(4.dp) }
    var trackThickness by remember { mutableStateOf(4.dp) }
    var isRTL by remember { mutableStateOf(true) }
    var isFlattened by remember { mutableStateOf(false) }
    var variant by remember { mutableStateOf("Material 3") }
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (variant == "Material 2") {
            WavySlider2(
                value = value,
                onValueChange = { value = it },
                waveLength = waveLength,
                waveHeight = waveHeight,
                waveThickness = waveThickness,
                trackThickness = trackThickness,
                shouldFlatten = isFlattened,
                animationDirection = if (isRTL) WaveAnimationDirection.RTL else WaveAnimationDirection.LTR
            )
        } else {
            WavySlider3(
                value = value,
                onValueChange = { value = it },
                waveLength = waveLength,
                waveHeight = waveHeight,
                waveThickness = waveThickness,
                trackThickness = trackThickness,
                shouldFlatten = isFlattened,
                animationDirection = if (isRTL) WaveAnimationDirection.RTL else WaveAnimationDirection.LTR
            )
        }
        Spacer(Modifier.height(60.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.width(280.dp)
        ) {
            Dropdown(
                label = "Variant: $variant",
                options = listOf("Material 3", "Material 2"),
                onChange = { variant = if (it == 0) "Material 3" else "Material 2" }
            )
            Dropdown(
                label = "Head to tail: ${if (isFlattened) "Flatten" else "Regular"}",
                options = listOf("Regular", "Flatten"),
                onChange = { isFlattened = it == 1 }
            )
            Dropdown(
                label = "Move direction: ${if (isRTL) "RTL" else "LTR"}",
                options = listOf("RTL", "LTR"),
                onChange = { isRTL = it == 0 }
            )
            Spacer(Modifier.height(1.dp))
            Slider(
                value = waveLength.value,
                onValueChange = { waveLength = it.dp },
                valueRange = 6f..200f
            )
            Slider(
                value = waveHeight.value,
                onValueChange = { waveHeight = it.dp },
                valueRange = 0f..48f
            )
            Slider(
                value = waveThickness.value,
                onValueChange = { waveThickness = it.dp },
                valueRange = 1f..20f
            )
            Slider(
                value = trackThickness.value,
                onValueChange = { trackThickness = it.dp },
                valueRange = 0f..20f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(
    label: String,
    options: List<String>,
    onChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = label,
            onValueChange = {},
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.mapIndexed { i, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onChange(i)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
