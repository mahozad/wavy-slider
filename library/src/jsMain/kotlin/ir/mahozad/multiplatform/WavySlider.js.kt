package ir.mahozad.multiplatform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.skiko.SkikoKey
import kotlin.math.abs

internal actual val defaultTrackThickness: Dp = 4.dp
internal actual val defaultWaveSize: Dp = 16.dp

@Composable
internal actual fun focusModifier(requester: (FocusRequester?) -> Unit): Modifier {
    val focusRequester = remember { FocusRequester() }
    requester(focusRequester)
    return Modifier.focusRequester(focusRequester)
}

@Composable
internal actual fun keyEventModifier(
    enabled: Boolean,
    value: Float,
    isRtl: Boolean,
    onValueChangeState: State<(Float) -> Unit>
): Modifier {
    return Modifier.slideOnKeyEvents(enabled, value, isRtl, onValueChangeState)
}

private fun Modifier.slideOnKeyEvents(
    enabled: Boolean,
    value: Float,
    isRtl: Boolean,
    onValueChangeState: State<(Float) -> Unit>
): Modifier {
    return this.onKeyEvent {
        if (it.type != KeyEventType.KeyDown || !enabled) return@onKeyEvent false
        val rangeLength = abs(1f - 0f)
        // When steps == 0, it means that a user is not limited by a step length (delta) when using touch or mouse.
        // But it is not possible to adjust the value continuously when using keyboard buttons -
        // the delta has to be discrete. In this case, 1% of the valueRange seems to make sense.
        val actualSteps = 100
        val delta = rangeLength / actualSteps
        when {
            it.isDirectionUp -> {
                onValueChangeState.value((value + delta).coerceIn(0f..1f))
                true
            }

            it.isDirectionDown -> {
                onValueChangeState.value((value - delta).coerceIn(0f..1f))
                true
            }

            it.isDirectionRight -> {
                val sign = if (isRtl) -1 else 1
                onValueChangeState.value((value + sign * delta).coerceIn(0f..1f))
                true
            }

            it.isDirectionLeft -> {
                val sign = if (isRtl) -1 else 1
                onValueChangeState.value((value - sign * delta).coerceIn(0f..1f))
                true
            }

            it.isHome -> {
                onValueChangeState.value(0f)
                true
            }

            it.isMoveEnd -> {
                onValueChangeState.value(1f)
                true
            }

            it.isPgUp -> {
                val page = (actualSteps / 10).coerceIn(1, 10)
                onValueChangeState.value((value - page * delta).coerceIn(0f..1f))
                true
            }

            it.isPgDn -> {
                val page = (actualSteps / 10).coerceIn(1, 10)
                onValueChangeState.value((value + page * delta).coerceIn(0f..1f))
                true
            }

            else -> false
        }
    }
}

private val KeyEvent.isDirectionUp: Boolean
    get() = key.keyCode == SkikoKey.KEY_UP.platformKeyCode.toLong()

private val KeyEvent.isDirectionDown: Boolean
    get() = key.keyCode == SkikoKey.KEY_DOWN.platformKeyCode.toLong()

private val KeyEvent.isDirectionRight: Boolean
    get() = key.keyCode == SkikoKey.KEY_RIGHT.platformKeyCode.toLong()

private val KeyEvent.isDirectionLeft: Boolean
    get() = key.keyCode == SkikoKey.KEY_LEFT.platformKeyCode.toLong()

private val KeyEvent.isHome: Boolean
    get() = key.keyCode == SkikoKey.KEY_HOME.platformKeyCode.toLong()

private val KeyEvent.isMoveEnd: Boolean
    get() = key.keyCode == SkikoKey.KEY_END.platformKeyCode.toLong()

private val KeyEvent.isPgUp: Boolean
    get() = key.keyCode == SkikoKey.KEY_PGUP.platformKeyCode.toLong()

private val KeyEvent.isPgDn: Boolean
    get() = key.keyCode == SkikoKey.KEY_PGDOWN.platformKeyCode.toLong()
