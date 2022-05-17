package com.example.noteapp.ui.dialog

interface LockDialogListener {
    fun onCreateLock(password: String)
    fun onChangeLock(password: String)
    fun onAccessNote()
    fun onRemoveLock()
}