package website

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import ir.mahozad.multiplatform.wavyslider.WaveDirection.HEAD
import ir.mahozad.multiplatform.wavyslider.WaveDirection.TAIL
import kotlinx.browser.document
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.get
import kotlin.math.roundToInt
import androidx.compose.material.MaterialTheme as MaterialTheme2
import androidx.compose.material3.MaterialTheme as MaterialTheme3
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

@Suppress("unused")
external object EventDetail : JsAny {
    val originId: String
    val oldState: String
    val newState: String
}

// See https://developer.mozilla.org/en-US/docs/Web/API/CustomEvent
external class CustomEvent: Event {
    val detail: EventDetail
}

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
    val isSystemThemeDark = isSystemInDarkTheme()
    val isPageThemeDark = isPageThemeDark()
    var isDark by remember { mutableStateOf(isPageThemeDark) }
    document.addEventListener("themeToggle") { event ->
        val themeToggleEvent = event as? CustomEvent ?: return@addEventListener
        isDark = when (themeToggleEvent.detail.newState) {
            "light" -> false
            "dark" -> true
            else -> isSystemThemeDark
        }
    }
    var layoutDirection by remember { mutableStateOf(LayoutDirection.Ltr) }
    document.querySelector("#flip-direction")?.addEventListener("click") { event ->
        (event.target as? Element)?.classList?.toggle("toggled")
        layoutDirection = if (layoutDirection == LayoutDirection.Ltr) LayoutDirection.Rtl else LayoutDirection.Ltr
    }
    MaterialTheme3(colorScheme = if (isDark) darkScheme else lightScheme) {
        Surface(color = if (isCurrentThemeDark()) MaterialTheme3.colorScheme.surface else Color.White) {
            Content(layoutDirection)
        }
    }
}

fun isPageThemeDark(): Boolean =
    document.documentElement?.attributes?.get("data-theme")?.value == "dark"

@Composable
fun isCurrentThemeDark(): Boolean {
    return MaterialTheme3.colorScheme.background.luminance() <= 0.5
}

@OptIn(ExperimentalLayoutApi::class)
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
    var fontRobotoSlab by remember { mutableStateOf<FontFamily?>(null) }

    LaunchedEffect(Unit) {
        val fontData = loadResource("RobotoSlab-Regular.ttf")
        fontRobotoSlab = FontFamily(
            Font(identity = "RobotoSlab", data = fontData)
        )
    }

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
                MaterialTheme2(if (isCurrentThemeDark()) darkColors(primary = primaryDark) else lightColors(primary = primaryLight)) {
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
    data class CodeColors(
        val keyword: Color,
        val number: Color,
        val member: Color,
        val function: Color,
        val argument: Color,
        val semantic1: Color,
        val semantic2: Color,
        val identifier: Color,
    )

    val colorOnSurface = MaterialTheme3.colorScheme.onSurface
    val valueRounded = remember(value) { roundTo2Decimals(value) }
    val lineHeight = remember { 25.sp }
    val fontSize = remember { 14.sp }
    val lightColors = remember(colorOnSurface) {
        CodeColors(
            keyword = Color(0xFF_0033b3),
            number = Color(0xFF_1750eb),
            member = Color(0xFF_871094),
            function = Color(0xFF_00627a),
            argument = Color(0xFF_4a86e8),
            semantic1 = Color(0xFF_9b3b6a),
            semantic2 = Color(0xFF_005910),
            identifier = colorOnSurface,
        )
    }
    // Adopted from IntelliJ IDEA 2025.1 Dark theme default Kotlin color schemes
    val darkColors = remember(colorOnSurface) {
        CodeColors(
            keyword = Color(0xFF_cf8e6d),
            number = Color(0xFF_2aacb8),
            member = Color(0xFF_c77dbb),
            function = Color(0xFF_57aaf7),
            argument = Color(0xFF_56c1d6),
            semantic1 = Color(0xFF_529d52),
            semantic2 = Color(0xFF_be7070),
            identifier = colorOnSurface,
        )
    }
    val codeTheme = if (isCurrentThemeDark()) darkColors else lightColors

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
        pushStyle(ParagraphStyle(lineHeight = lineHeight))
        withStyle(SpanStyle(codeTheme.keyword, fontSize)) { append("import ") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) {
            append("...wavyslider.${if (isMaterial3) "material3" else "material"}.WavySlider")
        }
        appendLine()
        withStyle(SpanStyle(codeTheme.keyword, fontSize)) { append("import ") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) {
            append("...wavyslider.WaveDirection.*")
        }
        appendLine()
        appendLine()
        withStyle(SpanStyle(codeTheme.keyword, fontSize)) { append("var ") }
        withStyle(SpanStyle(codeTheme.semantic1, fontSize)) { append("value ") }
        withStyle(SpanStyle(codeTheme.keyword, fontSize)) { append("by ") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("remember {") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    mutableFloatStateOf(") }
        withStyle(SpanStyle(codeTheme.number, fontSize)) { append("${valueRounded}f") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(")") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("}") }
        appendLine()
        appendLine()
        withStyle(SpanStyle(codeTheme.function, fontSize)) { append("WavySlider") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("(") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    value ") }
        withStyle(SpanStyle(codeTheme.argument, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.semantic1, fontSize)) { append("value,") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    onValueChange ") }
        withStyle(SpanStyle(codeTheme.argument, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("{") }
        withStyle(SpanStyle(codeTheme.semantic1, fontSize)) { append(" value ") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.semantic2, fontSize)) { append("it ") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("},") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    enabled ") }
        withStyle(SpanStyle(codeTheme.argument, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.keyword, fontSize)) { append("$isEnabled") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    waveLength ") }
        withStyle(SpanStyle(codeTheme.argument, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.number, fontSize)) { append("${waveLength.value.roundToInt()}") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(".") }
        withStyle(SpanStyle(codeTheme.member, fontSize)) { append("dp") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    waveHeight ") }
        withStyle(SpanStyle(codeTheme.argument, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.number, fontSize)) { append("${waveHeight.value.roundToInt()}") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(".") }
        withStyle(SpanStyle(codeTheme.member, fontSize)) { append("dp") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    waveVelocity ") }
        withStyle(SpanStyle(codeTheme.argument, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.number, fontSize)) { append("${waveSpeed.value.roundToInt()}") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(".") }
        withStyle(SpanStyle(codeTheme.member, fontSize)) { append("dp ") }
        withStyle(SpanStyle(codeTheme.function, fontSize)) { append("to ") }
        withStyle(SpanStyle(codeTheme.member, fontSize)) { append(if (isBackward) "TAIL" else "HEAD") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    waveThickness ") }
        withStyle(SpanStyle(codeTheme.argument, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.number, fontSize)) { append("${waveThickness.value.roundToInt()}") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(".") }
        withStyle(SpanStyle(codeTheme.member, fontSize)) { append("dp") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    trackThickness ") }
        withStyle(SpanStyle(codeTheme.argument, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.number, fontSize)) { append("${trackThickness.value.roundToInt()}") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(".") }
        withStyle(SpanStyle(codeTheme.member, fontSize)) { append("dp") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    incremental ") }
        withStyle(SpanStyle(codeTheme.argument, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.keyword, fontSize)) { append("$isIncremental") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(",") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("    animationSpecs ") }
        withStyle(SpanStyle(codeTheme.argument, fontSize)) { append("= ") }
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append("...") }
        appendLine()
        withStyle(SpanStyle(codeTheme.identifier, fontSize)) { append(")") }
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
