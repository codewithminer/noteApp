package com.example.noteapp.ui

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.noteapp.R
import com.example.noteapp.model.data.db.NoteDatabase
import com.example.noteapp.model.data.repository.AlarmRepository
import com.example.noteapp.model.data.repository.NoteRepository
import com.example.noteapp.receiver.AlarmReceiver
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.ui.viewmodel.NoteViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_items.*

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val receiver = ComponentName(applicationContext, AlarmReceiver::class.java)
        applicationContext.packageManager?.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // * disable night mode even if night mode is enable
        setStatusBarColor()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        val noteRep = NoteRepository(NoteDatabase(this))
        val alarmRep = AlarmRepository(NoteDatabase(this))
        val noteViewModelProviderFactory = NoteViewModelProviderFactory(noteRep,alarmRep)
        noteViewModel = ViewModelProvider(this, noteViewModelProviderFactory)[NoteViewModel::class.java]

    }

//    override fun onSupportNavigateUp(): Boolean {
//        return navController.navigateUp() || super.onSupportNavigateUp()
//    }

    private fun setStatusBarColor(){
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP){
            val window : Window = this@MainActivity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this@MainActivity,
                R.color.status_bar_color
            )
        }
    }


}

