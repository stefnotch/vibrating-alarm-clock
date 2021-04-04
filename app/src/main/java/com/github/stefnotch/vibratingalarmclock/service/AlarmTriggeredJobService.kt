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
import java.util.*

// I'm using jobs because https://stackoverflow.com/a/56534432
class AlarmTriggeredJobService: JobService() {
    companion object {
        private const val JOB_ID = 1

        fun scheduleJob(context: Context, alarmId: UUID?, day: Int) {
            if(alarmId == null) return

            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(
                JobInfo.Builder(JOB_ID, ComponentName(context, AlarmTriggeredJobService::class.java))
                    .setExtras(PersistableBundle().apply { putString("id", alarmId.toString()); putInt("day", day) })
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
        val alarmDay = params?.extras?.getInt("day", DaysOfTheWeek.None) ?: DaysOfTheWeek.None
        if(alarmId != null) {
            serviceScope.launch {
                withContext(Dispatchers.IO) {
                    val alarmRepository = AlarmRepository(applicationContext)
                    val alarm = alarmRepository.get(alarmId)
                    // TODO: Probably write a log message when stuff is null

                    val stopAlarmIntent = Intent(applicationContext, AlarmBroadcastReceiver::class.java).apply {
                        action = if (alarm != null) Alarm.ACTION_STOP_ALARM(alarm) else ""
                        putExtra("id", ParcelUuid(alarmId))
                    }

                    val snoozeAlarmIntent = Intent(applicationContext, AlarmBroadcastReceiver::class.java).apply {
                        action = if (alarm != null) Alarm.ACTION_SNOOZE_ALARM(alarm) else ""
                        putExtra("id", ParcelUuid(alarmId))
                    }

                    val fullscreenAlarmIntent = Intent(applicationContext, AlarmTriggeredActivity::class.java).apply {
                        action = if (alarm != null) Alarm.ACTION_ALARM_TRIGGERED(alarm) else ""
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra("id", ParcelUuid(alarmId))
                    }

                    // TODO: Hopefully its not https://stackoverflow.com/a/40421304
                    val notification = NotificationCompat.Builder(applicationContext, App.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentTitle("Snooze alarm")
                        .setContentText(
                            alarm?.getFormattedTime(applicationContext) + " " + (alarm?.title
                                ?: "Alarm with $alarmId not found")
                        )
                        .addAction(
                            R.drawable.ic_baseline_alarm_off_24,
                            "Stop Alarm",
                            PendingIntent.getBroadcast(
                                applicationContext,
                                0,
                                stopAlarmIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        )
                        .setFullScreenIntent(
                            PendingIntent.getActivity(
                                applicationContext,
                                0,
                                fullscreenAlarmIntent,
                                PendingIntent.FLAG_ONE_SHOT
                            ), true
                        )
                        .setDeleteIntent(PendingIntent.getBroadcast(
                            applicationContext,
                            0,
                            snoozeAlarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        ))
                        .setOngoing(true)
                        .setAutoCancel(true)
                        .build()

                    with(NotificationManagerCompat.from(applicationContext)) {
                        notify(alarmId.toString(),0, notification)
                    }


                    val ble = BleConnection.getInstance()
                    ble.startVibrating()

                    if (alarm?.isRecurring == true && alarmDay != DaysOfTheWeek.None) {
                        alarm.scheduleAlarmForNextDay(applicationContext, alarmDay)
                    } else if (alarm != null) {
                        alarm.isRunning = false
                        alarmRepository.update(alarm)
                    }
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