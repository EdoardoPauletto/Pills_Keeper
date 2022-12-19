package com.uninsubria.pillskeeper

import android.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class PrimaPersonaFidata : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var addPersonButton: Button
    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var telEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.prima_persona_fidata)
        auth = Firebase.auth //inizializza col db
        addPersonButton = findViewById(R.id.AddPersonButton)
        backButton = findViewById(R.id.BackButton)
        nextButton = findViewById(R.id.NextButton)
        addPersonButton.setOnClickListener { onAddPersonClick() }
        backButton.setOnClickListener { onBackClick() }
        nextButton.setOnClickListener { onNextClick() }
    }

    private fun onBackClick() { //svuoto la lista di persone fidate e torno indietro
        utente.personeFidate.clear()
        finish()
    }

    private fun onAddPersonClick() { //salvo persona, mostro avviso, apro l'altra
        try {
            salvaPersonaFidata()
            Toast.makeText(baseContext, "Massimo 5 persone", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PersonaFidata::class.java)
            startActivity(intent)
        } catch (n: NullPointerException){}

    }

    private fun onNextClick() {
        try {
            salvaPersonaFidata()
            createUser()
        } catch (n: NullPointerException){}
    }

    private fun salvaPersonaFidata(){
        nameEditText = findViewById(R.id.NomePersonaFidata)
        surnameEditText = findViewById(R.id.CognomePersonaFidata)
        telEditText = findViewById(R.id.telefonoPersonaFidata)
        val nome = nameEditText.text.toString().trim()
        val cognome = surnameEditText.text.toString().trim()
        val cell = telEditText.text.toString().trim()
        if (nome.isEmpty()) { //controlli basilari
            nameEditText.error = "Inserisci un nome"
            throw NullPointerException()
        }
        if (cognome.isEmpty()) { //controlli basilari
            surnameEditText.error = "Inserisci un cognome"
            throw NullPointerException()
        }
        if (cell.isEmpty()) { //controlli basilari
            telEditText.error = "Inserisci un numero di telefono"
            throw NullPointerException()
        }
        utente.addPersonaFidata(ContattiFidati(nome, cognome, cell))
    }

    private fun createUser() {
        Toast.makeText(baseContext, "Salvataggio in corso...", Toast.LENGTH_SHORT).show()
        auth.createUserWithEmailAndPassword(utente.email, utente.password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // Se registrato correttamente
                    val currentUser = auth.currentUser
                    val uid = currentUser!!.uid //prendo l'ID univoco creato automaticamente (e per forza esistente) !!! MEGLIO LA MAIL
                    val database = FirebaseDatabase.getInstance().getReference("Users").child(uid) //lo salvo nel RealTimeDB -> Users/emailUtente
                    database.setValue(utente).addOnCompleteListener { task ->
                        if (task.isSuccessful) { //se tutto va a buon fine, vado nel Main
                            val intent = Intent(this, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) //termina le schede precedenti
                            startActivity(intent)
                            finish()
                        }
                    }
                } else { // Se registrazione fallisce, mostro un Alert, devo anche cancellare tutto!!!!!!!!
                    val builder = AlertDialog.Builder(this)
                    with(builder) {
                        setTitle("Creazione di un nuovo utente fallita")
                        setMessage(task.exception?.message) //tradurre ogni possibile eccezione?
                        setPositiveButton("OK", null)
                        show()
                    }
                }
            }
    }
}