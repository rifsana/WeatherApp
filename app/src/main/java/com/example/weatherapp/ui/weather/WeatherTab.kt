// ui/weather/WeatherTab.kt
package com.example.weatherapp.ui.weather

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.theme.AppColors
import com.example.weatherapp.viewmodel.AppSettings
import com.example.weatherapp.viewmodel.WeatherUiState
import com.example.weatherapp.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTab(
    vm: WeatherViewModel,
    state: WeatherUiState,
    settings: AppSettings
) {
    var query   by remember { mutableStateOf("") }
    var lastLat by remember { mutableStateOf(0.0) }
    var lastLon by remember { mutableStateOf(0.0) }
    val focusManager = LocalFocusManager.current

    if (state is WeatherUiState.Success && state.lat != 0.0) { lastLat = state.lat; lastLon = state.lon }
    if (state is WeatherUiState.Error   && state.lat != 0.0) { lastLat = state.lat; lastLon = state.lon }

    val isRefreshing     = state is WeatherUiState.Loading
    val pullRefreshState = rememberPullToRefreshState()

    Column(Modifier.fillMaxSize().systemBarsPadding()) {
        Spacer(Modifier.height(8.dp))

        SearchBar(
            query         = query,
            onQueryChange = { query = it },
            onSearch      = {
                if (query.isNotBlank()) {
                    vm.loadWeatherByCityTracked(query)
                    focusManager.clearFocus()
                }
            },
            onGps = {
                query = ""
                focusManager.clearFocus()
                if (lastLat != 0.0 || lastLon != 0.0) vm.loadWeather(lastLat, lastLon)
            }
        )

        // Material 3 PullToRefreshBox — no accompanist dependency, no crashes
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = {
                if (query.isNotBlank()) vm.loadWeatherByCityTracked(query)
                else if (lastLat != 0.0 || lastLon != 0.0) vm.loadWeather(lastLat, lastLon)
            },
            state    = pullRefreshState,
            modifier = Modifier.weight(1f)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                when (state) {
                    is WeatherUiState.Idle    -> IdleScreen()
                    is WeatherUiState.Loading -> LoadingScreen()
                    is WeatherUiState.Success ->
                        WeatherContent(state.weather, state.forecast, settings)
                    is WeatherUiState.Error   ->
                        ErrorScreen(state.message) {
                            if (query.isNotBlank()) vm.loadWeatherByCityTracked(query)
                            else vm.retry(state.lat, state.lon)
                        }
                }
            }
        }
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onGps: () -> Unit
) {
    val accentColor  = AppColors.cyan400
    val borderColor  = AppColors.glassBorder
    val textColor    = MaterialTheme.colorScheme.onSurface
    val hintColor    = MaterialTheme.colorScheme.onSurfaceVariant
    val containerOn  = AppColors.glass12
    val containerOff = AppColors.glass06

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value         = query,
            onValueChange = onQueryChange,
            modifier      = Modifier.weight(1f).height(52.dp),
            placeholder   = {
                Text(
                    "Search city…",
                    color = hintColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            singleLine    = true,
            shape         = RoundedCornerShape(16.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor        = accentColor,
                unfocusedBorderColor      = borderColor,
                focusedTextColor          = textColor,
                unfocusedTextColor        = textColor,
                cursorColor               = accentColor,
                focusedContainerColor     = containerOn,
                unfocusedContainerColor   = containerOff,
                focusedLeadingIconColor   = accentColor,
                unfocusedLeadingIconColor = hintColor
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            leadingIcon     = { Icon(Icons.Rounded.Search, null, Modifier.size(20.dp)) },
            textStyle       = MaterialTheme.typography.bodyMedium
        )
        IconButton(
            onClick  = onGps,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(containerOn)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
        ) {
            Icon(
                Icons.Rounded.LocationOn, "My location",
                tint     = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Idle ──────────────────────────────────────────────────────────────────────

@Composable
private fun IdleScreen() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌍", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Search a city or use GPS",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Loading skeleton ──────────────────────────────────────────────────────────

@Composable
private fun LoadingScreen() {
    val inf = rememberInfiniteTransition(label = "l")
    val a by inf.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "a"
    )

    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SkeletonBlock(Modifier.width(160.dp).height(20.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.height(20.dp))
            SkeletonBlock(Modifier.width(120.dp).height(80.dp).clip(RoundedCornerShape(16.dp)))
            Spacer(Modifier.height(12.dp))
            SkeletonBlock(Modifier.width(200.dp).height(28.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.height(8.dp))
            SkeletonBlock(Modifier.width(140.dp).height(16.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) { SkeletonBlock(Modifier.size(80.dp).clip(RoundedCornerShape(18.dp))) }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Fetching weather…",
                style    = MaterialTheme.typography.bodyLarge,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(a)
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                color      = MaterialTheme.colorScheme.primary,
                trackColor = AppColors.glass12,
                modifier   = Modifier.width(140.dp).clip(RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun SkeletonBlock(modifier: Modifier) {
    val inf = rememberInfiniteTransition(label = "sk")
    val a by inf.animateFloat(
        initialValue  = 0.12f,
        targetValue   = 0.28f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "ska"
    )
    Box(modifier.background(MaterialTheme.colorScheme.onBackground.copy(alpha = a)))
}

// ── Error ─────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorScreen(message: String, onRetry: () -> Unit) {
    val isNetwork  = message.contains("internet",           ignoreCase = true) ||
            message.contains("network",            ignoreCase = true) ||
            message.contains("connection",         ignoreCase = true)
    val isNotFound = message.contains("not found",          ignoreCase = true) ||
            message.contains("Check the spelling", ignoreCase = true)

    val glass06     = AppColors.glass06
    val glassBorder = AppColors.glassBorder
    val accentColor = AppColors.cyan400

    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(
            Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(glass06)
                .border(1.dp, glassBorder, RoundedCornerShape(28.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                when {
                    isNetwork  -> "📡"
                    isNotFound -> "🔍"
                    else       -> "⚠️"
                },
                fontSize = 52.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                when {
                    isNetwork  -> "No Internet"
                    isNotFound -> "City Not Found"
                    else       -> "Something went wrong"
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick  = onRetry,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(
                    Icons.Rounded.Refresh, null,
                    Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Try Again",
                    color      = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}