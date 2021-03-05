package com.github.stefnotch.vibratingalarmclock.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class App: Application() {

    companion object {
        val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(CHANNEL_ID, "Alarm Service Channel", NotificationManager.IMPORTANCE_DEFAULT) // Maybe it should be more important?

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(serviceChannel)
    }
}