package com.github.stefnotch.vibratingalarmclock.application

import android.app.Application
import android.app.Notification
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
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Alarm Service Channel", NotificationManager.IMPORTANCE_HIGH)
            .apply {
                description = "Vibrating Alarm Clock Notifications"
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(serviceChannel)
    }
}