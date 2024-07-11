package ir.mahozad.multiplatform.wavyslider

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key

internal actual val KeyEvent.isDirectionUp: Boolean
    get() = key.keyCode == Key.DirectionUp.keyCode

internal actual val KeyEvent.isDirectionDown: Boolean
    get() = key.keyCode == Key.DirectionDown.keyCode

internal actual val KeyEvent.isDirectionRight: Boolean
    get() = key.keyCode == Key.DirectionRight.keyCode

internal actual val KeyEvent.isDirectionLeft: Boolean
    get() = key.keyCode == Key.DirectionLeft.keyCode

internal actual val KeyEvent.isHome: Boolean
    get() = key.keyCode == Key.Home.keyCode

internal actual val KeyEvent.isMoveEnd: Boolean
    get() = key.keyCode == Key.MoveEnd.keyCode

internal actual val KeyEvent.isPgUp: Boolean
    get() = key.keyCode == Key.PageUp.keyCode

internal actual val KeyEvent.isPgDn: Boolean
    get() = key.keyCode == Key.PageDown.keyCode
