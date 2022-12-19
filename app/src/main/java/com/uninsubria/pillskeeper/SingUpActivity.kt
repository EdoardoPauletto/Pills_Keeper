package com.uninsubria.pillskeeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

var utente = User()

class SingUpActivity : AppCompatActivity() {
    //inizializzazione variabili
    private lateinit var auth: FirebaseAuth
    lateinit var registerButton: Button
    lateinit var nameEditText: EditText
    lateinit var surnameEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var emailMedico: EditText
    lateinit var cellulare: EditText
    lateinit var loginTextView: TextView
    //persone fidate (da 1 a n) nome, cognome, ruolo(figlio,ecc)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singup)

        auth = Firebase.auth //inizializza utente col db
        registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener { onSignUpClick() }
        loginTextView = findViewById<TextView>(R.id.loginTextView)
        loginTextView.setOnClickListener { onLoginClick() }

    }

    private fun onLoginClick(){ //serve un intera funzione o basta farlo direttamente sopra??? P.S. stessa cosa nel loginActivity
        val intent = Intent(this, LogInActivity::class.java)
        startActivity(intent)
        finish()
    }

    /*private fun NextClick(){ //serve un intera funzione o basta farlo direttamente sopra??? P.S. stessa cosa nel loginActivity
        val intent = Intent(this, prima_persona_fidata::class.java)
        startActivity(intent)
        finish()
    }*/

    private fun onSignUpClick() {
        nameEditText = findViewById(R.id.Nome)
        surnameEditText = findViewById(R.id.Cognome)
        emailEditText = findViewById(R.id.emailEditText) //prendo i valori
        passwordEditText = findViewById(R.id.passwordEditText)
        cellulare = findViewById(R.id.cellulare)
        emailMedico = findViewById(R.id.emailMedico)
        val name = nameEditText.text.toString().trim()//trim rimuove gli spazi bianchi
        val surname = surnameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val cell = cellulare.text.toString().trim()
        val emailM = emailMedico.text.toString().trim()
        if (name.isEmpty()) { //controlli basilari
            nameEditText.error = "Inserisci nome" //...poi da cambiare con i dati che davvero ci servono
            return
        }
        if (email.isEmpty()) {
            emailEditText.error = "Inserisci cognome"
            return
        }
        if (email.isEmpty()) {
            emailEditText.error = "Inserisci email"
            return
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Inserisci password"
            return
        }
        if(cell.isEmpty()){
            cellulare.error = "Inserisci il tuo numero di cellulare"
            return
        }
        if(emailM.isEmpty()){
            emailMedico.error = "Inserisci l'email del medico curante"
            return
        }
        utente = User(name, surname, email, password, cell, emailM)//creo un oggetto utente
        //createUser(userName, email, password, cell, emailM) //ha senso passare ad un altra funzione...?
        val intent = Intent(this, PrimaPersonaFidata::class.java)
        startActivity(intent)
    }
}