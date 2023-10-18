package ir.mahozad.multiplatform.wavyslider

import android.view.KeyEvent.*
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal actual val defaultTrackThickness: Dp = 8.dp

internal actual val defaultWaveSize: Dp = 24.dp

internal actual val KeyEvent.isDirectionUp: Boolean
    get() = key.nativeKeyCode == KEYCODE_DPAD_UP

internal actual val KeyEvent.isDirectionDown: Boolean
    get() = key.nativeKeyCode == KEYCODE_DPAD_DOWN

internal actual val KeyEvent.isDirectionRight: Boolean
    get() = key.nativeKeyCode == KEYCODE_DPAD_RIGHT

internal actual val KeyEvent.isDirectionLeft: Boolean
    get() = key.nativeKeyCode == KEYCODE_DPAD_LEFT

internal actual val KeyEvent.isHome: Boolean
    get() = key.nativeKeyCode == KEYCODE_MOVE_HOME

internal actual val KeyEvent.isMoveEnd: Boolean
    get() = key.nativeKeyCode == KEYCODE_MOVE_END

internal actual val KeyEvent.isPgUp: Boolean
    get() = key.nativeKeyCode == KEYCODE_PAGE_UP

internal actual val KeyEvent.isPgDn: Boolean
    get() = key.nativeKeyCode == KEYCODE_PAGE_DOWN
