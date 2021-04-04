package com.github.stefnotch.vibratingalarmclock.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.github.stefnotch.vibratingalarmclock.ble.BleConnection
import com.github.stefnotch.vibratingalarmclock.data.Alarm
import com.github.stefnotch.vibratingalarmclock.data.DaysOfTheWeek
import com.github.stefnotch.vibratingalarmclock.service.AlarmRescheduleJobService
import com.github.stefnotch.vibratingalarmclock.service.AlarmSnoozeJobService
import com.github.stefnotch.vibratingalarmclock.service.AlarmTriggeredJobService
import java.time.LocalTime

class AlarmBroadcastReceiver : BroadcastReceiver() {

    // Gets called when the device boots or when an alarm actually fires
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if (context != null) {
                    AlarmRescheduleJobService.scheduleJob(context)
                }
            }
        }
        if(intent != null) {
            when (true) {
                Alarm.IS_ACTION_ALARM(intent.action) -> {
                    Log.d("AlarmBroadcastReceiver", "Alarm triggered")
                    if (context != null) {
                        Log.d("AlarmBroadcastReceiver", "Alarm vibration scheduled")
                        AlarmTriggeredJobService.scheduleJob(
                            context,
                            intent.getParcelableExtra<ParcelUuid>("id")?.uuid,
                            intent.getIntExtra("day", DaysOfTheWeek.None)
                        )
                    }
                }
                Alarm.IS_ACTION_STOP_ALARM(intent.action) -> {
                    val id = intent.getParcelableExtra<ParcelUuid>("id")?.uuid
                    if (id != null && context != null) {
                        with(NotificationManagerCompat.from(context)) {
                            cancel(id.toString(), 0)
                        }
                    }

                    // Alarm has happened and optionally, a new one (next week) has been scheduled
                    val ble = BleConnection.getInstance()
                    ble.stopVibrating()

                    // TODO: If the alarm is a snooze alarm, stop it now
                }
                Alarm.IS_ACTION_SNOOZE_ALARM(intent.action) -> {
                    val id = intent.getParcelableExtra<ParcelUuid>("id")?.uuid
                    if (id != null && context != null) {
                        with(NotificationManagerCompat.from(context)) {
                            cancel(id.toString(), 0)
                        }
                    }

                    val ble = BleConnection.getInstance()
                    ble.stopVibrating()

                    // Schedule snoozed alarm (9 minutes) and make sure to not interfere with the optional next week alarm
                    if (context != null) {
                        AlarmSnoozeJobService.scheduleJob(
                            context,
                            intent.getParcelableExtra<ParcelUuid>("id")?.uuid
                        )
                    }
                }
            }
        }
    }
}