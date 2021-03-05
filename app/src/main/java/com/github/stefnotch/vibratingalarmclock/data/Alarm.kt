package com.github.stefnotch.vibratingalarmclock.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.stefnotch.vibratingalarmclock.broadcastreceiver.AlarmBroadcastReceiver
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters
import java.util.*


@Entity(tableName = "alarms")
class Alarm(time: LocalTime) {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var id: Int = 0; // Should be readonly, whatever

    var title = "";
    var time = time;

    var isRecurring = false; // If it repeats on some day(s) of the week
    var days = DaysOfTheWeek.None;

    var isRunning = false;

    fun scheduleAlarm(context: Context) {
        if(isRunning) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager;

        if(!isRecurring) {
            val intent = Intent(context, AlarmBroadcastReceiver::class.java)
            intent.putExtra("title", title)
            intent.putExtra("time-hour", time.hour)
            intent.putExtra("time-minute", time.minute)
            intent.putExtra("time-second", time.second)
            intent.putExtra("time-nano", time.nano)
            intent.putExtra("is-recurring", isRecurring)
            intent.putExtra("day", DaysOfTheWeek.None)

            val pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0)

            val localDate = if (time <= LocalTime.now()) LocalDate.now().plusDays(1) else LocalDate.now();

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(LocalDateTime.of(localDate, time).toInstant(ZoneOffset.UTC).epochSecond, pendingIntent),
                pendingIntent
            )
        } else {
            if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Monday)) scheduleAlarmForDay(context, alarmManager, DaysOfTheWeek.Monday)
            if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Tuesday)) scheduleAlarmForDay(context, alarmManager, DaysOfTheWeek.Tuesday)
            if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Wednesday)) scheduleAlarmForDay(context, alarmManager, DaysOfTheWeek.Wednesday)
            if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Thursday)) scheduleAlarmForDay(context, alarmManager, DaysOfTheWeek.Thursday)
            if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Friday)) scheduleAlarmForDay(context, alarmManager, DaysOfTheWeek.Friday)
            if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Saturday)) scheduleAlarmForDay(context, alarmManager, DaysOfTheWeek.Saturday)
            if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Sunday)) scheduleAlarmForDay(context, alarmManager, DaysOfTheWeek.Sunday)
        }

        isRunning = true;
    }

    private fun scheduleAlarmForDay(context: Context, alarmManager: AlarmManager, day: Int) {
        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("time-hour", time.hour)
        intent.putExtra("time-minute", time.minute)
        intent.putExtra("time-second", time.second)
        intent.putExtra("time-nano", time.nano)
        intent.putExtra("is-recurring", isRecurring)
        intent.putExtra("day", day)

        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0)

        val date = LocalDate.now().with(TemporalAdjusters.nextOrSame(DaysOfTheWeek.getJavaDayOfWeek(day)))
        var dateTime = LocalDateTime.of(date, time)
        if(dateTime <= LocalDateTime.now()) {
            dateTime = dateTime.with(TemporalAdjusters.next(DaysOfTheWeek.getJavaDayOfWeek(day)))
        }

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(dateTime.toInstant(ZoneOffset.UTC).epochSecond, pendingIntent),
            pendingIntent
        )
    }

    fun scheduleAlarmForNextDay(context: Context, day: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager;

        val intent = Intent(context, AlarmBroadcastReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("time-hour", time.hour)
        intent.putExtra("time-minute", time.minute)
        intent.putExtra("time-second", time.second)
        intent.putExtra("time-nano", time.nano)
        intent.putExtra("is-recurring", isRecurring)
        intent.putExtra("day", day)

        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0)

        val date = LocalDateTime.now()
            .plusHours(1)
            .with(TemporalAdjusters.next(DaysOfTheWeek.getJavaDayOfWeek(day)))
            .toLocalDate()
        var dateTime = LocalDateTime.of(date, time)

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(dateTime.toInstant(ZoneOffset.UTC).epochSecond, pendingIntent),
            pendingIntent
        )
    }


    fun cancelAlarm(context: Context) {
        if(!isRunning) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager;

        alarmManager.cancel(
            PendingIntent.getBroadcast(context, id, Intent(context, AlarmBroadcastReceiver::class.java), 0)
        );

        Toast.makeText(context, "Cancelled Alarm", Toast.LENGTH_SHORT).show();
        isRunning = false;
    }

    fun getFormattedTime(): String = time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))

    fun getDaysText(): String {
        if(!isRecurring) return "Once off";

        var returnText = "";
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Monday)) returnText += "Mo";
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Tuesday)) returnText += "Tu";
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Wednesday)) returnText += "We";
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Thursday)) returnText += "Th";
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Friday)) returnText += "Fr";
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Saturday)) returnText += "Sa";
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Sunday)) returnText += "Su";

        return returnText;
    }
}