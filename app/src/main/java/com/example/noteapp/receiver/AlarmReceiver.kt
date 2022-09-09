package com.example.noteapp.receiver

import android.app.AlarmManager
import android.app.AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.noteapp.ui.MainActivity
import com.example.noteapp.R
import com.example.noteapp.ui.dialog.LockDialog
import com.example.noteapp.ui.dialog.LockDialogListener
import com.example.noteapp.utils.DESTINATION
import com.example.noteapp.utils.LockStates

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent(context, MainActivity::class.java)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        intent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//        val pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//        val pendingIntent = PendingIntent.getActivity(context, 0, i, 0)

//        val bundle = intent.extras
//        val noteId = intent.getStringExtra("id")
//        val destination = intent.getStringExtra(("Destination"))
//        val alarmId = intent.getIntExtra("alarmId",1)
//
////        val arr = intent.getStringArrayExtra(Intent.EXTRA_TEXT)
////        val noteId = arr?.get(0)
////        val destination = arr?.get(1)
////        val alarmId = arr?.get(2)?.toInt()
//        val bundle2 = Bundle()
//        bundle2.putString("id", noteId)
//        bundle2.putBoolean("fromNotification", true)
//        Log.i("ALARM", " note id in receiver: ${noteId.toString()}")
//        Log.i("ALARM", " destination in receiver: $destination")
//        Log.i("ALARM", " alarm id in receiver: $alarmId")
//        Log.i("ALARM", " content in receiver: ${intent.extras?.getString("content")}")
        val pendingIntent = NavDeepLinkBuilder(context!!)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.mainFragment)
            .createPendingIntent()

//        val pendingIntent = NavDeepLinkBuilder(context!!)
//            .setGraph(R.navigation.nav_graph)
//            .setDestination(if (destination == "1") R.id.noteFragment else R.id.checkListFragment)
//            .setArguments(bundle2)
//            .createTaskStackBuilder()
//            .getPendingIntent(alarmId!!, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, "notify")
            .setSmallIcon(R.drawable.ic_note)
            .setContentTitle("Note")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify((0..2147483647).random(), builder.build())
    }
}