package com.example.noteapp.model.data.repository

import com.example.noteapp.model.data.Note
import com.example.noteapp.model.data.db.NoteDatabase

class NoteRepository(
    val db: NoteDatabase
) {

    suspend fun insertNote(note: Note) = db.noteDao().insertNote(note)
    suspend fun deleteNote(id: Int) = db.noteDao().deleteNote(id)
    fun getAllNotes() = db.noteDao().getAllNotes()
    fun getNote(id: Int) = db.noteDao().getNote(id)
    fun searchNote(searchedText: String) = db.noteDao().searchNote(searchedText)
    fun getNoteById(id: Int): Note = db.noteDao().searchNoteByAlarmId(id)
    fun getNotesByDateLatest() = db.noteDao().getNotesByDateLatest()
    fun getNotesByDateOldest() = db.noteDao().getNotesByDateOldest()
    fun getNotesByColor() = db.noteDao().getNotesByColor()
}