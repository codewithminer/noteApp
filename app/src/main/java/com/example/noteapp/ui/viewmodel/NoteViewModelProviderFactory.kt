package com.example.noteapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp.model.data.repository.AlarmRepository
import com.example.noteapp.model.data.repository.NoteRepository

class NoteViewModelProviderFactory(
    private val noteRep: NoteRepository,
    private val alarmRep: AlarmRepository
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteViewModel(noteRep,alarmRep) as T
    }
}