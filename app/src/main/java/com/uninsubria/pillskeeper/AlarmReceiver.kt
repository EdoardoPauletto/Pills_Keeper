package com.uninsubria.pillskeeper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver:BroadcastReceiver() {
    private companion object{
        private const val CHANNEL_ID = "canale01"
    }

    override fun onReceive(p0: Context?, p1: Intent?) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "mynotifica"
            val desc = "my description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationChannel.description = desc
            val notificationManager = p0!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        if (p1 != null && p1.hasExtra("nome")) {
            //val date = Date()
            val notificationId = Random().nextInt(50)

            val notificationBuilder = NotificationCompat.Builder(p0!!, CHANNEL_ID)
            notificationBuilder.setSmallIcon(R.drawable.pill_icon_main)
            notificationBuilder.setContentTitle("Pillola: " + p1.getStringExtra("nome"))
            notificationBuilder.setContentText("Sveglia suonata")
            notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
            notificationBuilder.setAutoCancel(true)
            val notifictionmanagerCompat = NotificationManagerCompat.from(p0)
            notifictionmanagerCompat.notify(notificationId, notificationBuilder.build())
        }
    }

    /*override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "mynotifica"
            val desc = "my description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationChannel.description = desc
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
        if (intent != null && intent.hasExtra("nome")) {
            //val date = Date()
            val notificationId = Random().nextInt(50)

            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            notificationBuilder.setSmallIcon(R.drawable.pill_icon_main)
            notificationBuilder.setContentTitle("Pillola: " + intent.getStringExtra("nome"))
            notificationBuilder.setContentText("Sveglia suonata")
            notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
            notificationBuilder.setAutoCancel(true)
            val notifictionmanagerCompat = NotificationManagerCompat.from(this)
            notifictionmanagerCompat.notify(notificationId, notificationBuilder.build())
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        Toast.makeText(this, "MyAlarmService.onBind()", Toast.LENGTH_LONG).show()
        return null
    }*/
}