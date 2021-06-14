package com.example.nekomanju.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.nekomanju.chart.sendNotification
import timber.log.Timber

class Co2Receiver: BroadcastReceiver() {
    private val REQUEST_CODE = 0

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("onReceive()@Co2Reciever is called")
        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager
        notificationManager.sendNotification(
            "this is test notification", context
        )
    }

}