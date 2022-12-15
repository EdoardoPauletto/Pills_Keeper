package com.uninsubria.pillskeeper

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class persona_fidata : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    lateinit var CancelButton: Button
    lateinit var SaveButton: Button
    lateinit var MiddleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.persona_fidata)
        auth = Firebase.auth //inizializza col db
        MiddleButton = findViewById<Button>(R.id.middleButton)
        MiddleButton.setOnClickListener {
            onAddPersonClick()
        }
        CancelButton = findViewById<Button>(R.id.cancelButton)
        CancelButton.setOnClickListener {
            onCancelClick()
        }
        SaveButton = findViewById<Button>(R.id.saveButton)
        SaveButton.setOnClickListener {
            onSaveClick()
        }
    }

    private fun onCancelClick() { //quando pulsante cliccato
        val intent = Intent(this, persona_fidata::class.java)
        startActivity(intent)
        finish()
    }

    private fun onAddPersonClick() { //quando pulsante cliccato
        val intent = Intent(this, persona_fidata::class.java)
        startActivity(intent)
        finish()
    }
    private fun onSaveClick() { //quando pulsante cliccato
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}