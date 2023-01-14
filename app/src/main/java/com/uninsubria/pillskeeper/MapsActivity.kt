package com.uninsubria.pillskeeper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.uninsubria.pillskeeper.BuildConfig.MAPS_API_KEY
import com.uninsubria.pillskeeper.databinding.ActivityMapsBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var autorizzato = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) //mostra il back in alto
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.refresh -> getLocationPermission()
            R.id.geo -> openMaps()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openMaps() {
        // Search for pharmacy nearby
        val gmmIntentUri = Uri.parse("geo:0,0?q=farmacia")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        startActivity(mapIntent)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * Io ho aggiunto un marker vicino all'Insubria, Varese.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Varese and move the camera
        val varese = LatLng(45.800363, 8.8453171)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(varese, 12F))
        caricaFarmacieVicine(varese)// caricoVicino Varese
        getLocationPermission()
        if (autorizzato){//caricoFarmacie vicine
            trovami()
        }
        mMap.setOnInfoWindowClickListener { marker ->
            val intent = Intent(this, DetailsMarker::class.java)
            intent.putExtra("id", marker.tag.toString())
            startActivity(intent)
        }

    }

    @SuppressLint("MissingPermission")
    fun trovami(){
        mMap.clear()
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener(this){ task ->
            if (task.isSuccessful){
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = task.result
                if (lastKnownLocation != null) {
                    val qua = LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                    //mMap.addMarker(MarkerOptions().position(qua).title("Sono qua"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(qua, 12F))
                    caricaFarmacieVicine(qua)
                }
            }
        }
    }

    private fun caricaFarmacieVicine(latLng: LatLng){
        val stringa = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"+
                "location=${latLng.latitude}, ${latLng.longitude}"+
                "&radius=5000"+
                "&types=pharmacy"+
                "&sensor=true"+
                "&key=${MAPS_API_KEY}"
        val placesTask = PlacesTask(mMap)
        placesTask.execute(stringa)
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            autorizzato = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        autorizzato = false
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    autorizzato = true
                    Toast.makeText(this, "Localizzazione...", Toast.LENGTH_SHORT).show()
                    trovami()
                }
                else Toast.makeText(this, "Permesso rifiutato", Toast.LENGTH_SHORT).show()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private class PlacesTask(val mappa: GoogleMap) : AsyncTask<String, Int, String>() {
        var data: String = ""
        // Invoked by execute() method of this object
        override fun doInBackground(vararg p0: String?): String {
            data = downloadUrl(p0[0]!!)
            return data
        }
        private fun downloadUrl(url: String): String {
            val iStream: InputStream
            val urlConnection : HttpURLConnection
            val u = URL(url)
            // Creating an http connection to communicate with url
            urlConnection = u.openConnection() as HttpURLConnection
            // Connecting to url
            urlConnection.connect()
            // Reading data from url
            iStream = urlConnection.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line = br.readLine()
            while (line != null) {
                sb.append(line)
                line = br.readLine()
            }
            val da = sb.toString()
            br.close()
            iStream.close()
            urlConnection.disconnect()
            return da
        }
        // Executed after the complete execution of doInBackground() method
        override fun onPostExecute(result: String?) {
            val parserTask = ParserTask(mappa)
            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParserTask
            parserTask.execute(result)
        }

    }

    private class ParserTask(mappa: GoogleMap) : AsyncTask<String, Int, List<HashMap<String, String>>>() {
        var googleMap = mappa
        lateinit var jObject: JSONObject
        // Invoked by execute() method of this object
        override fun doInBackground(vararg p0: String?): List<HashMap<String, String>> {
            val places : List<HashMap<String,String>>
            val placeJson = PlaceJSON()
            jObject = JSONObject(p0[0]!!)
            places = placeJson.parse(jObject)
            return places
        }
        // Executed after the complete execution of doInBackground() method
        override fun onPostExecute(result: List<HashMap<String, String>>?) {
            //googleMap.clear()
            var i = 0
            while (i<result!!.size){
                val markerOptions = MarkerOptions()
                // Getting a place from the places list
                val hmPlace = result[i]
                val lat = hmPlace["lat"]!!.toDouble()
                val lng = hmPlace["lng"]!!.toDouble()
                val name = hmPlace["name"]
                //val vicinity = hmPlace["vicinity"]
                val hour = hmPlace["hour"]
                val latLng = LatLng(lat, lng)

                markerOptions.position(latLng)
                markerOptions.title(name)
                when (hour) {
                    "false" -> {
                        markerOptions.snippet("Chiuso")
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    }
                    "" -> {
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    }
                    else -> markerOptions.snippet("Aperto")
                }
                googleMap.addMarker(markerOptions)!!.tag = hmPlace["id"]
                i++
            }
        }
    }

    private class PlaceJSON {
        //Receives a JSONObject and returns a list
        fun parse(jObject: JSONObject): ArrayList<HashMap<String, String>> {
            val jPlaces : JSONArray
            // Retrieves all the elements in the 'places' array
            jPlaces = jObject.getJSONArray("results")
            //Invoking getPlaces with the array of json object
            //where each json object represent a place
            return getPlaces(jPlaces)
        }
        fun getPlaces(jPlaces: JSONArray): ArrayList<HashMap<String, String>> {
            val placesCount = jPlaces.length()
            val placesList = ArrayList<HashMap<String,String>>()
            var place: HashMap<String, String>
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
            val place = HashMap<String,String>()
            var placeName = ""
            //var vicinity = ""
            var hour = ""
            //var reference = ""
            val id: String = jPlace.getString("place_id")
            // Extracting Place name, if available
            if (!jPlace.isNull("name"))
                placeName = jPlace.getString("name")
            //if (!jPlace.isNull("vicinity"))
            //    vicinity = jPlace.getString("vicinity")
            val latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat")
            val longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng")
            if (!jPlace.isNull("opening_hours"))
                hour = jPlace.getJSONObject("opening_hours").getString("open_now")
            //reference = jPlace.getString("reference")
            place["id"] = id
            place["name"] = placeName
            //place["vicinity"] = vicinity
            place["lat"] = latitude
            place["lng"] = longitude
            place["hour"] = hour
            //place["reference"] = reference
            return place
        }
    }
}








