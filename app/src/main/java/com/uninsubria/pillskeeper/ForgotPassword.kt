package com.uninsubria.pillskeeper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    lateinit var emailResetEditText: EditText
    lateinit var resetButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()
        emailResetEditText = findViewById(R.id.emailResetEditText)
        resetButton = findViewById(R.id.resetButton)
        resetButton.setOnClickListener{ resetPWD(); }
    }

    private fun resetPWD(){
        val email = emailResetEditText.text.toString().trim()

        if(email.isEmpty()){
            emailResetEditText.error = "email richiesta"
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailResetEditText.error = "inserire un'email valida, grazie"
            return
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this,
                    "abbiamo inviato alla tua email, il modo per poter ressettare la tua password",
                    Toast.LENGTH_LONG
                ).show()
                finish() //torno al login
            } else {
                Toast.makeText(this, "riprova, qualcosa è andato storto", Toast.LENGTH_LONG).show()
            }
        }
    }
}