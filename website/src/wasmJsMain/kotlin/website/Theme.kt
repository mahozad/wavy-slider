package website

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val animationSpec = tween<Color>(300, easing = Ease)

@Composable
fun animateColorScheme(scheme: ColorScheme) = ColorScheme(
    primary = animateColorAsState(scheme.primary, animationSpec).value,
    onPrimary = animateColorAsState(scheme.onPrimary, animationSpec).value,
    primaryContainer = animateColorAsState(scheme.primaryContainer, animationSpec).value,
    onPrimaryContainer = animateColorAsState(scheme.onPrimaryContainer, animationSpec).value,
    inversePrimary = animateColorAsState(scheme.inversePrimary, animationSpec).value,
    secondary = animateColorAsState(scheme.secondary, animationSpec).value,
    onSecondary = animateColorAsState(scheme.onSecondary, animationSpec).value,
    secondaryContainer = animateColorAsState(scheme.secondaryContainer, animationSpec).value,
    onSecondaryContainer = animateColorAsState(scheme.onSecondaryContainer, animationSpec).value,
    tertiary = animateColorAsState(scheme.tertiary, animationSpec).value,
    onTertiary = animateColorAsState(scheme.onTertiary, animationSpec).value,
    tertiaryContainer = animateColorAsState(scheme.tertiaryContainer, animationSpec).value,
    onTertiaryContainer = animateColorAsState(scheme.onTertiaryContainer, animationSpec).value,
    background = animateColorAsState(scheme.background, animationSpec).value,
    onBackground = animateColorAsState(scheme.onBackground, animationSpec).value,
    surface = animateColorAsState(scheme.surface, animationSpec).value,
    onSurface = animateColorAsState(scheme.onSurface, animationSpec).value,
    surfaceVariant = animateColorAsState(scheme.surfaceVariant, animationSpec).value,
    onSurfaceVariant = animateColorAsState(scheme.onSurfaceVariant, animationSpec).value,
    surfaceTint = animateColorAsState(scheme.surfaceTint, animationSpec).value,
    inverseSurface = animateColorAsState(scheme.inverseSurface, animationSpec).value,
    inverseOnSurface = animateColorAsState(scheme.inverseOnSurface, animationSpec).value,
    error = animateColorAsState(scheme.error, animationSpec).value,
    onError = animateColorAsState(scheme.onError, animationSpec).value,
    errorContainer = animateColorAsState(scheme.errorContainer, animationSpec).value,
    onErrorContainer = animateColorAsState(scheme.onErrorContainer, animationSpec).value,
    outline = animateColorAsState(scheme.outline, animationSpec).value,
    outlineVariant = animateColorAsState(scheme.outlineVariant, animationSpec).value,
    scrim = animateColorAsState(scheme.scrim, animationSpec).value
)

val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = /* surfaceLight */ Color.White,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
)

val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
)
