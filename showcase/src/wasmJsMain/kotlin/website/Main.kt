package website

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.lightColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import ir.mahozad.multiplatform.wavyslider.WaveDirection.HEAD
import ir.mahozad.multiplatform.wavyslider.WaveDirection.TAIL
import kotlinx.browser.document
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import kotlin.math.roundToInt
import androidx.compose.material.MaterialTheme as MaterialTheme2
import androidx.compose.material3.MaterialTheme as MaterialTheme3
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

/**
 * To generate the website, see the README in the website branch.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.querySelector("#content")!!) {
        App()
    }

    // onWasmReady {
    //     CanvasBasedWindow(
    //         title = "Wavy slider showcase",
    //         canvasElementId = "content",
    //         applyDefaultStyles = false,
    //         requestResize = { IntSize(width = 900, height = 800) }
    //     ) {
    //         App()
    //     }
    // }

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
    MaterialTheme3(colorScheme = lightScheme) {
        Content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Content() {
    var value by remember { mutableFloatStateOf(0.5f) }
    var waveLength by remember { mutableStateOf(30.dp) }
    var waveHeight by remember { mutableStateOf(8.dp) }
    var waveSpeed by remember { mutableStateOf(20.dp) }
    var waveThickness by remember { mutableStateOf(4.dp) }
    var trackThickness2 by remember { mutableStateOf(4.dp) }
    var trackThickness3 by remember { mutableStateOf(16.dp) }
    var isEnabled by remember { mutableStateOf(true) }
    var isBackward by remember { mutableStateOf(true) }
    var isMaterial3 by remember { mutableStateOf(true) }
    var isIncremental by remember { mutableStateOf(false) }
    var fontRobotoSlab by remember { mutableStateOf<FontFamily?>(null) }

    LaunchedEffect(Unit) {
        val fontData = loadResource("RobotoSlab-Regular.ttf")
        fontRobotoSlab = FontFamily(
            Font(identity = "RobotoSlab", data = fontData)
        )
    }

    // FIXME: Fix the layout not being scrollable (except outside the canvas) in mobile/touchscreen devices
    //  See https://github.com/JetBrains/compose-multiplatform/issues/1555
    //  and https://github.com/JetBrains/compose-multiplatform/issues/4672

    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 60.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isMaterial3) {
            WavySlider3(
                enabled = isEnabled,
                value = value,
                onValueChange = { value = it },
                waveLength = waveLength,
                waveHeight = waveHeight,
                waveVelocity = waveSpeed to if (isBackward) TAIL else HEAD,
                waveThickness = waveThickness,
                trackThickness = trackThickness3,
                incremental = isIncremental
            )
        } else {
            MaterialTheme2(lightColors(primary = primaryLight)) {
                WavySlider2(
                    enabled = isEnabled,
                    value = value,
                    onValueChange = { value = it },
                    waveLength = waveLength,
                    waveHeight = waveHeight,
                    waveVelocity = waveSpeed to if (isBackward) TAIL else HEAD,
                    waveThickness = waveThickness,
                    trackThickness = trackThickness2,
                    incremental = isIncremental
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.Top),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier
                    .border(1.dp, MaterialTheme3.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .width(320.dp)
            ) {
                CompositionLocalProvider(
                    LocalTextStyle provides LocalTextStyle.current.copy(
                        fontFamily = fontRobotoSlab,
                        fontSize = 16.sp
                    )
                ) {
                    MaterialDesignVersion(isMaterial3) { isMaterial3 = it }
                    Spacer(Modifier.height(0.dp))
                    Toggle(isEnabled, "Enabled:") { isEnabled = it }
                    Toggle(!isBackward, "Reversed animation:") { isBackward = !it }
                    Toggle(isIncremental, "Incremental wave height:") { isIncremental = it }
                    LabeledSlider(
                        label = "Wave length:",
                        value = waveLength.value,
                        valueRange = 0f..200f,
                        onValueChange = { waveLength = it.dp }
                    )
                    LabeledSlider(
                        label = "Wave height:",
                        value = waveHeight.value,
                        valueRange = 0f..44f,
                        onValueChange = { waveHeight = it.dp }
                    )
                    LabeledSlider(
                        label = "Wave speed:",
                        value = waveSpeed.value,
                        valueRange = 0f..100f,
                        onValueChange = { waveSpeed = it.dp }
                    )
                    LabeledSlider(
                        label = "Wave thickness:",
                        value = waveThickness.value,
                        valueRange = 0f..20f,
                        onValueChange = { waveThickness = it.dp }
                    )
                    LabeledSlider(
                        label = "Track thickness:",
                        value = if (isMaterial3) trackThickness3.value else trackThickness2.value,
                        valueRange = 0f..20f,
                        onValueChange = { if (isMaterial3) trackThickness3 = it.dp else trackThickness2 = it.dp }
                    )
                }
            }
            Code(
                isMaterial3 = isMaterial3,
                value = value,
                isEnabled = isEnabled,
                waveLength = waveLength,
                waveHeight = waveHeight,
                waveSpeed = waveSpeed,
                waveThickness = waveThickness,
                trackThickness = if (isMaterial3) trackThickness3 else trackThickness2,
                isIncremental = isIncremental,
                isBackward = isBackward,
                modifier = Modifier
                    .border(1.dp, MaterialTheme3.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .width(320.dp)
                    .height(484.dp)
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MaterialDesignVersion(
    isMaterial3Selected: Boolean,
    onChange: (isMaterial3Selected: Boolean) -> Unit
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().height(34.dp)) {
        SegmentedButton(
            selected = isMaterial3Selected,
            onClick = { onChange(true) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
        ) {
            Text("Material 3", modifier = Modifier.offset(y = (-2).dp))
        }
        SegmentedButton(
            selected = !isMaterial3Selected,
            onClick = { onChange(false) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) {
            Text("Material 2", modifier = Modifier.offset(y = (-2).dp))
        }
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
        Text(label, modifier = Modifier.weight(1f))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Could also use implementation("dev.snipme:highlights:0.7.1"):
 *
 * ```kotlin
 * val h = Highlights
 *     .Builder()
 *     .code(c)
 *     .theme(SyntaxThemes.notepad(true))
 *     .language(SyntaxLanguage.KOTLIN)
 *     .emphasis(PhraseLocation(13, 25)) // ExampleClass
 *     .build()
 * Text(
 *     modifier = modifier,
 *     text = buildAnnotatedString {
 *         withStyle(SpanStyle(fontSize = 13.sp)) {
 *             append(h.getCode())
 *         }
 *         h
 *             .getHighlights()
 *             .filterIsInstance<ColorHighlight>()
 *             .forEach {
 *                 addStyle(
 *                     SpanStyle(fontSize = 13.sp, color = Color(it.rgb).copy(alpha = 1f)),
 *                     start = it.location.start,
 *                     end = it.location.end,
 *                 )
 *             }
 *         h
 *             .getHighlights()
 *             .filterIsInstance<BoldHighlight>()
 *             .forEach {
 *                 addStyle(
 *                     SpanStyle(fontWeight = FontWeight.Bold),
 *                     start = it.location.start,
 *                     end = it.location.end,
 *                 )
 *             }
 *     }
 * )
 * ```
 */
@Composable
fun Code(
    isMaterial3: Boolean,
    value: Float,
    isEnabled: Boolean,
    waveLength: Dp,
    waveHeight: Dp,
    waveSpeed: Dp,
    waveThickness: Dp,
    trackThickness: Dp,
    isIncremental: Boolean,
    isBackward: Boolean,
    modifier: Modifier
) {
    val valueRounded = remember(value) { roundTo2Decimals(value) }

    val fontSize = remember { 14.sp }
    val lineHeight = remember { 25.sp }
    val colorKeyword = remember { Color(0xff0033B3) }
    val colorNumber = remember { Color(0xff1750EB) }
    val colorMember = remember { Color(0xff871094) }
    val colorFunction = remember { Color(0xff00627A) }
    val colorArgument = remember { Color(0xff4A86E8) }
    val colorIdentifier = remember { Color(0xff000000) }
    val colorSemantic1 = remember { Color(0xff9B3B6A) }
    val colorSemantic2 = remember { Color(0xff005910) }

    // Equivalent to the following
    """
        import ...wavyslider.${if (isMaterial3) "material3" else "material"}.WavySlider
        import ...wavyslider.WaveDirection.*

        var value by remember {
            mutableFloatStateOf(${valueRounded}f)
        }

        WavySlider(
            value = value,
            onValueChange = { value = it },
            enabled = ${if (isEnabled) "true" else "false"},
            waveLength = ${waveLength.value.roundToInt()}.dp,
            waveHeight = ${waveHeight.value.roundToInt()}.dp,
            waveVelocity = ${waveSpeed.value.roundToInt()}.dp to ${if (isBackward) "TAIL" else "HEAD"},
            waveThickness = ${waveThickness.value.roundToInt()}.dp,
            trackThickness = ${trackThickness.value.roundToInt()}.dp,
            incremental = ${if (isIncremental) "true" else "false"},
            animationSpecs = ...
        )
    """.trimIndent()

    val code = buildAnnotatedString {
        pushStyle(ParagraphStyle(lineHeight = lineHeight))
        withStyle(SpanStyle(colorKeyword, 12.sp)) { append("import ") }
        withStyle(SpanStyle(colorIdentifier, 12.sp)) {
            append("...wavyslider.${if (isMaterial3) "material3" else "material"}.WavySlider")
        }
        appendLine()
        withStyle(SpanStyle(colorKeyword, 12.sp)) { append("import ") }
        withStyle(SpanStyle(colorIdentifier, 12.sp)) {
            append("...wavyslider.WaveDirection.*")
        }
        appendLine()
        appendLine()
        withStyle(SpanStyle(colorKeyword, fontSize)) { append("var ") }
        withStyle(SpanStyle(colorSemantic1, fontSize)) { append("value ") }
        withStyle(SpanStyle(colorKeyword, fontSize)) { append("by ") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("remember {") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    mutableFloatStateOf(") }
        withStyle(SpanStyle(colorNumber, fontSize)) { append("${valueRounded}f") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(")") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("}") }
        appendLine()
        appendLine()
        withStyle(SpanStyle(colorFunction, fontSize)) { append("WavySlider") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("(") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    value ") }
        withStyle(SpanStyle(colorArgument, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorSemantic1, fontSize)) { append("value,") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    onValueChange ") }
        withStyle(SpanStyle(colorArgument, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("{") }
        withStyle(SpanStyle(colorSemantic1, fontSize)) { append(" value ") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorSemantic2, fontSize)) { append("it ") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("},") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    enabled ") }
        withStyle(SpanStyle(colorArgument, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorKeyword, fontSize)) { append("$isEnabled") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    waveLength ") }
        withStyle(SpanStyle(colorArgument, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorNumber, fontSize)) { append("${waveLength.value.roundToInt()}") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(".") }
        withStyle(SpanStyle(colorMember, fontSize)) { append("dp") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    waveHeight ") }
        withStyle(SpanStyle(colorArgument, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorNumber, fontSize)) { append("${waveHeight.value.roundToInt()}") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(".") }
        withStyle(SpanStyle(colorMember, fontSize)) { append("dp") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    waveVelocity ") }
        withStyle(SpanStyle(colorArgument, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorNumber, fontSize)) { append("${waveSpeed.value.roundToInt()}") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(".") }
        withStyle(SpanStyle(colorMember, fontSize)) { append("dp ") }
        withStyle(SpanStyle(colorFunction, fontSize)) { append("to ") }
        withStyle(SpanStyle(colorMember, fontSize)) { append(if (isBackward) "TAIL" else "HEAD") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    waveThickness ") }
        withStyle(SpanStyle(colorArgument, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorNumber, fontSize)) { append("${waveThickness.value.roundToInt()}") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(".") }
        withStyle(SpanStyle(colorMember, fontSize)) { append("dp") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    trackThickness ") }
        withStyle(SpanStyle(colorArgument, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorNumber, fontSize)) { append("${trackThickness.value.roundToInt()}") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(".") }
        withStyle(SpanStyle(colorMember, fontSize)) { append("dp") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    incremental ") }
        withStyle(SpanStyle(colorArgument, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorKeyword, fontSize)) { append("$isIncremental") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("    animationSpecs ") }
        withStyle(SpanStyle(colorArgument, fontSize)) { append("= ") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append("...") }
        appendLine()
        withStyle(SpanStyle(colorIdentifier, fontSize)) { append(")") }
    }

    // Makes it possible for user to select the code (for copy/paste)
    SelectionContainer {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            modifier = modifier
        )
    }
}

/**
 * Calls the standard JavaScript `Number.toFixed` function using the [js] helper.
 *
 * The [js] call will be compiled to a JS arrow function that takes the specified parameters.
 * For example, here, the `js("number.toFixed(2)")` will be compiled to `(number) => number.toFixed(2)`.
 *
 * See [this SO post](https://stackoverflow.com/q/42791492)
 * and [this Kotlin guide](https://kotlinlang.org/docs/wasm-js-interop.html)
 */
fun roundTo2Decimals(@Suppress("unused") number: Float): JsNumber = js("number.toFixed(2)")

@OptIn(InternalResourceApi::class)
suspend fun loadResource(path: String): ByteArray {
    return readResourceBytes(path)
}
