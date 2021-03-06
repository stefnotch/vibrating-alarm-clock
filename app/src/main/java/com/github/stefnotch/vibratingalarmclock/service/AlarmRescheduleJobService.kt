package com.github.stefnotch.vibratingalarmclock.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import com.github.stefnotch.vibratingalarmclock.data.AlarmRepository
import kotlinx.coroutines.*

class AlarmRescheduleJobService: JobService() {
    companion object {
        private const val JOB_ID = 2

        fun scheduleJob(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(
                JobInfo.Builder(JOB_ID, ComponentName(context, AlarmTriggeredJobService::class.java))
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
        serviceScope.launch {
            withContext(Dispatchers.IO) {
                val alarmRepository = AlarmRepository(applicationContext)
                alarmRepository.getAll().forEach {
                    if(it.isRunning) {
                        it.scheduleAlarm(applicationContext)
                    }
                }

                restartOnDestroy = false
                jobFinished(params, false)
            }
        }

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        serviceScope.cancel()
        return restartOnDestroy
    }
}