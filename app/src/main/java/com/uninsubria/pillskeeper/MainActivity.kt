package com.uninsubria.pillskeeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    lateinit var addButton : com.google.android.material.floatingactionbutton.FloatingActionButton

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
            //R.id.email -> sendEmail()
            R.id.logout -> onLogoutClick()
        }
        return super.onOptionsItemSelected(item)
    }
    @Suppress("DEPRECATION")
    private fun sendSMS(){
        val obj : SmsManager = SmsManager.getDefault() //a me da errore
        obj.sendTextMessage("+39 3467635500", null, "sei malato", null, null)
    }

    private fun onLogoutClick(){
        //auth.signOut() ne basta uno dei due, da capire perchè
        Firebase.auth.signOut()
        onStart()
    }

    private fun onAddClick() { //quando pulsante cliccato
        val intent = Intent(this, AddPillActivity::class.java)
        startActivity(intent)
        //finish() non va terminata
        //SMS
       /* val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage("+39 " + myNumber, null, myMsg, null, null)
        Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show()
        //oppure
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)));
        //o
        Intent it = new Intent(Intent.ACTION_SENDTO, uri); 
        it.putExtra("sms_body", "Here you can set the SMS text to be sent"); 
        startActivity(it);
        //mail
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
      
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
                    val data = ArrayList<Upload>()
                    for (f in snapshot.children){
                        val tmp = f.getValue(Upload::class.java)
                        //tmp!!.mImageUrl = "https://firebasestorage.googleapis.com/v0/b/prove-b822e.appspot.com/o" + tmp.mImageUrl + "?alt=media&token=aeeefb3e-c3ac-4da3-a62c-0bd67a420c3e"
                        data.add(tmp!!)
                    }
                    //data.add(Upload("Item ", "https://firebasestorage.googleapis.com/v0/b/prove-b822e.appspot.com/o/1656494538372.jpg?alt=media&token=aeeefb3e-c3ac-4da3-a62c-0bd67a420c3e"))

                    // This will pass the ArrayList to our Adapter
                    val adapter = PilloleAdapter(data)

                    // Setting the Adapter with the recyclerview
                    recyclerview.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", error.message); //Don't ignore errors!
            }
        })
    }
}