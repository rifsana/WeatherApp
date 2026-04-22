// ui/weather/DailyForecast.kt
package com.example.weatherapp.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.weatherapp.data.ForecastItem
import com.example.weatherapp.data.ForecastResponse
import com.example.weatherapp.ui.theme.AppColors
import com.example.weatherapp.ui.utils.celsiusToF
import com.example.weatherapp.ui.utils.fmt0
import com.example.weatherapp.ui.utils.fmtDay
import com.example.weatherapp.viewmodel.AppSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DailyForecastCard(forecast: ForecastResponse, settings: AppSettings) {
    val daily = forecast.list
        .groupBy { item ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(item.dt * 1000L))
        }
        .values
        .take(5)
        .map { dayItems ->
            dayItems.minByOrNull { item ->
                val h = Calendar.getInstance().apply { time = Date(item.dt * 1000L) }.get(Calendar.HOUR_OF_DAY)
                Math.abs(h - 12)
            } ?: dayItems.first()
        }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.glass06)
            .border(1.dp, AppColors.glassBorder, RoundedCornerShape(24.dp))
    ) {
        daily.forEachIndexed { idx, item ->
            DailyRow(item, settings, isLast = idx == daily.lastIndex)
        }
    }
}

@Composable
fun DailyRow(item: ForecastItem, settings: AppSettings, isLast: Boolean) {
    val hi = if (settings.useCelsius) item.main.temp_max.fmt0() + "°" else celsiusToF(item.main.temp_max).fmt0() + "°"
    val lo = if (settings.useCelsius) item.main.temp_min.fmt0() + "°" else celsiusToF(item.main.temp_min).fmt0() + "°"

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 16.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Text(
            fmtDay(item.dt),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(52.dp)
        )
        AsyncImage(
            model = "https://openweathermap.org/img/wn/${item.weather.firstOrNull()?.icon ?: "01d"}@2x.png",
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Text(
            item.weather.firstOrNull()?.main ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Light),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(hi, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = AppColors.amber300)
            Text(lo, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Light),    color = AppColors.cyan300)
        }
    }
    if (!isLast) {
        Box(Modifier.fillMaxWidth().padding(start = 68.dp).height(1.dp).background(AppColors.glassBorder))
    }
}