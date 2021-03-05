package com.github.stefnotch.vibratingalarmclock.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.github.stefnotch.vibratingalarmclock.data.AlarmRepository
import kotlinx.coroutines.*

/**
 * Gets called upon boot, reschedules every single alarm
 */
class AlarmRescheduleService: Service() {
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        serviceScope.launch {
            withContext(Dispatchers.IO) {
                val alarmRepository = AlarmRepository(applicationContext)
                alarmRepository.getAll().forEach {
                    if(it.isRunning) {
                        it.scheduleAlarm(applicationContext)
                    }
                }
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}