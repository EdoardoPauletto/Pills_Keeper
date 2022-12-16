package com.uninsubria.pillskeeper

import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class prima_persona_fidata : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    lateinit var backButton: Button
    lateinit var nextButton: Button
    lateinit var addPersonButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prima_persona_fidata)
        auth = Firebase.auth //inizializza col db
        addPersonButton = findViewById<Button>(R.id.AddPersonButton)
        addPersonButton.setOnClickListener {
            onAddPersonClick()
        }
        backButton = findViewById<Button>(R.id.BackButton)
        backButton.setOnClickListener {
            onBackClick()
        }
        nextButton = findViewById<Button>(R.id.NextButton)
        nextButton.setOnClickListener {
            onNextClick()
        }
        Toast.makeText(baseContext, "Massimo 5 persone", Toast.LENGTH_SHORT).show()
    }

    private fun onBackClick() { //quando pulsante cliccato
        //val intent = Intent(this, MainActivity::class.java)
        //startActivity(intent)
        finish()
    }

    private fun onAddPersonClick() { //quando pulsante cliccato
        val intent = Intent(this, persona_fidata::class.java)
        startActivity(intent)
        //finish()
    }
    private fun onNextClick() { //quando pulsante cliccato
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}