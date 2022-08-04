package it.uninsubria.pillskeeper

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
    lateinit var forgottenPasswordTextView: TextView

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

        forgottenPasswordTextView = findViewById<TextView>(R.id.forgottenPasswordTextView);
        forgottenPasswordTextView.setOnClickListener {
            onSignUpClick()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Forgot Password")
            val view = layoutInflater.inflate(R.layout.activity_forgotten_password, null)
            val username = view.findViewById<EditText>(R.id.User)
            builder.setView(view)
            builder.setPositiveButton("Reset", DialogInterface.OnClickListener{_, _ ->
                forgottenPassword(username)
            })
            builder.setNegativeButton("close", DialogInterface.OnClickListener{_, _->})
            builder.show()
        }
    }
   private fun forgottenPassword(username : EditText){
        if(username.text.toString().isEmpty()){
            return
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()){
            return
        }

        auth.sendPasswordResetEmail(username.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(this, "ti abbiamo inviato l'email",Toast.LENGTH_SHORT).show()
                }

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
        forgottenPasswordTextView = findViewById<EditText>(R.id.passwordEditText)
        val password = forgottenPasswordTextView.text.toString().trim()
        if (email.isEmpty()) {
            emailEditText.error = "Enter email"
            return
        }else if (password.isEmpty()) {
            forgottenPasswordTextView.error = "Enter password"
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