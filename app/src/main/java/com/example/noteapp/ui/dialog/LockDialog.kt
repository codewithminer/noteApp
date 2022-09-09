package com.example.noteapp.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.example.noteapp.R
import com.example.noteapp.utils.LockStates
import com.example.noteapp.utils.hideKeyboard
import kotlinx.android.synthetic.main.lock_input_design_layout.*
import kotlinx.android.synthetic.main.lock_dialog_layout.*


class LockDialog(
    context: Context,
    val lockDialogListener: LockDialogListener,
    val state: LockStates,
    val isCancelable: Boolean = true
) :
    AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var changePasswordState = 0  // 0 -> enter previous password, 1 -> enter new password
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.lock_dialog_layout)

        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
//        this.setCancelable(isCancellable)
        this.setCanceledOnTouchOutside(isCancelable)
        this.setOnCancelListener {
            if (!isCancelable)
                (context as Activity).finish()
        }
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        when (state) {
            LockStates.CreateLock -> tv_lock.text = context.getString(R.string.createLock)
            LockStates.ChangeLock -> tv_lock.text = context.getString(R.string.preChangeLock)
            LockStates.EnterNote -> tv_lock.text = context.getString(R.string.accessLock)
            LockStates.RemoveLock -> tv_lock.text = context.getString(R.string.removeLock)
            LockStates.RemoveLockedNote -> tv_lock.text =
                context.getString(R.string.removeLockedNote)
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
                        val password = getEnteredPassword()
                        val lockValue = getStoredPassword()

                        if (state == LockStates.CreateLock) {
                            storePassword(password)
                            lockDialogListener.onCreateLock(password)
                            closeKeyboard()
                            dismiss()
                        } else if (state == LockStates.ChangeLock) {
                            if (changePasswordState == 0) {
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
                                closeKeyboard()
                                dismiss()
                            }
                        } else if (state == LockStates.EnterNote) {
                            if (lockValue == password) {
                                lockDialogListener.onAccessNote()
                                closeKeyboard()
                                dismiss()
                            }
                        } else if (state == LockStates.RemoveLock || state == LockStates.RemoveLockedNote) {
                            if (lockValue == password) {
                                lockDialogListener.onRemoveLock()
                                closeKeyboard()
                                dismiss()
                            }
                        }
                    } else
                        lock_number_three.requestFocus()

                }

                override fun afterTextChanged(p0: Editable?) {
                }
            }
        )
        setOnDismissListener {
            closeKeyboard()
        }

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

    private fun getStoredPassword(): String {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("LOCK", Context.MODE_PRIVATE)
        return sharedPreferences.getString("lock_value", "").toString()
    }

    private fun getEnteredPassword(): String {
        return "${lock_number_one.text}${lock_number_two.text}" +
                "${lock_number_three.text}${lock_number_four.text}"
    }

    private fun storePassword(password: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("LOCK", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("lock_value", password)
        editor.apply()
    }

    private fun showKeyboard() {
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