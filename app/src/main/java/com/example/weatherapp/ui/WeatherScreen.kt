package com.example.weatherapp.ui

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.weatherapp.data.ForecastItem
import com.example.weatherapp.data.ForecastResponse
import com.example.weatherapp.data.RetrofitInstance
import com.example.weatherapp.data.WeatherResponse
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.ui.theme.*
import com.example.weatherapp.viewmodel.AppSettings
import com.example.weatherapp.viewmodel.WeatherUiState
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.example.weatherapp.viewmodel.WeatherViewModelFactory
import com.google.accompanist.permissions.*
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.math.roundToInt


private const val API_KEY = "2d264bcdc410fe844deaeb57155a7b35"


@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherScreen() {
    val repository = remember {
        WeatherRepository(api = RetrofitInstance.api, apiKey = API_KEY)
    }
    val vm: WeatherViewModel = viewModel(factory = WeatherViewModelFactory(repository))
    val state    by vm.uiState.collectAsState()
    val settings by vm.settings.collectAsState()

    val permState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Bottom nav tab state
    var selectedTab by remember { mutableStateOf(0) } // 0=Weather, 1=Settings

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Navy950, Navy900, Navy800)))
    ) {
        // Ambient glow orbs
        AmbientOrbs()

        when {
            permState.allPermissionsGranted -> {
                LocationFetcher(vm)
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = {
                        AppBottomBar(selectedTab) { selectedTab = it }
                    }
                ) { paddingValues ->
                    Box(Modifier.padding(paddingValues)) {
                        AnimatedContent(
                            targetState   = selectedTab,
                            transitionSpec = {
                                if (targetState > initialState)
                                    (slideInHorizontally { it } + fadeIn()) togetherWith
                                            (slideOutHorizontally { -it } + fadeOut())
                                else
                                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                                            (slideOutHorizontally { it } + fadeOut())
                            },
                            label = "tab"
                        ) { tab ->
                            when (tab) {
                                0    -> WeatherTab(vm, state, settings)
                                else -> SettingsTab(vm, settings)
                            }
                        }
                    }
                }
            }
            permState.shouldShowRationale ->
                PermissionScreen(true) { permState.launchMultiplePermissionRequest() }
            else -> {
                LaunchedEffect(Unit) { permState.launchMultiplePermissionRequest() }
                PermissionScreen(false) {}
            }
        }
    }
}



@Composable
private fun AppBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color.Transparent, Navy950.copy(alpha = 0.98f)))
            )
    ) {
        NavigationBar(
            containerColor    = Color.Transparent,
            contentColor      = White60,
            tonalElevation    = 0.dp,
            modifier          = Modifier.fillMaxWidth()
        ) {
            listOf(
                Triple(0, Icons.Rounded.Home,     "Weather"),
                Triple(1, Icons.Rounded.Settings, "Settings")
            ).forEach { (idx, icon, label) ->
                NavigationBarItem(
                    selected = selected == idx,
                    onClick  = { onSelect(idx) },
                    icon     = {
                        Icon(icon, label, modifier = Modifier.size(22.dp))
                    },
                    label    = {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Cyan400,
                        selectedTextColor   = Cyan400,
                        unselectedIconColor = White38,
                        unselectedTextColor = White38,
                        indicatorColor      = CyanGlow
                    )
                )
            }
        }
    }
}



@Composable
private fun AmbientOrbs() {
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .size(320.dp)
                .offset((-90).dp, (-90).dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x1A0077B6), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(70.dp, 70.dp)
                .background(
                    Brush.radialGradient(listOf(Color(0x1200B4D8), Color.Transparent)),
                    CircleShape
                )
        )
    }
}



@SuppressLint("MissingPermission")
@Composable
private fun LocationFetcher(vm: WeatherViewModel) {
    val context     = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    LaunchedEffect(Unit) {
        try {
            val last = fusedClient.lastLocation.await()
            if (last != null) { vm.loadWeather(last.latitude, last.longitude); return@LaunchedEffect }
            val req = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY).build()
            val fresh = suspendCancellableCoroutine<android.location.Location?> { c ->
                fusedClient.getCurrentLocation(req, null)
                    .addOnSuccessListener { c.resume(it) }
                    .addOnFailureListener { c.resume(null) }
            }
            if (fresh != null) vm.loadWeather(fresh.latitude, fresh.longitude)
            else vm.setLocationError("Unable to get location. Please enable GPS.")
        } catch (e: Exception) { vm.setLocationError(e.message ?: "Location error") }
    }
}



@Composable
private fun WeatherTab(vm: WeatherViewModel, state: WeatherUiState, settings: AppSettings) {
    var query        by remember { mutableStateOf("") }
    var lastLat      by remember { mutableStateOf(0.0) }
    var lastLon      by remember { mutableStateOf(0.0) }
    val focusManager = LocalFocusManager.current

    if (state is WeatherUiState.Success && state.lat != 0.0) { lastLat = state.lat; lastLon = state.lon }
    if (state is WeatherUiState.Error   && state.lat != 0.0) { lastLat = state.lat; lastLon = state.lon }

    Column(Modifier.fillMaxSize().systemBarsPadding()) {
        Spacer(Modifier.height(8.dp))

        // Search bar
        SearchBar(
            query         = query,
            onQueryChange = { query = it },
            onSearch      = {
                if (query.isNotBlank()) { vm.loadWeatherByCityTracked(query); focusManager.clearFocus() }
            },
            onGps         = {
                query = ""; focusManager.clearFocus()
                if (lastLat != 0.0 || lastLon != 0.0) vm.loadWeather(lastLat, lastLon)
            }
        )

        // State content
        AnimatedContent(
            targetState   = state,
            transitionSpec = {
                (fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 20 }) togetherWith
                        fadeOut(tween(250))
            },
            label         = "weather",
            modifier      = Modifier.weight(1f)
        ) { s ->
            when (s) {
                is WeatherUiState.Idle,
                is WeatherUiState.Loading -> LoadingScreen()
                is WeatherUiState.Success ->
                    WeatherContent(s.weather, s.forecast, settings) {
                        vm.loadWeather(lastLat, lastLon)
                    }
                is WeatherUiState.Error ->
                    ErrorScreen(s.message) {
                        if (query.isNotBlank()) vm.loadWeatherByCityTracked(query)
                        else vm.retry(s.lat, s.lon)
                    }
            }
        }
    }
}



@Composable
private fun SearchBar(
    query: String, onQueryChange: (String) -> Unit,
    onSearch: () -> Unit, onGps: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value         = query,
            onValueChange = onQueryChange,
            modifier      = Modifier.weight(1f).height(52.dp),
            placeholder   = { Text("Search city…", color = White38, style = MaterialTheme.typography.bodyMedium) },
            singleLine    = true,
            shape         = RoundedCornerShape(16.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor        = Cyan400,
                unfocusedBorderColor      = GlassBorder,
                focusedTextColor          = White87,
                unfocusedTextColor        = White87,
                cursorColor               = Cyan400,
                focusedContainerColor     = Glass12,
                unfocusedContainerColor   = Glass06,
                focusedLeadingIconColor   = Cyan400,
                unfocusedLeadingIconColor = White38
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            leadingIcon     = { Icon(Icons.Rounded.Search, null, Modifier.size(20.dp)) },
            textStyle       = MaterialTheme.typography.bodyMedium.copy(color = White87)
        )
        // GPS button
        IconButton(
            onClick  = onGps,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Glass12)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
        ) {
            Icon(Icons.Rounded.LocationOn, "My location", tint = Cyan400, modifier = Modifier.size(22.dp))
        }
    }
}



@Composable
private fun LoadingScreen() {
    val inf = rememberInfiniteTransition(label = "l")
    val a by inf.animateFloat(0.25f, 1f, infiniteRepeatable(tween(900, easing = EaseInOutSine), RepeatMode.Reverse), label = "a")
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌤", fontSize = 72.sp, modifier = Modifier.alpha(a))
            Spacer(Modifier.height(20.dp))
            Text("Fetching weather…", style = MaterialTheme.typography.bodyLarge, color = White60, modifier = Modifier.alpha(a))
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                color            = Cyan400,
                trackColor       = Glass12,
                modifier         = Modifier.width(140.dp).clip(RoundedCornerShape(50))
            )
        }
    }
}



@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Glass06)
                .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚠️", fontSize = 52.sp)
            Spacer(Modifier.height(16.dp))
            Text("Something went wrong", style = MaterialTheme.typography.titleLarge, color = White87)
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = White60, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick  = onRetry,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Cyan400)
            ) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(18.dp), tint = Navy950)
                Spacer(Modifier.width(8.dp))
                Text("Try Again", color = Navy950, fontWeight = FontWeight.Bold)
            }
        }
    }
}



@Composable
private fun PermissionScreen(showRationale: Boolean, onGrant: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Glass06)
                .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📍", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text("Location Access", style = MaterialTheme.typography.headlineLarge, color = White87)
            Spacer(Modifier.height(8.dp))
            Text(
                if (showRationale) "Allow location access to show weather for your current position." else "Requesting location…",
                style = MaterialTheme.typography.bodyMedium, color = White60, textAlign = TextAlign.Center
            )
            if (showRationale) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick  = onGrant,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Cyan400)
                ) { Text("Grant Permission", color = Navy950, fontWeight = FontWeight.Bold) }
            }
        }
    }
}



@Composable
private fun WeatherContent(
    weather:  WeatherResponse,
    forecast: ForecastResponse,
    settings: AppSettings,
    onRefresh: () -> Unit
) {
    val scroll    = rememberScrollState()
    val condition = weather.weather.firstOrNull()
    val condId    = condition?.id ?: 800

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(condBg(condId))))

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, null, tint = Cyan400, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "${weather.name}, ${weather.sys.country}",
                            style    = MaterialTheme.typography.headlineLarge,
                            color    = White87,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        fmtDateTime(weather.dt, settings.use24Hour),
                        style = MaterialTheme.typography.labelMedium,
                        color = White38
                    )
                }
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Glass12)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .clickable(
                            indication        = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onRefresh() },
                    Alignment.Center
                ) {
                    Icon(Icons.Rounded.Refresh, "Refresh", tint = White60, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            condition?.let {
                AsyncImage(
                    "https://openweathermap.org/img/wn/${it.icon}@4x.png",
                    it.description,
                    Modifier.size(120.dp)
                )
            }

            val tempStr = if (settings.useCelsius)
                "${weather.main.temp.fmt0()}°C"
            else
                "${celsiusToF(weather.main.temp).fmt0()}°F"

            Text(
                tempStr,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 90.sp, fontWeight = FontWeight.Thin, letterSpacing = (-3).sp
                ),
                color = White100
            )

            condition?.let {
                Text(it.description.titleCase(), style = MaterialTheme.typography.headlineMedium, color = White60)
            }

            Spacer(Modifier.height(10.dp))

            // H / L pill
            val unit = if (settings.useCelsius) "C" else "F"
            val hi   = if (settings.useCelsius) weather.main.temp_max else celsiusToF(weather.main.temp_max)
            val lo   = if (settings.useCelsius) weather.main.temp_min else celsiusToF(weather.main.temp_min)

            Row(
                Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Glass12)
                    .border(1.dp, GlassBorder, RoundedCornerShape(50.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("↑ ${hi.fmt0()}°$unit", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = Amber300)
                Box(Modifier.width(1.dp).height(14.dp).background(White20))
                Text("↓ ${lo.fmt0()}°$unit", style = MaterialTheme.typography.labelLarge, color = Cyan300)
            }

            Spacer(Modifier.height(28.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                StatChip(Modifier.weight(1f), "💧", "${weather.main.humidity}%", "Humidity")
                StatChip(Modifier.weight(1f), "💨", "${weather.wind.speed.fmt1()} m/s", "Wind")
                StatChip(Modifier.weight(1f), "🌡", "${
                    if (settings.useCelsius) weather.main.feels_like.fmt0() + "°C"
                    else celsiusToF(weather.main.feels_like).fmt0() + "°F"
                }", "Feels Like")
            }

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                StatChip(Modifier.weight(1f), "🔵", "${weather.main.pressure} hPa", "Pressure")
                StatChip(Modifier.weight(1f), "👁", visStr(weather.visibility), "Visibility")
                StatChip(Modifier.weight(1f), "🧭", windDir(weather.wind.deg), "Wind Dir")
            }

            Spacer(Modifier.height(10.dp))

            // Sunrise / Sunset
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                SunChip(Modifier.weight(1f), "🌅", "Sunrise", fmtTime(weather.sys.sunrise, settings.use24Hour))
                SunChip(Modifier.weight(1f), "🌇", "Sunset",  fmtTime(weather.sys.sunset,  settings.use24Hour))
            }

            Spacer(Modifier.height(24.dp))

            // ── Hourly forecast
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
                color = White20
            )
        }
    }
}



@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, color = White87)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = White38)
        }
        Box(Modifier.width(36.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(
            Brush.horizontalGradient(listOf(Cyan400, Color.Transparent))
        ))
    }
}



@Composable
private fun HourlyForecastRow(forecast: ForecastResponse, settings: AppSettings) {
    val hourly = forecast.list.take(8)
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding        = PaddingValues(horizontal = 2.dp)
    ) {
        items(hourly) { item ->
            HourlyChip(item, settings)
        }
    }
}

@Composable
private fun HourlyChip(item: ForecastItem, settings: AppSettings) {
    val temp = if (settings.useCelsius) item.main.temp.fmt0() + "°"
    else celsiusToF(item.main.temp).fmt0() + "°"
    Column(
        Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Glass06)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            fmtHour(item.dt, settings.use24Hour),
            style = MaterialTheme.typography.labelSmall,
            color = White38
        )
        Spacer(Modifier.height(8.dp))
        AsyncImage(
            model              = "https://openweathermap.org/img/wn/${item.weather.firstOrNull()?.icon ?: "01d"}@2x.png",
            contentDescription = null,
            modifier           = Modifier.size(36.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(temp, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = White87)
    }
}



@Composable
private fun DailyForecastCard(forecast: ForecastResponse, settings: AppSettings) {
    val daily = forecast.list
        .groupBy { item ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(item.dt * 1000L))
        }
        .values
        .take(5)
        .map { dayItems ->
            // Pick midday entry or middle of list
            dayItems.minByOrNull { item ->
                val h = Calendar.getInstance().apply { time = Date(item.dt * 1000L) }.get(Calendar.HOUR_OF_DAY)
                Math.abs(h - 12)
            } ?: dayItems.first()
        }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Glass06)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
    ) {
        daily.forEachIndexed { idx, item ->
            DailyRow(item, settings, isLast = idx == daily.lastIndex)
        }
    }
}

@Composable
private fun DailyRow(item: ForecastItem, settings: AppSettings, isLast: Boolean) {
    val hi = if (settings.useCelsius) item.main.temp_max.fmt0() + "°" else celsiusToF(item.main.temp_max).fmt0() + "°"
    val lo = if (settings.useCelsius) item.main.temp_min.fmt0() + "°" else celsiusToF(item.main.temp_min).fmt0() + "°"

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        // Day label
        Text(
            fmtDay(item.dt),
            style    = MaterialTheme.typography.titleMedium,
            color    = White87,
            modifier = Modifier.width(52.dp)
        )
        // Icon
        AsyncImage(
            model              = "https://openweathermap.org/img/wn/${item.weather.firstOrNull()?.icon ?: "01d"}@2x.png",
            contentDescription = null,
            modifier           = Modifier.size(40.dp)
        )
        // Condition
        Text(
            item.weather.firstOrNull()?.main ?: "",
            style    = MaterialTheme.typography.bodyMedium,
            color    = White60,
            modifier = Modifier.width(80.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // Hi / Lo
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(hi, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = Amber300)
            Text(lo, style = MaterialTheme.typography.labelLarge, color = Cyan300)
        }
    }
    if (!isLast) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(1.dp).background(GlassBorder))
    }
}



@Composable
private fun StatChip(modifier: Modifier, emoji: String, value: String, label: String) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Glass06)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = White87, maxLines = 1)
        Spacer(Modifier.height(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = White38, maxLines = 1)
    }
}

@Composable
private fun SunChip(modifier: Modifier, emoji: String, label: String, time: String) {
    Row(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Glass06)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(emoji, fontSize = 22.sp)
        Column {
            Text(time, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = White87)
            Text(label, style = MaterialTheme.typography.labelSmall, color = White38)
        }
    }
}



@Composable
private fun SettingsTab(vm: WeatherViewModel, settings: AppSettings) {
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        Text("Settings", style = MaterialTheme.typography.headlineLarge, color = White87)
        Text("Personalise your weather app", style = MaterialTheme.typography.bodyMedium, color = White38)

        Spacer(Modifier.height(24.dp))

        SettingsSection("Preferences") {
            SettingsToggle(
                icon    = Icons.Rounded.Info,
                title   = "Temperature Unit",
                subtitle = if (settings.useCelsius) "Celsius (°C)" else "Fahrenheit (°F)",
                checked = settings.useCelsius,
                onToggle = { vm.toggleCelsius() }
            )
            SettingsDivider()
            SettingsToggle(
                icon     = Icons.Rounded.Settings,
                title    = "Time Format",
                subtitle = if (settings.use24Hour) "24-hour clock" else "12-hour clock (AM/PM)",
                checked  = settings.use24Hour,
                onToggle = { vm.toggle24Hour() }
            )
            SettingsDivider()
            SettingsToggle(
                icon     = Icons.Rounded.Notifications,
                title    = "Weather Alerts",
                subtitle = if (settings.notificationsOn) "Severe weather notifications on" else "Notifications off",
                checked  = settings.notificationsOn,
                onToggle = { vm.toggleNotifications() }
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsSection("Data & Privacy") {
            SettingsInfoRow(Icons.Rounded.LocationOn, "Location Data", "Used only to fetch local weather. Never stored or shared.")
            SettingsDivider()
            SettingsInfoRow(Icons.Rounded.Place, "Weather Data", "Powered by OpenWeatherMap API. Data refreshed on request only.")
            SettingsDivider()
            SettingsInfoRow(Icons.Rounded.Lock, "No Tracking", "This app collects no personal data and contains no ads.")
            SettingsDivider()
            SettingsInfoRow(Icons.Rounded.Info, "Local Storage", "No data is stored on servers. All preferences are device-local only.")
        }

        Spacer(Modifier.height(16.dp))

        SettingsSection("About") {
            SettingsInfoRow(Icons.Rounded.Info, "App Version", "1.0.0")
            SettingsDivider()
            SettingsInfoRow(Icons.Rounded.Place, "Weather Source", "OpenWeatherMap — openweathermap.org")
            SettingsDivider()
            SettingsInfoRow(Icons.Rounded.Star, "Data Updates", "Current weather + 5-day / 3-hour forecast")
        }

        Spacer(Modifier.height(32.dp))

        // Footer
        Text(
            "WeatherApp • No ads, no tracking",
            style     = MaterialTheme.typography.labelSmall,
            color     = White20,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
    }
}



@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = Cyan400,
        letterSpacing = 1.2.sp
    )
    Spacer(Modifier.height(8.dp))
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Glass06)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsToggle(
    icon:     ImageVector,
    title:    String,
    subtitle: String,
    checked:  Boolean,
    onToggle: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(remember { MutableInteractionSource() }, null) { onToggle() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (checked) CyanGlow else Glass12),
                Alignment.Center
            ) {
                Icon(icon, null, tint = if (checked) Cyan400 else White38, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, color = White87)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = White38)
            }
        }
        Switch(
            checked  = checked,
            onCheckedChange = { onToggle() },
            colors   = SwitchDefaults.colors(
                checkedThumbColor   = Navy950,
                checkedTrackColor   = Cyan400,
                uncheckedThumbColor = White38,
                uncheckedTrackColor = Glass20
            )
        )
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Glass12),
            Alignment.Center
        ) {
            Icon(icon, null, tint = White38, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = White87)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = White38)
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(Modifier.fillMaxWidth().padding(start = 68.dp).height(1.dp).background(GlassBorder))
}



private fun Double.fmt0() = roundToInt().toString()
private fun Double.fmt1() = "%.1f".format(this)
private fun String.titleCase() = split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
private fun celsiusToF(c: Double) = c * 9.0 / 5.0 + 32.0

private fun fmtTime(unix: Long, use24: Boolean): String {
    val fmt = if (use24) "HH:mm" else "hh:mm a"
    return SimpleDateFormat(fmt, Locale.getDefault()).format(Date(unix * 1000L))
}

private fun fmtHour(unix: Long, use24: Boolean): String {
    val fmt = if (use24) "HH:mm" else "ha"
    return SimpleDateFormat(fmt, Locale.getDefault()).format(Date(unix * 1000L))
}

private fun fmtDay(unix: Long): String {
    val cal   = Calendar.getInstance()
    val today = Calendar.getInstance()
    cal.time  = Date(unix * 1000L)
    return if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
        "Today"
    else
        SimpleDateFormat("EEE", Locale.getDefault()).format(Date(unix * 1000L))
}

private fun fmtDateTime(unix: Long, use24: Boolean): String {
    val fmt = if (use24) "EEE d MMM, HH:mm" else "EEE d MMM, hh:mm a"
    return SimpleDateFormat(fmt, Locale.getDefault()).format(Date(unix * 1000L))
}

private fun visStr(m: Int) = if (m >= 1000) "${"%.0f".format(m / 1000.0)} km" else "$m m"

private fun windDir(deg: Int): String {
    val d = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return d[((deg + 22.5) / 45).toInt() % 8]
}

private fun condBg(id: Int): List<Color> = when {
    id in 200..299 -> listOf(Color(0xFF06060E), Color(0xFF09091C), Color(0xFF0E0E2E))
    id in 300..599 -> listOf(Color(0xFF060C14), Color(0xFF091422), Color(0xFF0D1F35))
    id in 600..699 -> listOf(Color(0xFF080E18), Color(0xFF0C1628), Color(0xFF12223C))
    id in 700..799 -> listOf(Color(0xFF0A0A0E), Color(0xFF111116), Color(0xFF18181F))
    id == 800      -> listOf(Color(0xFF020508), Color(0xFF040B14), Color(0xFF071526))
    id in 801..802 -> listOf(Color(0xFF050A14), Color(0xFF081222), Color(0xFF0D1C34))
    else           -> listOf(Color(0xFF060A12), Color(0xFF0A111E), Color(0xFF0F1A2E))
}