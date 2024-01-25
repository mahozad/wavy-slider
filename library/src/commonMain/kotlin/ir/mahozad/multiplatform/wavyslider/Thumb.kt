package ir.mahozad.multiplatform.wavyslider

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderPositions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.material3.SliderTokens
import ir.mahozad.multiplatform.wavyslider.material3.ThumbDefaultElevation
import ir.mahozad.multiplatform.wavyslider.material3.ThumbPressedElevation

class WavySliderThumb {
    companion object {
        val Diamond: @Composable (SliderPositions) -> Unit = { Diamond(20.dp)(it) }
        @Composable fun Diamond(
            width: Dp,
            height: Dp = width,
            roundness: Dp = 0.dp,
            interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
            modifier: Modifier = Modifier,
            colors: SliderColors = SliderDefaults.colors(),
            enabled: Boolean = true
        ): @Composable (SliderPositions) -> Unit {
            return {
                val interactions = remember { mutableStateListOf<Interaction>() }
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        when (interaction) {
                            is PressInteraction.Press -> interactions.add(interaction)
                            is PressInteraction.Release -> interactions.remove(interaction.press)
                            is PressInteraction.Cancel -> interactions.remove(interaction.press)
                            is DragInteraction.Start -> interactions.add(interaction)
                            is DragInteraction.Stop -> interactions.remove(interaction.start)
                            is DragInteraction.Cancel -> interactions.remove(interaction.start)
                        }
                    }
                }
                val elevation = if (interactions.isNotEmpty()) {
                    ThumbPressedElevation
                } else {
                    ThumbDefaultElevation
                }
                val shape = RoundedCornerShape(roundness)
                Spacer(
                    modifier
                        .width(width)
                        .height(height)
                        .rotate(45f)
                        .indication(
                            interactionSource = interactionSource,
                            indication = rememberRipple(
                                bounded = false,
                                radius = SliderTokens.StateLayerSize / 2
                            )
                        )
                        .hoverable(interactionSource = interactionSource)
                        .shadow(if (enabled) elevation else 0.dp, shape, clip = false)
                        .background(@Suppress("INVISIBLE_MEMBER") colors.thumbColor(enabled).value, shape)
                )
            }
            // return content
        }
    }
}
