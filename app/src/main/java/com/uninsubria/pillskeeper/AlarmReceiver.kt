package com.uninsubria.pillskeeper

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat

class AlarmReceiver:BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val builder = NotificationCompat.Builder(p0!!, "1")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText("allarme ricevuto")
            .setContentTitle("ciao")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notiManager = p0.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notiManager.notify(0, builder)
        Toast.makeText(p0, "Ricevuto ", Toast.LENGTH_SHORT).show()
        Toast.makeText(p0, "Ricevuto ", Toast.LENGTH_SHORT).show()
        Toast.makeText(p0, "Ricevuto ", Toast.LENGTH_SHORT).show()
        Toast.makeText(p0, "Ricevuto ", Toast.LENGTH_SHORT).show()
    }
}