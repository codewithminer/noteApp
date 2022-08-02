package com.example.noteapp.ui.dialog

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import com.example.noteapp.R
import kotlinx.android.synthetic.main.image_dialog.*

class ImageDialog(
    context: Context,
    val bitmap: Bitmap
):AppCompatDialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.image_dialog)
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        img_dialog.setImageBitmap(bitmap)
    }
}