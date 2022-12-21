package com.uninsubria.pillskeeper

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class send_email : AppCompatActivity() {
    private lateinit var button: Button

    //private lateinit var sendto: EditText
    private lateinit var subject: EditText
    private lateinit var body: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_email)

        // Getting instance of edittext and button
        //sendto = findViewById(R.id.editText1)
        subject = findViewById(R.id.objEditText)
        body = findViewById(R.id.testoEdit)
        button = findViewById(R.id.sendButtonE)

        // attach setOnClickListener to button with Intent object define in it
        button.setOnClickListener {
            val emailsend = "giangifumagalli1@gmail.com"
            val emailsubject = subject.getText().toString()
            val emailbody = body.getText().toString()
            // define Intent object with action attribute as ACTION_SEND
            val intent = Intent(Intent.ACTION_SEND)
            // add three fields to intent using putExtra function
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailsend))
            intent.putExtra(Intent.EXTRA_SUBJECT, emailsubject)
            intent.putExtra(Intent.EXTRA_TEXT, emailbody)
            // set type of intent
            intent.type = "message/rfc822"
            // startActivity with intent with chooser as Email client using createChooser function
            startActivity(Intent.createChooser(intent, "Choose an Email client :"))
        }
    }
}