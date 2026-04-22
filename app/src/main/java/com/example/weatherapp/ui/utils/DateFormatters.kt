// ui/utils/DateFormatters.kt
package com.example.weatherapp.ui.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun fmtTime(unix: Long, use24: Boolean): String {
    val fmt = if (use24) "HH:mm" else "hh:mm a"
    return SimpleDateFormat(fmt, Locale.getDefault()).format(Date(unix * 1000L))
}

fun fmtHour(unix: Long, use24: Boolean): String {
    val fmt = if (use24) "HH:mm" else "ha"
    return SimpleDateFormat(fmt, Locale.getDefault()).format(Date(unix * 1000L))
}

fun fmtDay(unix: Long): String {
    val cal   = Calendar.getInstance()
    val today = Calendar.getInstance()
    cal.time  = Date(unix * 1000L)
    return if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
        "Today"
    else
        SimpleDateFormat("EEE", Locale.getDefault()).format(Date(unix * 1000L))
}

fun fmtDateTime(unix: Long, use24: Boolean): String {
    val fmt = if (use24) "EEE d MMM, HH:mm" else "EEE d MMM, hh:mm a"
    return SimpleDateFormat(fmt, Locale.getDefault()).format(Date(unix * 1000L))
}