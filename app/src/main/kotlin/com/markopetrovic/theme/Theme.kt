package com.markopetrovic.leaflog.views.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GreenPrimary = Color(0xFF1B5E20)
val GreenLight = Color(0xFF4C8C4A)
val GreenDark = Color(0xFF003300)
val greyLight = Color(0xFFB0BEC5)
val greyDark = Color(0xFF455A64)

val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenLight,
    tertiary = greyLight,
    background = Color.White,
    surface = Color.White,
)

val DarkColorScheme = darkColorScheme(
    primary = GreenLight,
    secondary = GreenPrimary,
    tertiary = greyDark,
    background = Color(0xFF121212),
    surface = Color(0xFF1D1D1D),
)

@Composable
fun LeafLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
