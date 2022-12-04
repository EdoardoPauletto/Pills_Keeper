package com.uninsubria.pillskeeper

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogInActivity : AppCompatActivity() {
    //inizializzazione variabili
    private lateinit var auth: FirebaseAuth
    private var TAG = "LoginActivity" //inutile, serve solo per i LOG
    lateinit var loginButton: Button
    lateinit var signUpTextView: TextView
    lateinit var forgotPWDTextView: TextView
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth //inizializza utente col db
        loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener { onLoginClick() }
        signUpTextView = findViewById<TextView>(R.id.signUpTextView)
        signUpTextView.setOnClickListener { onSignUpClick() }
        forgotPWDTextView = findViewById<TextView>(R.id.forgottenPasswordTextView)
        forgotPWDTextView.setOnClickListener { onForgotClick() }
    }

    private fun onSignUpClick() { //per registrarsi deve andare nell'apposita Activity
        val intent = Intent(this, SingUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onLoginClick() {
        emailEditText = findViewById<EditText>(R.id.emailEditText) //prendo i valori
        val email = emailEditText.text.toString().trim() //trim rimuove gli spazi bianchi
        passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val password = passwordEditText.text.toString().trim()
        if (email.isEmpty()) { //controlli basilari
            emailEditText.error = "Inserisci email" //multilingue...
            return
        }else if (password.isEmpty()) {
            passwordEditText.error = "Inserisci password"
            return
        }
        loginUser(email, password) //ha senso passare ad un altra funzione...?
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // Se loggato correttamente, lo scrivo nel log
                    Log.d(TAG, "signInWithEmail:success")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else { // Se fallisce, mostra un messaggio all'utente
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    val builder = AlertDialog.Builder(this)
                    with(builder) {
                        setTitle("Autenticazione fallita")
                        setMessage(task.exception?.message) //tradurre ogni possibile eccezione
                        setPositiveButton("OK", null)
                        show()
                    }
                }
            }
    }

    private fun onForgotClick() { //da fare seriamente
        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle("Opzione non valida")
            setMessage("Opzione in sviluppo, sarà posssibile in futuro")
            setPositiveButton("OK", null)
            show()
        }
        TODO("Not yet implemented")
    }
}