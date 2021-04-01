package com.github.stefnotch.vibratingalarmclock.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.stefnotch.vibratingalarmclock.BuildConfig
import com.github.stefnotch.vibratingalarmclock.ble.BleConnection
import com.github.stefnotch.vibratingalarmclock.broadcastreceiver.AlarmBroadcastReceiver
import java.time.*
import java.time.chrono.IsoChronology
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjusters


@Entity(tableName = "alarms")
class Alarm(time: LocalTime) {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var id: Int = 0 // Should be readonly, whatever

    var title = ""
    var time = time // Relative to the current time zone!

    var isRecurring = false // If it repeats on some day(s) of the week
    var days = DaysOfTheWeek.None

    var isRunning = false

    companion object {
        const val ACTION_ALARM = BuildConfig.APPLICATION_ID + ".ACTION_ALARM"
        const val ACTION_ALARM_TRIGGERED = BuildConfig.APPLICATION_ID + ".ACTION_ALARM_TRIGGERED"
        const val ACTION_STOP_ALARM = BuildConfig.APPLICATION_ID + ".ACTION_STOP_ALARM"
        const val ACTION_SNOOZE_ALARM = BuildConfig.APPLICATION_ID + ".ACTION_SNOOZE_ALARM"

        private var toast: Toast? = null

        fun showMessage(context: Context, text: String) {
            toast?.cancel()
            toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
            toast?.show()
        }

        fun createIntent(context: Context, alarm: Alarm, day: Int): Intent {
            val intent = Intent(context, AlarmBroadcastReceiver::class.java)
            intent.action = ACTION_ALARM + "_" + day // Make sure to generate a unique intent
            intent.putExtra("id", alarm.id)
            intent.putExtra("day", day)

            return intent
        }
    }

    fun scheduleAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if(!isRecurring) {
            val intent = createIntent(context, this, DaysOfTheWeek.None)
            val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            if(isRunning) {
                alarmManager.cancel(pendingIntent)
            }

            val localDate = if (LocalTime.now() >= time) LocalDate.now().plusDays(1) else LocalDate.now()

            // Uses this API because https://stackoverflow.com/a/33110418
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(LocalDateTime.of(localDate, time).toInstant(OffsetDateTime.now().offset).toEpochMilli(), pendingIntent),
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

        showMessage(context, "Scheduled Alarm")
        isRunning = true
    }

    private fun scheduleAlarmForDay(context: Context, alarmManager: AlarmManager, day: Int) {
        val intent = createIntent(context, this, day)

        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if(isRunning) {
            alarmManager.cancel(pendingIntent)
        }

        val date = LocalDate.now().with(TemporalAdjusters.nextOrSame(DaysOfTheWeek.getJavaDayOfWeek(day)))
        var dateTime = LocalDateTime.of(date, time)
        if(LocalDateTime.now() >= dateTime) {
            dateTime = dateTime.with(TemporalAdjusters.next(DaysOfTheWeek.getJavaDayOfWeek(day)))
        }

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(dateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli(), pendingIntent),
            pendingIntent
        )
    }

    fun scheduleAlarmForNextDay(context: Context, day: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = createIntent(context, this, day)
        val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if(isRunning) {
            alarmManager.cancel(pendingIntent)
        }

        val date = LocalDateTime.now()
            .plusHours(1)
            .with(TemporalAdjusters.next(DaysOfTheWeek.getJavaDayOfWeek(day)))
            .toLocalDate()
        val dateTime = LocalDateTime.of(date, time)

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(dateTime.toInstant(OffsetDateTime.now().offset).toEpochMilli(), pendingIntent),
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context) {
        if(!isRunning) return

        val ble = BleConnection.getInstance()
        ble.stopVibrating()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if(!isRecurring) {
            val intent = createIntent(context, this, DaysOfTheWeek.None)
            val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManager.cancel(pendingIntent)
        } else {
            DaysOfTheWeek.everyDay().forEach {
                val intent = createIntent(context, this, it)
                val pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.cancel(pendingIntent)
            }
        }
        showMessage(context, "Cancelled Alarm")
        isRunning = false
    }

    fun getFormattedTime(context: Context): String {
        // This is downright silly
        // See https://github.com/JakeWharton/ThreeTenABP/issues/16

        val config = context.resources.configuration
        val locale = config.locales.let { if(it.isEmpty) java.util.Locale.getDefault() else it.get(0) }

        val legacyFormat = android.text.format.DateFormat.getTimeFormat(context) as? java.text.SimpleDateFormat
        return if(legacyFormat == null) {
            time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
        } else {
            time.format(DateTimeFormatterBuilder().appendPattern(legacyFormat.toPattern()).toFormatter(locale).withChronology(IsoChronology.INSTANCE))
        }
    }

    fun getDaysText(): String {
        if(!isRecurring) return "Once off"

        var returnText = ""
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Monday)) returnText += "Mo"
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Tuesday)) returnText += "Tu"
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Wednesday)) returnText += "We"
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Thursday)) returnText += "Th"
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Friday)) returnText += "Fr"
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Saturday)) returnText += "Sa"
        if (DaysOfTheWeek.contains(days, DaysOfTheWeek.Sunday)) returnText += "Su"

        return returnText
    }
}