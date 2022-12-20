package com.uninsubria.pillskeeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class send_email : AppCompatActivity() {
    lateinit var Subject : EditText
    lateinit var Text : EditText
    lateinit var Send : Button
    lateinit var sub : String
    lateinit var Tex : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_email)
        Subject = findViewById(R.id.textViewOgg)
        Text = findViewById(R.id.testoEdit)
        Send = findViewById(R.id.sendButtonE)
        Send.setOnClickListener {
            getData()
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_SUBJECT, sub)
            intent.putExtra(Intent.EXTRA_TEXT, Tex)
            intent.type = "message/rfc822"
            startActivity(Intent.createChooser(intent,"select"))
        }
    }

    private fun getData(){
        sub = Subject.text.toString()
        Tex = Text.text.toString()
    }
}