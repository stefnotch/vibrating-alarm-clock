package com.github.stefnotch.vibratingalarmclock.service

import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.os.PersistableBundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.stefnotch.vibratingalarmclock.AlarmTriggeredActivity
import com.github.stefnotch.vibratingalarmclock.R
import com.github.stefnotch.vibratingalarmclock.application.App
import com.github.stefnotch.vibratingalarmclock.ble.BleConnection
import com.github.stefnotch.vibratingalarmclock.broadcastreceiver.AlarmBroadcastReceiver
import com.github.stefnotch.vibratingalarmclock.data.Alarm
import com.github.stefnotch.vibratingalarmclock.data.AlarmRepository
import com.github.stefnotch.vibratingalarmclock.data.DaysOfTheWeek
import kotlinx.coroutines.*
import java.time.LocalTime
import java.util.*

class AlarmSnoozeJobService : JobService() {
    companion object {
        private const val JOB_ID = 3

        fun scheduleJob(context: Context, alarmId: UUID?) {
            if(alarmId == null) return

            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(
                JobInfo.Builder(JOB_ID, ComponentName(context, AlarmTriggeredJobService::class.java))
                    .setExtras(PersistableBundle().apply { putString("id", alarmId.toString()); })
                    .setOverrideDeadline(0)
                    .build()
            )
        }

        fun cancelJob(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(JOB_ID)
        }
    }

    private var restartOnDestroy = true
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onStartJob(params: JobParameters?): Boolean {
        val alarmId: UUID? = params?.extras?.getString("id")?.let { UUID.fromString(it) }
        if(alarmId != null) {
            serviceScope.launch {
                withContext(Dispatchers.IO) {
                    val alarmRepository = AlarmRepository(applicationContext)

                    val snoozeAlarm = Alarm(LocalTime.now().plusMinutes(9))
                    snoozeAlarm.isSnooze = true
                    snoozeAlarm.scheduleAlarm(applicationContext)

                    alarmRepository.insert(snoozeAlarm)

                    restartOnDestroy = false
                    jobFinished(params, false)
                }
            }
            return true
        } else {
            return false
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        serviceScope.cancel()
        return restartOnDestroy
    }
}