package com.example.noteapp.ui.fragment

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.SeekBar
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
import com.example.noteapp.adapter.RecordAdapter
import com.example.noteapp.adapter.RecorderAdapter
import com.example.noteapp.adapter.ReminderAdapter
import com.example.noteapp.receiver.AlarmReceiver
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.add_box_layout.*
import kotlinx.android.synthetic.main.back_dialog_layout.*
import kotlinx.android.synthetic.main.color_dialog_layout.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.android.synthetic.main.item_recording.view.*
import kotlinx.android.synthetic.main.reminder.*
import kotlinx.android.synthetic.main.take_image_dialog.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import com.example.noteapp.adapter.ImageAdapter
import com.example.noteapp.model.data.*
import java.io.ByteArrayOutputStream

import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import com.example.noteapp.ui.dialog.*


class NoteFragment : Fragment(R.layout.fragment_note), RecorderAdapter.RecorderCallBack {

    private val args: NoteFragmentArgs by navArgs()
    lateinit var noteViewModel: NoteViewModel
    lateinit var reminderAdapter: ReminderAdapter
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

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var fileName: String? = null

    private var recordingStopped: Boolean = false
    private var recordAdapter: RecordAdapter? = null

    private val RECORD_AUDIO_REQUEST_CODE = 101
    private val CAMERA_REQUEST_CODE = 102

    private var mediaPlayer: MediaPlayer? = null
    private var lastProgress = 0
    private val mHandler = Handler()
    private var isPlaying = false
    private var last_index = -1

    lateinit var recorderAdapter: RecorderAdapter
    var recordNumber = 0
    val recordingList = arrayListOf<Recording>()
    val handler = Handler()

    private var imageFileName: String? = null
    var imageNumber = 0
    var values: ContentValues? = null
    var imageUri: Uri? = null

    lateinit var imageAdapter: ImageAdapter
    val imageList = arrayListOf<Image>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteViewModel = (activity as MainActivity).noteViewModel
        if (args.id == "-1")
            noteID = UUID.randomUUID().toString()
        Toast.makeText(requireContext(), noteID.toString(), Toast.LENGTH_SHORT).show()
        Log.i("time", "primary noteId is $noteID")
        primaryText = et_todo.text.toString()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

//        output = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"
//        mediaRecorder.setOutputFile(output)
        if (args.id != "-1") {
            noteID = args.id
            noteViewModel.getNote(args.id).observe(viewLifecycleOwner, Observer { note ->
                note?.let {
                    noteID = note.id
                    primaryText = note.content
                    colorIndex = note.color_index
                    isLock = note.isLock
                    if (final_text != "") {
                        et_todo.setText(final_text)
                        colorIndex = COLOR_INDEX
                        isLock = IS_LOCK
                    } else
                        et_todo.setText(note.content)
                    if (note.alarm_id != -1) {
                        getAlarm(note.alarm_id)
                    }
                    setLockIcon()
                    setUpNoteFragmentDesign(colorIndex)
                }
            })
        }

        getAllRecording()
        getAllImages()
        hideKeyboard()
        noteViewModel.alarmId.observe(viewLifecycleOwner, Observer {
            alarmID = it
            setAlarmIcon()
            Log.i("viewmodel", "change alarmId to $alarmID")
        })

        noteViewModel.noteId.observe(viewLifecycleOwner, Observer {
            noteID = it.toString()
            Log.i("time", "change noteId to: $noteID")
        })

        createNotificationChannel()

        btn_color.setOnClickListener {
            showColorListDialog()
        }

        tv_save.setOnClickListener {
            saveNote()
            view.let { activity?.hideKeyboard(it) }
        }

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
                if (isLock) {
                    LockDialog(requireContext(), object : LockDialogListener {
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
                            NavHostFragment.findNavController(this@NoteFragment).navigateUp()
                        }

                    }, LockStates.RemoveLockedNote).show()
                } else {
                    noteViewModel.deleteNote(noteID)
                    noteViewModel.deleteAlarm(alarmID)
                    if (alarmID != -1)
                        cancelAlarm()
                    resetData()
                    bottomSheetDialog.dismiss()
                    NavHostFragment.findNavController(this@NoteFragment).navigateUp()
                }
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
                val shareBody = et_todo.text.toString()
                if (shareBody != "") {
                    intent.setType("text/plain")
                    intent.putExtra(Intent.EXTRA_SUBJECT, "From Note App")
                    intent.putExtra(Intent.EXTRA_TEXT, shareBody)
                    startActivity(Intent.createChooser(intent, "اشتراک متن"))
                } else
                    Toast.makeText(
                        requireContext(),
                        "متنی برای اشتراک وجود ندارد",
                        Toast.LENGTH_SHORT
                    )
                        .show()
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

            bottomSheetDialog.tv_add_voice.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getPermissionToRecordAudio()
                }
                RecorderDialog(requireContext(), object : RecorderDialogListener {
                    override fun onStopRecorder() {
                        try {
                            mediaRecorder!!.stop()
                            mediaRecorder!!.release()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        mediaRecorder = null
                        Toast.makeText(requireContext(), "stop recording.", Toast.LENGTH_SHORT)
                            .show()
                        addRecord()
                    }
                }).show()

                mediaRecorder = MediaRecorder()
                mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
//                mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                val root = android.os.Environment.getExternalStorageDirectory()
                val file = File(root.absolutePath + "/NoteApplication/${noteID}/Audios/")
                if (!file.exists()) {
                    file.mkdirs()
                }
                recordNumber++
                fileName =
                    root.absolutePath + "/NoteApplication/${noteID}/Audios/" + "$recordNumber.mp3"
                Log.d("filename", fileName.toString())
                mediaRecorder!!.setOutputFile(fileName)
                mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                try {
                    mediaRecorder!!.prepare()
                    mediaRecorder!!.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.tv_add_photo.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getPermissionToTakePhoto()
                }
                val imageBottomSheetDialog = BottomSheetDialog(
                    requireContext(), R.style.CustomBottomSheetDialog
                )
                imageBottomSheetDialog.setContentView(R.layout.take_image_dialog)

                imageBottomSheetDialog.img_camera.setOnClickListener {
                    values = ContentValues()
                    values!!.put(MediaStore.Images.Media.TITLE, "New Picture")
                    values!!.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
                    imageUri = requireContext().contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                    )
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(intent, CAMERA_REQUEST_CODE)

//                    val fileOutPutStream = FileOutputStream(imageFileName)
//                    val bitmap = data.extras?.get("data") as Bitmap
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutPutStream)
//                    fileOutPutStream.flush()
//                    fileOutPutStream.close()
//                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//                    startActivityForResult(intent, CAMERA_REQUEST_CODE)
//                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                    imageBottomSheetDialog.dismiss()
                }
                imageBottomSheetDialog.img_gallery.setOnClickListener {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, GALLERY_REQUEST_CODE)
                    imageBottomSheetDialog.dismiss()
                }

                imageBottomSheetDialog.show()
                bottomSheetDialog.dismiss()
            }
            bottomSheetDialog.show()
        }

        tv_markdown.setOnClickListener {
            view.let { activity?.hideKeyboard(it) }
            final_text = et_todo.text.toString()
            COLOR_INDEX = colorIndex
            IS_LOCK = isLock
            recordingList.clear()
            if (isPlaying)
                stopPlaying()
            findNavController().navigate(NoteFragmentDirections.actionNoteFragmentToShowContentFragment())
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

        recorderAdapter.setOnItemClickListener {
//            Log.i("Recorder","get record")
//            mediaPlayer = MediaPlayer()
//            playRecord(it.uri)
        }
        noteViewModel.recordingList.observe(viewLifecycleOwner, Observer { records ->
            Log.i("Recorder", "call observer")
            records?.let {
                recorderAdapter.differ.submitList(records.toMutableList())
            }
        })
        noteViewModel.imageList.observe(viewLifecycleOwner, Observer { images ->
            images?.let {
                imageAdapter.submitList(images)
            }
        })
    }

    private fun getAllImages() {
        imageAdapter = ImageAdapter { image, pos ->
            if (pos == -1) {
                ImageDialog(
                    requireContext(),
                    image.originalBitmap
                ).show()
            } else {
                val file = File(image.contentUri)
                file.delete()
                Log.i("result",image.contentUri.toString())
                noteViewModel.imageList.postValue(
                    noteViewModel.imageList.value?.toMutableList()?.apply {
                        removeAt(pos)
                    }
                )
            }

        }
        rv_image.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )
        rv_image.adapter = imageAdapter

        val root = android.os.Environment.getExternalStorageDirectory()
        val path = root.absolutePath + "/NoteApplication/${noteID}/Images"
        val directory = File(path)
        val files = directory.listFiles()
        if (files != null) {
            val nameArray = arrayListOf<Int>()
            for (i in files.indices) {
                val fileName = files[i].name
                val tempName = fileName.substring(0, fileName.length - 4)
                nameArray.add(tempName.toInt())
                Log.i("Image", tempName)
            }
            nameArray.sort()
            imageNumber = nameArray[nameArray.lastIndex]

            for (i in 0 until nameArray.size) {
                Log.i("Image", nameArray[i].toString())
                val imageUri =
                    root.absolutePath + "/NoteApplication/${noteID}/Images/" + "${nameArray[i]}.png"
                val file = File(imageUri)
                val uri = Uri.fromFile(file)
                val bit = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                val scaleWidth = bit.width / 2
                val scaleHeight = bit.height / 2
                val downsizedImageBytesBMP: ByteArray = getDownsizedImageBytes(
                    bit,
                    scaleWidth,
                    scaleHeight
                )!!
                val bmp2: Bitmap = BitmapFactory.decodeByteArray(
                    downsizedImageBytesBMP,
                    0,
                    downsizedImageBytesBMP.size
                )
                imageList.add(Image(UUID.randomUUID().toString(), imageUri, bit, bmp2, "name"))
            }
            noteViewModel.imageList.postValue(imageList)
//            recorderAdapter.differ.submitList(noteViewModel.recordingList.value)
        }
    }


    override fun removeRecord(record: Recording, position: Int) {
        Log.i("Recorder", "delete: $position")
        val file = File(record.uri)
        file.delete()
        if (position == last_index) {
            stopPlaying()
            noteViewModel.recordPosition = 0
        }
        noteViewModel.recordingList.postValue(
            noteViewModel.recordingList.value!!.toMutableList().apply {
//                remove(record)
                removeAt(position)
            }
        )
    }

    override fun getRecordItems(
        record: Recording,
        holder: RecorderAdapter.ViewHolder,
        itemView: View,
        position: Int
    ) {
        Log.i("Recorder", "get record: $position")
        if (isPlaying) {
            noteViewModel.recordPosition = 0
            stopPlaying()
            if (last_index == holder.adapterPosition) {
                if (record.isPlaying) {
                    record.isPlaying = false
                    isPlaying = false
                    noteViewModel.recordPosition = lastProgress
                    if (colorIndex == 1)
                        itemView.img_view_play.setImageResource(R.drawable.ic_play_white)
                    else
                        itemView.img_view_play.setImageResource(R.drawable.ic_play_black)
//                    recorderAdapter.notifyItemChanged(last_index)
                } else {
                    playRecord(record.uri, holder, itemView, holder.adapterPosition)
                    record.isPlaying = true
                    if (colorIndex == 1)
                        itemView.img_view_play.setImageResource(R.drawable.ic_stop_white)
                    else
                        itemView.img_view_play.setImageResource(R.drawable.ic_stop_black)
                }
            } else {
                Log.i("Recorder", "notify last_index: $last_index")
                handler.removeCallbacksAndMessages(null)
                if (last_index != -1) {
                    recorderAdapter.differ.currentList[last_index].isPlaying = false
                    recorderAdapter.notifyItemChanged(last_index)
                }
                playRecord(record.uri, holder, itemView, holder.adapterPosition)
                record.isPlaying = true
                if (colorIndex == 1)
                    itemView.img_view_play.setImageResource(R.drawable.ic_stop_white)
                else
                    itemView.img_view_play.setImageResource(R.drawable.ic_stop_black)
            }
        } else {
            handler.removeCallbacksAndMessages(null)
            if (last_index != holder.adapterPosition) {
                noteViewModel.recordPosition = 0
                if (last_index != -1)
                    recorderAdapter.notifyItemChanged(last_index)
            }
            playRecord(record.uri, holder, itemView, holder.adapterPosition)
            record.isPlaying = true
            if (colorIndex == 1)
                itemView.img_view_play.setImageResource(R.drawable.ic_stop_white)
            else
                itemView.img_view_play.setImageResource(R.drawable.ic_stop_black)
        }
    }

    private fun playRecord(
        uri: String,
        holder: RecorderAdapter.ViewHolder,
        itemView: View,
        position: Int
    ) {
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer!!.setDataSource(uri)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        } catch (e: IOException) {
            Log.e("LOG_TAG", "prepare() failed")
        }
        //showing the pause button
//        seekBar!!.max = mediaPlayer!!.duration
        isPlaying = true
        if (noteViewModel.recordPosition != 0) {
            Log.i("Recorder", "seekTo")
            mediaPlayer!!.seekTo(noteViewModel.recordPosition)
        }
        mediaPlayer!!.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            Log.i("Recorder", "finish record")
            Log.i("Recorder", "pos -> $position")
            Log.i("Recorder", "holder -> ${holder.adapterPosition}")
            noteViewModel.recordPosition = 0
            stopPlaying()
            isPlaying = false
            recorderAdapter.differ.currentList[holder.adapterPosition].isPlaying = false
            recorderAdapter.notifyItemChanged(holder.adapterPosition)
        })
        last_index = holder.adapterPosition
        itemView.seekBar.max = mediaPlayer!!.duration


        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
//                    itemView.seekBar.tag = holder.adapterPosition
                    if (mediaPlayer != null && itemView.seekBar.tag == position) {
                        itemView.seekBar.progress = mediaPlayer!!.currentPosition
                        lastProgress = mediaPlayer!!.currentPosition
                        last_index = holder.adapterPosition
                        Log.i("Recorder", "holder.adapterPosition: ${last_index}")
                        handler.postDelayed(this, 100)
                    }
                } catch (e: Exception) {
                    itemView.seekBar.progress = 0
                    recorderAdapter.notifyItemChanged(holder.adapterPosition)
                }
            }
        }, 0)

        itemView.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser)
                    mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
    }

    private fun getAllRecording() {
//        val recordArrayList = ArrayList<Recording>()
        val systemHeight = Resources.getSystem().displayMetrics.heightPixels
        Log.i("Recording", "height integer $systemHeight")
        val listLayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, systemHeight / 1.5.toInt(),
        )
        Log.i("Recording", listLayoutParams.height.toString())
        recorderAdapter = RecorderAdapter(noteViewModel, this)
        rv_record.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//        recordAdapter!!.setListener(this)
        rv_record.layoutParams = listLayoutParams
        rv_record.adapter = recorderAdapter

        val root = android.os.Environment.getExternalStorageDirectory()
        val path = root.absolutePath + "/NoteApplication/${noteID}/Audios"
        val directory = File(path)
        val files = directory.listFiles()
        if (files != null) {
            val nameArray = arrayListOf<Int>()
            for (i in files.indices) {
//                recordNumber++
                val fileName = files[i].name
                val tempName = fileName.substring(0, fileName.length - 4)
                nameArray.add(tempName.toInt())
                Log.i("Recorder", tempName)
            }
            nameArray.sort()
            recordNumber = nameArray[nameArray.lastIndex]

            for (i in 0 until nameArray.size) {
                Log.i("Recorder", nameArray[i].toString())
                val recordingUri =
                    root.absolutePath + "/NoteApplication/${noteID}/Audios/" + "${nameArray[i]}.mp3"
//                recordArrayList.add(Recording(recordingUri, fileName, false))
                recordingList.add(Recording(recordingUri, nameArray[i].toString(), false))
            }
            noteViewModel.recordingList.postValue(recordingList)
//            recorderAdapter.differ.submitList(noteViewModel.recordingList.value)
        }
    }

    private fun addRecord() {
//        recordAdapter?.addItem(Recording(fileName!!,"dummy", false))
        recordingList.add(Recording(fileName!!, "dummy", false))
        noteViewModel.recordingList.postValue(recordingList)
//        noteViewModel.recordingList.value?.toMutableList()?.add(Recording(fileName!!, "dummy", false))
//        recorderAdapter.differ.submitList(noteViewModel.recordingList.value)
//        recorderAdapter.notifyItemInserted(noteViewModel.recordingList.value!!.size)
    }

    private fun createNotificationChannel() {
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
        intent.putExtra("content", et_todo.text.toString())
        intent.putExtra("Destination", "1")
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

    private fun saveNote() {
        resetData()
        val text = et_todo.text.toString()
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

            val action = NoteFragmentDirections.actionNoteFragmentToMainFragment()
            findNavController().navigate(action)
        } else if (text == "" && isChange) {
            deleteNoteAndAlarm()
        } else
            NavHostFragment.findNavController(this).navigateUp()
    }

    private fun deleteNoteAndAlarm() {
        noteViewModel.deleteNote(noteID)
        noteViewModel.deleteAlarm(alarmID)
        if (alarmID != -1)
            cancelAlarm()
        NavHostFragment.findNavController(this).navigateUp()
    }

    private fun showColorListDialog() {
        val bottomSheetDialog =
            BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.color_dialog_layout)
        getSelectedColor(bottomSheetDialog)
        bottomSheetDialog.color_group.setOnCheckedChangeListener { _, i ->
            isChange = isChange.or(true) // changed Color
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

    private fun setUpNoteFragmentDesign(index: Int) {

        note_main_layout.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        et_todo.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
        et_todo.setTextColor(Color.parseColor(getForegroundColor(index)))
        toolbarTitleTv.setTextColor(Color.parseColor(getForegroundColor(index)))
        tv_markdown.setTextColor(Color.parseColor(getForegroundColor(index)))
        tv_save.setTextColor(Color.parseColor(getForegroundColor(index)))
        appBarLayout.setBackgroundColor(Color.parseColor(getBackgroundColor(index)))
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

            }
            2 -> {
                backBtn.setImageResource(R.drawable.ic_back_white)
                btn_add_box.setImageResource(R.drawable.ic_add_box_white)
                btn_color.setImageResource(R.drawable.ic_color_white)
                btn_delete.setImageResource(R.drawable.ic_delete_white)
                btn_reminder.setImageResource(R.drawable.ic_reminder_white)
                tv_save.setTextColor(Color.parseColor(getForegroundColor(index)))

            }
            3 -> {
                backBtn.setImageResource(R.drawable.ic_back_red)
                btn_add_box.setImageResource(R.drawable.ic_add_box_red)
                btn_color.setImageResource(R.drawable.ic_color_red)
                btn_delete.setImageResource(R.drawable.ic_delete_red)
                btn_reminder.setImageResource(R.drawable.ic_reminder_red)
            }
            4 -> {

                backBtn.setImageResource(R.drawable.ic_back_green)
                btn_add_box.setImageResource(R.drawable.ic_add_box_green)
                btn_color.setImageResource(R.drawable.ic_color_green)
                btn_delete.setImageResource(R.drawable.ic_delete_green)
                btn_reminder.setImageResource(R.drawable.ic_reminder_green)
            }
            5 -> {
                backBtn.setImageResource(R.drawable.ic_back_blue)
                btn_add_box.setImageResource(R.drawable.ic_add_box_blue)
                btn_color.setImageResource(R.drawable.ic_color_blue)
                btn_delete.setImageResource(R.drawable.ic_delete_blue)
                btn_reminder.setImageResource(R.drawable.ic_reminder_blue)
            }
            6 -> {
                backBtn.setImageResource(R.drawable.ic_back_yellow)
                btn_add_box.setImageResource(R.drawable.ic_add_box_yellow)
                btn_color.setImageResource(R.drawable.ic_color_yellow)
                btn_delete.setImageResource(R.drawable.ic_delete_yellow)
                btn_reminder.setImageResource(R.drawable.ic_reminder_yellow)
            }
            7 -> {
                backBtn.setImageResource(R.drawable.ic_back_pink)
                btn_add_box.setImageResource(R.drawable.ic_add_box_pink)
                btn_color.setImageResource(R.drawable.ic_color_pink)
                btn_delete.setImageResource(R.drawable.ic_delete_pink)
                btn_reminder.setImageResource(R.drawable.ic_reminder_pink)
            }
        }
        if (isPlaying)
            stopPlaying()
        COLOR_INDEX = index
        recorderAdapter.notifyDataSetChanged()
    }

    private fun handleBackButtonPressed() {
        if (et_todo.text.toString() != primaryText)
            isChange = isChange.or(true)
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
                NavHostFragment.findNavController(this@NoteFragment).navigateUp()
            }
            bottomSheetDialog.show()
        } else {
            NavHostFragment.findNavController(this@NoteFragment).navigateUp()
            resetData()
        }
        if (mediaPlayer != null)
            stopPlaying()
        noteViewModel.recordingList.postValue(null)
        noteViewModel.recordPosition = 0
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

    override fun onDestroy() {
        super.onDestroy()
//        noteViewModel.noteId.value = -1
        noteViewModel.alarmId.value = -1
        noteViewModel.setSelectedSortOption(2)
        noteViewModel.recordingList.postValue(null)
        noteViewModel.recordPosition = 0
    }

    private fun getPermissionToRecordAudio() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), RECORD_AUDIO_REQUEST_CODE
            )
        }
    }

    private fun getPermissionToTakePhoto() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }
    }

    // Callback with the request from calling requestPermissions(...)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.size == 3 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                grantResults[2] == PackageManager.PERMISSION_GRANTED
            ) {
                //Toast.makeText(this, "Record Audio permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(
                    requireContext(),
                    "You must give permissions to use this app",
                    Toast.LENGTH_SHORT
                ).show()
//                finishAffinity(requireActivity())
            }
        }
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.size == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

            } else {
                Toast.makeText(
                    requireContext(),
                    "You must give permissions to use this app",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun stopPlaying() {
        try {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            mediaPlayer!!.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.i("Recorder", "onStop called mediaPlayer=null and isPlaying=false")
        mediaPlayer = null
        isPlaying = false
    }

    fun dpFromPx(context: Context, px: Float): Float {
        Log.i("Recording", " density: ${context.resources.displayMetrics.density}")
        Log.i("Recording", " pixel: ${px / context.resources.displayMetrics.density}")
        return px / context.resources.displayMetrics.density
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {
            Log.i("result", "camera")
            val root = android.os.Environment.getExternalStorageDirectory()
            val file = File(root.absolutePath + "/NoteApplication/${noteID}/Images/")
            if (!file.exists()) {
                file.mkdirs()
            }
            imageNumber++
            imageFileName =
                root.absolutePath + "/NoteApplication/${noteID}/Images/" + "$imageNumber.png"
            try {
                val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            requireContext().contentResolver,
                            imageUri!!
                        )
                    )
                } else {
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                }
                val scaleWidth = thumbnail.width / 6
                val scaleHeight = thumbnail.height / 6
                val downsizedImageBytes: ByteArray = getDownsizedImageBytes(
                    thumbnail,
                    scaleWidth,
                    scaleHeight
                )!!
                // 3. Upload the byte[]; Eg, if you are using Firebase
                val bmp: Bitmap =
                    BitmapFactory.decodeByteArray(downsizedImageBytes, 0, downsizedImageBytes.size)
//                val imageurl = getRealPathFromURI(imageUri)
                Log.i("result", imageFileName.toString())
                val fileOutPutStream = FileOutputStream(imageFileName)
                //            val bitmap = data.extras?.get("data") as Bitmap
                bmp.compress(Bitmap.CompressFormat.PNG, 85, fileOutPutStream)
                fileOutPutStream.flush()
                fileOutPutStream.close()
                val scaleWidthBMP = bmp.width / 2
                val scaleHeightBMP = bmp.height / 2
                val downsizedImageBytesBMP: ByteArray = getDownsizedImageBytes(
                    bmp,
                    scaleWidthBMP,
                    scaleHeightBMP
                )!!
                val bmp2: Bitmap = BitmapFactory.decodeByteArray(
                    downsizedImageBytesBMP,
                    0,
                    downsizedImageBytesBMP.size
                )
//                bmp2.compress(Bitmap.CompressFormat.PNG, 85, null)
                imageList.add(Image(UUID.randomUUID().toString(), imageFileName!!, bmp, bmp2, "image"))
                noteViewModel.imageList.postValue(imageList)
                //          imgView.setImageBitmap(thumbnail)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

        }
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQUEST_CODE) {
//            imageView.setImageURI(data?.data) // handle chosen image
            val root = android.os.Environment.getExternalStorageDirectory()
            val file = File(root.absolutePath + "/NoteApplication/${noteID}/Images/")
            if (!file.exists()) {
                file.mkdirs()
            }
            imageNumber++
            imageFileName =
                root.absolutePath + "/NoteApplication/${noteID}/Images/" + "$imageNumber.png"
            val fileOutPutStream = FileOutputStream(imageFileName)
            val uri: Uri? = data?.data
            val bit = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            bit.compress(Bitmap.CompressFormat.PNG, 85, fileOutPutStream)
            fileOutPutStream.flush()
            fileOutPutStream.close()
            val scaleWidth = bit.width / 2
            val scaleHeight = bit.height / 2
            val downsizedImageBytesBMP: ByteArray = getDownsizedImageBytes(
                bit,
                scaleWidth,
                scaleHeight
            )!!
            val bmp2: Bitmap = BitmapFactory.decodeByteArray(
                downsizedImageBytesBMP,
                0,
                downsizedImageBytesBMP.size
            )
            imageList.add(Image(UUID.randomUUID().toString(), imageFileName!!, bit, bmp2, "image"))
            noteViewModel.imageList.postValue(imageList)
        }
    }

    fun getRealPathFromURI(contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = requireActivity().managedQuery(contentUri, proj, null, null, null)
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    @Throws(IOException::class)
    fun getDownsizedImageBytes(
        fullBitmap: Bitmap?,
        scaleWidth: Int,
        scaleHeight: Int
    ): ByteArray? {
        val scaledBitmap = Bitmap.createScaledBitmap(
            fullBitmap!!,
            scaleWidth,
            scaleHeight,
            true
        )

        // 2. Instantiate the downsized image content as a byte[]
        val baos = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return baos.toByteArray()
    }

}

