package it.uninsubria.pillskeeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.uninsubria.pillskeeper.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var outButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        outButton.setOnClickListener {
            onlogOutClick()
        }


    }

    private fun onlogOutClick() {
        val intent = Intent(this, LogInActivity::class.java)
        auth.signOut()
        startActivity(intent)
        finish()
    }
}

   /*override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null)
        //val currentUser = auth.currentUser
        //if(currentUser == null) {
            val intent = Intent(this, LogInActivity::class.java)
            startActivity(intent)
            finish()
        //}
    }*/






    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (id == R.id.action_settings){

        } else if (id == R.id.action_new_item){

        } else if (id == R.id.action_logout){

        }
        return super.onOptionsItemSelected(item)
    }
}*/