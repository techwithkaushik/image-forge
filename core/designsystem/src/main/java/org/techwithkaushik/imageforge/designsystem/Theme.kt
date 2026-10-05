package org.techwithkaushik.imageforge.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val ImageForgeLightColors = lightColorScheme(
    primary = Color(0xFF4F58A8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4E5FF),
    onPrimaryContainer = Color(0xFF11184A),
    secondary = Color(0xFF5A5D72),
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFF4F4FA),
)

private val ImageForgeDarkColors = darkColorScheme(
    primary = Color(0xFFB8C4FF),
    onPrimary = Color(0xFF002A73),
    primaryContainer = Color(0xFF173F9B),
    onPrimaryContainer = Color(0xFFDCE2FF),
    secondary = Color(0xFFC2C4DA),
    surface = Color(0xFF121318),
    surfaceContainer = Color(0xFF1E1F26),
)

private val ImageForgeTypography = androidx.compose.material3.Typography(
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

@Composable
public fun ImageForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ImageForgeDarkColors else ImageForgeLightColors,
        typography = ImageForgeTypography,
        content = content,
    )
}
