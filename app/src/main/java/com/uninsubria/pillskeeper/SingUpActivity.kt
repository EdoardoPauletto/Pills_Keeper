package com.uninsubria.pillskeeper

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SingUpActivity : AppCompatActivity() {
    //inizializzazione variabili
    private lateinit var auth: FirebaseAuth
    private var TAG = "SignUpActivity"
    lateinit var registerButton: Button
    lateinit var nameEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText
    //lateinit var emailMedico: EditText
    //lateinit var cellulare: EditText
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
        emailEditText = findViewById<EditText>(R.id.emailEditText) //prendo i valori
        val email = emailEditText.text.toString().trim() //trim rimuove gli spazi bianchi
       /* emailMedico = findViewById<EditText>(R.id.emailMedico)
        val emailM = emailEditText.text.toString().trim()
        cellulare = findViewById<EditText>(R.id.cellulare)
        val cell = emailEditText.text.toString().trim()*/
        passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val password = passwordEditText.text.toString().trim()
        nameEditText = findViewById<EditText>(R.id.nameEditText)
        val userName = nameEditText.text.toString().trim()
        if (userName.isEmpty()) { //controlli basilari
            nameEditText.error = "Inserisci userName" //...poi da cambiare con i dati che davvero ci servono
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
        /*if(emailM.isEmpty()){
            emailMedico.error = "Inserisci l'email del medico curante"
            return
        }
        if(cell.isEmpty()){
            cellulare.error = "Inserisci il tuo numero di cellulare"
            return
        }*/
        createUser(userName, email, password) //ha senso passare ad un altra funzione...?
    }

    private fun createUser(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // Se registrato correttamente, lo scrivo nel log e...
                    Log.d(TAG, "createUserWithEmail:success")
                    val currentUser = auth.currentUser
                    val uid = currentUser!!.uid //prendo l'ID univoco creato automaticamente (e per forza esistente) !!! MEGLIO LA MAIL
                    val userMap = HashMap<String, String>() //faccio chiave,valore con name=...
                    userMap["name"] = userName
                    userMap["email"] = email
                    userMap["password"] = password
                    //userMap["email medico"] = emailM
                    //userMap["numero di cellulare"] = cell
                    val database = FirebaseDatabase.getInstance().getReference("Users").child(uid) //lo salvo nel RealTimeDB -> Users/emailUtente
                    database.setValue(userMap).addOnCompleteListener { task ->
                        if (task.isSuccessful) { //se tutto va a buon fine, vado nel Main
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else { // Se registrazione fallisce, mostro un toast !!! mentre nel login mostravo un Alert
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    //Toast.makeText(baseContext, "Autenticazione fallita", Toast.LENGTH_SHORT).show()
                    val builder = AlertDialog.Builder(this)
                    with(builder) {
                        setTitle("Creazione di un nuovo utente fallita fallita")
                        setMessage(task.exception?.message) //tradurre ogni possibile eccezione
                        setPositiveButton("OK", null)
                        show()
                    }
                }
            }
    }
}