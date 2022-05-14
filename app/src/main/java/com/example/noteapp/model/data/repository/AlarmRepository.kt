package com.example.noteapp.model.data.repository

import com.example.noteapp.model.data.Alarm
import com.example.noteapp.model.data.db.NoteDatabase

class AlarmRepository(
    val db: NoteDatabase
) {

    suspend fun insertAlarm(alarm: Alarm): Long = db.alarmDao().insertAlarm(alarm)
    suspend fun deleteAlarm(id: Int) = db.alarmDao().deleteAlarm(id)
    fun getAllAlarms() = db.alarmDao().getAllAlarms()
    fun getAlarm(id: Int) = db.alarmDao().getAlarm(id)
}