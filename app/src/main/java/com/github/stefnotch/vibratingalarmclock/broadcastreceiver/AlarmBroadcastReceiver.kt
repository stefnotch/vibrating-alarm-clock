package com.github.stefnotch.vibratingalarmclock.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
        }
    }
}