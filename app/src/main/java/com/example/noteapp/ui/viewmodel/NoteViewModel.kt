package com.example.noteapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aminography.primecalendar.common.operators.DayOfMonth
import com.aminography.primecalendar.common.operators.plusAssign
import com.aminography.primecalendar.persian.PersianCalendar
import com.example.noteapp.model.data.Alarm
import com.example.noteapp.model.data.CheckBoxContent
import com.example.noteapp.model.data.DateModel
import com.example.noteapp.model.data.Note
import com.example.noteapp.model.data.repository.AlarmRepository
import com.example.noteapp.model.data.repository.NoteRepository
import com.example.noteapp.utils.setPersianNumber
import kotlinx.coroutines.*

class NoteViewModel(
    private val noteRepository: NoteRepository,
    private val alarmRepository: AlarmRepository,
) : ViewModel() {

    private var notes: LiveData<List<Note>>
    private var alarms: LiveData<List<Alarm>>

    var isEmpty: MutableLiveData<Boolean> = MutableLiveData()
    var checkBoxContent = arrayListOf<CheckBoxContent>()
    var boxCountCheck: MutableLiveData<Int> = MutableLiveData()
    var contentsChange: MutableLiveData<Boolean> = MutableLiveData()

    var alarmId: MutableLiveData<Int> = MutableLiveData()
    var noteId: MutableLiveData<Int> = MutableLiveData()

    var selectedItemToDelete: MutableLiveData<Int> = MutableLiveData()
    private var selectedSortOption: MutableLiveData<Int> = MutableLiveData()

    init {
        contentsChange.value = false
        isEmpty.value = true
        selectedSortOption.value = 2
        notes = noteRepository.getAllNotes()
        alarms = alarmRepository.getAllAlarms()
    }

    fun getNotesForUI(): LiveData<List<Note>> = notes
    fun getEmptyState(): LiveData<Boolean> = isEmpty

    fun saveNote(note: Note) = viewModelScope.launch {
        insertNote(note)
    }

    fun saveAlarm(alarm: Alarm) = viewModelScope.launch {
        insertAlarm(alarm)
    }

    fun deleteNote(id: String) = viewModelScope.launch {
        removeNote(id)
    }

    fun deleteAlarm(id: Int) = viewModelScope.launch {
        removeAlarm(id)
    }

    private suspend fun insertNote(note: Note) {
        noteRepository.insertNote(note)
    }

    private suspend fun removeNote(id: String) {
            noteRepository.deleteNote(id)
    }

    fun getNote(id: String): LiveData<Note> = noteRepository.getNote(id)

    fun searchNote(searchedText: String, isLock: Boolean): LiveData<List<Note>> {
        return noteRepository.searchNote(searchedText, isLock)
    }

    private suspend fun insertAlarm(alarm: Alarm) {
        alarmId.postValue(alarmRepository.insertAlarm(alarm).toInt())
    }

    private suspend fun removeAlarm(id: Int) {
        alarmRepository.deleteAlarm(id)
    }

    fun getAlarmFromRoom(id: Int) = alarmRepository.getAlarm(id)

    fun getNotesByDateLatest() = noteRepository.getNotesByDateLatest()
    fun getNotesByDateOldest() = noteRepository.getNotesByDateOldest()
    fun getNotesByColor() = noteRepository.getNotesByColor()

    fun getNumberOfItemsSelectedToDelete() = selectedItemToDelete
    fun setNumberOfItemsSelectedToDelete(value: Int) {
        selectedItemToDelete.value = value
    }

    fun getSelectedSortOption() = selectedSortOption
    fun setSelectedSortOption(value: Int){
        selectedSortOption.value = value
    }


//    private fun resetExpireReminders() = viewModelScope.launch {
//        val reminders = alarmRepository.getAllAlarms().value
//        if (!reminders.isNullOrEmpty()) {
//            val currentDate = PersianCalendar()
//            val reminderDate = PersianCalendar()
//            for (i in reminders.indices) {
//                reminderDate.year = reminders[i].year
//                reminderDate.month = reminders[i].month
//                reminderDate.dayOfMonth = reminders[i].day
//                reminderDate.hourOfDay = reminders[i].hour
//                reminderDate.minute = reminders[i].minute
//                if (reminderDate <= currentDate) {
//                    val note = noteRepository.getNoteById(reminders[i].id!!)
//                    note.alarm_id = -1
//                    insertNote(note)
//                    alarmRepository.deleteAlarm(reminders[i])
//                }
//            }
//        }
//    }


    fun createDate(): MutableList<DateModel> {
        val list = mutableListOf<DateModel>()
        val persian = PersianCalendar()

        val y = persian.year + 1
        var counterMonth = persian.month
        for (i in persian.year..y) {
            for (j in counterMonth..11) {
                persian.month = j
                val e = persian.monthLength - persian.dayOfMonth
                for (k in persian.dayOfMonth..persian.monthLength) {
                    persian.dayOfMonth = k
                    Log.i(
                        "date",
                        setPersianNumber(k.toString()) + ", " + persian.weekDayNameShort + ", " + persian.monthName + ", " + i
                    )
                    list += DateModel(
                        setPersianNumber(i.toString()),
                        persian.monthName,
                        persian.month,
                        setPersianNumber(k.toString()),
                        persian.weekDayNameShort
                    )
                }
                persian.dayOfMonth -= e
                persian += DayOfMonth(e + 1)
                counterMonth++
            }
            counterMonth = 0
        }
        return list
    }
}