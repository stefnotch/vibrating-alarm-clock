package com.github.stefnotch.vibratingalarmclock.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.stefnotch.vibratingalarmclock.data.Alarm

class AlarmBroadcastReceiver: BroadcastReceiver() {

    // Gets called when the device boots or when an alarm actually fires
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> startRescheduleAlarmService(context)
            Alarm.ACTION_ALARM -> startAlarmService(context, intent)
        }
    }

    private fun startAlarmService(context: Context?, intent: Intent) {
        TODO("Not yet implemented")
    }

    private fun startRescheduleAlarmService(context: Context?) {
        TODO("Not yet implemented")
    }

}