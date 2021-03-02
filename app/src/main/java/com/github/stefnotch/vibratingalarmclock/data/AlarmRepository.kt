package com.github.stefnotch.vibratingalarmclock.data

import android.app.Application
import android.content.Context
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

class AlarmRepository {
    private lateinit var alarmDao: AlarmDao

    fun AlarmRepository(context: Context) {
        val db: AppDatabase = AppDatabase.getInstance(context)
        alarmDao = db.alarmDao()
    }

    fun get(id: String): Alarm? {
        return alarmDao.get(id)
    }

    fun insert(alarm: Alarm): Long{
        return alarmDao.insert(alarm)
    }

    fun delete(alarm: Alarm) {
        return alarmDao.delete(alarm)
    }

    fun getAll(): List<Alarm> {
        return alarmDao.getAll()
    }

    fun update(alarm: Alarm){
        return alarmDao.update(alarm)
    }
}