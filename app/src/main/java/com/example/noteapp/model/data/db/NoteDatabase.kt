package com.example.noteapp.model.data.db

import android.content.Context
import androidx.room.*
import com.example.noteapp.model.data.Alarm
import com.example.noteapp.model.data.Note
import com.example.noteapp.model.data.repository.AlarmDao
import com.example.noteapp.model.data.repository.NoteDao

@Database(
    entities = [Note::class, Alarm::class],
    version = 1
)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var instance: NoteDatabase? = null
        private var LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                NoteDatabase::class.java,
                "note_db"
            ).build()
    }
}