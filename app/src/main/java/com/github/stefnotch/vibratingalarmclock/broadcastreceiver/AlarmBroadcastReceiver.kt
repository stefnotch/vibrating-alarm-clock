package com.github.stefnotch.vibratingalarmclock.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.stefnotch.vibratingalarmclock.data.Alarm
import com.github.stefnotch.vibratingalarmclock.service.AlarmRescheduleService
import com.github.stefnotch.vibratingalarmclock.service.AlarmTriggeredService

class AlarmBroadcastReceiver: BroadcastReceiver() {

    // Gets called when the device boots or when an alarm actually fires
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> context?.startForegroundService(Intent(context, AlarmRescheduleService::class.java))
            Alarm.ACTION_ALARM -> {
                val alarmTriggerIntent = Intent(context, AlarmTriggeredService::class.java)
                alarmTriggerIntent.putExtra("id", intent.getStringExtra("id"))
                context?.startForegroundService(alarmTriggerIntent)
            }
        }
    }
}