package ir.mahozad.multiplatform.wavyslider

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.skiko.SkikoKey

internal actual val defaultTrackThickness: Dp = 8.dp

internal actual val defaultWaveSize: Dp = 24.dp

internal actual val KeyEvent.isDirectionUp: Boolean
    get() = key.keyCode == SkikoKey.KEY_UP.platformKeyCode.toLong()

internal actual val KeyEvent.isDirectionDown: Boolean
    get() = key.keyCode == SkikoKey.KEY_DOWN.platformKeyCode.toLong()

internal actual val KeyEvent.isDirectionRight: Boolean
    get() = key.keyCode == SkikoKey.KEY_RIGHT.platformKeyCode.toLong()

internal actual val KeyEvent.isDirectionLeft: Boolean
    get() = key.keyCode == SkikoKey.KEY_LEFT.platformKeyCode.toLong()

internal actual val KeyEvent.isHome: Boolean
    get() = key.keyCode == SkikoKey.KEY_HOME.platformKeyCode.toLong()

internal actual val KeyEvent.isMoveEnd: Boolean
    get() = key.keyCode == SkikoKey.KEY_END.platformKeyCode.toLong()

internal actual val KeyEvent.isPgUp: Boolean
    get() = key.keyCode == SkikoKey.KEY_PGUP.platformKeyCode.toLong()

internal actual val KeyEvent.isPgDn: Boolean
    get() = key.keyCode == SkikoKey.KEY_PGDOWN.platformKeyCode.toLong()
