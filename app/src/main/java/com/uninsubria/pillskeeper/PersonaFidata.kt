package com.uninsubria.pillskeeper

import android.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class PersonaFidata : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth //variabile se già loggato o meno
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var middleButton: Button
    private val num = (5 - utente.personeFidate.size) //leggere quanti utenti già salvati
    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var telEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.persona_fidata)
        auth = Firebase.auth //inizializza col db
        middleButton = findViewById(R.id.middleButton)
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)
        middleButton.setOnClickListener { onAddPersonClick() }
        cancelButton.setOnClickListener { onCancelClick() }
        saveButton.setOnClickListener { onSaveClick() }
        //stampare i rimanenti
        Toast.makeText(baseContext, "Ancora $num spazi", Toast.LENGTH_SHORT).show()
        if (num == 1){
            middleButton.isEnabled = false
            val builder = AlertDialog.Builder(this)
            with(builder) {
                setTitle("Numero massimo raggiunto")
                setMessage("Salvate 4 persone, ancora solo una persona memorizzabile.")
                setPositiveButton("OK", null)
                show()
            }
        }
    }

    private fun onCancelClick() { //ogni volta che torno indietro cancello l'ultimo inserimento
        if (utente.personeFidate.size > 1)
            utente.personeFidate.removeLast()
        finish()
    }

    private fun onAddPersonClick() {
        try {
            salvaPersonaFidata()
            val intent = Intent(this, PersonaFidata::class.java)
            startActivity(intent)
        } catch (n: NullPointerException){}
    }

    private fun onSaveClick() {
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