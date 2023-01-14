package com.uninsubria.pillskeeper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse

class DetailsMarker : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_marker)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //mostra il back in alto

        val nome = findViewById<TextView>(R.id.textViewNome)
        val info = findViewById<TextView>(R.id.textViewInfo)
        val orari = findViewById<TextView>(R.id.textViewOrari)
        val id = intent.getStringExtra("id")

        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        val placesClient = Places.createClient(this)
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.OPENING_HOURS, Place.Field.PHONE_NUMBER, Place.Field.RATING)
        val request = FetchPlaceRequest.newInstance(id!!, placeFields)
        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                nome.text = place.name
                info.text = "${place.address} \n Tel: ${place.phoneNumber} \n Valutazione: ${place.rating} su 5"
                if (place.openingHours != null){
                    val stringBuilder = StringBuilder()
                    for (day in place.openingHours!!.weekdayText){
                        stringBuilder.appendLine(day)
                    }
                    orari.text = "Orari di apertura: \n $stringBuilder"
                }
                else orari.text = "Orari di apertura: \n sconosciuti"
            }
    }
}