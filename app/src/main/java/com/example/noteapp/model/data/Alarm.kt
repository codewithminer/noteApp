package com.example.noteapp.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_tb")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
)
