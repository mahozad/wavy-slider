package ir.mahozad.multiplatform.wavyslider

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key

// Copied from https://github.com/JetBrains/compose-multiplatform-core/blob/jb-main/compose/material/material/src/jsNativeMain/kotlin/androidx/compose/material/NavigationKeyEvents.jsNative.kt

internal actual val KeyEvent.isDirectionUp: Boolean
    get() = key == Key.DirectionUp

internal actual val KeyEvent.isDirectionDown: Boolean
    get() = key == Key.DirectionDown

internal actual val KeyEvent.isDirectionRight: Boolean
    get() = key == Key.DirectionRight

internal actual val KeyEvent.isDirectionLeft: Boolean
    get() = key == Key.DirectionLeft

internal actual val KeyEvent.isHome: Boolean
    get() = key == Key.Home

internal actual val KeyEvent.isMoveEnd: Boolean
    get() = key == Key.MoveEnd

internal actual val KeyEvent.isPgUp: Boolean
    get() = key == Key.PageUp

internal actual val KeyEvent.isPgDn: Boolean
    get() = key == Key.PageDown
