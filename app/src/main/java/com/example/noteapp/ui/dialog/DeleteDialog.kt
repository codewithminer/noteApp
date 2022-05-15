package com.example.noteapp.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import com.example.noteapp.R
import kotlinx.android.synthetic.main.filter_layout.*

class DeleteDialog(context: Context, var filterDialogListener: FilterDialogListener, private val sortOption: Int)
    :AppCompatDialog(context){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.filter_layout)

        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        when(sortOption){
            1 ->{ radio_filter1.isChecked = true
                radio_filter2.isChecked = false
                radio_filter3.isChecked = false}
            2 -> { radio_filter2.isChecked = true
                radio_filter1.isChecked = false
                radio_filter3.isChecked = false}
            3 -> { radio_filter3.isChecked = true
                radio_filter1.isChecked = false
                radio_filter2.isChecked = false}
        }

        radio_filter1.setOnClickListener {
            filterDialogListener.filterOneSelected()
            dismiss()
        }

        radio_filter2.setOnClickListener {
            filterDialogListener.filterTwoSelected()
            dismiss()
        }

        radio_filter3.setOnClickListener {
            filterDialogListener.filterThreeSelected()
            dismiss()
        }
    }
}