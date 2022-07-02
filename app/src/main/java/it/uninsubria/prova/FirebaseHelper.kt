package it.uninsubria.prova

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.FirebaseDatabase
import java.io.DataInput

class FirebaseHelper {
    companion object{
        //classe che contiene la reference ad una posizione del database
        //può essere usata per scrivere e leggere dal database
        private var firebaseDbPercorso = FirebaseDatabase.getInstance("https://prove-b822e-default-rtdb.europe-west1.firebasedatabase.app/").getReference()

        //legge data from DB
        fun readUtentiOggetti(getUserInput: ChildEventListener){
            firebaseDbPercorso.addChildEventListener(getUserInput)
        }

        //update o insert a Uopdate node in Firebase
        fun setUtentiOggetti(key: String, Upload: Upload){
            firebaseDbPercorso.child(key).setValue(Upload)
        }

        //Delete a Update node in Firebase
        fun removeUtentiOggetti(key: String){
            firebaseDbPercorso.child(key).removeValue()
        }

    }
}