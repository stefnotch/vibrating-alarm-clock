package com.github.stefnotch.vibratingalarmclock.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.stefnotch.vibratingalarmclock.R
import com.github.stefnotch.vibratingalarmclock.application.App
import com.github.stefnotch.vibratingalarmclock.data.AlarmRepository
import com.github.stefnotch.vibratingalarmclock.data.DaysOfTheWeek
import kotlinx.coroutines.*

// I'm using jobs because https://stackoverflow.com/a/56534432
class AlarmTriggeredJobService: JobService() {
    companion object {
        private const val JOB_ID = 1

        fun scheduleJob(context: Context, alarmId: Int, day: Int) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(
                JobInfo.Builder(JOB_ID, ComponentName(context, AlarmTriggeredJobService::class.java))
                    .setExtras(PersistableBundle().apply { putInt("id", alarmId); putInt("day", day) })
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
        val alarmId = if(params?.extras?.containsKey("id") == true) params?.extras?.getInt("id", 0) else null
        val alarmDay = params?.extras?.getInt("day", DaysOfTheWeek.None) ?: DaysOfTheWeek.None
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