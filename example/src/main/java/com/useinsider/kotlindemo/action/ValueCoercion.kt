package com.useinsider.kotlindemo.action

import com.useinsider.insider.InsiderGender

internal fun coerceInt(value: String): Int = value.toIntOrNull() ?: 0

internal fun coerceDouble(value: String): Double = value.toDoubleOrNull() ?: 0.0

internal fun coerceBoolean(value: String): Boolean = value.toBooleanStrictOrNull() ?: false

internal fun mapGender(value: String): InsiderGender = when (value.lowercase()) {
    "male", "0" -> InsiderGender.MALE
    "female", "1" -> InsiderGender.FEMALE
    else -> InsiderGender.OTHER
}
