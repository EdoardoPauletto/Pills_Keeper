package com.uninsubria.pillskeeper

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    lateinit var addButton : com.google.android.material.floatingactionbutton.FloatingActionButton
    val listaFarmaci = ArrayList<Upload>()
    private companion object{
        private const val CHANNEL_ID = "canale01"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth //inizializza col db
        addButton = findViewById(R.id.floatingActionButton)
        addButton.setOnClickListener { onAddClick() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.rev_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.sms -> sendSMS()
            R.id.email -> sendEmail()
            R.id.not -> Notification()
            R.id.logout -> onLogoutClick()
            //R.id.geo -> setAlarm()
            R.id.geo -> openMaps()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openMaps() {
        // Search for restaurants nearby
        val gmmIntentUri = Uri.parse("geo:0,0?q=farmacia")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        //mapIntent.setPackage("com.google.android.apps.maps") togliendo questo si può scelgiere quale navigatore utilizzare
        startActivity(mapIntent)
    }

    private fun sendSMS(){
        /*val obj : SmsManager = SmsManager.getDefault() //a me da errore
        obj.sendTextMessage("+39 3467635500", null, "sei malato", null, null)*/
        /*val it =  Intent(Intent.ACTION_SENDTO, 3467635500);
        it.putExtra("sms_body", "Here you can set the SMS text to be sent");
        startActivity(it);*/
            val uri = Uri.parse("smsto:3776894189")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra("sms_body", "Here goes your message...")
            startActivity(intent)

    }

    private fun sendEmail(){
        val intent = Intent(this, send_email::class.java)
        startActivity(intent)
        finish()
    }

    private fun onLogoutClick(){
        //auth.signOut() ne basta uno dei due, da capire perchè
        Firebase.auth.signOut()
        onStart()
    }

    private fun Notification(){
        createNotificationChannel()

        val date = Date()
        val notificationId = SimpleDateFormat("ddHHmmss", Locale.ITALIAN).format(date).toInt()

        val intent = Intent(this, AddPillActivity::class.java)
        intent.putExtra("key", "35")
        //startActivity(intent)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val mainPendingInetnt = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.drawable.pill_icon_main)
        notificationBuilder.setContentTitle("Aspetta Aspetta ASPETTA")
        notificationBuilder.setContentText("non hai nessuna medicina aggiunta o almeno segnalata")
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setContentIntent(mainPendingInetnt)
        val notifictionmanagerCompat = NotificationManagerCompat.from(this)
        notifictionmanagerCompat.notify(notificationId, notificationBuilder.build())
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name: CharSequence = "mynotifica"
            val desc = "my description"

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationChannel.description = desc
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun onAddClick() { //quando pulsante cliccato
        val intent = Intent(this, AddPillActivity::class.java)
        startActivity(intent)
        //finish() non va terminata
        //SMS
       /* val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage("+39 " + myNumber, null, myMsg, null, null)
        Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show()*/
        //oppure
        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
        //o

        //mail
       /* Intent emailIntent = new Intent(Intent.ACTION_SEND);
      
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        //oppure
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, "");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }*/
        //recycleView https://www.geeksforgeeks.org/android-recyclerview-in-kotlin/ (foto, nome, quantità da assumere, quantità confezione)
    }

    override fun onStart() { //quando l'app si avvia
        super.onStart()
        if(auth.currentUser == null) { // Se l'utente non già loggato (non-null)
            val intent = Intent(this, LogInActivity::class.java) //vado al login
            startActivity(intent)
            finish()
        } else
            caricaFarmaci()
    }
    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (id == R.id.action_settings){

        } else if (id == R.id.action_new_item){

        } else if (id == R.id.action_logout){
            auth.signOut()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }*/

    private fun caricaFarmaci(){
        listaFarmaci.clear()
        val farmaciDB = FirebaseDatabase.getInstance().getReference("Users/" + auth.currentUser!!.uid)
        farmaciDB.child("farmaci/").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) {
                    Toast.makeText(baseContext, "Non c'è cartella farmaci", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, "C'è tutto!!!", Toast.LENGTH_SHORT).show()
                    findViewById<ImageView>(R.id.imageView2).isVisible = false //disattivo testo e immagine
                    findViewById<TextView>(R.id.textView).isVisible = false
                    val mStorageRef = FirebaseStorage.getInstance().reference
                    // prendo la recycleView
                    val recyclerview = findViewById<RecyclerView>(R.id.recycleView)

                    // this creates a vertical layout Manager
                    recyclerview.layoutManager = LinearLayoutManager(MainActivity())

                    // lista degli elementi da inserire
                    for (f in snapshot.children){
                        val tmp = f.getValue(Upload::class.java)
                        //tmp!!.mImageUrl = "https://firebasestorage.googleapis.com/v0/b/prove-b822e.appspot.com/o" + tmp.mImageUrl + "?alt=media&token=aeeefb3e-c3ac-4da3-a62c-0bd67a420c3e"
                        listaFarmaci.add(tmp!!)
                    }

                    // This will pass the ArrayList to our Adapter
                    val adapter = PilloleAdapter(listaFarmaci) { position -> onListItemClick(position) }

                    // Setting the Adapter with the recyclerview
                    recyclerview.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", error.message); //Don't ignore errors!
            }
        })
    }

    private fun onListItemClick(position: Int) {
        //Toast.makeText(baseContext, "ciao " + listaFarmaci[position].name, Toast.LENGTH_SHORT).show()
        val intent = Intent(this, AddPillActivity::class.java)
        intent.putExtra("nomeFarmaco", listaFarmaci[position].name)
        startActivity(intent)
    }

    private fun setAlarm(){
        /*val calendar: Calendar = Calendar.getInstance()
        calendar.set(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            17,
            10,
            0
        )*/
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= 31){
            if (!alarmManager.canScheduleExactAlarms()) { //da android 12 bisogna dare il permesso
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                startActivity(intent)
            }
            Toast.makeText(this, "Può? " + alarmManager.canScheduleExactAlarms(), Toast.LENGTH_SHORT).show()
        }
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 5, intent, FLAG_MUTABLE)
        alarmManager.set(
            AlarmManager.RTC,
            3000,
            pendingIntent
        )
        //Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show()
    }
}