package ir.mahozad.multiplatform.wavyslider

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal actual val defaultTrackThickness: Dp = 4.dp

internal actual val defaultWaveSize: Dp = 16.dp

internal actual val KeyEvent.isDirectionUp: Boolean
    get() = key.nativeKeyCode == java.awt.event.KeyEvent.VK_UP

internal actual val KeyEvent.isDirectionDown: Boolean
    get() = key.nativeKeyCode == java.awt.event.KeyEvent.VK_DOWN

internal actual val KeyEvent.isDirectionRight: Boolean
    get() = key.nativeKeyCode == java.awt.event.KeyEvent.VK_RIGHT

internal actual val KeyEvent.isDirectionLeft: Boolean
    get() = key.nativeKeyCode == java.awt.event.KeyEvent.VK_LEFT

internal actual val KeyEvent.isHome: Boolean
    get() = key.nativeKeyCode == java.awt.event.KeyEvent.VK_HOME

internal actual val KeyEvent.isMoveEnd: Boolean
    get() = key.nativeKeyCode == java.awt.event.KeyEvent.VK_END

internal actual val KeyEvent.isPgUp: Boolean
    get() = key.nativeKeyCode == java.awt.event.KeyEvent.VK_PAGE_UP

internal actual val KeyEvent.isPgDn: Boolean
    get() = key.nativeKeyCode == java.awt.event.KeyEvent.VK_PAGE_DOWN
