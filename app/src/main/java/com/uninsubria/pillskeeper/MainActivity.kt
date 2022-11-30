package com.uninsubria.pillskeeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    lateinit var addButton : com.google.android.material.floatingactionbutton.FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth //inizializza col db
        addButton = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.floatingActionButton)
        addButton.setOnClickListener {
            onAddClick()
        }
    }

    private fun onAddClick() { //quando pulsante cliccato
        val intent = Intent(this, AddPillActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() { //quando l'app si avvia
        super.onStart()
        if(auth.currentUser == null) { // Se l'utente non già loggato (non-null)
            val intent = Intent(this, LogInActivity::class.java) //vado al login
            startActivity(intent)
            finish()
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
}