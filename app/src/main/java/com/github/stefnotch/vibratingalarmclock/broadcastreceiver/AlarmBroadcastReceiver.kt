package com.github.stefnotch.vibratingalarmclock.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.github.stefnotch.vibratingalarmclock.data.Alarm
import com.github.stefnotch.vibratingalarmclock.data.DaysOfTheWeek
import com.github.stefnotch.vibratingalarmclock.service.AlarmRescheduleJobService
import com.github.stefnotch.vibratingalarmclock.service.AlarmTriggeredJobService

class AlarmBroadcastReceiver : BroadcastReceiver() {

    // Gets called when the device boots or when an alarm actually fires
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if(context != null) {
                    AlarmRescheduleJobService.scheduleJob(context)
                }
            }
            Alarm.ACTION_ALARM -> {
                if (context != null) {
                    AlarmTriggeredJobService.scheduleJob(
                        context,
                        intent.getIntExtra("id", 0),
                        intent.getIntExtra("day", DaysOfTheWeek.None)
                    )
                }
            }
            Alarm.ACTION_STOP_ALARM -> {
                val id = if(intent.hasExtra("id")) intent.getIntExtra("id", 0) else null
                if(id != null && context != null) {
                    with(NotificationManagerCompat.from(context)) {
                        cancel(id)
                    }
                }

                // TODO: Stop alarm and update database
            }
            Alarm.ACTION_SNOOZE_ALARM -> {
                // TODO: Stop alarm and schedule snoozed alarm (5 minutes)
                // TODO: Update database
            }
        }
    }
}