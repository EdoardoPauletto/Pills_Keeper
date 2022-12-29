package com.uninsubria.pillskeeper

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.AlarmClock
import android.text.Editable
import android.text.format.DateFormat
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*


class AddPillActivity : AppCompatActivity(),TimePickerDialog.OnTimeSetListener  {
    private val PICK_IMAGE_REQUEST: Int = 1
    private lateinit var auth: FirebaseAuth
    private lateinit var imageView: ImageView
    private lateinit var buttonChooseImage: Button
    private lateinit var editTextPillName: EditText
    private lateinit var editTextQntTot: EditText
    private lateinit var editTextQnt: EditText
    private lateinit var spinnerWhen: Spinner
    private lateinit var editTextTime: EditText
    private lateinit var buttonTimePicker: Button
    //private lateinit var textViewDay: TextView prossimamente
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonUpload: Button
    private lateinit var textViewShowUplaods: TextView
    private lateinit var imageUri: Uri //tipo url ma per i file
    //UPLOAD
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference
    private var mod: Boolean = false//se modifica o carica

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pill)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) //mostra il back in alto

        auth = Firebase.auth
        imageView = findViewById(R.id.immagine)
        buttonChooseImage = findViewById(R.id.selectFileButton) //gli assegno il reale elemento
        editTextPillName = findViewById(R.id.nomeFarmacoEditText)
        editTextQntTot = findViewById(R.id.quantitaTotaleEditText)
        editTextQnt = findViewById(R.id.quantitaEditText)
        spinnerWhen = findViewById(R.id.ogniQuantoSpinner)
        editTextTime = findViewById(R.id.editTextTime)
        buttonTimePicker = findViewById(R.id.buttonTimePicker)
        progressBar = findViewById(R.id.progressBar)
        buttonUpload = findViewById(R.id.caricaButton)
        textViewShowUplaods = findViewById(R.id.mostraCaricaTextView)
        //UPLOAD, con la string "uploads" andremo in quella cartella senò andiamo al top node
        storageRef = FirebaseStorage.getInstance().reference
        databaseRef = FirebaseDatabase.getInstance().getReference("Users/" + auth.currentUser!!.uid + "/farmaci/")

        buttonChooseImage.setOnClickListener { openFileChooser() } //creare metodo
        buttonTimePicker.setOnClickListener { openTimePicker() }
        buttonUpload.setOnClickListener { uploadFile() }
        textViewShowUplaods.setOnClickListener{
            val openClockIntent = Intent(AlarmClock.ACTION_SET_ALARM) //apre direttamente l'orologio, funziona
            openClockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(openClockIntent)
        }
        //quando modifica uno già esistente
        if(intent.hasExtra("key")){
            caricaDati()
        }
    }

    private fun openTimePicker() {//faccio selezionare un orario
        val calendario: Calendar = Calendar.getInstance()
        val ore = calendario.get(Calendar.HOUR)
        val minuti = calendario.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this, this, ore, minuti, DateFormat.is24HourFormat(this))
        timePickerDialog.show()
    }

    override fun onTimeSet(p0: TimePicker?, h: Int, m: Int) {//quando sel un orario
        editTextTime.text = Editable.Factory.getInstance().newEditable("$h:$m")
        editTextTime.error = null //tolgo eventuale errore segnalato perchè vuoto
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*" //vede solo immagini
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
        //dopo creo onActivityResult che verrà chiamato quando prendo il file ^
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //requestcode == richiesta che vogliamo che faccia
        //resultcode == RESULT_OK se prende l'immagine va alla next line
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) { //se l'utente sceglie un' immagine controlla che non sia nulla
            imageUri = data.data!! //return del uri dell'immagine scelta (forziamo non null)
            Toast.makeText(this, "File selezionato", Toast.LENGTH_SHORT).show()
            Picasso.get().load(imageUri).into(imageView)
        }
    }

    //UPLOAD ritorna il tipo di file
    private fun getFileExtension(uri: Uri): String? {
        val cR = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    //UPLOAD
    private fun uploadFile() {
        val pillName = editTextPillName.text.toString().trim()
        val qntTot = editTextQntTot.text.toString().trim()
        val qnt = editTextQnt.text.toString().trim()
        val hourspinner = spinnerWhen.selectedItem.toString()
        val time = editTextTime.text.toString().trim()
        if (pillName.isEmpty()) editTextPillName.error = "Inserire il nome del farmaco"
        else if (qntTot.isEmpty()) editTextQntTot.error = "Inserire la quantità della confezione"
        else if (qnt.isEmpty()) editTextQnt.error = "Inserire quantità da assumere"
        else if (qntTot.toInt() < qnt.toInt()) editTextQnt.error = "Non possono essere meno di quelle in confezione"
        else if (time.isEmpty()) editTextTime.error = "Scegliere un orario"
        else if (!mod) {
             if (this::imageUri.isInitialized) { //controllo che sia stato dato un valore (essendo lateinit è sempre != null)
                //mStorageRef punta alla cartella Storage
                val fileReference = storageRef.child(System.currentTimeMillis().toString() + "." + getFileExtension(imageUri))
                //il nome è formato da tempo in ms ed estensione per evitare omonimi
                fileReference.putFile(imageUri) //upload del file , continua sotto
                    .addOnProgressListener { tasksnapshot ->//quando upload sta avvenendo, voglio aggiornare la progress bar con la percentuale corrente
                        val progress = 100.0 * tasksnapshot.bytesTransferred / tasksnapshot.totalByteCount //estrarre il progresso da tasksnapshot
                        progressBar.progress = progress.toInt() //aggiorno la progress bar
                    }
                    .addOnSuccessListener { taskSnapshot ->//quando upload finisce
                        Toast.makeText(this, "Upload riuscito", Toast.LENGTH_LONG).show()

                        //chiamo costruttore con edit text nome farmaco, percorso DELL'IMMAGINE (diverso per ogni utente), ecc...
                        val upload = Farmaco(pillName, taskSnapshot.storage.path, qntTot.toInt(), qnt.toInt(), hourspinner, time)
                        //crea nuova entrata nel db con unico id
                        val uploadId = databaseRef.push().key
                        //prendo id e gli setto i dati dell'upload file che contiene nome, immagine, ecc...
                        databaseRef.child(uploadId!!).setValue(upload)

                    }
                    .addOnFailureListener { e -> //azioni quando upload non avviene
                        Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                    }
                    .addOnCompleteListener{ //quando finisce, aspetto un attimo e poi torno al main
                        Thread.sleep(2000)
                        finish() //chiude questa e torna a quella prima
                    }
            } else {
                Toast.makeText(this, "Nessun file selezionato", Toast.LENGTH_SHORT).show()
            }
        }
        else {
            //modifico i valori

        }
    }

    private fun caricaDati() {
        val farmaco = intent.getSerializableExtra("Farmaco") as Farmaco
        farmaco.convertImg().downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri.toString()).into(imageView)
        }.addOnFailureListener {
            // Handle any errors
        }
        editTextPillName.text = Editable.Factory.getInstance().newEditable(farmaco.name)
        editTextQntTot.text = Editable.Factory.getInstance().newEditable(farmaco.qTot.toString())
        editTextQnt.text = Editable.Factory.getInstance().newEditable(farmaco.q.toString())
        val orari = resources.getStringArray(R.array.orari_allarmi)
        spinnerWhen.setSelection(orari.indexOf(farmaco.every))
        editTextTime.text = Editable.Factory.getInstance().newEditable(farmaco.time)
        mod = true
        buttonUpload.text = "Modifica"
    }
}