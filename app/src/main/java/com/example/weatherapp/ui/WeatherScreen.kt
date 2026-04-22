// ui/WeatherScreen.kt
package com.example.weatherapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.settings.SettingsTab
import com.example.weatherapp.ui.theme.AppColors
import com.example.weatherapp.ui.theme.DarkCyanGlow
import com.example.weatherapp.ui.theme.LocalIsDarkTheme
import com.example.weatherapp.ui.weather.WeatherTab
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WeatherScreen(vm: WeatherViewModel) {
    val state    by vm.uiState.collectAsState()
    val settings by vm.settings.collectAsState()
    val context  = LocalContext.current

    val permState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var selectedTab        by remember { mutableStateOf(0) }
    var permissionRequested by remember { mutableStateOf(false) }

    // Theme-aware gradient for the root background
    val bgGradient = AppColors.backgroundGradient

    Box(
        Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
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
                            targetState  = selectedTab,
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

            !permState.shouldShowRationale && permissionRequested -> {
                PermissionDeniedScreen {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }

            else -> {
                LaunchedEffect(Unit) {
                    permissionRequested = true
                    permState.launchMultiplePermissionRequest()
                }
                PermissionScreen(permState.shouldShowRationale) {
                    permissionRequested = true
                    permState.launchMultiplePermissionRequest()
                }
            }
        }
    }
}

// ── Bottom navigation bar ─────────────────────────────────────────────────────

@Composable
private fun AppBottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val isDark      = LocalIsDarkTheme.current
    val bgColor     = MaterialTheme.colorScheme.background
    val accentColor = AppColors.cyan400
    val glowColor   = if (isDark) DarkCyanGlow else accentColor.copy(alpha = 0.15f)

    Box(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, bgColor.copy(alpha = 0.98f))
                )
            )
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor   = MaterialTheme.colorScheme.onSurfaceVariant,
            tonalElevation = 0.dp,
            modifier       = Modifier.fillMaxWidth()
        ) {
            listOf(
                Triple(0, Icons.Rounded.Home,     "Weather"),
                Triple(1, Icons.Rounded.Settings, "Settings")
            ).forEach { (idx, icon, label) ->
                NavigationBarItem(
                    selected = selected == idx,
                    onClick  = { onSelect(idx) },
                    icon     = { Icon(icon, label, modifier = Modifier.size(22.dp)) },
                    label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors   = NavigationBarItemDefaults.colors(
                        selectedIconColor   = accentColor,
                        selectedTextColor   = accentColor,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor      = glowColor
                    )
                )
            }
        }
    }
}

// ── Ambient decorative orbs ───────────────────────────────────────────────────

@Composable
private fun AmbientOrbs() {
    val isDark = LocalIsDarkTheme.current
    val color1 = if (isDark) Color(0x1A0077B6) else Color(0x1400ACC1)
    val color2 = if (isDark) Color(0x1200B4D8) else Color(0x1200ACC1)

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .size(320.dp)
                .offset((-90).dp, (-90).dp)
                .background(
                    Brush.radialGradient(listOf(color1, Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            Modifier
                .size(220.dp)
                .align(Alignment.BottomEnd)
                .offset(70.dp, 70.dp)
                .background(
                    Brush.radialGradient(listOf(color2, Color.Transparent)),
                    CircleShape
                )
        )
    }
}

// ── Location fetcher ──────────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
@Composable
private fun LocationFetcher(vm: WeatherViewModel) {
    val context     = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        try {
            val last = fusedClient.lastLocation.await()
            if (last != null) {
                vm.loadWeather(last.latitude, last.longitude)
                return@LaunchedEffect
            }

            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                10_000L
            ).apply {
                setWaitForAccurateLocation(false)
                setMinUpdateIntervalMillis(5_000L)
                setMaxUpdates(1)
            }.build()

            val fresh = suspendCancellableCoroutine<android.location.Location?> { cont ->
                val cb = object : com.google.android.gms.location.LocationCallback() {
                    override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                        fusedClient.removeLocationUpdates(this)
                        cont.resume(result.lastLocation)
                    }
                    override fun onLocationAvailability(avail: com.google.android.gms.location.LocationAvailability) {
                        if (!avail.isLocationAvailable) {
                            fusedClient.removeLocationUpdates(this)
                            cont.resume(null)
                        }
                    }
                }
                fusedClient.requestLocationUpdates(
                    locationRequest, cb, android.os.Looper.getMainLooper()
                )
                cont.invokeOnCancellation { fusedClient.removeLocationUpdates(cb) }
            }

            if (fresh != null) vm.loadWeather(fresh.latitude, fresh.longitude)
            else vm.setLocationError("Unable to get location. Please enable GPS and try again.")

        } catch (e: Exception) {
            vm.setLocationError(e.message ?: "Location error")
        }
    }
}

// ── Permission screens ────────────────────────────────────────────────────────

@Composable
private fun PermissionScreen(showRationale: Boolean, onGrant: () -> Unit) {
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
            Text("📍", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Location Access",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (showRationale)
                    "Location access lets us show accurate weather for your current position. We never store or share your location."
                else
                    "We need location permission to fetch weather for where you are right now.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (showRationale) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick  = onGrant,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(
                        "Allow Location",
                        color      = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    color      = accentColor,
                    trackColor = AppColors.glass12,
                    modifier   = Modifier.width(120.dp).clip(RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
private fun PermissionDeniedScreen(onOpenSettings: () -> Unit) {
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
            Text("🔒", fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Permission Required",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Location permission was denied. Please open Settings and allow location access for WeatherApp.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick  = onOpenSettings,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Icon(
                    Icons.Rounded.OpenInNew, null,
                    Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Open Settings",
                    color      = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}