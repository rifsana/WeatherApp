// ui/utils/WeatherUtils.kt
package com.example.weatherapp.ui.utils

import androidx.compose.ui.graphics.Color

fun visStr(m: Int) = if (m >= 1000) "${"%.0f".format(m / 1000.0)} km" else "$m m"

fun windDir(deg: Int): String {
    val d = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return d[((deg + 22.5) / 45).toInt() % 8]
}

/**
 * Returns a background gradient appropriate for the given condition id.
 * Pass [isDark] = true for the dark theme (deep navy tones),
 *                 false for the light theme (soft pastel tones).
 */
fun condBg(id: Int, isDark: Boolean = true): List<Color> {
    return if (isDark) {
        when {
            id in 200..299 -> listOf(Color(0xFF06060E), Color(0xFF09091C), Color(0xFF0E0E2E)) // Thunderstorm
            id in 300..599 -> listOf(Color(0xFF060C14), Color(0xFF091422), Color(0xFF0D1F35)) // Drizzle / Rain
            id in 600..699 -> listOf(Color(0xFF080E18), Color(0xFF0C1628), Color(0xFF12223C)) // Snow
            id in 700..799 -> listOf(Color(0xFF0A0A0E), Color(0xFF111116), Color(0xFF18181F)) // Atmosphere
            id == 800      -> listOf(Color(0xFF020508), Color(0xFF040B14), Color(0xFF071526)) // Clear
            id in 801..802 -> listOf(Color(0xFF050A14), Color(0xFF081222), Color(0xFF0D1C34)) // Few / Scattered clouds
            else           -> listOf(Color(0xFF060A12), Color(0xFF0A111E), Color(0xFF0F1A2E)) // Broken / Overcast
        }
    } else {
        when {
            id in 200..299 -> listOf(Color(0xFFD6D8E7), Color(0xFFDEE0EF), Color(0xFFE8EAF6)) // Thunderstorm – blue-grey
            id in 300..599 -> listOf(Color(0xFFCFDEF3), Color(0xFFD8E8F5), Color(0xFFE4EFF8)) // Drizzle / Rain – cool blue
            id in 600..699 -> listOf(Color(0xFFE8EEF5), Color(0xFFEEF3F8), Color(0xFFF3F7FB)) // Snow – icy white
            id in 700..799 -> listOf(Color(0xFFE0E0E8), Color(0xFFE8E8EF), Color(0xFFEFEFF5)) // Atmosphere – grey
            id == 800      -> listOf(Color(0xFFD6ECF8), Color(0xFFDEF1FA), Color(0xFFEAF5FB)) // Clear – sky blue
            id in 801..802 -> listOf(Color(0xFFDAE8F2), Color(0xFFE2EEF5), Color(0xFFEBF3F8)) // Few clouds
            else           -> listOf(Color(0xFFD9E3EE), Color(0xFFE1EAF3), Color(0xFFEAF0F7)) // Overcast
        }
    }
}