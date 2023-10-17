package ir.mahozad.multiplatform.material

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal actual val defaultTrackThickness: Dp = 8.dp
internal actual val defaultWaveSize: Dp = 24.dp

@Composable
internal actual fun focusModifier(requester: (FocusRequester?) -> Unit): Modifier {
    requester(null)
    return Modifier
}

@Composable
internal actual fun keyEventModifier(
    enabled: Boolean,
    value: Float,
    isRtl: Boolean,
    onValueChangeState: State<(Float) -> Unit>
): Modifier = Modifier
