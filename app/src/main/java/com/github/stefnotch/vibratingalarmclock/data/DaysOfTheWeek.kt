package com.github.stefnotch.vibratingalarmclock.data

import java.time.DayOfWeek

class DaysOfTheWeek {
    companion object {
        val None = 0;
        val Monday = 1 shl 0;
        val Tuesday = 1 shl 1;
        val Wednesday = 1 shl 2;
        val Thursday = 1 shl 3;
        val Friday = 1 shl 4;
        val Saturday = 1 shl 5;
        val Sunday = 1 shl 6;

        fun contains(days: Int, day: Int): Boolean {
            return days and day != 0
        }

        fun getJavaDayOfWeek(day: Int): DayOfWeek? {
            return when(day) {
                Monday -> DayOfWeek.MONDAY
                Tuesday -> DayOfWeek.TUESDAY
                Wednesday -> DayOfWeek.WEDNESDAY
                Thursday -> DayOfWeek.THURSDAY
                Friday -> DayOfWeek.FRIDAY
                Saturday -> DayOfWeek.SATURDAY
                Sunday -> DayOfWeek.SUNDAY
                else -> null
            }
        }
    }
}