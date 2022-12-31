package com.uninsubria.pillskeeper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*

class BackgroundWorker(val c: Context, p: WorkerParameters) : Worker(c, p) {
    private val CHANNEL_ID = "canale01"

    override fun doWork(): Result {
        val key = inputData.getString("key")
        //leggi dal db info farmaco
        createNotificationChannel()
        sendNotification()
        //qntTot - q (perchè presa una)
        //if qntTot <= 2 avviso farmaco quasi finito
        //leggi orario
        //rischedula, richiamando il worker ad un altro orario

        return Result.success()
    }

    private fun sendNotification() {
        val notificationId = SimpleDateFormat("ddHHmmss", Locale.ITALIAN).format(Date()).toInt()//così se più notifiche allo stesso tempo non va
        val uri = Uri.parse("smsto:+393467635500, +393334678999")//si possono mettere tutti i numeri
        val smsIntent = Intent(Intent.ACTION_SENDTO, uri)
        smsIntent.putExtra("sms_body", "mi dovresti andare a comprare la .... , grazie")
        val smsPendingIntent = PendingIntent.getActivity(c, 1, smsIntent, PendingIntent.FLAG_IMMUTABLE)
        val emailIntent = Intent(Intent.ACTION_VIEW)
        val data: Uri = Uri.parse("mailto:?subject=" + "Buongiorno dottore" + "&body=" + "Volevo avvisarla che ho esaurito la ...." + "&to=" + "giangifumagalli1@gmail.com")
        emailIntent.data = data
        val emailPendingIntent = PendingIntent.getActivity(c, 2, emailIntent, PendingIntent.FLAG_IMMUTABLE)
        val notificationBuilder = NotificationCompat.Builder(c, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_foreground)
        notificationBuilder.setContentTitle("Sveglia")
        notificationBuilder.setContentText("Sono le 12:30, è l'ora di prendere n di farmaco")
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notificationBuilder.setAutoCancel(false) //almeno rimane anche dopo averci cliccato
        notificationBuilder.addAction(R.drawable.ic_baseline_sms_24, "sms a persone fidate", smsPendingIntent )
        notificationBuilder.setContentIntent(smsPendingIntent)
        notificationBuilder.addAction(R.drawable.ic_baseline_email_24, "email a medico", emailPendingIntent )
        notificationBuilder.setContentIntent(emailPendingIntent)
        val notifictionmanagerCompat = NotificationManagerCompat.from(c)
        notifictionmanagerCompat.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Avviso"
            val desc = "my description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationChannel.description = desc
            val notificationManager = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}