package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class WavySliderThumb {

    internal abstract val drawing: @Composable () -> Unit

    open class Diamond(
        val width: Dp,
        val height: Dp = width,
        val roundness: Dp = 0.dp
    ) : WavySliderThumb() {
        override fun toString() = "WavySliderThumb.Diamond"
        companion object : Diamond(width = 24.dp, height = 24.dp, roundness = 2.dp)
        override val drawing = @Composable {
            Box(modifier = Modifier.width(width).height(height).background(Color.Red))
        }
    }

    open class Pill(
        val width: Dp,
        val height: Dp = width * 4,
        val roundness: Dp = 0.dp
    ) : WavySliderThumb() {
        override fun toString() = "WavySliderThumb.Pill"
        companion object : Pill(width = 4.dp, height = 20.dp, roundness = 8.dp)
        override val drawing = @Composable {
            Box(modifier = Modifier.width(width).height(height).background(Color.Red))
        }
    }

    data class Custom(override val drawing: @Composable () -> Unit) : WavySliderThumb()
}

@Composable
fun ExampleComponent(thumb: WavySliderThumb) {
    thumb.drawing()
    when (thumb) {
        is WavySliderThumb.Pill -> println("Pill")
        is WavySliderThumb.Diamond -> println("Diamond")
        is WavySliderThumb.Custom -> println("Custom")
    }
}

@Composable
fun ExampleCaller() {
    Column {
        ExampleComponent(thumb = WavySliderThumb.Diamond)
        ExampleComponent(thumb = WavySliderThumb.Diamond(10.dp))
        ExampleComponent(thumb = WavySliderThumb.Custom {
            Box(modifier = Modifier.size(14.dp).background(Color.Green))
        })
    }
}
