package com.uninsubria.pillskeeper

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class email : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val testIntent = Intent(Intent.ACTION_VIEW)
        val data: Uri = Uri.parse("mailto:?subject=" + "Buongiorno dottore" + "&body=" + "Volevo avvisarla che ho esaurito la ...." + "&to=" + "giangifumagalli1@gmail.com")
        testIntent.data = data
        startActivity(testIntent)
        finish()
    }
}