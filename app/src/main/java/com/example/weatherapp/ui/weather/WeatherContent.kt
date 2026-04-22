// ui/weather/WeatherContent.kt
package com.example.weatherapp.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.data.ForecastResponse
import com.example.weatherapp.data.WeatherResponse
import com.example.weatherapp.ui.theme.AppColors
import com.example.weatherapp.ui.theme.LocalIsDarkTheme
import com.example.weatherapp.ui.utils.celsiusToF
import com.example.weatherapp.ui.utils.condBg
import com.example.weatherapp.ui.utils.fmt0
import com.example.weatherapp.ui.utils.fmt1
import com.example.weatherapp.ui.utils.fmtDateTime
import com.example.weatherapp.ui.utils.fmtTime
import com.example.weatherapp.ui.utils.titleCase
import com.example.weatherapp.ui.utils.visStr
import com.example.weatherapp.ui.utils.windDir
import com.example.weatherapp.viewmodel.AppSettings

@Composable
fun WeatherContent(
    weather: WeatherResponse,
    forecast: ForecastResponse,
    settings: AppSettings
) {
    val condition = weather.weather.firstOrNull()
    val condId    = condition?.id ?: 800
    val isDark    = LocalIsDarkTheme.current

    Box(Modifier.fillMaxSize()) {
        // Condition-aware background gradient that respects the current theme
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(condBg(condId, isDark)))
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // Header: city name + date-time
            Row(Modifier.fillMaxWidth(), Arrangement.Start, Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.LocationOn, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(5.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${weather.name}, ${weather.sys.country}",
                        style    = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color    = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        fmtDateTime(weather.dt, settings.use24Hour),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Light),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Weather icon
            condition?.let {
                AsyncImage(
                    model       = "https://openweathermap.org/img/wn/${it.icon}@4x.png",
                    contentDescription = it.description,
                    modifier    = Modifier.size(120.dp)
                )
            }

            // Temperature
            val tempStr = if (settings.useCelsius)
                "${weather.main.temp.fmt0()}°C"
            else
                "${celsiusToF(weather.main.temp).fmt0()}°F"

            Text(
                tempStr,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize      = 88.sp,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = (-3).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            // Condition description
            condition?.let {
                Text(
                    it.description.titleCase(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(10.dp))

            // H / L pill
            val unit = if (settings.useCelsius) "C" else "F"
            val hi   = if (settings.useCelsius) weather.main.temp_max else celsiusToF(weather.main.temp_max)
            val lo   = if (settings.useCelsius) weather.main.temp_min else celsiusToF(weather.main.temp_min)

            Row(
                Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(AppColors.glass12)
                    .border(1.dp, AppColors.glassBorder, RoundedCornerShape(50.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    "↑ ${hi.fmt0()}°$unit",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Light),
                    color = AppColors.amber300
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(14.dp)
                        .background(AppColors.glassBorder)
                )
                Text(
                    "↓ ${lo.fmt0()}°$unit",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Light),
                    color = AppColors.cyan300
                )
            }

            Spacer(Modifier.height(28.dp))

            // Stat chips – row 1
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                StatChip(Modifier.weight(1f), "💧", "${weather.main.humidity}%", "Humidity")
                StatChip(Modifier.weight(1f), "💨", "${weather.wind.speed.fmt1()} m/s", "Wind")
                StatChip(
                    Modifier.weight(1f), "🌡",
                    if (settings.useCelsius) weather.main.feels_like.fmt0() + "°C"
                    else celsiusToF(weather.main.feels_like).fmt0() + "°F",
                    "Feels Like"
                )
            }

            Spacer(Modifier.height(10.dp))

            // Stat chips – row 2
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                StatChip(Modifier.weight(1f), "🔵", "${weather.main.pressure} hPa", "Pressure")
                StatChip(Modifier.weight(1f), "👁",  visStr(weather.visibility),     "Visibility")
                StatChip(Modifier.weight(1f), "🧭", windDir(weather.wind.deg),       "Wind Dir")
            }

            Spacer(Modifier.height(10.dp))

            // Sunrise / Sunset
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                SunChip(Modifier.weight(1f), "🌅", "Sunrise", fmtTime(weather.sys.sunrise, settings.use24Hour))
                SunChip(Modifier.weight(1f), "🌇", "Sunset",  fmtTime(weather.sys.sunset,  settings.use24Hour))
            }

            Spacer(Modifier.height(24.dp))

            SectionHeader("Hourly Forecast", "Next 24 hours")
            Spacer(Modifier.height(10.dp))
            HourlyForecastRow(forecast, settings)

            Spacer(Modifier.height(20.dp))

            SectionHeader("5-Day Forecast", "Daily overview")
            Spacer(Modifier.height(10.dp))
            DailyForecastCard(forecast, settings)

            Spacer(Modifier.height(16.dp))

            Text(
                "Updated ${fmtDateTime(weather.dt, settings.use24Hour)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Light),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            Modifier
                .width(36.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.primary, Color.Transparent)
                    )
                )
        )
    }
}