package com.github.stefnotch.vibratingalarmclock

import android.content.Context
import android.os.PowerManager

class WakeLocker {
    companion object {
        val LOCK_TAG = BuildConfig.APPLICATION_ID + ":ALARM_TRIGGERED_WAKE_LOCK"
        var wakeLock: PowerManager.WakeLock? = null

        fun acquire(context: Context) {
            wakeLock?.release()

            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE, LOCK_TAG)
            wakeLock?.acquire(10*60*1000L /*10 minutes*/)
        }

        fun release() {
            wakeLock?.release()
            wakeLock = null
        }
    }
}