package com.github.stefnotch.vibratingalarmclock.data

import com.github.stefnotch.vibratingalarmclock.data.Alarm
import androidx.lifecycle.LiveData
import androidx.room.*
import android.database.Cursor;

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms WHERE id=:id ")
    fun get(id: String): Alarm?

    @Insert
    fun insert(alarm: Alarm): Long

    @Delete
    fun delete(alarm: Alarm)

    @Query("SELECT * FROM alarms ORDER BY title ASC")
    fun getAll(): List<Alarm>

    @Update
    fun update(alarm: Alarm)
}