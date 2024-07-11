package website

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import ir.mahozad.multiplatform.wavyslider.WaveDirection.HEAD
import ir.mahozad.multiplatform.wavyslider.WaveDirection.TAIL
import ir.mahozad.wavyslider.Res
import ir.mahozad.wavyslider.m2_logo
import ir.mahozad.wavyslider.m3_logo
import kotlinx.browser.document
import org.jetbrains.compose.resources.*
import kotlin.math.roundToInt
import ir.mahozad.multiplatform.wavyslider.material.WavySlider as WavySlider2
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider as WavySlider3

private val material2ColorPrimary = Color(0xff591b52)

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
    MaterialTheme(colorScheme = lightScheme) {
        Content()
    }
}

@Composable
fun Content() {
    var value by remember { mutableFloatStateOf(0.5f) }
    var waveLength by remember { mutableStateOf(20.dp) }
    var waveHeight by remember { mutableStateOf(7.dp) }
    var waveSpeed by remember { mutableStateOf(12.dp) }
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
                waveVelocity = waveSpeed to if (isBackward) TAIL else HEAD,
                waveThickness = waveThickness,
                trackThickness = trackThickness3,
                incremental = isIncremental
            )
        } else {
            androidx.compose.material.MaterialTheme(lightColors(primary = material2ColorPrimary)) {
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .width(360.dp)
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
                    Toggle(isIncremental, "Incremental wave height:") { isIncremental = it }
                    Toggle(!isBackward, "Reverse animation:") { isBackward = !it }
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
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .width(360.dp)
                    .fillMaxHeight()
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
                painterResource(if (n == 2) Res.drawable.m2_logo else Res.drawable.m3_logo),
                contentDescription = "Material $n",
                modifier = Modifier.size(34.dp),
                tint = if (n == 3 && isMaterial3) {
                    MaterialTheme.colorScheme.primary
                } else if (n == 3 && !isMaterial3) {
                    LocalContentColor.current
                } else if (n == 2 && isMaterial3) {
                    LocalContentColor.current
                } else {
                    material2ColorPrimary
                }
            )
            Spacer(Modifier.width(8.dp))
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
        Text(label, modifier = Modifier.width(164.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
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
    // https://stackoverflow.com/q/42791492
    val valueRounded = remember(value) { value.asDynamic().toFixed(2) }

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
        withStyle(SpanStyle(colorKeyword, fontSize)) { append("import ") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) {
            append("...wavyslider.${if (isMaterial3) "material3" else "material"}.WavySlider")
        }
        appendLine()
        withStyle(SpanStyle(colorKeyword, fontSize)) { append("import ") }
        withStyle(SpanStyle(colorIdentifier, fontSize)) {
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

@OptIn(InternalResourceApi::class)
suspend fun loadResource(path: String): ByteArray {
    return readResourceBytes(path)
}
