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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.*


@Entity(tableName = "alarms")
class Alarm(id: Int, time: LocalTime) {
    @PrimaryKey
    @NonNull
    val id = id;

    var title = "";
    var time = time;

    var isRecurring = false; // If it repeats on some day(s) of the week
    var days = DaysOfTheWeek.None;

    var isRunning = false;

    fun scheduleAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager;

        val intent = Intent(context, AlarmBroadcastReceiver::class.java);
        intent.putExtra("title", title);
        intent.putExtra("time-hour", time.hour);
        intent.putExtra("time-minute", time.minute);
        intent.putExtra("time-second", time.second);
        intent.putExtra("time-nano", time.nano);
        intent.putExtra("is-recurring", isRecurring);
        intent.putExtra("days", days);

        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0)

        val localDate = if (time <= LocalTime.now()) LocalDate.now().plusDays(1) else LocalDate.now();

        if(!isRecurring) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(LocalDateTime.of(localDate, time).toInstant(ZoneOffset.UTC).epochSecond, pendingIntent),
                pendingIntent
            );
        } else {
            // TODO: Set an alarm for every chosen day

        }

        // TODO: Code code code code code code code code code code code code code code code code code code code code code

        isRunning = true;
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager;

        alarmManager.cancel(
            PendingIntent.getBroadcast(context, id, Intent(context, AlarmBroadcastReceiver::class.java), 0)
        );

        Toast.makeText(context, "Cancelled Alarm", Toast.LENGTH_SHORT).show();
        isRunning = false;
    }

    fun getDaysText(): String {
        if(isRecurring) return "";

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