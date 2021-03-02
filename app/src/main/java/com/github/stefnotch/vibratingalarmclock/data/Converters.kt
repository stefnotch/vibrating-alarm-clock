package com.github.stefnotch.vibratingalarmclock.data

import androidx.room.TypeConverter
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? {
        return LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME)
    }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? {
        return value?.format(DateTimeFormatter.ISO_LOCAL_TIME)
    }
}