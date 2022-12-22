package com.uninsubria.pillskeeper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class send_email : AppCompatActivity() {
    private lateinit var button: Button

    /*private lateinit var sendto: EditText
    private lateinit var subject: EditText
    private lateinit var body: EditText*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_email)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) //mostra il back in alto

        // Getting instance of edittext and button
        /*sendto = findViewById(R.id.editText1)
        subject = findViewById(R.id.objEditText)
        body = findViewById(R.id.testoEdit)
        button = findViewById(R.id.sendButtonE)

        // attach setOnClickListener to button with Intent object define in it
        button.setOnClickListener {
            val emailsend = "giangifumagalli1@gmail.com"
            val emailsubject = "soggetto"
            val emailbody = "ciao"
            // define Intent object with action attribute as ACTION_SEND
            val intent = Intent(Intent.ACTION_SEND)
            // add three fields to intent using putExtra function
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailsend))
            intent.putExtra(Intent.EXTRA_SUBJECT, emailsubject)
            intent.putExtra(Intent.EXTRA_TEXT, emailbody)
            // set type of intent
            intent.type = ""
            // startActivity with intent with chooser as Email client using createChooser function
            startActivity(Intent.createChooser(intent, "Choose an Email client :"))*/
        val testIntent = Intent(Intent.ACTION_VIEW)
        val data: Uri = Uri.parse("mailto:?subject=" + "blah blah subject" + "&body=" + "blah blah body" + "&to=" + "giangifumagalli1@gmail.com")
        testIntent.data = data
        startActivity(testIntent)
    }
}
