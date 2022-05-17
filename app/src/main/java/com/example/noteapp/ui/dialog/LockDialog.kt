package com.example.noteapp.ui.dialog

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.example.noteapp.R
import kotlinx.android.synthetic.main.lock_input_design_layout.*
import kotlinx.android.synthetic.main.lock_dialog_layout.*


class LockDialog(context: Context, val lockDialogListener: LockDialogListener, val state: Int) :
    AppCompatDialog(context) {
    // state 1 -> create LOCK
    // state 2 -> change LOCK
    // state 3 -> enter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var changePasswordState = 0
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.lock_dialog_layout)

        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        when (state) {
            1 -> tv_lock.text = context.getString(R.string.createLock)
            2 -> tv_lock.text = context.getString(R.string.preChangeLock)
            3 -> tv_lock.text = context.getString(R.string.accessLock)
            4 -> tv_lock.text = context.getString(R.string.removeLock)
            5 -> tv_lock.text = context.getString(R.string.removeLockedNote)
        }

        lock_number_one.requestFocus()
        showKeyboard()

        lock_number_one.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (lock_number_one.text.toString().length == 1)
                        lock_number_two.requestFocus()
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            }
        )

        lock_number_two.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (lock_number_two.text.toString().length == 1)
                        lock_number_three.requestFocus()
                    else
                        lock_number_one.requestFocus()

                }

                override fun afterTextChanged(p0: Editable?) {
                }

            }
        )

        lock_number_three.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (lock_number_three.text.toString().length == 1)
                        lock_number_four.requestFocus()
                    else
                        lock_number_two.requestFocus()
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            }
        )

        lock_number_four.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (
                        lock_number_four.text.toString().length == 1 &&
                        lock_number_three.text.toString().length == 1 &&
                        lock_number_two.text.toString().length == 1 &&
                        lock_number_one.text.toString().length == 1
                    ) {
                        val password = "${lock_number_one.text}${lock_number_two.text}" +
                                "${lock_number_three.text}${lock_number_four.text}"
                        if (state == 1) {
                            storePassword(password)
                            lockDialogListener.onCreateLock(password)
                            dismiss()
                            closeKeyboard()
                        } else if (state == 2) {
                            if (changePasswordState == 0) {
                                val sharedPreferences: SharedPreferences =
                                    context.getSharedPreferences("LOCK", Context.MODE_PRIVATE)
                                val lockValue = sharedPreferences.getString("lock_value", "")
                                if (lockValue == password) {
                                    tv_lock.text = context.getString(R.string.createLock)
                                    lock_number_one.setText("")
                                    lock_number_two.setText("")
                                    lock_number_three.setText("")
                                    lock_number_four.setText("")
                                    lock_number_one.requestFocus()
                                    changePasswordState = 1
                                }
                            } else if (changePasswordState == 1) {
                                storePassword(password)
                                lockDialogListener.onChangeLock(password)
                                dismiss()
                                closeKeyboard()
                            }
                        } else if (state == 3) {
                            val sharedPreferences: SharedPreferences =
                                context.getSharedPreferences("LOCK", Context.MODE_PRIVATE)
                            val lockValue = sharedPreferences.getString("lock_value", "")
                            Toast.makeText(context, lockValue, Toast.LENGTH_SHORT).show()
                            if (lockValue == password) {
                                lockDialogListener.onAccessNote()
                                dismiss()
                                closeKeyboard()
                            }
                        } else if (state == 4 || state == 5) {
                            val sharedPreferences: SharedPreferences =
                                context.getSharedPreferences("LOCK", Context.MODE_PRIVATE)
                            val lockValue = sharedPreferences.getString("lock_value", "")
                            if (lockValue == password) {
                                lockDialogListener.onRemoveLock()
                                dismiss()
                                closeKeyboard()
                            }
                        }
                    } else
                        lock_number_three.requestFocus()

                }

                override fun afterTextChanged(p0: Editable?) {
                }

            }
        )

//        lock_number_two.setOnKeyListener { view, keyCode, keyEvent ->
//            if (keyCode == 67)
//                lock_number_one.requestFocus()
//            false
//        }
//
//        lock_number_three.setOnKeyListener { view, keyCode, keyEvent ->
//            if (keyCode == KeyEvent.KEYCODE_DEL)
//                lock_number_two.requestFocus()
//            false
//        }
//
//        lock_number_four.setOnKeyListener { view, keyCode, keyEvent ->
//            if (keyCode == KeyEvent.KEYCODE_DEL)
//                lock_number_three.requestFocus()
//            false
//        }


    }

    private fun storePassword(password: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("LOCK", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("lock_value", password)
        editor.apply()
    }

    fun showKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    fun closeKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }
}