package com.github.stefnotch.vibratingalarmclock.data

import com.github.stefnotch.vibratingalarmclock.data.Alarm
import androidx.lifecycle.LiveData
import androidx.room.*
import android.database.Cursor;

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms WHERE id=:id ")
    suspend fun get(id: Int): Alarm?

    @Insert
    suspend fun insert(alarm: Alarm): Long

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("SELECT * FROM alarms ORDER BY title ASC")
    suspend fun getAll(): List<Alarm>

    @Update
    suspend fun update(alarm: Alarm)
}