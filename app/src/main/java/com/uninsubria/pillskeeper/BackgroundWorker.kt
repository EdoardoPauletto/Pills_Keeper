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
    private val databaseRef = FirebaseDatabase.getInstance().getReference("Users/" + Firebase.auth.currentUser!!.uid)
    private lateinit var farmaco: Farmaco

    override fun doWork(): Result {
        //leggi dal db info farmaco
        //qntTot - q (perchè presa una)
        //if qntTot <= 2 avviso farmaco quasi finito
        //leggi orario
        //rischedula, richiamando il worker ad un altro orario
        val key = inputData.getString("key")
        databaseRef.child("farmaci/$key").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                farmaco = snapshot.getValue(Farmaco::class.java)!!
                val nonSforo = farmaco.day[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1]//se erano le 23:40 e la nuova sveglia doveva suonare dopo 30min, va al giorno dopo, ma se il giorno dopo (ovvero quando il work viene chiamato) non devono suonare sveglie...
                if (nonSforo) {
                    createNotificationChannel()
                    sendRemainderNotification()
                    aggiornaQnt(key!!)
                }//se sforo ri imposto l'allarme soltanto
                //riSchedula(key, aggiornaOrario(key))
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
        databaseRef.child("farmaci/$key").setValue(farmaco)//e aggiorno sul db
        if ((farmaco.qTot-farmaco.q*2) <= 0)
            sendFewPillsNotification()
    }

    private fun aggiornaOrario(key: String): Long {
        val now = Calendar.getInstance()
        var newH = farmaco.time.split(":")[0].toInt()
        var newM = farmaco.time.split(":")[1].toInt()
        var delay = 0L
        var i = now.get(Calendar.DAY_OF_WEEK)-1
        if (farmaco.day[i]) {//se oggi deve prenderlo, aggiungo solo ore
            if (farmaco.every.contains("30")) {
                delay += 1800//30*60sec
                newM += 30
            } else {
                delay += (farmaco.every.split(" ")[1].toInt() * 3600)//sec in h
                newH += farmaco.every.split(" ")[1].toInt()
            }
            if (newM>=60){
                newM -= 60
                newH += 1
            }
            if (newH>=24)
                newH -= 24
            if (newM<10) //solo per effetto visivo
                farmaco.time = "$newH:0$newM"
            else farmaco.time = "$newH:$newM"
            databaseRef.child("farmaci/$key").setValue(farmaco)//e aggiorno sul db
        }
        else {
            while (!farmaco.day[i%7])
                i++
            delay += (i-(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1))*86400//aggiungo 24h*n
        }
        return delay
    }

    private fun sendFewPillsNotification() {
        //1)preparo la notifica
        val notificationBuilder = NotificationCompat.Builder(c, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_foreground)
        notificationBuilder.setContentTitle("Attenzione!")
        notificationBuilder.setContentText("Rimangono solo ${farmaco.qTot} di ${farmaco.name}, stanno per finire!")
        if (farmaco.qTot == 1.0) notificationBuilder.setContentText("Rimane solo 1 di ${farmaco.name}, stanno per finire!")
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notificationBuilder.setAutoCancel(false) //almeno rimane anche dopo averci cliccato
        //2)preparo gli intent (sms e ...)
        databaseRef.child("personeFidate").addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var numeri = ""
                for (p in snapshot.children) {
                    var tmp = p.getValue(ContattiFidati::class.java)
                    if (numeri == "") numeri += tmp!!.tel
                    else numeri += ", ${tmp!!.tel}"
                }
                val uri = Uri.parse("smsto:$numeri")//si possono mettere più numeri
                val smsIntent = Intent(Intent.ACTION_SENDTO, uri)
                smsIntent.putExtra("sms_body", "Mi rimangono solo ${farmaco.qTot} di ${farmaco.name}")
                val smsPendingIntent = PendingIntent.getActivity(c, 1, smsIntent, PendingIntent.FLAG_IMMUTABLE)
                notificationBuilder.addAction(R.drawable.ic_baseline_sms_24, "sms a persone fidate", smsPendingIntent )
                notificationBuilder.setContentIntent(smsPendingIntent)
                //3)preparo gli intent (... e mail medico)
                databaseRef.child("emailMedico").addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val mailMedico = snapshot.getValue(String::class.java)!!
                        val emailIntent = Intent(Intent.ACTION_VIEW)
                        val data: Uri = Uri.parse("mailto:?subject=" + "Esaurimento medicine" + "&body=" + "Buongiorno Dottore,\nVolevo avvisarla che mi rimangono solo ${farmaco.qTot} di ${farmaco.name}.\nMe ne potrebbe prescrivere altre?\nGrazie" + "&to=" + mailMedico)
                        emailIntent.data = data
                        val emailPendingIntent = PendingIntent.getActivity(c, 2, emailIntent, PendingIntent.FLAG_IMMUTABLE)
                        notificationBuilder.addAction(R.drawable.ic_baseline_email_24, "email a medico", emailPendingIntent )
                        notificationBuilder.setContentIntent(emailPendingIntent)
                        //4)genero la notifica
                        val notificationId = SimpleDateFormat("ddHHmmss", Locale.ITALIAN).format(Date()).toInt()//così se più notifiche allo stesso tempo non va
                        val notifictionmanagerCompat = NotificationManagerCompat.from(c)
                        notifictionmanagerCompat.notify(notificationId, notificationBuilder.build())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("TAG", error.message); //Don't ignore errors!
                    }
                })
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", error.message); //Don't ignore errors!
            }
        })
    }

    private fun riSchedula(key: String, diff: Long){
        val workRequest = OneTimeWorkRequestBuilder<BackgroundWorker>()
            .setInitialDelay(diff, TimeUnit.SECONDS)
            .setInputData(workDataOf("key" to key))
            .build()
        WorkManager.getInstance(c).enqueue(workRequest)
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