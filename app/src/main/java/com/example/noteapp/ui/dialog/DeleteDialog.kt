package com.example.noteapp.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import com.example.noteapp.R
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.filter_layout.*

class DeleteDialog(context: Context, var deleteDialogListener: DeleteDialogListener)
    :AppCompatDialog(context){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.delete_dialog_layout)

        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        btn_ok_delete_dialog.setOnClickListener {
            deleteDialogListener.onPositiveClick()
            dismiss()
        }

        btn_cancel_delete_dialog.setOnClickListener {
            deleteDialogListener.onNegativeClick()
            dismiss()
        }

    }
}