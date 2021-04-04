package com.github.stefnotch.vibratingalarmclock.data

import com.github.stefnotch.vibratingalarmclock.data.Alarm
import androidx.lifecycle.LiveData
import androidx.room.*
import android.database.Cursor;
import java.util.*

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms WHERE id=:id ")
    suspend fun get(id: UUID): Alarm?

    @Insert
    suspend fun insert(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("SELECT * FROM alarms ORDER BY title ASC")
    suspend fun getAll(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE isSnooze=0 ORDER BY title ASC")
    suspend fun getAllNonSnoozed(): List<Alarm>

    @Query("SELECT * FROM alarms WHERE isSnooze=1 ORDER BY title ASC")
    suspend fun getAllSnoozed(): List<Alarm>

    @Update
    suspend fun update(alarm: Alarm)
}