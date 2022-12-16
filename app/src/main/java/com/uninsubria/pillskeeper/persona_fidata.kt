package com.uninsubria.pillskeeper

import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class persona_fidata : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    lateinit var cancelButton: Button
    lateinit var saveButton: Button
    lateinit var middleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.persona_fidata)
        auth = Firebase.auth //inizializza col db
        middleButton = findViewById<Button>(R.id.middleButton)
        middleButton.setOnClickListener {
            onAddPersonClick()
        }
        cancelButton = findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            onCancelClick()
        }
        saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            onSaveClick()
        }
        //leggere quanti utenti già salvati
        //stampare i rimanenti
    }

    private fun onCancelClick() { //quando pulsante cliccato
        //val intent = Intent(this, persona_fidata::class.java)
        //startActivity(intent)
        finish()
    }

    private fun onAddPersonClick() { //quando pulsante cliccato
        //se già a 5, o disattivare pulsante oppure msg errore
        val intent = Intent(this, persona_fidata::class.java)
        startActivity(intent)
        //finish()
    }
    private fun onSaveClick() { //quando pulsante cliccato
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}