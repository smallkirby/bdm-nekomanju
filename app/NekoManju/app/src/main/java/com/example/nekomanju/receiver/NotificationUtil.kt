package com.example.nekomanju.chart

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.example.nekomanju.MainActivity
import com.example.nekomanju.R
import timber.log.Timber

// notification ID
private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

fun NotificationManager.sendNotification(messageBody: String, applicationContext: Context){
    val nekoImage = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.neko)
    val bigPicStyle = NotificationCompat.BigPictureStyle()
        .bigPicture(nekoImage)
        .bigLargeIcon(null)

    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(applicationContext, applicationContext.getString(R.string.notification_channel_id))
        .setSmallIcon(R.drawable.neko)
        .setContentTitle(applicationContext.getString(R.string.notification_title_string))
        .setContentText(messageBody)
        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)
        .setStyle(bigPicStyle)
        .setLargeIcon(nekoImage)

    notify(NOTIFICATION_ID, builder.build())

    Timber.d("sendNotification() end")
}

fun NotificationManager.cancelNotifications(){
    cancelAll()
    Timber.d("cancelNotification() end")
}

