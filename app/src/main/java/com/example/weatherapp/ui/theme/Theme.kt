package com.example.weatherapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Scheme = darkColorScheme(
    primary            = Cyan400,
    onPrimary          = Navy950,
    secondary          = Amber400,
    onSecondary        = Navy950,
    background         = Navy900,
    onBackground       = White87,
    surface            = Navy800,
    onSurface          = White87,
    surfaceVariant     = Navy700,
    onSurfaceVariant   = White60,
    error              = ErrorRed,
    onError            = White100
)

@Composable
fun WeatherAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme, typography = WeatherTypography, content = content)
}