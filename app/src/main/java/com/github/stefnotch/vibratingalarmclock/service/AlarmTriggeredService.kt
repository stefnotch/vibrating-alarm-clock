package com.github.stefnotch.vibratingalarmclock.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.stefnotch.vibratingalarmclock.R
import com.github.stefnotch.vibratingalarmclock.application.App
import com.github.stefnotch.vibratingalarmclock.data.AlarmRepository
import com.github.stefnotch.vibratingalarmclock.data.DaysOfTheWeek
import kotlinx.coroutines.*

class AlarmTriggeredService: Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val alarmId = if(intent?.hasExtra("id") == true) intent?.getIntExtra("id", 0) else null
        val alarmDay = intent?.getIntExtra("day", DaysOfTheWeek.None) ?: DaysOfTheWeek.None
        if(alarmId != null) {
            serviceScope.launch {
                withContext(Dispatchers.IO) {
                    val alarmRepository = AlarmRepository(applicationContext)
                    val alarm = alarmRepository.get(alarmId)

                    val notification = NotificationCompat.Builder(applicationContext, App.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentTitle("Snooze alarm" )
                        .setContentText(alarm?.title ?: "Alarm not found")
                        .setContentInfo(alarm?.getFormattedTime(applicationContext))
                        .setAutoCancel(true)
                        .build()

                    with(NotificationManagerCompat.from(applicationContext)) {
                        notify(alarmId, notification)
                    }

                    // TODO: Do something when you click on it (snooze)
                    // TODO: Stop butt-on

                    if(alarm?.isRecurring == true && alarmDay != DaysOfTheWeek.None) {
                        alarm.scheduleAlarmForNextDay(applicationContext, alarmDay)
                    }
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}