package com.github.stefnotch.vibratingalarmclock.data

import java.time.DayOfWeek

class DaysOfTheWeek {
    companion object {
        const val None = 0;
        const val Monday = 1 shl 0;
        const val Tuesday = 1 shl 1;
        const val Wednesday = 1 shl 2;
        const val Thursday = 1 shl 3;
        const val Friday = 1 shl 4;
        const val Saturday = 1 shl 5;
        const val Sunday = 1 shl 6;

        fun contains(days: Int, day: Int): Boolean {
            return days and day != 0
        }

        fun everyDay(): List<Int> {
            return listOf(Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday)
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