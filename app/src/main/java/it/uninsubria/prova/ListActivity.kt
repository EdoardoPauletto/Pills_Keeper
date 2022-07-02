package it.uninsubria.prova

import android.content.ContentValues.TAG
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_list.*
import java.text.FieldPosition

class ListActivity : AppCompatActivity() {
    lateinit var tv: TextView
    //array con dati utente
    private val dataUtente: MutableList<Upload> = ArrayList()
    //array di tipo oggetto Upload
    private lateinit var adattatore: ArrayAdapter<Upload>
    //oggetti lista
    private lateinit var oggettiLista: ListView
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        //tv = findViewById<TextView>(R.id.testoPercorso)

        //OIRIGNALE
        /* FirebaseDatabase.getInstance().reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tv.text = snapshot.value.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })*/

        //PROVA
        //mostro all'adapter lo stile da seguire "simple_list_item" e i dati da inserire "dataUtente"
        adattatore = ArrayAdapter(this, android.R.layout.simple_list_item_1, dataUtente)
        oggettiLista = findViewById<ListView>(R.id.lista)
        //Assegno alla listview l'adapter "adattore"
        oggettiLista.adapter = adattatore

        FirebaseHelper.readUtentiOggetti(getUserInput())


        //prova ListView
        //arraylist inizializzata prima
        /*val data = arrayListOf<String>()
        for (i in 1..100) {
            data.add("Item $i")
        }
        val adattore = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data)
        lista.adapter = adattore*/

    }

    //lettura del database get User Input
    private fun getUserInput(): ChildEventListener {
        //riceve i cambiamenti nei figli
        //location di un database dato
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val utente = snapshot.getValue(Upload::class.java)
                //add nuovo Upload item
                dataUtente.add(utente!!)
                val utenteIndex = dataUtente.indexOf(utente)
                //oggettiLista.setItemChecked(utenteIndex) spunta
                adattatore.notifyDataSetChanged()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val utente = snapshot.getValue(Upload::class.java)
                //trova elementi upload modificati
                val utenteIndex = dataUtente.indexOf(utente)
                dataUtente[utenteIndex].set(utente)
                adattatore.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val utente = snapshot.getValue(Upload::class.java)
                dataUtente.remove(utente)
                adattatore.notifyDataSetChanged()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //metodo eseguito quando viene cambiato posizione di un figlio
            }

            // triggerato quando un event listener fallisce al server
            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "postComments:onCancelled", error.toException())
                Toast.makeText(this@ListActivity, "Caricamento fallito!", Toast.LENGTH_SHORT).show()
            }

        }
        return childEventListener
    }

    private fun getItemsEventListener(): ChildEventListener {
        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //val elemento = snapshot.getValue(Upload::class.java)
                //data.add(elemento!!)
                //adapter.notifyDataSetChanged()
                //tv.text = snapshot.getValue(String::class.java)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                //val elemento = snapshot.getValue(Upload::class.java)
                //val posizione = data.indexOf(elemento)
                //data[posizione] = elemento!! //gli assicuro che non sarà null
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        return childEventListener
    }
}
