package com.uninsubria.pillskeeper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class BackgroundWorker(val c: Context, p: WorkerParameters) : Worker(c, p) {
    private val CHANNEL_ID = "canale01"
    private val databaseRef = FirebaseDatabase.getInstance().getReference("Users/" + Firebase.auth.currentUser!!.uid + "/farmaci/")
    private lateinit var farmaco: Farmaco

    override fun doWork(): Result {
        //leggi dal db info farmaco
        //qntTot - q (perchè presa una)
        //if qntTot <= 2 avviso farmaco quasi finito
        //leggi orario
        //rischedula, richiamando il worker ad un altro orario
        val key = inputData.getString("key")
        databaseRef.child(key!!).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                farmaco = snapshot.getValue(Farmaco::class.java)!!
                val nonSforo = farmaco.day[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1]
                if (nonSforo){
                    createNotificationChannel()
                    sendRemainderNotification()
                    aggiornaQnt(key)
                    aggiornaOrario(key)
                    riSchedula(key)
                } else
                    aggiornaOrario(key)
                    riSchedula(key)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", error.message); //Don't ignore errors!
            }
        })

        return Result.success()
    }

    private fun sendRemainderNotification() {
        val main = Intent(c, MainActivity::class.java)
        val pendingMain = PendingIntent.getActivity(c, 3, main, PendingIntent.FLAG_IMMUTABLE)
        val notificationId = SimpleDateFormat("ddHHmmss", Locale.ITALIAN).format(Date()).toInt()//così se più notifiche allo stesso tempo non va
        val notificationBuilder = NotificationCompat.Builder(c, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_foreground)
        notificationBuilder.setContentTitle("Sveglia")
        notificationBuilder.setContentText("Sono le ${farmaco.time}, è l'ora di prendere ${farmaco.q} di ${farmaco.name}")
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notificationBuilder.setAutoCancel(false) //almeno rimane anche dopo averci cliccato
        notificationBuilder.setContentIntent(pendingMain)
        val notifictionmanagerCompat = NotificationManagerCompat.from(c)
        notifictionmanagerCompat.notify(notificationId, notificationBuilder.build())
    }

    private fun aggiornaQnt(key: String) {
        farmaco.qTot = farmaco.qTot - farmaco.q
        if ((farmaco.qTot-farmaco.q*2) <= 0)
            sendFewPillsNotification()
    }

    private fun aggiornaOrario(key: String) {
        val now = Calendar.getInstance()
        var h = farmaco.time.split(":")[0].toInt()
        var m = farmaco.time.split(":")[1].toInt()
        var count = 0
        do {//assegno nuovo orario
            count++
            if (farmaco.every.contains("30")){
                m += 30
                if (m >= 60){
                    m -= 60
                    h++
                }
            }
            else if (farmaco.every.contains("giorni")){
                //nulla
            } else {
                h += (farmaco.every.split(" ")[1].toInt())
                if (h >= 24) h-=24
            }
        } while ((now.get(Calendar.HOUR_OF_DAY)!=h || now.get(Calendar.MINUTE)-m > 1) && count!=2)//lo fa al massimo 2 volte(se già non lanciato al suo normale orario)
        farmaco.time = "$h:$m"
        databaseRef.child(key).setValue(farmaco)
    }

    private fun sendFewPillsNotification() {
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
        notificationBuilder.setContentTitle("Attenzione!")
        notificationBuilder.setContentText("Rimangono solo ${farmaco.qTot} di ${farmaco.name}, stanno per finire!")
        if (farmaco.qTot == 1.0) notificationBuilder.setContentText("Rimane solo 1 di ${farmaco.name}, stanno per finire!")
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notificationBuilder.setAutoCancel(false) //almeno rimane anche dopo averci cliccato
        notificationBuilder.addAction(R.drawable.ic_baseline_sms_24, "sms a persone fidate", smsPendingIntent )
        notificationBuilder.setContentIntent(smsPendingIntent)
        notificationBuilder.addAction(R.drawable.ic_baseline_email_24, "email a medico", emailPendingIntent )
        notificationBuilder.setContentIntent(emailPendingIntent)
        val notifictionmanagerCompat = NotificationManagerCompat.from(c)
        notifictionmanagerCompat.notify(notificationId, notificationBuilder.build())
    }

    private fun riSchedula(key: String){
        val now = Calendar.getInstance()
        if (farmaco.qTot > 0){
            val h = farmaco.time.split(":")[0]
            val m = farmaco.time.split(":")[1]
            val calendar = Calendar.getInstance()
            calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                h.toInt(),
                m.toInt(),
                0
            )
            var diff = (calendar.timeInMillis/1000L)-(Calendar.getInstance().timeInMillis/1000L)
            if (diff<0){
                var i = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1
                while (!farmaco.day[i%7])
                    i++
                diff += (i-(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1))*86400//aggiungo 24h*n
            }
            val workRequest = OneTimeWorkRequestBuilder<BackgroundWorker>()
                .setInitialDelay(diff, TimeUnit.SECONDS)
                .setInputData(workDataOf("key" to key))
                .build()
            WorkManager.getInstance(c).enqueue(workRequest)
            /*var delay = 0
            var i = now.get(Calendar.DAY_OF_WEEK)-1
            while (!farmaco.day[i%7])
                i++
            delay = (i-(now.get(Calendar.DAY_OF_WEEK)-1))*1440 //oggi+(tra n giorni)-oggi *(minuti in 24h)
            val workRequest = OneTimeWorkRequestBuilder<BackgroundWorker>()
                .setInputData(workDataOf("key" to key))
            if (ns){//se non sforo (sforo se per esempio imposto la prossima alle 2 di giovedì, ma giovedì non è tra i giorni true)
                if (farmaco.every.contains("30"))
                    delay += 30
                else if (farmaco.every.contains("giorni")){
                    //nulla
                } else {
                    delay += (farmaco.every.split(" ")[1].toInt()*60)
                }
            }
            workRequest.setInitialDelay(delay.toLong(), TimeUnit.MINUTES)
            WorkManager.getInstance(c).enqueue(workRequest.build())*/
        }
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