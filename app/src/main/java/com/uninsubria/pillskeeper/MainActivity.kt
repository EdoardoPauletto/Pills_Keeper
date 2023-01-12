package com.uninsubria.pillskeeper

import android.Manifest
import android.app.*
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    private lateinit var addButton : com.google.android.material.floatingactionbutton.FloatingActionButton
    private var listaFarmaci = ArrayList<Farmaco>()
    private var listaKey = ArrayList<String>()
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

        //mapIntent.setPackage("com.google.android.apps.maps") togliendo questo si può scelgiere quale navigatore utilizzare
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val gmmIntentUri = Uri.parse("geo:0,0?q=farmacia")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "permesso consentito", Toast.LENGTH_SHORT).show()
                        startActivity(mapIntent)
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

    private fun caricaFarmaci(){
        var files = cacheDir
        var fileKey = File("")
        var fileFarmaci = File("")
        for (f in files.listFiles()){
            if (f.name.contains("listaKey"))
                fileKey = f
            else if (f.name.contains("listaFarmaci"))
                fileFarmaci = f
        }
        if (fileKey.path != "" && fileFarmaci.path != ""){//la prima volta (dopo register) DA ERRORE, esistono ma vuoti
            //esiste, lo leggo
            listaKey = ObjectInputStream(FileInputStream(fileKey)).readObject() as ArrayList<String>
            listaFarmaci = ObjectInputStream(FileInputStream(fileFarmaci)).readObject() as ArrayList<Farmaco>
            createRecycle()//così anche offline crea la lista
        }
        else{
            //non esiste, lo creo
            fileKey = File.createTempFile("listaKey",null,baseContext.cacheDir)
            fileFarmaci = File.createTempFile("listaFarmaci",null,baseContext.cacheDir)
        }

        var newListaFarmaci = ArrayList<Farmaco>()
        val newListaKey = ArrayList<String>()
        val farmaciDB = FirebaseDatabase.getInstance().getReference("Users/" + auth.currentUser!!.uid)
        farmaciDB.child("farmaci/").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) {
                    Toast.makeText(baseContext, "Non ci sono farmaci", Toast.LENGTH_SHORT).show()
                } else {
                    // lista degli elementi da inserire
                    for (f in snapshot.children){
                        newListaKey.add(f.key.toString())
                        val tmp = f.getValue(Farmaco::class.java)
                        newListaFarmaci.add(tmp!!)
                    }
                    var cambiato = false
                    if (listaKey.size == newListaKey.size && listaFarmaci.size == newListaKey.size){
                        for ((i, k) in listaKey.withIndex()){
                            if (k != newListaKey[i] || listaFarmaci[i] != newListaFarmaci[i])
                                cambiato=true
                        }
                    } else cambiato=true
                    if (cambiato){//se cancello è DA GESTIRE
                        listaKey.clear()
                        listaKey = newListaKey
                        listaFarmaci.clear()
                        listaFarmaci = newListaFarmaci
                        Toast.makeText(baseContext, "Aggiorno", Toast.LENGTH_SHORT).show()
                        ObjectOutputStream(FileOutputStream(fileKey)).writeObject(listaKey)
                        ObjectOutputStream(FileOutputStream(fileFarmaci)).writeObject(listaFarmaci)
                        createRecycle()//aggiorno recycle
                        setAlarm()//aggiorno gli allarmi
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", error.message) //Don't ignore errors!
            }
        })
    }
    private fun createRecycle(){
        //Toast.makeText(baseContext, "C'è tutto!", Toast.LENGTH_SHORT).show()
        findViewById<ImageView>(R.id.imageView2).isVisible = false//disattivo testo
        findViewById<TextView>(R.id.textView).isVisible = false//e immagine
        // prendo la recycleView
        val recyclerview = findViewById<RecyclerView>(R.id.recycleView)
        // this creates a vertical layout Manager (altrimenti non sa come visualizzarla)
        recyclerview.layoutManager = LinearLayoutManager(MainActivity())
        // This will pass the ArrayList to our Adapter
        val adapter = PilloleAdapter(listaFarmaci) { position -> onListItemClick(position) }
        // Setting the Adapter with the recyclerview
        recyclerview.adapter = adapter
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
        val f = listaFarmaci[position]
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