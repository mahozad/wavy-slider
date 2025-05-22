package website

/**
 * Generated from the primary/base color #727d1a
 * with https://material-foundation.github.io/material-theme-builder/
 */

import androidx.compose.ui.graphics.Color

val primaryLight = Color(0xFF5B631E)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFE0E995)
val onPrimaryContainerLight = Color(0xFF1A1E00)
val secondaryLight = Color(0xFF5E6144)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFE3E5C1)
val onSecondaryContainerLight = Color(0xFF1B1D07)
val tertiaryLight = Color(0xFF3B665B)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFBEECDD)
val onTertiaryContainerLight = Color(0xFF002019)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF410002)
val backgroundLight = Color(0xFFFCFAED)
val onBackgroundLight = Color(0xFF1B1C14)
val surfaceLight = Color(0xFFFCFAED)
val onSurfaceLight = Color(0xFF1B1C14)
val surfaceVariantLight = Color(0xFFE4E3D2)
val onSurfaceVariantLight = Color(0xFF47483B)
val outlineLight = Color(0xFF78786A)
val outlineVariantLight = Color(0xFFC8C7B7)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF313128)
val inverseOnSurfaceLight = Color(0xFFF3F1E4)
val inversePrimaryLight = Color(0xFFC3CD7B)

val primaryDark = Color(0xFFC3CD7B)
val onPrimaryDark = Color(0xFF2E3400)
val primaryContainerDark = Color(0xFF434B05)
val onPrimaryContainerDark = Color(0xFFE0E995)
val secondaryDark = Color(0xFFC7C9A7)
val onSecondaryDark = Color(0xFF2F321A)
val secondaryContainerDark = Color(0xFF46492F)
val onSecondaryContainerDark = Color(0xFFE3E5C1)
val tertiaryDark = Color(0xFFA2D0C1)
val onTertiaryDark = Color(0xFF06372D)
val tertiaryContainerDark = Color(0xFF224E43)
val onTertiaryContainerDark = Color(0xFFBEECDD)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF13140D)
val onBackgroundDark = Color(0xFFE5E3D6)
val surfaceDark = Color(0xFF13140D)
val onSurfaceDark = Color(0xFFE5E3D6)
val surfaceVariantDark = Color(0xFF47483B)
val onSurfaceVariantDark = Color(0xFFC8C7B7)
val outlineDark = Color(0xFF929283)
val outlineVariantDark = Color(0xFF47483B)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE5E3D6)
val inverseOnSurfaceDark = Color(0xFF313128)
val inversePrimaryDark = Color(0xFF5B631E)

data class CodeColors(
    val keyword: Color,
    val number: Color,
    val member: Color,
    val function: Color,
    val argument: Color,
    val identifier: Color,
    val semantic1: Color,
    val semantic2: Color
)

val codeColorsLight: (colorOnSurface: Color) -> CodeColors = {
    CodeColors(
        keyword = Color(0xFF_0033b3),
        number = Color(0xFF_1750eb),
        member = Color(0xFF_871094),
        function = Color(0xFF_00627a),
        argument = Color(0xFF_4a86e8),
        semantic1 = Color(0xFF_9b3b6a),
        semantic2 = Color(0xFF_005910),
        identifier = it
    )
}

// Adopted from IntelliJ IDEA 2025.1 Dark theme default Kotlin color schemes
val codeColorsDark: (colorOnSurface: Color) -> CodeColors = {
    CodeColors(
        keyword = Color(0xFF_cf8e6d),
        number = Color(0xFF_2aacb8),
        member = Color(0xFF_c77dbb),
        function = Color(0xFF_57aaf7),
        argument = Color(0xFF_56c1d6),
        semantic1 = Color(0xFF_529d52),
        semantic2 = Color(0xFF_be7070),
        identifier = it
    )
}
