package com.github.stefnotch.vibratingalarmclock.data

import android.app.Application
import android.content.Context
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

class AlarmRepository(context: Context) {
    private val alarmDao: AlarmDao

    init {
        val db: AppDatabase = AppDatabase.getInstance(context)
        alarmDao = db.alarmDao()
    }

    suspend fun get(id: Int): Alarm? {
        return alarmDao.get(id)
    }

    suspend fun insert(alarm: Alarm): Alarm {
        alarmDao.insert(alarm)

        return alarm
    }

    suspend fun delete(alarm: Alarm) {
        return alarmDao.delete(alarm)
    }

    suspend fun getAll(): List<Alarm> {
        return alarmDao.getAll()
    }

    suspend fun update(alarm: Alarm){
        return alarmDao.update(alarm)
    }
}