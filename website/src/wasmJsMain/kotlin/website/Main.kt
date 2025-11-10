package website

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import ir.mahozad.multiplatform.wavyslider.WaveDirection.HEAD
import ir.mahozad.multiplatform.wavyslider.WaveDirection.TAIL
import ir.mahozad.wavyslider.Res
import ir.mahozad.wavyslider.RobotoSlab_Regular
import kotlinx.browser.document
import org.jetbrains.compose.resources.Font
import org.w3c.dom.Element
import org.w3c.dom.get
import kotlin.math.roundToInt
import androidx.compose.material.MaterialTheme as MaterialTheme2
import androidx.compose.material3.MaterialTheme as MaterialTheme3
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

/**
 * To generate the website, see the README in the website branch.
 */
fun main() {
    @OptIn(ExperimentalComposeUiApi::class)
    ComposeViewport(viewportContainerId = "app") {
        App()
    }
}

@Composable
fun App() {
    var colorScheme by remember { mutableStateOf(getM3ColorScheme()) }
    var layoutDirection by remember { mutableStateOf(LayoutDirection.Ltr) }
    document.addEventListener("themeToggle") { colorScheme = getM3ColorScheme() }
    document.getElementById("flip-button")?.addEventListener("click") {
        (it.target as? Element)?.classList?.toggle("flipped")
        layoutDirection = if (layoutDirection == LayoutDirection.Ltr) LayoutDirection.Rtl else LayoutDirection.Ltr
    }
    MaterialTheme3(animateColorScheme(colorScheme)) {
        Surface {
            Content(layoutDirection)
        }
    }
}

@Composable
fun Content(layoutDirection: LayoutDirection) {
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
    val fontRobotoSlab = FontFamily(Font(Res.font.RobotoSlab_Regular))
    val verticalScrollState = rememberScrollState(initial = 0)
    // VerticalScrollbar(
    //     modifier = Modifier.fillMaxHeight(),
    //     adapter = rememberScrollbarAdapter(verticalScrollState)
    // )
    Column(
        verticalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(verticalScrollState)
            .padding(start = 8.dp, end = 8.dp, top = 60.dp, bottom = 58.dp)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
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
                MaterialTheme2(colors = getM2Colors()) {
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
            Text(text = "Material 3")
        }
        SegmentedButton(
            selected = !isMaterial3Selected,
            onClick = { onChange(false) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) {
            Text(text = "Material 2")
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
 * Could also use implementation("dev.snipme:highlights:1.0.0"):
 *
 * ```kotlin
 * val valueRounded = remember(value) { roundTo2Decimals(value) }
 * val lineHeight = remember { 25.sp }
 * val fontSize = remember { 14.sp }
 * val rawCode = /* language=kotlin */ """..."""
 * val highlights = Highlights
 *     .Builder()
 *     .code(rawCode)
 *     .theme(SyntaxThemes.pastel(darkMode = isCurrentThemeDark()))
 *     .language(SyntaxLanguage.KOTLIN)
 *     .build()
 * val highlightedCode = buildAnnotatedString {
 *     pushStyle(ParagraphStyle(lineHeight = lineHeight))
 *     withStyle(SpanStyle(fontSize = fontSize)) {
 *         append(highlights.getCode())
 *     }
 *     highlights
 *         .getHighlights()
 *         .filterIsInstance<ColorHighlight>()
 *         .forEach {
 *             addStyle(
 *                 SpanStyle(fontSize = fontSize, color = Color(it.rgb).copy(alpha = 1f)),
 *                 start = it.location.start,
 *                 end = it.location.end,
 *             )
 *         }
 * }
 * SelectionContainer {
 *     Text(
 *         text = highlightedCode,
 *         modifier = modifier,
 *         fontFamily = FontFamily.Monospace
 *     )
 * }
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
    val colorOnSurface = MaterialTheme3.colorScheme.onSurface
    val valueRounded = remember(value) { roundTo2Decimals(value) }
    val codeTheme = if (isThemeDark()) codeColorsDark(colorOnSurface) else codeColorsLight(colorOnSurface)

    /* Equivalent to the following code */ /* language=kotlin */ """
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
        withStyle(SpanStyle(color = codeTheme.keyword, fontSize = 13.sp)) { append("import ") }
        withStyle(SpanStyle(color = codeTheme.identifier, fontSize = 13.sp)) {
            append("...wavyslider.${if (isMaterial3) "material3" else "material"}.WavySlider")
        }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.keyword, fontSize = 13.sp)) { append("import ") }
        withStyle(SpanStyle(color = codeTheme.identifier, fontSize = 13.sp)) {
            append("...wavyslider.WaveDirection.*")
        }
        appendLine()
        appendLine()
        withStyle(SpanStyle(color = codeTheme.keyword)) { append("var ") }
        withStyle(SpanStyle(color = codeTheme.semantic1)) { append("value ") }
        withStyle(SpanStyle(color = codeTheme.keyword)) { append("by ") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("remember {") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    mutableFloatStateOf(") }
        withStyle(SpanStyle(color = codeTheme.number)) { append("${valueRounded}f") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(")") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("}") }
        appendLine()
        appendLine()
        withStyle(SpanStyle(color = codeTheme.function)) { append("WavySlider") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("(") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    value ") }
        withStyle(SpanStyle(color = codeTheme.argument)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.semantic1)) { append("value,") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    onValueChange ") }
        withStyle(SpanStyle(color = codeTheme.argument)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("{") }
        withStyle(SpanStyle(color = codeTheme.semantic1)) { append(" value ") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.semantic2)) { append("it ") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("},") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    enabled ") }
        withStyle(SpanStyle(color = codeTheme.argument)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.keyword)) { append("$isEnabled") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(",") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    waveLength ") }
        withStyle(SpanStyle(color = codeTheme.argument)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.number)) { append("${waveLength.value.roundToInt()}") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(".") }
        withStyle(SpanStyle(color = codeTheme.member)) { append("dp") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(",") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    waveHeight ") }
        withStyle(SpanStyle(color = codeTheme.argument)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.number)) { append("${waveHeight.value.roundToInt()}") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(".") }
        withStyle(SpanStyle(color = codeTheme.member)) { append("dp") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(",") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    waveVelocity ") }
        withStyle(SpanStyle(color = codeTheme.argument)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.number)) { append("${waveSpeed.value.roundToInt()}") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(".") }
        withStyle(SpanStyle(color = codeTheme.member)) { append("dp ") }
        withStyle(SpanStyle(color = codeTheme.function)) { append("to ") }
        withStyle(SpanStyle(color = codeTheme.member)) { append(if (isBackward) "TAIL" else "HEAD") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(",") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    waveThickness ") }
        withStyle(SpanStyle(color = codeTheme.argument)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.number)) { append("${waveThickness.value.roundToInt()}") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(".") }
        withStyle(SpanStyle(color = codeTheme.member)) { append("dp") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(",") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    trackThickness ") }
        withStyle(SpanStyle(color = codeTheme.argument)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.number)) { append("${trackThickness.value.roundToInt()}") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(".") }
        withStyle(SpanStyle(color = codeTheme.member)) { append("dp") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(",") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    incremental ") }
        withStyle(SpanStyle(color = codeTheme.argument)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.keyword)) { append("$isIncremental") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(",") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("    animationSpecs ") }
        withStyle(SpanStyle(color = codeTheme.argument)) { append("= ") }
        withStyle(SpanStyle(color = codeTheme.identifier)) { append("...") }
        appendLine()
        withStyle(SpanStyle(color = codeTheme.identifier)) { append(")") }
    }
    // Makes it possible for user to select the code (for copy/paste)
    SelectionContainer {
        Text(
            text = code,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 25.sp,
            letterSpacing = 1.sp,
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
 * and [this Kotlin guide](https://kotlinlang.org/docs/wasm-js-interop.html).
 */
fun roundTo2Decimals(@Suppress("unused") number: Float): JsNumber = js("number.toFixed(2)")

fun getM3ColorScheme(): ColorScheme =
    if (isThemeDark()) darkScheme else lightScheme

fun getM2Colors(): Colors =
    if (isThemeDark()) darkColors(primaryDark) else lightColors(primaryLight)

fun isThemeDark(): Boolean =
    document.documentElement?.attributes?.get("data-theme")?.value == "dark"
