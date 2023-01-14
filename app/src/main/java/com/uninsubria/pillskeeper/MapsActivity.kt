package com.uninsubria.pillskeeper

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.uninsubria.pillskeeper.BuildConfig.MAPS_API_KEY
import com.uninsubria.pillskeeper.databinding.ActivityMapsBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private lateinit var placesClient: PlacesClient

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    // The entry point to the Places API.

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Construct a PlacesClient
        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(this)

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(this){ task ->
            if (task.isSuccessful){
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = task.result
                if (lastKnownLocation != null) {
                    val qua = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                    mMap.addMarker(MarkerOptions().position(qua).title("Sono qua"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(qua, 15F))

                    val stringa = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"+
                            "location=${lastKnownLocation!!.latitude}, ${lastKnownLocation!!.longitude}"+
                            "&radius=5000"+
                            "&types=pharmacy"+
                            "&sensor=true"+
                            "&key=${MAPS_API_KEY}"
                    var placesTask = PlacesTask(mMap, placesClient)
                    placesTask.execute(stringa)
                }
            }
        }
        mMap.setOnInfoWindowClickListener { marker ->
            Toast.makeText(this, marker.title, Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AddPillActivity::class.java)
            intent.putExtra("id", marker.id)
            startActivity(intent)
        }
        /*mMap.setOnMarkerClickListener { marker ->
            if (marker.isInfoWindowShown) {
                marker.hideInfoWindow()
            } else {
                marker.showInfoWindow()
                Toast.makeText(this, marker.title, Toast.LENGTH_SHORT).show()
            }
            true
        }*/
    }

    private class PlacesTask(val mappa: GoogleMap,val placesClient: PlacesClient) : AsyncTask<String, Int, String>() {
        var data: String = ""
        // Invoked by execute() method of this object
        override fun doInBackground(vararg p0: String?): String {
            data = downloadUrl(p0[0]!!)
            return data
        }
        private fun downloadUrl(url: String): String {
            var da = ""
            var iStream: InputStream
            var urlConnection : HttpURLConnection
            var u = URL(url)
            // Creating an http connection to communicate with url
            urlConnection = u.openConnection() as HttpURLConnection
            // Connecting to url
            urlConnection.connect()
            // Reading data from url
            iStream = urlConnection.inputStream
            var br = BufferedReader(InputStreamReader(iStream))
            var sb = StringBuffer()
            var line = br.readLine()
            while (line != null) {
                sb.append(line)
                line = br.readLine()
            }
            da = sb.toString()
            br.close()
            iStream.close()
            urlConnection.disconnect()
            return da
        }
        // Executed after the complete execution of doInBackground() method
        override fun onPostExecute(result: String?) {
            var parserTask = ParserTask(mappa,placesClient)
            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParserTask
            parserTask.execute(result)
        }

    }

    private class ParserTask(mappa: GoogleMap, placesClient: PlacesClient) : AsyncTask<String, Int, List<HashMap<String, String>>>() {
        var googleMap = mappa
        var plaCli = placesClient
        lateinit var jObject: JSONObject
        // Invoked by execute() method of this object
        override fun doInBackground(vararg p0: String?): List<HashMap<String, String>> {
            var places : List<HashMap<String,String>>
            var placeJson = Place_JSON()
            jObject = JSONObject(p0[0]!!)
            places = placeJson.parse(jObject)
            return places
        }
        // Executed after the complete execution of doInBackground() method
        override fun onPostExecute(result: List<HashMap<String, String>>?) {
            //googleMap.clear()
            var i = 0
            while (i<result!!.size){
                var markerOptions = MarkerOptions()
                // Getting a place from the places list
                var hmPlace = result[i]
                var lat = hmPlace["lat"]!!.toDouble()
                var lng = hmPlace["lng"]!!.toDouble()
                var name = hmPlace["name"]
                var vicinity = hmPlace["vicinity"]
                var hour = hmPlace["hour"]
                var latLng = LatLng(lat, lng)

                val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.OPENING_HOURS, Place.Field.LAT_LNG)
                val request = FetchPlaceRequest.newInstance(hmPlace["id"], placeFields)
                plaCli.fetchPlace(request)
                    .addOnSuccessListener { response: FetchPlaceResponse ->
                        val place = response.place
                        markerOptions.position(place.latLng!!)
                        markerOptions.title(place.name)
                        if (hour != ""){
                            var lunedi = place.openingHours!!.weekdayText
                            markerOptions.title(lunedi[1])
                        }
                        googleMap.addMarker(markerOptions)
                    }

                /*markerOptions.position(latLng)
                if (hour == "false")
                    markerOptions.title("CHIUSO: $name")
                else
                    markerOptions.title(name)
                googleMap.addMarker(markerOptions)*/
                i++
            }
        }
    }

    private class Place_JSON {
        //Receives a JSONObject and returns a list
        fun parse(jObject: JSONObject): ArrayList<HashMap<String, String>> {
            var jPlaces : JSONArray
            // Retrieves all the elements in the 'places' array
            jPlaces = jObject.getJSONArray("results")
            //Invoking getPlaces with the array of json object
            //where each json object represent a place
            return getPlaces(jPlaces)
        }
        fun getPlaces(jPlaces: JSONArray): ArrayList<HashMap<String, String>> {
            var placesCount = jPlaces.length()
            var placesList = ArrayList<HashMap<String,String>>()
            var place = HashMap<String,String>()
            var i = 0
            // Taking each place, parses and adds to list object
            while (i<placesCount){
                // Call getPlace with place JSON object to parse the place
                place = getPlace(jPlaces.get(i) as JSONObject)
                placesList.add(place)
                i++
            }
            return placesList
        }
        //Parsing the Place JSON object
        fun getPlace(jPlace : JSONObject): HashMap<String, String> {
            var place = HashMap<String,String>()
            var id = ""
            var placeName = ""
            var vicinity = ""
            var latitude = ""
            var longitude = ""
            var hour = ""
            var reference = ""
            id = jPlace.getString("place_id")
            // Extracting Place name, if available
            if (!jPlace.isNull("name"))
                placeName = jPlace.getString("name")
            if (!jPlace.isNull("vicinity"))
                vicinity = jPlace.getString("vicinity")
            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat")
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng")
            if (!jPlace.isNull("opening_hours"))
                hour = jPlace.getJSONObject("opening_hours").getString("open_now")
            reference = jPlace.getString("reference")
            place["id"] = id
            place["name"] = placeName
            place["vicinity"] = vicinity
            place["lat"] = latitude
            place["lng"] = longitude
            place["hour"] = hour
            place["reference"] = reference
            return place

        }
    }
}








