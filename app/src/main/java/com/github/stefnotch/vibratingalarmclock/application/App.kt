package com.github.stefnotch.vibratingalarmclock.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class App: Application() {

    companion object {
        val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Alarm Service Channel", NotificationManager.IMPORTANCE_DEFAULT)
            .apply { description = "Vibrating Alarm Clock Notifications" } // Maybe it should be more important?

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(serviceChannel)
    }
}