package com.example.noteapp.ui.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.example.noteapp.R
import kotlinx.android.synthetic.main.toolbar.view.*

class NoteToolbar(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    var onBackButtonClickListener: View.OnClickListener? = null
        set(value) {
            field=value
            backBtn.setOnClickListener(onBackButtonClickListener)
        }
    init {
        inflate(context, R.layout.toolbar, this)

        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.toolbar)
            val title = a.getString(R.styleable.toolbar_mtitle)
            if (title != null && title.isNotEmpty())
                toolbarTitleTv.text = title

            a.recycle()
        }
    }
}