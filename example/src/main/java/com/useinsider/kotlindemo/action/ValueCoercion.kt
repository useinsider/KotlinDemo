package com.useinsider.kotlindemo.action

import com.useinsider.insider.InsiderGender
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal fun coerceInt(value: String): Int = value.toIntOrNull() ?: 0

internal fun coerceDouble(value: String): Double = value.toDoubleOrNull() ?: 0.0

internal fun coerceBoolean(value: String): Boolean = value.toBooleanStrictOrNull() ?: false

internal fun parseBirthday(value: String): Date? {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        isLenient = false
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return runCatching { format.parse(value.trim()) }.getOrNull()
}

internal fun mapGender(value: String): InsiderGender = when (value.lowercase()) {
    "male", "0" -> InsiderGender.MALE
    "female", "1" -> InsiderGender.FEMALE
    else -> InsiderGender.OTHER
}
