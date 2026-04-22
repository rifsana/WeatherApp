// ui/weather/HourlyForecast.kt
package com.example.weatherapp.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.weatherapp.data.ForecastItem
import com.example.weatherapp.data.ForecastResponse
import com.example.weatherapp.ui.theme.AppColors
import com.example.weatherapp.ui.utils.celsiusToF
import com.example.weatherapp.ui.utils.fmt0
import com.example.weatherapp.ui.utils.fmtHour
import com.example.weatherapp.viewmodel.AppSettings

@Composable
fun HourlyForecastRow(forecast: ForecastResponse, settings: AppSettings) {
    val hourly = forecast.list.take(8)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(hourly) { item -> HourlyChip(item, settings) }
    }
}

@Composable
fun HourlyChip(item: ForecastItem, settings: AppSettings) {
    val temp = if (settings.useCelsius) item.main.temp.fmt0() + "°"
    else celsiusToF(item.main.temp).fmt0() + "°"
    Column(
        Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.glass06)
            .border(1.dp, AppColors.glassBorder, RoundedCornerShape(18.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            fmtHour(item.dt, settings.use24Hour),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Light),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        AsyncImage(
            model = "https://openweathermap.org/img/wn/${item.weather.firstOrNull()?.icon ?: "01d"}@2x.png",
            contentDescription = null,
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            temp,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}