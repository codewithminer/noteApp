package com.example.noteapp.ui.fragment

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.aminography.primecalendar.persian.PersianCalendar
import com.example.noteapp.ui.MainActivity
import com.example.noteapp.R
import com.example.noteapp.adapter.CheckListAdapter
import com.example.noteapp.adapter.ReminderAdapter
import com.example.noteapp.model.data.Alarm
import com.example.noteapp.model.data.CheckBoxContent
import com.example.noteapp.model.data.DateModel
import com.example.noteapp.model.data.Note
import com.example.noteapp.receiver.AlarmReceiver
import com.example.noteapp.ui.dialog.LockDialog
import com.example.noteapp.ui.dialog.LockDialogListener
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.add_box_layout.*
import kotlinx.android.synthetic.main.back_dialog_layout.*
import kotlinx.android.synthetic.main.color_dialog_layout.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.fragment_checklist.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.android.synthetic.main.fragment_note.appBarLayout
import kotlinx.android.synthetic.main.fragment_note.btn_add_box
import kotlinx.android.synthetic.main.fragment_note.btn_color
import kotlinx.android.synthetic.main.fragment_note.btn_delete
import kotlinx.android.synthetic.main.fragment_note.btn_reminder
import kotlinx.android.synthetic.main.fragment_note.navigation_todo
import kotlinx.android.synthetic.main.fragment_note.todo_toolbar
import kotlinx.android.synthetic.main.reminder.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class CheckListFragment: Fragment(R.layout.fragment_checklist) {

    private val args: NoteFragmentArgs by navArgs()
    lateinit var noteViewModel: NoteViewModel
    lateinit var reminderAdapter: ReminderAdapter
    lateinit var checkListAdapter: CheckListAdapter
    lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    lateinit var calendar: Calendar
    private var colorIndex = 1
    private var noteID: String = "-1"
    private var alarmID: Int = -1
    private var isChange = false
    private var isAlarmChanged = false
    private var isLock = false
    private lateinit var primaryAlarm: Alarm
    private var primaryText = ""
    var test = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noteViewModel = (activity as MainActivity).noteViewModel
        noteID = args.id.toString()
        if (noteID == "-1")
            noteID = UUID.randomUUID().toString()
        checkListAdapter = CheckListAdapter(noteViewModel)
        setUpRecycler()
        Log.i("time", "primary noteId is $noteID")
        noteViewModel.getNote(args.id.toString()).observe(viewLifecycleOwner, Observer { note ->
            note?.let {
                noteID = note.id
                primaryText = note.content
                colorIndex = note.color_index
                isLock = note.isLock
                if (note.alarm_id != -1)
                    getAlarm(note.alarm_id)
                setLockIcon()
//                noteViewModel.checkBoxContent = convertTextToCheckBox(note.content)
                val t = convertTextToCheckBox(note.content)
                for (i in 0 until t.size){
                    noteViewModel.checkBoxContent.add(CheckBoxContent(t[i].content, t[i].check))
                    test++
                    checkListAdapter.differ.submitList(noteViewModel.checkBoxContent)
                }
                Toast.makeText(requireContext(), test.toString(), Toast.LENGTH_SHORT).show()
                setUpNoteFragmentDesign(colorIndex)
            }
        })

        hideKeyboard()
        noteViewModel.alarmId.observe(viewLifecycleOwner, Observer {
            alarmID = it
            setAlarmIcon()
            Log.i("viewmodel", "change alarmId to $alarmID")
        })

        noteViewModel.noteId.observe(viewLifecycleOwner, Observer {
            noteID = it.toString()
        })

        createNotificationChannel()

        btn_color.setOnClickListener {
            showColorListDialog()
        }

        tv_save.setOnClickListener {
            saveNote()
            view.let { activity?.hideKeyboard(it) }
        }

        add_item.setOnClickListener {
            noteViewModel.checkBoxContent.add(CheckBoxContent("hello, dear", false))
            checkListAdapter.changeIndex(colorIndex)
            checkListAdapter.differ.submitList(noteViewModel.checkBoxContent)
//            checkListAdapter.notifyDataSetChanged()
            checkListAdapter.notifyItemInserted(test)
            test++
            if (noteViewModel.checkBoxContent.size>0){
                tv_save.visibility = View.VISIBLE
            }
            isChange = true
        }

        noteViewModel.boxCountCheck.observe(viewLifecycleOwner, Observer {
            if (noteViewModel.checkBoxContent.size<1)
                tv_save.visibility = View.GONE
            isChange = true
        })

        noteViewModel.contentsChange.observe(viewLifecycleOwner, Observer {
            if (it){
                tv_save.visibility = View.VISIBLE
                isChange = true
            }else{
                tv_save.visibility = View.GONE
            }
        })

        ic_lock_toolbar.setOnClickListener {
            LockDialog(requireContext(), object : LockDialogListener {
                override fun onCreateLock(password: String) {
                }

                override fun onChangeLock(password: String) {
                }

                override fun onAccessNote() {
                }

                override fun onRemoveLock() {
                    isChange = true
                    isLock = false
                    setLockIcon()
//                    view.let { activity?.hideKeyboard(it) }
                    Toast.makeText(requireContext(), "password REMOVED.", Toast.LENGTH_SHORT).show()
                }

            }, LockStates.RemoveLock).show()
        }

        btn_reminder.setOnClickListener {
            showDatePickerDialog()
        }

        btn_delete.setOnClickListener {
            val bottomSheetDialog =
                BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
            bottomSheetDialog.setContentView(R.layout.delete_dialog_layout)
            bottomSheetDialog.btn_ok_delete_dialog.setOnClickListener {
                if (isLock){
                    LockDialog(requireContext(), object: LockDialogListener{
                        override fun onCreateLock(password: String) {
                        }

                        override fun onChangeLock(password: String) {
                        }

                        override fun onAccessNote() {
                        }

                        override fun onRemoveLock() {
                            noteViewModel.deleteNote(noteID)
                            noteViewModel.deleteAlarm(alarmID)
                            if (alarmID != -1)
                                cancelAlarm()
                            resetData()
                            bottomSheetDialog.dismiss()
                            NavHostFragment.findNavController(this@CheckListFragment).navigateUp()
                        }

                    },LockStates.RemoveLockedNote).show()
                }else {
                    noteViewModel.deleteNote(noteID)
                    noteViewModel.deleteAlarm(alarmID)
                    if (alarmID != -1)
                        cancelAlarm()
                    resetData()
                    bottomSheetDialog.dismiss()
                    NavHostFragment.findNavController(this@CheckListFragment).navigateUp()
                }
            }
            bottomSheetDialog.btn_cancel_delete_dialog.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()
        }

        btn_add_box.setOnClickListener {

            val bottomSheetDialog =
                BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
            bottomSheetDialog.setContentView(R.layout.add_box_layout)

            if (!isLock)
                bottomSheetDialog.tv_create_lock.text = getString(R.string.LockBottomSheet)
            if (isLock)
                bottomSheetDialog.tv_create_lock.text = getString(R.string.changeLockBottomSheet)

            bottomSheetDialog.tv_share.setOnClickListener {
                val intent = Intent(android.content.Intent.ACTION_SEND)
//                val shareBody = et_todo.text.toString()
//                if (shareBody != "") {
//                    intent.setType("text/plain")
//                    intent.putExtra(Intent.EXTRA_SUBJECT, "From Note App")
//                    intent.putExtra(Intent.EXTRA_TEXT, shareBody)
//                    startActivity(Intent.createChooser(intent, "اشتراک متن"))
//                } else
//                    Toast.makeText(
//                        requireContext(),
//                        "متنی برای اشتراک وجود ندارد",
//                        Toast.LENGTH_SHORT
//                    )
//                        .show()
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.tv_create_lock.setOnClickListener {
                val lockOption = if (isLock) LockStates.ChangeLock else LockStates.CreateLock

                if (lockOption == LockStates.CreateLock) {
                    val sharedPreferences: SharedPreferences =
                        requireContext().getSharedPreferences("LOCK", Context.MODE_PRIVATE)
                    val lockValue = sharedPreferences.getString("lock_value", "")
                    if (lockValue != "") {
                        isLock = true
                        isChange = true
                        setLockIcon()
                        Toast.makeText(
                            requireContext(),
                            "password CREATED.",
                            Toast.LENGTH_SHORT
                        ).show()
                        bottomSheetDialog.dismiss()
                        return@setOnClickListener
                    }
                }
                LockDialog(requireContext(), object : LockDialogListener {

                    override fun onCreateLock(password: String) {
                        isLock = true
                        isChange = true
                        setLockIcon()
                        bottomSheetDialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "password CREATED.",
                            Toast.LENGTH_SHORT
                        ).show()
                        hideKeyboard()
                        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }

                    override fun onChangeLock(password: String) {
                        isLock = true
                        isChange = true
                        bottomSheetDialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "password CHANGED.",
                            Toast.LENGTH_SHORT
                        ).show()
                        hideKeyboard()
                        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    }

                    override fun onAccessNote() {
                    }

                    override fun onRemoveLock() {
                    }


                }, lockOption).show()
            }
            bottomSheetDialog.show()
        }

        todo_toolbar.onBackButtonClickListener = View.OnClickListener {
            view.let { activity?.hideKeyboard(it) }
            handleBackButtonPressed()
        }

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    view.let { activity?.hideKeyboard(it) }
                    handleBackButtonPressed()
                }

            })
    }

    private fun saveNote() {
        resetData()
        var text = ""
        for (i in 0 until noteViewModel.checkBoxContent.size){
            text += if (noteViewModel.checkBoxContent[i].check)
                "[x]${noteViewModel.checkBoxContent[i].content}!@#"
            else
                "[e]${noteViewModel.checkBoxContent[i].content}!@#"
        }
        Log.i("equal", "text1: $text")
        Log.i("equal", "text2: $primaryText")
        if (text != primaryText)
            isChange = isChange.or(true)

        if (text != "" && isChange) {
            Log.i("screen", "open")
            val persianCalendar = PersianCalendar()
            var note: Note? = null
            note = if (noteID == "-1") {
                Log.i("viewmodel", "if 1")
                Note(
                    noteID, text, colorIndex, alarmID,
                    persianCalendar.year, persianCalendar.month, persianCalendar.dayOfMonth,
                    persianCalendar.hour, persianCalendar.minute, isLock
                )
            } else {
                Log.i("viewmodel", "if 2")
                Note(
                    noteID, text, colorIndex, alarmID,
                    persianCalendar.year, persianCalendar.month, persianCalendar.dayOfMonth,
                    persianCalendar.hour, persianCalendar.minute, isLock
                )
            }

            if (alarmID != -1 && isAlarmChanged)
                setAlarm()
            noteViewModel.saveNote(note)
            noteViewModel.checkBoxContent.clear()
            noteViewModel.contentsChange.value = false
            val action = CheckListFragmentDirections.actionCheckListFragmentToMainFragment()
            findNavController().navigate(action)
        } else if (text == "" && isChange) {
            deleteNoteAndAlarm()
        } else
            NavHostFragment.findNavController(this).navigateUp()
    }

    private fun showColorListDialog(){
        val bottomSheetDialog =
            BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.color_dialog_layout)
        getSelectedColor(bottomSheetDialog)
        bottomSheetDialog.color_group.setOnCheckedChangeListener { _, i ->
            isChange = isChange.or(true) // changed Color
            noteViewModel.contentsChange.value = true
            when (i) {
                bottomSheetDialog.color1.id -> {
                    colorIndex = 1
                    setUpNoteFragmentDesign(1)
                    setAlarmIcon()
                }
                bottomSheetDialog.color2.id -> {
                    colorIndex = 2
                    setUpNoteFragmentDesign(2)
                    setAlarmIcon()
                }
                bottomSheetDialog.color3.id -> {
                    colorIndex = 3
                    setUpNoteFragmentDesign(3)
                    setAlarmIcon()
                }
                bottomSheetDialog.color4.id -> {
                    colorIndex = 4
                    setUpNoteFragmentDesign(4)
                    setAlarmIcon()
                }
                bottomSheetDialog.color5.id -> {
                    colorIndex = 5
                    setUpNoteFragmentDesign(5)
                    setAlarmIcon()
                }
                bottomSheetDialog.color6.id -> {
                    colorIndex = 6
                    setUpNoteFragmentDesign(6)
                    setAlarmIcon()
                }
                bottomSheetDialog.color7.id -> {
                    colorIndex = 7
                    setUpNoteFragmentDesign(7)
                    setAlarmIcon()
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun getSelectedColor(bottomSheetDialog: BottomSheetDialog) {
        when (colorIndex) {
            1 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color1.id)
            2 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color2.id)
            3 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color3.id)
            4 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color4.id)
            5 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color5.id)
            6 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color6.id)
            7 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color7.id)
        }
    }

    private fun showDatePickerDialog() {
        val bottomSheetDialog =
            BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.reminder)
        reminderAdapter = ReminderAdapter()
        bottomSheetDialog.recycler_date_picker.apply {
            adapter = reminderAdapter
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
        }
        if (alarmID != -1) {
            val pos = setDatePickerAndTimePickerValue(bottomSheetDialog)
            reminderAdapter.selectedItem = pos
        }
        val date = noteViewModel.createDate()
        reminderAdapter.differ.submitList(date)
        bottomSheetDialog.tv_year_reminder.text = date[0].Year
        bottomSheetDialog.make_alarm.setOnClickListener {

            val alarmDate = reminderAdapter.getSelectedDate()
            val alarmHour = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bottomSheetDialog.layout_time_picker.hour
            } else {
                bottomSheetDialog.layout_time_picker.currentHour
            }
            val alarmMinute = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bottomSheetDialog.layout_time_picker.minute
            } else {
                bottomSheetDialog.layout_time_picker.currentMinute
            }

            val civilDate = convertPersianCalendarToCivilCalendar(alarmDate)
            calendar = Calendar.getInstance()
            var currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            var currentMinute = calendar.get(Calendar.MINUTE)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            if (alarmHour > currentHour ||
                (alarmHour == currentHour && alarmMinute > currentMinute) ||
                civilDate[2] > currentDay
            ) {

                currentHour = alarmHour
                currentMinute = alarmMinute
                val holdTime =
                    String.format("%02d", currentHour) + " : " + String.format(
                        "%02d", currentMinute
                    )

                calendar.set(
                    civilDate[0],
                    civilDate[1],
                    civilDate[2],
                    currentHour,
                    currentMinute
                )
                calendar.timeInMillis
                var alarm: Alarm? = null
                alarm = if (alarmID == -1) {
                    Alarm(
                        0, alarmDate.Year.toInt(), alarmDate.Month, alarmDate.Day.toInt(),
                        alarmHour, alarmMinute
                    )
                } else {
                    Alarm(
                        alarmID, alarmDate.Year.toInt(), alarmDate.Month, alarmDate.Day.toInt(),
                        alarmHour, alarmMinute
                    )
                }
                Log.i("viewmodel", "created alarmId is $alarmID")
                primaryAlarm = alarm
                noteViewModel.saveAlarm(alarm)
                isAlarmChanged = true
                isChange = isChange.or(true)    // changed Alarm
                noteViewModel.contentsChange.value = true
                setAlarmIcon()
                Toast.makeText(
                    requireContext(),
                    "آلارم با موفقیت ایجاد شد.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "زمان انتخاب شده صحیح نیست!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.remove_alarm.setOnClickListener {
            primaryAlarm.let {
                cancelAlarm()
                ic_alarm_toolbar.visibility = View.GONE
                isChange = isChange.or(true)    // changed Alarm
                noteViewModel.alarmId.value = -1
                noteViewModel.contentsChange.value = true
                bottomSheetDialog.dismiss()
            }
        }

        reminderAdapter.setOnItemClickListener {
            bottomSheetDialog.tv_year_reminder.text = it.Year
        }

        bottomSheetDialog.show()
    }

    private fun setDatePickerAndTimePickerValue(bottomSheetDialog: BottomSheetDialog): Int {
        // set time picker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bottomSheetDialog.layout_time_picker.hour = primaryAlarm.hour
            bottomSheetDialog.layout_time_picker.minute = primaryAlarm.minute
        } else {
            bottomSheetDialog.layout_time_picker.currentHour = primaryAlarm.hour
            bottomSheetDialog.layout_time_picker.currentMinute = primaryAlarm.minute
        }
        // set date picker
        val date = noteViewModel.createDate()
        for (i in 0 until date.size) {
            if (date[i].Year.toInt() == primaryAlarm.year &&
                date[i].Month == primaryAlarm.month &&
                date[i].Day.toInt() == primaryAlarm.day
            ) {
                return i
            }
        }
        return 0
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "reminder"
            val description = "channel for alarm manager"
            val important = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("notify", name, important)
            channel.description = description
            val notificationManager =
                requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setAlarm() {
        if (noteID == "-1")
            return
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        intent.putExtra("id", noteID)
        intent.putExtra("content", "Check your List!")
        intent.putExtra("Destination", "2")
        Log.i("viewmodel", "give noteId is$noteID")
        pendingIntent = PendingIntent.getBroadcast(requireContext(), alarmID, intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY, pendingIntent
//        )
    }

    private fun cancelAlarm() {
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(requireContext(), alarmID, intent, 0)
        alarmManager.cancel(pendingIntent)
        Toast.makeText(requireContext(), "آلارم حذف شد.", Toast.LENGTH_SHORT).show()
    }

    private fun setAlarmIcon() {
        Log.i("viewmodel", "ALARMID IS $alarmID")
        if (alarmID != -1) {
            ic_alarm_toolbar.visibility = View.VISIBLE
            when (colorIndex) {
                1 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_black)
                2 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_white)
                3 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_red)
                4 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_green)
                5 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_blue)
                6 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_yellow)
                7 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_pink)
            }
        } else
            ic_alarm_toolbar.visibility = View.GONE
    }

    private fun setLockIcon() {
        if (isLock) {
            ic_lock_toolbar.visibility = View.VISIBLE
            when (colorIndex) {
                1 -> ic_lock_toolbar.setImageResource(R.drawable.ic_lock_black)
                2 -> ic_lock_toolbar.setImageResource(R.drawable.ic_lock_white)
                3 -> ic_lock_toolbar.setImageResource(R.drawable.ic_lock_red)
                4 -> ic_lock_toolbar.setImageResource(R.drawable.ic_lock_green)
                5 -> ic_lock_toolbar.setImageResource(R.drawable.ic_lock_blue)
                6 -> ic_lock_toolbar.setImageResource(R.drawable.ic_lock_yellow)
                7 -> ic_lock_toolbar.setImageResource(R.drawable.ic_lock_pink)
            }
        } else
            ic_lock_toolbar.visibility = View.GONE
    }

    private fun getAlarm(id: Int) {
        noteViewModel.getAlarmFromRoom(id).observe(viewLifecycleOwner, Observer { alarm ->
            alarm?.let {
                primaryAlarm = alarm
                alarmID = alarm.id
                setAlarmIcon()
            }
        })
    }

    private fun deleteNoteAndAlarm(){
        noteViewModel.deleteNote(noteID)
        noteViewModel.deleteAlarm(alarmID)
        if (alarmID != -1)
            cancelAlarm()
        NavHostFragment.findNavController(this).navigateUp()
    }

    private fun setUpNoteFragmentDesign(index: Int) {
        checkListAdapter.changeIndex(index)
        checkListAdapter.notifyDataSetChanged()
        cb_main_layout.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        rv_check_list.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        toolbarTitleTv.setTextColor(Color.parseColor(getForegroundColor(index)))
        tv_markdown.setTextColor(Color.parseColor(getForegroundColor(index)))
        tv_save.setTextColor(Color.parseColor(getForegroundColor(index)))
        appBarLayout.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        add_item.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        navigation_todo.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        btn_add_box.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        btn_color.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        btn_delete.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        btn_reminder.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && index == 1)
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.dark_white)
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && index == 1)
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.status_bar_color)
        else
            requireActivity().window.statusBarColor =
                Color.parseColor(getBackgroundColor(index))
        if (isLock) setLockIcon()
        when (index) {
            1 -> {
                backBtn.setImageResource(R.drawable.ic_back)
                btn_add_box.setImageResource(R.drawable.ic_add_box_black)
                btn_color.setImageResource(R.drawable.ic_color)
                btn_delete.setImageResource(R.drawable.ic_delete)
                btn_reminder.setImageResource(R.drawable.ic_reminder)
                img_add_checklist.setImageResource(R.drawable.ic_add_to_list)
                tv_add_checklist.setTextColor(Color.parseColor(getForegroundColor(1)))
            }
            2 -> {
                backBtn.setImageResource(R.drawable.ic_back_white)
                btn_add_box.setImageResource(R.drawable.ic_add_box_white)
                btn_color.setImageResource(R.drawable.ic_color_white)
                btn_delete.setImageResource(R.drawable.ic_delete_white)
                btn_reminder.setImageResource(R.drawable.ic_reminder_white)
                tv_save.setTextColor(Color.parseColor(getForegroundColor(index)))
                img_add_checklist.setImageResource(R.drawable.ic_add_to_list_white)
                tv_add_checklist.setTextColor(Color.parseColor(getForegroundColor(2)))
            }
            3 -> {
                backBtn.setImageResource(R.drawable.ic_back_red)
                btn_add_box.setImageResource(R.drawable.ic_add_box_red)
                btn_color.setImageResource(R.drawable.ic_color_red)
                btn_delete.setImageResource(R.drawable.ic_delete_red)
                btn_reminder.setImageResource(R.drawable.ic_reminder_red)
                img_add_checklist.setImageResource(R.drawable.ic_add_to_list)
                tv_add_checklist.setTextColor(Color.parseColor(getForegroundColor(1)))
            }
            4 -> {

                backBtn.setImageResource(R.drawable.ic_back_green)
                btn_add_box.setImageResource(R.drawable.ic_add_box_green)
                btn_color.setImageResource(R.drawable.ic_color_green)
                btn_delete.setImageResource(R.drawable.ic_delete_green)
                btn_reminder.setImageResource(R.drawable.ic_reminder_green)
                img_add_checklist.setImageResource(R.drawable.ic_add_to_list)
                tv_add_checklist.setTextColor(Color.parseColor(getForegroundColor(1)))
            }
            5 -> {
                backBtn.setImageResource(R.drawable.ic_back_blue)
                btn_add_box.setImageResource(R.drawable.ic_add_box_blue)
                btn_color.setImageResource(R.drawable.ic_color_blue)
                btn_delete.setImageResource(R.drawable.ic_delete_blue)
                btn_reminder.setImageResource(R.drawable.ic_reminder_blue)
                img_add_checklist.setImageResource(R.drawable.ic_add_to_list)
                tv_add_checklist.setTextColor(Color.parseColor(getForegroundColor(1)))
            }
            6 -> {
                backBtn.setImageResource(R.drawable.ic_back_yellow)
                btn_add_box.setImageResource(R.drawable.ic_add_box_yellow)
                btn_color.setImageResource(R.drawable.ic_color_yellow)
                btn_delete.setImageResource(R.drawable.ic_delete_yellow)
                btn_reminder.setImageResource(R.drawable.ic_reminder_yellow)
                img_add_checklist.setImageResource(R.drawable.ic_add_to_list)
                tv_add_checklist.setTextColor(Color.parseColor(getForegroundColor(1)))
            }
            7 -> {
                backBtn.setImageResource(R.drawable.ic_back_pink)
                btn_add_box.setImageResource(R.drawable.ic_add_box_pink)
                btn_color.setImageResource(R.drawable.ic_color_pink)
                btn_delete.setImageResource(R.drawable.ic_delete_pink)
                btn_reminder.setImageResource(R.drawable.ic_reminder_pink)
                img_add_checklist.setImageResource(R.drawable.ic_add_to_list)
                tv_add_checklist.setTextColor(Color.parseColor(getForegroundColor(1)))
            }
        }


    }

    private fun handleBackButtonPressed() {
//        if (et_todo.text.toString() != primaryText)
//            isChange = isChange.or(true)
        if (isChange) {
            val bottomSheetDialog =
                BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
            bottomSheetDialog.setContentView(R.layout.back_dialog_layout)
            bottomSheetDialog.btn_ok_back_dialog.setOnClickListener {
                bottomSheetDialog.dismiss()
                saveNote()
            }
            bottomSheetDialog.btn_cancel_back_dialog.setOnClickListener {
                bottomSheetDialog.dismiss()
                resetData()
                NavHostFragment.findNavController(this@CheckListFragment).navigateUp()
            }
            bottomSheetDialog.show()
        } else {
            NavHostFragment.findNavController(this@CheckListFragment).navigateUp()
            resetData()
        }
    }

    private fun convertPersianCalendarToCivilCalendar(date: DateModel): Array<Int> {
        val persianCalendar = PersianCalendar()
        persianCalendar.year = date.Year.toInt()
        persianCalendar.month = date.Month
        persianCalendar.dayOfMonth = date.Day.toInt()
        val englishCalendar = persianCalendar.toCivil()

        return arrayOf(englishCalendar.year, englishCalendar.month, englishCalendar.dayOfMonth)
    }

    fun resetData() {
        final_text = ""
        COLOR_INDEX = 1
        IS_LOCK = false
    }

    private fun setUpRecycler() {
        rv_check_list.apply {
            adapter = checkListAdapter
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        noteViewModel.noteId.value = -1
        noteViewModel.alarmId.value = -1
        noteViewModel.setSelectedSortOption(2)
        noteViewModel.checkBoxContent.clear()
        noteViewModel.contentsChange.value = false
    }
}