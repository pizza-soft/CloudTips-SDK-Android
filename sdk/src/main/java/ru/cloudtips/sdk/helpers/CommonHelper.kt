package ru.cloudtips.sdk.helpers

import android.annotation.SuppressLint
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object CommonHelper {
    @SuppressLint("SimpleDateFormat")
    fun stringToDate(date: String?): Date? {
        if (date == null) return null
        val formatList = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "dd MMMM yyyy"
        )
        for (format in formatList) {
            try {
                return SimpleDateFormat(format, Locale.getDefault()).parse(date)
            } catch (ignored: Exception) {
            }
        }
        return null
    }

    fun formatDouble(_value: Double?, prefix: String = ""): String {
        val value = _value ?: 0.0
        return when {
            value - value.toInt() < 1e-6 -> {
                "%.0f"
            }
            value * 10 - (value * 10).toInt() < 1e-6 -> {
                "%.1f"
            }
            else -> "%.2f"
        }.format(value.toFloat()).plus(prefix)
    }
}