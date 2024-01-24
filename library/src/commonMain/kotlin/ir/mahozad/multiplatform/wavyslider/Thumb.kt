package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderPositions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider

class WavySliderThumb {
    companion object {
        val Diamond: @Composable (SliderPositions) -> Unit = Diamond(24.dp)
        fun Diamond(width: Dp, height: Dp = width, roundness: Dp = 0.dp): @Composable (SliderPositions) -> Unit {
            val f: @Composable (SliderPositions) -> Unit = {
                Box(modifier = Modifier.width(width).height(height).background(Color.Red))
            }
            return f
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Examples() {
    WavySlider(0.5f, {}, thumb = WavySliderThumb.Diamond)
    WavySlider(0.5f, {}, thumb = WavySliderThumb.Diamond(16.dp))
    WavySlider(0.5f, {}, thumb = WavySliderThumb.Diamond(8.dp, 24.dp))
    // Nothing
    WavySlider(0.5f, {}, thumb = {})
    // Custom
    WavySlider(0.5f, {}, thumb = { Text("XYX") })
    // Default
    WavySlider(0.5f, {}, thumb = { SliderDefaults.Thumb(remember { MutableInteractionSource() }, enabled = false) })
}
