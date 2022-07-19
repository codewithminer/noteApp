package com.example.noteapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.example.noteapp.ui.MainActivity
import com.example.noteapp.R
import com.example.noteapp.utils.DESTINATION

class AlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent(context, MainActivity::class.java)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        intent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
//        val pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//        val pendingIntent = PendingIntent.getActivity(context, 0, i, 0)
        val bundle = Bundle()
        bundle.putInt("id",intent.getIntExtra("id",-1))

        val pendingIntent = NavDeepLinkBuilder(context!!)
            .setGraph(R.navigation.nav_graph)
            .setDestination(if (intent.getStringExtra("Destination") == "1") R.id.noteFragment else R.id.checkListFragment)
            .setArguments(bundle)
            .createPendingIntent()

        val builder = NotificationCompat.Builder(context, "notify")
            .setSmallIcon(R.drawable.ic_note)
            .setContentTitle("Note")
            .setContentText(intent.getStringExtra("content"))
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify((0..2147483647).random(), builder.build())
    }
}