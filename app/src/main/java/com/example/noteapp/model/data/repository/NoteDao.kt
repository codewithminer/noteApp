package com.example.noteapp.model.data.repository

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.noteapp.model.data.Note

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("DELETE FROM note_tb WHERE id=:id")
    suspend fun deleteNote(id: Int)

    @Query("SELECT * FROM note_tb")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM note_tb WHERE id=:id")
    fun getNote(id: Int): LiveData<Note>

    @Query("SELECT * FROM note_tb WHERE content LIKE :searchedText and isLock=0")
    fun searchNote(searchedText: String): LiveData<List<Note>>

    @Query("SELECT * FROM note_tb WHERE content LIKE :searchedText")
    fun searchAllNote(searchedText: String): LiveData<List<Note>>

    @Query("SELECT * FROM note_tb WHERE alarm_id=:id")
    fun searchNoteByAlarmId(id: Int): Note

    @Query("SELECT * FROM note_tb ORDER BY year desc, month desc, day desc, hour desc, minute desc")
    fun getNotesByDateLatest(): LiveData<List<Note>>

    @Query("SELECT * FROM note_tb ORDER BY year asc, month asc, day asc, hour asc, minute asc")
    fun getNotesByDateOldest(): LiveData<List<Note>>

    @Query("SELECT * FROM note_tb ORDER BY color_index")
    fun getNotesByColor(): LiveData<List<Note>>
}