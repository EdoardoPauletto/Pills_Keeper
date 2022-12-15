package com.uninsubria.pillskeeper

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class prima_persona_fidata : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    lateinit var BackButton: Button
    lateinit var NextButton: Button
    lateinit var AddPersonButton: Button

    //@SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prima_persona_fidata)
        auth = Firebase.auth //inizializza col db
        AddPersonButton = findViewById<Button>(R.id.AddPersonButton)
        AddPersonButton.setOnClickListener {
            onAddPersonClick()
        }
        BackButton = findViewById<Button>(R.id.BackButton)
        BackButton.setOnClickListener {
            onBackClick()
        }
        NextButton = findViewById<Button>(R.id.NextButton)
        NextButton.setOnClickListener {
            onNextClick()
        }
    }

    private fun onBackClick() { //quando pulsante cliccato
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onAddPersonClick() { //quando pulsante cliccato
        val intent = Intent(this, persona_fidata::class.java)
        startActivity(intent)
        finish()
    }
    private fun onNextClick() { //quando pulsante cliccato
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}