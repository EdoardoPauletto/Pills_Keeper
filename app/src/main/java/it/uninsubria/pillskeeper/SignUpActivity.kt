package it.uninsubria.pillskeeper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var TAG = "SignUpActivity"
    lateinit var registerButton: Button
    lateinit var nameEditText: TextView
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText
    lateinit var giaAccount: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = Firebase.auth
        registerButton = findViewById<Button>(R.id.registerButton)
        registerButton.setOnClickListener {
            onSignUpClick()
        }
        giaAccount = findViewById<TextView>(R.id.loginTextView)
        giaAccount.setOnClickListener {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onSignUpClick() {
        emailEditText = findViewById<EditText>(R.id.emailEditText)
        val email = emailEditText.text.toString().trim()
        passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val password = passwordEditText.text.toString().trim()
        nameEditText = findViewById<TextView>(R.id.nameEditText)
        val userName = nameEditText.text.toString().trim()
        if (userName.isEmpty()) {
            nameEditText.error = "Enter userName"
            return
        }
        if (email.isEmpty()) {
            emailEditText.error = "Enter email"
            return
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Enter password"
            return
        }
        createUser(userName, email, password)
    }

    private fun createUser(userName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) { // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "createUserWithEmail:success")
                val currentUser = auth.currentUser
                val uid = currentUser!!.uid
                val userMap = HashMap<String, String>()
                userMap["name"] = userName
                val database = FirebaseDatabase.getInstance().getReference("Users").child(uid)
                database.setValue(userMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } else { // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}
