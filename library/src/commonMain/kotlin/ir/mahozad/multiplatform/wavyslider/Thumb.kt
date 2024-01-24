package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class WaveThumb {

    internal abstract val drawing: @Composable () -> Unit

    open class Diamond(
        val width: Dp,
        val height: Dp = width
    ) : WaveThumb() {
        companion object : Diamond(24.dp, 24.dp)
        override val drawing = @Composable {
            Box(modifier = Modifier.width(width).height(height).background(Color.Red))
        }
    }

    data class Custom(override val drawing: @Composable () -> Unit) : WaveThumb()
}

@Composable
fun ExampleComponent(thumb: WaveThumb) {
    thumb.drawing()
    when (thumb) {
        is WaveThumb.Diamond -> println("Diamond")
        is WaveThumb.Custom -> println("Custom")
    }
}

@Composable
fun ExampleCaller() {
    Column {
        ExampleComponent(thumb = WaveThumb.Diamond)
        ExampleComponent(thumb = WaveThumb.Diamond(10.dp))
        ExampleComponent(thumb = WaveThumb.Custom {
            Box(modifier = Modifier.size(14.dp).background(Color.Green))
        })
    }
}
