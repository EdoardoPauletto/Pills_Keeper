package com.uninsubria.pillskeeper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.*

class BackgroundWorker(val c: Context, p: WorkerParameters) : Worker(c, p) {
    private val CHANNEL_ID = "canale01"

    override fun doWork(): Result {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "mynotifica"
            val desc = "my description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationChannel.description = desc
            val notificationManager = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationId = Random().nextInt(50)

        val notificationBuilder = NotificationCompat.Builder(c, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.drawable.pill_icon_main)
        notificationBuilder.setContentTitle("Pillola: " + inputData.getString("nome"))
        notificationBuilder.setContentText("Sveglia suonata")
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notificationBuilder.setAutoCancel(true)
        val notifictionmanagerCompat = NotificationManagerCompat.from(c)
        notifictionmanagerCompat.notify(notificationId, notificationBuilder.build())

        return Result.success()
    }
}