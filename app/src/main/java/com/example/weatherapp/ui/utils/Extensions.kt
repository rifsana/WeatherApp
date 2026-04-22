// ui/utils/Extensions.kt
package com.example.weatherapp.ui.utils

import kotlin.math.roundToInt

fun Double.fmt0() = roundToInt().toString()
fun Double.fmt1() = "%.1f".format(this)
fun String.titleCase() = split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
fun celsiusToF(c: Double) = c * 9.0 / 5.0 + 32.0