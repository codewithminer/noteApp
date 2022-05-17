package com.example.noteapp.model.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "note_tb")
data class Note(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val content: String,
    val color_index: Int,
    var alarm_id: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    var isLock: Boolean
): Serializable
