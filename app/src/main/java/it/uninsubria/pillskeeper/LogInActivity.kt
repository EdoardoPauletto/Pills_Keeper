package it.uninsubria.pillskeeper

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogInActivity: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var TAG = "LoginActivity"
    lateinit var loginButton: Button
    lateinit var signUpTextView: TextView
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            onLoginClick()
        }
        signUpTextView = findViewById<TextView>(R.id.signUpTextView);
        signUpTextView.setOnClickListener {
            onSignUpClick()
        }
    }

    private fun onSignUpClick() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun onLoginClick() {
        emailEditText = findViewById<EditText>(R.id.emailEditText)
        val email = emailEditText.text.toString().trim()
        passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val password = passwordEditText.text.toString().trim()
        if (email.isEmpty()) {
            emailEditText.error = "Enter email"
            return
        }else if (password.isEmpty()) {
            passwordEditText.error = "Enter password"
            return
        }
        loginUser(email, password)
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val intent = Intent(this, LogInActivity::class.java)
                    startActivity(intent)
                    finish()
                } else { // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    val builder = AlertDialog.Builder(this)
                    with(builder)
                    {
                        setTitle("Authentication failed")
                        setMessage(task.exception?.message)
                        setPositiveButton("OK", null)
                        show()
                    }
                }
            }
    }
}