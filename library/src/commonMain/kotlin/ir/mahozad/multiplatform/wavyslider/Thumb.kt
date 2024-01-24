package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class WaveThumb {
    @Composable
    internal abstract fun shape()

    data class Diamond(
        val width: Dp,
        val height: Dp = width
    ) : WaveThumb() {

        @Composable
        override fun shape() {
            Box(modifier = Modifier.width(width).height(height).background(Color.Red))
        }

        companion object : WaveThumb() {
            @Composable
            override fun shape() = Diamond(24.dp, 24.dp).shape()
        }
    }

    data class Custom(private val drawing: @Composable () -> Unit) : WaveThumb() {
        @Composable
        override fun shape() = drawing()
    }
}

@Composable
fun ExampleComponent(thumb: WaveThumb) {
    thumb.shape()
    when (thumb) {
        WaveThumb.Diamond -> TODO()
        is WaveThumb.Diamond -> TODO()
        is WaveThumb.Custom -> TODO()
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
