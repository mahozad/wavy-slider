package ir.mahozad.multiplatform.wavyslider

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.dp

/**
 * The direction of wave movement.
 *
 * By default, and also when set to [WaveAnimationDirection.UNSPECIFIED],
 * it moves from right to left on LTR layouts and from left to right on RTL layouts.
 */
enum class WaveAnimationDirection {
    /**
     * Always move from right to left regardless of the layout direction.
     */
    RTL,
    /**
     * Always move from left to right regardless of the layout direction.
     */
    LTR,
    /**
     * Based on layout direction; on LTR move from right to left and on RTL move from left to right.
     */
    UNSPECIFIED
}

internal val defaultTrackThickness = 4.dp

internal val defaultWaveSize = 16.dp

internal expect val KeyEvent.isDirectionUp: Boolean

internal expect val KeyEvent.isDirectionDown: Boolean

internal expect val KeyEvent.isDirectionRight: Boolean

internal expect val KeyEvent.isDirectionLeft: Boolean

internal expect val KeyEvent.isHome: Boolean

internal expect val KeyEvent.isMoveEnd: Boolean

internal expect val KeyEvent.isPgUp: Boolean

internal expect val KeyEvent.isPgDn: Boolean
