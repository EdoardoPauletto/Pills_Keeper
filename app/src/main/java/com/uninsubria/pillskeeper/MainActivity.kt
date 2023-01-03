package com.uninsubria.pillskeeper

import android.Manifest
import android.app.*
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    lateinit var addButton : com.google.android.material.floatingactionbutton.FloatingActionButton
    val listaFarmaci = ArrayList<Farmaco>()
    val listaKey = ArrayList<String>()
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
            //R.id.not -> Notification()
            R.id.logout -> onLogoutClick()
            R.id.not -> setAlarm()
            R.id.geo -> openMaps()
            R.id.refresh -> caricaFarmaci()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openMaps() {
        // Search for restaurants nearby
        val gmmIntentUri = Uri.parse("geo:0,0?q=farmacia")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        //mapIntent.setPackage("com.google.android.apps.maps") togliendo questo si può scelgiere quale navigatore utilizzare
        startActivity(mapIntent)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "permesso consentito", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permesso rifiutato", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    private fun onLogoutClick(){
        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle("Attenzione")
            setMessage("Vuoi davvero effettuare il logout dall'account " + auth.currentUser!!.email + " ?")
            setPositiveButton("Sì", logout)
            setNegativeButton("No", undo)
            show()
        }
    }
    private val logout = { _: DialogInterface, _: Int ->
        Toast.makeText(this, "Logout...", Toast.LENGTH_SHORT).show()
        //auth.signOut() ne basta uno dei due, da capire perchè
        Firebase.auth.signOut()
        onStart()
    }
    private val undo = { _: DialogInterface, _: Int ->
        Toast.makeText(this, "Operazione annullata", Toast.LENGTH_SHORT).show()
    }

    private fun Notification(){
        createNotificationChannel()

        val date = Date()
        val notificationId = SimpleDateFormat("ddHHmmss", Locale.ITALIAN).format(Date()).toInt()

        val uri = Uri.parse("smsto:+393467635500")
        val smsIntent = Intent(Intent.ACTION_SENDTO, uri)
        smsIntent.putExtra("sms_body", "mi dovresti andare a comprare la .... , grazie")
        val smsPendingIntent = PendingIntent.getActivity(this, 1, smsIntent, PendingIntent.FLAG_IMMUTABLE)
        val emailIntent = Intent(Intent.ACTION_VIEW)
        val data: Uri = Uri.parse("mailto:?subject=" + "Buongiorno dottore" + "&body=" + "Volevo avvisarla che ho esaurito la ...." + "&to=" + "giangifumagalli1@gmail.com")
        emailIntent.data = data
        val emailPendingIntent = PendingIntent.getActivity(this, 2, emailIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher_foreground)
        notificationBuilder.setContentTitle("Salve")
        notificationBuilder.setContentText("è mio compito avvertirla che la pillola ... è terminata")
        notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        notificationBuilder.setAutoCancel(false) //almeno rimane anche dopo averci cliccato
        notificationBuilder.addAction(R.drawable.ic_baseline_sms_24, "sms a persone fidate", smsPendingIntent )
        notificationBuilder.setContentIntent(smsPendingIntent)
        notificationBuilder.addAction(R.drawable.ic_baseline_email_24, "email a medico", emailPendingIntent )
        notificationBuilder.setContentIntent(emailPendingIntent)
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
    }

    override fun onStart() { //quando l'app si avvia
        super.onStart()
        if(auth.currentUser == null) { // Se l'utente non già loggato (non-null)
            val intent = Intent(this, LogInActivity::class.java) //vado al login
            startActivity(intent)
            finish()
        } else{
            caricaFarmaci()
            //setAlarm()
        }
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
        listaKey.clear()
        val farmaciDB = FirebaseDatabase.getInstance().getReference("Users/" + auth.currentUser!!.uid)
        farmaciDB.child("farmaci/").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) {
                    Toast.makeText(baseContext, "Non ci sono farmaci", Toast.LENGTH_SHORT).show()
                } else {
                    //Toast.makeText(baseContext, "C'è tutto!", Toast.LENGTH_SHORT).show()
                    findViewById<ImageView>(R.id.imageView2).isVisible = false//disattivo testo
                    findViewById<TextView>(R.id.textView).isVisible = false//e immagine
                    // prendo la recycleView
                    val recyclerview = findViewById<RecyclerView>(R.id.recycleView)
                    // this creates a vertical layout Manager (altrimenti non sa come visualizzarla)
                    recyclerview.layoutManager = LinearLayoutManager(MainActivity())
                    // lista degli elementi da inserire
                    for (f in snapshot.children){
                        listaKey.add(f.key.toString())
                        val tmp = f.getValue(Farmaco::class.java)
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
        val intent = Intent(this, AddPillActivity::class.java)
        intent.putExtra("key", listaKey[position])
        intent.putExtra("Farmaco", listaFarmaci[position])//per questo serve Serializable
        startActivity(intent)
    }

    private fun setAlarm(){
        WorkManager.getInstance(this).cancelAllWork()
        for ((i, f) in listaFarmaci.withIndex()){
            if (f.qTot > 0){//quelli esauriti non li schedulo
                val h = f.time.split(":")[0]
                val m = f.time.split(":")[1]
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
                while (diff<0) {
                    diff += rischedula(i)
                }
                val databaseRef = FirebaseDatabase.getInstance().getReference("Users/" + Firebase.auth.currentUser!!.uid + "/farmaci/")
                databaseRef.child(listaKey[i]).setValue(listaFarmaci[i])//carico orario nuovo
                Toast.makeText(baseContext, "Impostata alle " + listaFarmaci[i].time, Toast.LENGTH_LONG).show()
                val workRequest = OneTimeWorkRequestBuilder<BackgroundWorker>()
                    .setInitialDelay(diff, TimeUnit.SECONDS)
                    .setInputData(workDataOf("key" to listaKey[i]))
                    .build()
                WorkManager.getInstance(this).enqueue(workRequest)
            }
        }
    }

    private fun rischedula(position: Int): Long {
        var f = listaFarmaci[position]
        var newH = f.time.split(":")[0].toInt()
        var newM = f.time.split(":")[1].toInt()
        var l = 0L
        var i = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1
        if (f.day[i]) {//se oggi deve prenderlo, aggiungo solo ore
            if (f.every.contains("30")) {
                l += 1800//30*60sec
                newM += 30
            } else {
                l += (f.every.split(" ")[1].toInt() * 3600)//sec in h
                newH += f.every.split(" ")[1].toInt()
            }
            if (newM>=60){
                newM -= 60
                newH += 1
            }
            if (newH>=24)
                newH -= 24
            if (newM<10) //solo per effetto visivo
                listaFarmaci[position].time = "$newH:0$newM"
            else listaFarmaci[position].time = "$newH:$newM"
        }
        else {
            while (!f.day[i%7])
                i++
            l += (i-(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1))*86400//aggiungo 24h*n
        }

        return l
    }
}