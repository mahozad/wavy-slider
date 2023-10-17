package ir.mahozad.multiplatform.material

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.skiko.SkikoKey

internal actual val defaultTrackThickness: Dp = 4.dp
internal actual val defaultWaveSize: Dp = 16.dp

private val DIRECTION_UP_KEY_CODE = SkikoKey.KEY_UP.platformKeyCode.toLong()
private val DIRECTION_DOWN_KEY_CODE = SkikoKey.KEY_DOWN.platformKeyCode.toLong()
private val DIRECTION_LEFT_KEY_CODE = SkikoKey.KEY_LEFT.platformKeyCode.toLong()
private val DIRECTION_RIGHT_KEY_CODE = SkikoKey.KEY_RIGHT.platformKeyCode.toLong()
private val HOME_KEY_CODE = SkikoKey.KEY_HOME.platformKeyCode.toLong()
private val END_KEY_CODE = SkikoKey.KEY_END.platformKeyCode.toLong()
private val PG_UP_KEY_CODE = SkikoKey.KEY_PGUP.platformKeyCode.toLong()
private val PG_DN_KEY_CODE = SkikoKey.KEY_PGDOWN.platformKeyCode.toLong()

internal actual val KeyEvent.isDirectionUp: Boolean
    get() = key.keyCode == DIRECTION_UP_KEY_CODE

internal actual val KeyEvent.isDirectionDown: Boolean
    get() = key.keyCode == DIRECTION_DOWN_KEY_CODE

internal actual val KeyEvent.isDirectionRight: Boolean
    get() = key.keyCode == DIRECTION_RIGHT_KEY_CODE

internal actual val KeyEvent.isDirectionLeft: Boolean
    get() = key.keyCode == DIRECTION_LEFT_KEY_CODE

internal actual val KeyEvent.isHome: Boolean
    get() = key.keyCode == HOME_KEY_CODE

internal actual val KeyEvent.isMoveEnd: Boolean
    get() = key.keyCode == END_KEY_CODE

internal actual val KeyEvent.isPgUp: Boolean
    get() = key.keyCode == PG_UP_KEY_CODE

internal actual val KeyEvent.isPgDn: Boolean
    get() = key.keyCode == PG_DN_KEY_CODE
