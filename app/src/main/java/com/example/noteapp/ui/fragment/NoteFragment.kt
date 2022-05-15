package com.example.noteapp.ui.fragment

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.noteapp.adapter.ReminderAdapter
import com.example.noteapp.model.data.Alarm
import com.example.noteapp.model.data.DateModel
import com.example.noteapp.model.data.Note
import com.example.noteapp.receiver.AlarmReceiver
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.back_dialog_layout.*
import kotlinx.android.synthetic.main.color_dialog_layout.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.android.synthetic.main.reminder.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class NoteFragment: Fragment(R.layout.fragment_note) {

    private val args: NoteFragmentArgs by navArgs()
    lateinit var noteViewModel: NoteViewModel
    lateinit var reminderAdapter: ReminderAdapter
    lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    lateinit var calendar: Calendar
    private var colorIndex = 1
    private var noteID: Int = -1
    private var alarmID: Int = -1
    private var isChange = false
    private var isAlarmChanged = false
    private lateinit var primaryAlarm: Alarm
    private var primaryText = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        noteViewModel = (activity as MainActivity).noteViewModel
        noteID = args.id
        Log.i("time", "primary noteId is $noteID")
        primaryText = et_todo.text.toString()

        noteViewModel.getNote(args.id).observe(viewLifecycleOwner, Observer { note ->
            note?.let {
                noteID = note.id
                primaryText = note.content
                colorIndex = note.color_index
                if (final_text != ""){
                    et_todo.setText(final_text)
                    colorIndex = COLOR_INDEX
                }
                else
                    et_todo.setText(note.content)
                if (note.alarm_id != -1){
                    getAlarm(note.alarm_id)
                }
                setUpNoteFragmentDesign(colorIndex)
            }
        })

        noteViewModel.alarmId.observe(viewLifecycleOwner, Observer {
            alarmID = it
            setAlarmIcon()
            Log.i("viewmodel", "change alarmId to $alarmID")
        })

        noteViewModel.noteId.observe(viewLifecycleOwner, Observer {
            noteID = it
        })

        createNotificationChannel()

        btn_color.setOnClickListener {
            showColorListDialog()
        }

        tv_save.setOnClickListener {
            saveNote()
            view.let { activity?.hideKeyboard(it) }
        }

        btn_reminder.setOnClickListener {
            showDatePickerDialog()
        }

        btn_delete.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
            bottomSheetDialog.setContentView(R.layout.delete_dialog_layout)
            bottomSheetDialog.btn_ok_delete_dialog.setOnClickListener {
                noteViewModel.deleteNote(noteID)
                noteViewModel.deleteAlarm(alarmID)
                if (alarmID != -1)
                    cancelAlarm()
                resetData()
                bottomSheetDialog.dismiss()
                NavHostFragment.findNavController(this@NoteFragment).navigateUp()
            }
            bottomSheetDialog.btn_cancel_delete_dialog.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()
//            SaveDialog(requireContext(),object: SaveDialogListener{
//                override fun onPositiveClick() {
//                    noteViewModel.deleteNote(noteID)
//                    noteViewModel.deleteAlarm(alarmID)
//                    if (alarmID != -1)
//                        cancelAlarm()
//                    resetData()
//                    NavHostFragment.findNavController(this@NoteFragment).navigateUp()
//                }
//                override fun onNegativeClick() {
//                }
//            }, getString(R.string.title_delete_dialog)).show()
        }

        btn_share.setOnClickListener {
            val intent = Intent(android.content.Intent.ACTION_SEND)
            val shareBody = et_todo.text.toString()
            if (shareBody != "") {
                intent.setType("text/plain")
                intent.putExtra(Intent.EXTRA_SUBJECT, "From Note App")
                intent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(intent, "اشتراک متن"))
            } else
                Toast.makeText(requireContext(), "متنی برای اشتراک وجود ندارد", Toast.LENGTH_SHORT)
                    .show()
        }

        tv_markdown.setOnClickListener {
            view.let { activity?.hideKeyboard(it) }
            final_text = et_todo.text.toString()
            COLOR_INDEX = colorIndex
            findNavController().navigate(NoteFragmentDirections.actionNoteFragmentToShowContentFragment())
        }

        todo_toolbar.onBackButtonClickListener = View.OnClickListener {
            view.let { activity?.hideKeyboard(it) }
            handleBackButtonPressed()
        }

        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
                override fun handleOnBackPressed() {
                    view.let { activity?.hideKeyboard(it) }
                    handleBackButtonPressed()
                }

            })

        et_todo.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    val checkContent = et_todo.text.toString()
                    if (checkContent != "") {
                        tv_save.visibility = View.VISIBLE
                        tv_markdown.visibility = View.VISIBLE
                    } else {
                        tv_save.visibility = View.GONE
                        tv_markdown.visibility = View.GONE
                    }
                }
                override fun afterTextChanged(p0: Editable?) {
                }
            })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name: CharSequence = "reminder"
            val description = "channel for alarm manager"
            val important = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("notify",name,important)
            channel.description = description
            val notificationManager =
                requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setAlarm(){
        if (noteID == -1)
            return
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        intent.putExtra("id", noteID)
        intent.putExtra("content",et_todo.text.toString())
        Log.i("viewmodel", "give noteId is$noteID")
        pendingIntent = PendingIntent.getBroadcast(requireContext(), alarmID, intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,  calendar.timeInMillis, pendingIntent)
//        alarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
//            AlarmManager.INTERVAL_DAY, pendingIntent
//        )
    }

    private fun cancelAlarm(){
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(requireContext(), alarmID, intent, 0)
        alarmManager.cancel(pendingIntent)
        Toast.makeText(requireContext(), "آلارم حذف شد.", Toast.LENGTH_SHORT).show()
    }

    private fun setAlarmIcon() {
        Log.i("viewmodel", "ALARMID IS $alarmID")
        if (alarmID != -1){
            ic_alarm_toolbar.visibility = View.VISIBLE
            when(colorIndex){
                1 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_black)
                2 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_white)
                3 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_red)
                4 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_green)
                5 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_blue)
                6 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_yellow)
                7 -> ic_alarm_toolbar.setImageResource(R.drawable.ic_alarm_pink)
            }
        }
        else
            ic_alarm_toolbar.visibility = View.GONE
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

    private fun saveNote() {
        resetData()
        val text = et_todo.text.toString()
        Log.i("equal", "text1: $text")
        Log.i("equal", "text2: $primaryText")
        if (text != primaryText)
            isChange = isChange.or(true)

        if (text != "" && isChange){
            Log.i("screen", "open")
            val persianCalendar = PersianCalendar()
            var note: Note? = null
            note = if (noteID == -1){
                Log.i("viewmodel", "if 1")
                Note(
                    0,text, colorIndex, alarmID,
                    persianCalendar.year, persianCalendar.month, persianCalendar.dayOfMonth,
                    persianCalendar.hour, persianCalendar.minute
                )
            }else{
                Log.i("viewmodel", "if 2")
                Note(
                    noteID,text, colorIndex, alarmID,
                    persianCalendar.year, persianCalendar.month, persianCalendar.dayOfMonth,
                    persianCalendar.hour, persianCalendar.minute
                )
            }

            if (alarmID != -1 && isAlarmChanged)
                 setAlarm()
            noteViewModel.saveNote(note)

            val action = NoteFragmentDirections.actionNoteFragmentToMainFragment()
            findNavController().navigate(action)
        }else if (text == "" && isChange){
            deleteNoteAndAlarm()
        }
        else
            NavHostFragment.findNavController(this).navigateUp()
    }

    private fun deleteNoteAndAlarm() {
        noteViewModel.deleteNote(noteID)
        noteViewModel.deleteAlarm(alarmID)
        if (alarmID != -1)
            cancelAlarm()
        NavHostFragment.findNavController(this).navigateUp()
    }

    private fun showColorListDialog(){
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.color_dialog_layout)
        getSelectedColor(bottomSheetDialog)
        bottomSheetDialog.color_group.setOnCheckedChangeListener { _, i ->
            isChange = isChange.or(true) // changed Color
            when(i){
                bottomSheetDialog.color1.id -> {
                    setUpNoteFragmentDesign(1)
                    colorIndex = 1
                    setAlarmIcon()
                }
                bottomSheetDialog.color2.id -> {
                    setUpNoteFragmentDesign(2)
                    colorIndex = 2
                    setAlarmIcon()
                }
                bottomSheetDialog.color3.id -> {
                    setUpNoteFragmentDesign(3)
                    colorIndex = 3
                    setAlarmIcon()
                }
                bottomSheetDialog.color4.id -> {
                    setUpNoteFragmentDesign(4)
                    colorIndex = 4
                    setAlarmIcon()
                }
                bottomSheetDialog.color5.id -> {
                    setUpNoteFragmentDesign(5)
                    colorIndex = 5
                    setAlarmIcon()
                }
                bottomSheetDialog.color6.id -> {
                    setUpNoteFragmentDesign(6)
                    colorIndex = 6
                    setAlarmIcon()
                }
                bottomSheetDialog.color7.id -> {
                    setUpNoteFragmentDesign(7)
                    colorIndex = 7
                    setAlarmIcon()
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun getSelectedColor(bottomSheetDialog: BottomSheetDialog){
        when(colorIndex){
            1 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color1.id)
            2 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color2.id)
            3 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color3.id)
            4 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color4.id)
            5 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color5.id)
            6 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color6.id)
            7 -> bottomSheetDialog.color_group.check(bottomSheetDialog.color7.id)
        }
    }

    private fun showDatePickerDialog(){
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.reminder)
        reminderAdapter = ReminderAdapter()
        bottomSheetDialog.recycler_date_picker.apply {
            adapter = reminderAdapter
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
        }
        if (alarmID != -1){
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
                civilDate[2] > currentDay) {

                currentHour = alarmHour
                currentMinute = alarmMinute
                val holdTime =
                    String.format("%02d", currentHour) + " : " + String.format(
                        "%02d", currentMinute
                    )

                calendar.set(civilDate[0], civilDate[1],civilDate[2], currentHour, currentMinute)
                calendar.timeInMillis
                var alarm: Alarm? = null
                alarm = if (alarmID == -1){
                    Alarm(
                        0, alarmDate.Year.toInt(), alarmDate.Month, alarmDate.Day.toInt(),
                        alarmHour, alarmMinute
                    )
                }else{
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
                setAlarmIcon()
                Toast.makeText(requireContext(), "آلارم با موفقیت ایجاد شد.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "زمان انتخاب شده صحیح نیست!", Toast.LENGTH_SHORT).show()
            }
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.remove_alarm.setOnClickListener {
            primaryAlarm.let {
                cancelAlarm()
                ic_alarm_toolbar.visibility = View.GONE
                isChange = isChange.or(true)    // changed Alarm
                noteViewModel.alarmId.value = -1
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

    private fun setUpNoteFragmentDesign(index: Int){

        et_todo.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        et_todo.setTextColor(Color.parseColor(getForegroundColor(index)))
        toolbarTitleTv.setTextColor(Color.parseColor(getForegroundColor(index)))
        tv_markdown.setTextColor(Color.parseColor(getForegroundColor(index)))
        tv_save.setTextColor(Color.parseColor(getForegroundColor(index)))
        appBarLayout.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        navigation_todo.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        btn_share.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
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
            requireActivity().window.statusBarColor = Color.parseColor(getBackgroundColor(index))

        when(index){
            1 -> {
                backBtn.setImageResource(R.drawable.ic_back)
                btn_share.setImageResource(R.drawable.ic_share)
                btn_color.setImageResource(R.drawable.ic_color)
                btn_delete.setImageResource(R.drawable.ic_delete)
                btn_reminder.setImageResource(R.drawable.ic_reminder)

            }
            2 -> {
                backBtn.setImageResource(R.drawable.ic_back_white)
                btn_share.setImageResource(R.drawable.ic_share_white)
                btn_color.setImageResource(R.drawable.ic_color_white)
                btn_delete.setImageResource(R.drawable.ic_delete_white)
                btn_reminder.setImageResource(R.drawable.ic_reminder_white)
                tv_save.setTextColor(Color.parseColor(getForegroundColor(index)))

            }
            3 -> {
                backBtn.setImageResource(R.drawable.ic_back_red)
                btn_share.setImageResource(R.drawable.ic_share_red)
                btn_color.setImageResource(R.drawable.ic_color_red)
                btn_delete.setImageResource(R.drawable.ic_delete_red)
                btn_reminder.setImageResource(R.drawable.ic_reminder_red)
            }
            4 -> {

                backBtn.setImageResource(R.drawable.ic_back_green)
                btn_share.setImageResource(R.drawable.ic_share_green)
                btn_color.setImageResource(R.drawable.ic_color_green)
                btn_delete.setImageResource(R.drawable.ic_delete_green)
                btn_reminder.setImageResource(R.drawable.ic_reminder_green)
            }
            5 -> {
                backBtn.setImageResource(R.drawable.ic_back_blue)
                btn_share.setImageResource(R.drawable.ic_share_blue)
                btn_color.setImageResource(R.drawable.ic_color_blue)
                btn_delete.setImageResource(R.drawable.ic_delete_blue)
                btn_reminder.setImageResource(R.drawable.ic_reminder_blue)
            }
            6 -> {
                backBtn.setImageResource(R.drawable.ic_back_yellow)
                btn_share.setImageResource(R.drawable.ic_share_yellow)
                btn_color.setImageResource(R.drawable.ic_color_yellow)
                btn_delete.setImageResource(R.drawable.ic_delete_yellow)
                btn_reminder.setImageResource(R.drawable.ic_reminder_yellow)
            }
            7 -> {
                backBtn.setImageResource(R.drawable.ic_back_pink)
                btn_share.setImageResource(R.drawable.ic_share_pink)
                btn_color.setImageResource(R.drawable.ic_color_pink)
                btn_delete.setImageResource(R.drawable.ic_delete_pink)
                btn_reminder.setImageResource(R.drawable.ic_reminder_pink)
            }
        }


    }

    private fun handleBackButtonPressed() {
        if (et_todo.text.toString() != primaryText)
            isChange = isChange.or(true)
        if (isChange){
            val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
            bottomSheetDialog.setContentView(R.layout.back_dialog_layout)
            bottomSheetDialog.btn_ok_back_dialog.setOnClickListener {
                bottomSheetDialog.dismiss()
                saveNote()
            }
            bottomSheetDialog.btn_cancel_back_dialog.setOnClickListener {
                bottomSheetDialog.dismiss()
                resetData()
                NavHostFragment.findNavController(this@NoteFragment).navigateUp()
            }
            bottomSheetDialog.show()
        }else{
            NavHostFragment.findNavController(this@NoteFragment).navigateUp()
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

    fun resetData(){
        final_text = ""
        COLOR_INDEX = 1
    }


    override fun onDestroy() {
        super.onDestroy()
        noteViewModel.noteId.value = -1
        noteViewModel.alarmId.value = -1
        noteViewModel.setSelectedSortOption(2)
    }
}