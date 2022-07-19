package com.example.noteapp.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import com.example.noteapp.R
import kotlinx.android.synthetic.main.recorder_dialog_layout.*

class RecorderDialog(context: Context, var recorderDialogListener: RecorderDialogListener)
    :AppCompatDialog(context){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.recorder_dialog_layout)


        anim_stop.setOnClickListener {
            recorderDialogListener.onStopRecorder()
            dismiss()
        }
        setOnCancelListener {
            recorderDialogListener.onStopRecorder()
            dismiss()
        }
    }
}