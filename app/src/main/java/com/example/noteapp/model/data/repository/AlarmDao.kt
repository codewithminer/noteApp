package com.example.noteapp.model.data.repository

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.noteapp.model.data.Alarm

@Dao
interface AlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long

    @Query("DELETE FROM alarm_tb WHERE id=:id")
    suspend fun deleteAlarm(id: Int)

    @Query("SELECT * FROM alarm_tb")
    fun getAllAlarms(): LiveData<List<Alarm>>

    @Query("SELECT * FROM alarm_tb WHERE id=:id")
    fun getAlarm(id: Int): LiveData<Alarm>

}