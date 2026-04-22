// ui/theme/Theme.kt
package com.example.weatherapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// CompositionLocal to propagate dark theme state
val LocalIsDarkTheme = staticCompositionLocalOf { true }

// ── Material 3 color schemes ──────────────────────────────────────────────────
private val DarkScheme = darkColorScheme(
    primary            = DarkCyan400,
    onPrimary          = DarkNavy950,
    secondary          = DarkAmber400,
    onSecondary        = DarkNavy950,
    background         = DarkNavy900,
    onBackground       = DarkWhite87,
    surface            = DarkNavy800,
    onSurface          = DarkWhite87,
    surfaceVariant     = DarkNavy700,
    onSurfaceVariant   = DarkWhite60,
    error              = ErrorRed,
    onError            = DarkWhite100
)

private val LightScheme = lightColorScheme(
    primary            = LightCyan400,
    onPrimary          = Color.White,
    secondary          = LightAmber400,
    onSecondary        = Color.White,
    background         = LightBg,
    onBackground       = LightOnSurface,
    surface            = LightSurface,
    onSurface          = LightOnSurface,
    surfaceVariant     = LightSurfaceVariant,
    onSurfaceVariant   = LightOnSurfaceVariant,
    error              = ErrorRed,
    onError            = Color.White
)

@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = WeatherTypography,
            content     = content
        )
    }
}

// ── AppColors: custom glass / accent colors that adapt to the active theme ────
object AppColors {
    val glass06: Color
        @Composable get() = if (LocalIsDarkTheme.current) DarkGlass06 else LightGlass06
    val glass12: Color
        @Composable get() = if (LocalIsDarkTheme.current) DarkGlass12 else LightGlass12
    val glass20: Color
        @Composable get() = if (LocalIsDarkTheme.current) DarkGlass20 else LightGlass20
    val glassBorder: Color
        @Composable get() = if (LocalIsDarkTheme.current) DarkGlassBorder else LightGlassBorder

    val cyan400: Color
        @Composable get() = if (LocalIsDarkTheme.current) DarkCyan400 else LightCyan400
    val cyan300: Color
        @Composable get() = if (LocalIsDarkTheme.current) DarkCyan300 else LightCyan300
    val amber400: Color
        @Composable get() = if (LocalIsDarkTheme.current) DarkAmber400 else LightAmber400
    val amber300: Color
        @Composable get() = if (LocalIsDarkTheme.current) DarkAmber300 else LightAmber300

    val backgroundGradient: Brush
        @Composable get() = if (LocalIsDarkTheme.current)
            Brush.verticalGradient(listOf(DarkGradientStart, DarkGradientMid, DarkGradientEnd))
        else
            Brush.verticalGradient(listOf(LightGradientStart, LightGradientMid, LightGradientEnd))
}