package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class WavySliderThumb {
    companion object {
        @Composable fun Custom(content: @Composable () -> Unit) = content
        val Diamond: @Composable () -> Unit = Diamond(24.dp)
        fun Diamond(width: Dp, height: Dp = width): @Composable () -> Unit {
            val f: @Composable () -> Unit = {
                Box(modifier = Modifier.width(width).height(height).background(Color.Red))
            }
            return f
        }
    }
}

@Composable
fun ExampleComponent(thumb: @Composable () -> Unit) {
    thumb()
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
