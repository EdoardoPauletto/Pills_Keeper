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
    lateinit var nameEditText: EditText
    lateinit var surnameEditText: EditText
    lateinit var telEditText: EditText

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
        Toast.makeText(baseContext, "Massimo 5 persone", Toast.LENGTH_SHORT).show()
    }

    private fun onBackClick() { //quando pulsante cliccato
        //val intent = Intent(this, MainActivity::class.java)
        //startActivity(intent)
        finish()
    }

    private fun onAddPersonClick() { //quando pulsante cliccato
        salvaPersonaFidata()
        val intent = Intent(this, PersonaFidata::class.java)
        startActivity(intent)
        //finish()
    }

    private fun onNextClick() { //quando pulsante cliccato
        salvaPersonaFidata()
        createUser()
        /*val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()*/
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
            return
        }
        if (cognome.isEmpty()) { //controlli basilari
            surnameEditText.error = "Inserisci un cognome"
            return
        }
        if (cell.isEmpty()) { //controlli basilari
            telEditText.error = "Inserisci un numero di telefono"
            return
        }
        utente.addPersonaFidata(ContattiFidati(nome, cognome, cell))
    }

    private fun createUser() {
        auth.createUserWithEmailAndPassword(utente.email, utente.pwd)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // Se registrato correttamente, lo scrivo nel log e...
                    Log.d("PrimaPersonaFidata", "createUserWithEmail:success")
                    val currentUser = auth.currentUser
                    val uid = currentUser!!.uid //prendo l'ID univoco creato automaticamente (e per forza esistente) !!! MEGLIO LA MAIL
                    val database = FirebaseDatabase.getInstance().getReference("Users").child(uid) //lo salvo nel RealTimeDB -> Users/emailUtente
                    database.setValue(utente).addOnCompleteListener { task ->
                        if (task.isSuccessful) { //se tutto va a buon fine, vado nel Main
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else { // Se registrazione fallisce, mostro un Alert, devo anche cancellare tutto!!!!!!!!
                    Log.w("PrimaPersonaFidata", "createUserWithEmail:failure", task.exception)
                    //Toast.makeText(baseContext, "Autenticazione fallita", Toast.LENGTH_SHORT).show()
                    val builder = AlertDialog.Builder(this)
                    with(builder) {
                        setTitle("Creazione di un nuovo utente fallita fallita")
                        setMessage(task.exception?.message) //tradurre ogni possibile eccezione?
                        setPositiveButton("OK", null)
                        show()
                    }
                }
            }
    }
}