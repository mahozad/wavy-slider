import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import ir.mahozad.multiplatform.wavyslider.WaveAnimationDirection
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
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
    var isEnabled by remember { mutableStateOf(true) }
    var waveLength by remember { mutableStateOf(24.dp) }
    var waveHeight by remember { mutableStateOf(24.dp) }
    var waveThickness by remember { mutableStateOf(4.dp) }
    var trackThickness by remember { mutableStateOf(4.dp) }
    var isRTL by remember { mutableStateOf(true) }
    var isFlattened by remember { mutableStateOf(false) }
    var isMaterial3 by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 90.dp),
        verticalArrangement = Arrangement.spacedBy(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isMaterial3) {
            WavySlider3(
                enabled = isEnabled,
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
            WavySlider2(
                enabled = isEnabled,
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
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(16.dp)
                .width(300.dp)
        ) {
            MaterialDesignVersion(isMaterial3) { isMaterial3 = it }
            Spacer(Modifier.height(0.dp))
            Toggle(isEnabled, "Enabled:") { isEnabled = it }
            Toggle(isFlattened, "Flatten:") { isFlattened = it }
            Toggle(!isRTL, "Reverse:") { isRTL = !it }
            LabeledSlider(
                label = "Wave length:",
                value = waveLength.value,
                valueRange = 6f..200f,
                onValueChange = { waveLength = it.dp }
            )
            LabeledSlider(
                label = "Wave height:",
                value = waveHeight.value,
                valueRange = 0f..48f,
                onValueChange = { waveHeight = it.dp }
            )
            LabeledSlider(
                label = "Wave thickness:",
                value = waveThickness.value,
                valueRange = 1f..20f,
                onValueChange = { waveThickness = it.dp }
            )
            LabeledSlider(
                label = "Track thickness:",
                value = trackThickness.value,
                valueRange = 0f..20f,
                onValueChange = { trackThickness = it.dp }
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MaterialDesignVersion(isMaterial3: Boolean, onChange: (Boolean) -> Unit) {
    @Composable
    fun Version(n: Int) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onChange(n == 3) }
            )
        ) {
            Icon(
                painterResource("m$n-logo.png"),
                contentDescription = "Material $n",
                modifier = Modifier.size(34.dp),
                tint = if (n == 3 && isMaterial3) {
                    MaterialTheme.colorScheme.primary
                } else if (n == 3 && !isMaterial3) {
                    LocalContentColor.current
                } else if (n == 2 && isMaterial3) {
                    LocalContentColor.current
                } else {
                    Color(0xff6200ee)
                }
            )
            Spacer(Modifier.width(6.dp))
            Text("Material $n")
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Version(3)
        Version(2)
    }
}

@Composable
fun Toggle(
    isOn: Boolean,
    label: String,
    onToggle: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(label)
        Switch(isOn, onCheckedChange = onToggle)
    }
}

@Composable
fun LabeledSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(136.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}
