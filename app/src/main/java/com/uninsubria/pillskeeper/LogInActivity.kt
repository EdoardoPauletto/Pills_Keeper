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
    private lateinit var loginButton: Button
    private lateinit var signUpTextView: TextView
    private lateinit var forgotPWDTextView: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth //inizializza utente col db
        loginButton = findViewById(R.id.loginButton)
        loginButton.setOnClickListener { onLoginClick() }
        signUpTextView = findViewById(R.id.signUpTextView)
        signUpTextView.setOnClickListener { onSignUpClick() }
        forgotPWDTextView = findViewById(R.id.forgottenPasswordTextView)
        forgotPWDTextView.setOnClickListener { onForgotClick() }
    }

    private fun onSignUpClick() { //per registrarsi deve andare nell'apposita Activity
        val intent = Intent(this, SingUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onLoginClick() {
        emailEditText = findViewById(R.id.emailEditText) //prendo i valori
        val email = emailEditText.text.toString().trim() //trim rimuove gli spazi bianchi
        passwordEditText = findViewById(R.id.passwordEditText)
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
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) //termina le schede precedenti
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

    private fun onForgotClick(){
        val intent = Intent(this, ForgotPassword::class.java)
        startActivity(intent)
    }
}