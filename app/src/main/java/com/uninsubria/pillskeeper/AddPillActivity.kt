package com.uninsubria.pillskeeper

import android.Manifest
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList


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
    private lateinit var textViewUndoOrDelete: TextView
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
        imageView = findViewById(R.id.Imm)
        buttonChooseImage = findViewById(R.id.selectFileButton) //gli assegno il reale elemento
        editTextPillName = findViewById(R.id.nomeFarmacoEditText)
        editTextQntTot = findViewById(R.id.quantitaTotaleEditText)
        editTextQnt = findViewById(R.id.quantitaEditText)
        spinnerWhen = findViewById(R.id.ogniQuantoSpinner)
        editTextTime = findViewById(R.id.editTextTime)
        buttonTimePicker = findViewById(R.id.buttonTimePicker)
        progressBar = findViewById(R.id.progressBar)
        buttonUpload = findViewById(R.id.caricaButton)
        textViewUndoOrDelete = findViewById(R.id.annOcancTextView)
        //UPLOAD, con la string "uploads" andremo in quella cartella senò andiamo al top node
        storageRef = FirebaseStorage.getInstance().reference
        databaseRef = FirebaseDatabase.getInstance().getReference("Users/" + auth.currentUser!!.uid + "/farmaci/")

        buttonChooseImage.setOnClickListener { openFileChooser() } //creare metodo
        buttonTimePicker.setOnClickListener { openTimePicker() }
        buttonUpload.setOnClickListener { uploadFile() }
        textViewUndoOrDelete.setOnClickListener{ finish() }
        //quando modifica uno già esistente
        if(intent.hasExtra("key")){
            caricaDati()
        }
    }

    private fun alertDelete() {
        val builder = AlertDialog.Builder(this)
        with(builder) {
            setTitle("Attenzione")
            setMessage("Vuoi davvero cancellare " + intent.getStringExtra("key")!! + "?")
            setPositiveButton("Sì", delete)
            setNegativeButton("No", undo)
            show()
        }
    }
    private val delete = { _: DialogInterface, _: Int ->
        Toast.makeText(this, "Cancello " + intent.getStringExtra("key")!!, Toast.LENGTH_SHORT).show()
        val farmaco = intent.getSerializableExtra("Farmaco") as Farmaco
        storageRef.child(farmaco.mImageUrl).delete()
        databaseRef.child(intent.getStringExtra("key")!!).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children){
                    child.ref.removeValue()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", error.message) //Don't ignore errors!
            }
        })
        finish()
    }
    private val undo = { _: DialogInterface, _: Int ->
        Toast.makeText(this, "Cancellazione annullata", Toast.LENGTH_SHORT).show()
    }

    private fun openTimePicker() {//faccio selezionare un orario
        val calendario: Calendar = Calendar.getInstance()
        val ore = calendario.get(Calendar.HOUR_OF_DAY)
        val minuti = calendario.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(this, this, ore, minuti, true)
        timePickerDialog.show()
    }

    override fun onTimeSet(p0: TimePicker?, h: Int, m: Int) {//quando sel un orario
        editTextTime.error = null //tolgo eventuale errore segnalato perchè vuoto
        if(m<10)
            editTextTime.text = Editable.Factory.getInstance().newEditable("$h:0$m")
        else
            editTextTime.text = Editable.Factory.getInstance().newEditable("$h:$m")
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*" //vede solo immagini
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
        //dopo creo onActivityResult che verrà chiamato quando prendo il file ^
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "permesso consentito", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permesso rifiutato", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
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
        val days = ArrayList<Boolean>(7)
        days.add(findViewById<ToggleButton>(R.id.domToggleButton).isChecked)
        days.add(findViewById<ToggleButton>(R.id.lunToggleButton).isChecked)
        days.add(findViewById<ToggleButton>(R.id.marToggleButton).isChecked)
        days.add(findViewById<ToggleButton>(R.id.merToggleButton).isChecked)
        days.add(findViewById<ToggleButton>(R.id.gioToggleButton).isChecked)
        days.add(findViewById<ToggleButton>(R.id.venToggleButton).isChecked)
        days.add(findViewById<ToggleButton>(R.id.sabToggleButton).isChecked)
        if (pillName.isEmpty()) editTextPillName.error = "Inserire il nome del farmaco"
        else if (qntTot.isEmpty()) editTextQntTot.error = "Inserire la quantità della confezione"
        else if (qntTot.toDouble() == 0.0) editTextQntTot.error = "Inserire una quantità maggiore di 0"
        else if (qnt.toDouble() == 0.0) editTextQnt.error = "Inserire una quantità maggiore di 0"
        else if (qnt.isEmpty()) editTextQnt.error = "Inserire quantità da assumere"
        else if (qntTot.toDouble() < qnt.toDouble()) editTextQnt.error = "Non possono essere meno di quelle in confezione"
        else if (time.isEmpty()) editTextTime.error = "Scegliere un orario"
        //else if (time.split(":")[0].toInt()<now.get(Calendar.HOUR_OF_DAY) || (time.split(":")[0].toInt()==now.get(Calendar.HOUR_OF_DAY) && time.split(":")[1].toInt()<=now.get(Calendar.MINUTE)))
        //    editTextTime.error = "Non è possibile selezionare un orario passato"
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
                        val upload = Farmaco(pillName, taskSnapshot.storage.path, qntTot.toDouble(), qnt.toDouble(), hourspinner, time, days)
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
        } else {//modifico i valori
            val farmaco = intent.getSerializableExtra("Farmaco") as Farmaco
            val key = intent.getStringExtra("key")
            if (this::imageUri.isInitialized) {
                val fileReference = storageRef.child(farmaco.mImageUrl) //nello stesso percorso della vecchia
                fileReference.putFile(imageUri) //sovrascrivo
                    .addOnProgressListener { tasksnapshot ->
                        val progress = 100.0 * tasksnapshot.bytesTransferred / tasksnapshot.totalByteCount
                        progressBar.progress = progress.toInt() //aggiorno la progress bar
                    }
                    .addOnSuccessListener { taskSnapshot ->//quando upload finisce
                        //chiamo costruttore con edit text nome farmaco, percorso DELL'IMMAGINE (diverso per ogni utente), ecc...
                        val upload = Farmaco(pillName, taskSnapshot.storage.path, qntTot.toDouble(), qnt.toDouble(), hourspinner, time, days)
                        databaseRef.child(key!!).setValue(upload)
                    }
                    .addOnFailureListener { e -> //quando non avviene
                        Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                    }
            } else {//modifico solo i dati
                val upload = Farmaco(pillName, farmaco.mImageUrl, qntTot.toDouble(), qnt.toDouble(), hourspinner, time, days)
                databaseRef.child(key!!).setValue(upload)
            }
            Toast.makeText(this, "Modifica riuscita", Toast.LENGTH_LONG).show()
            Thread.sleep(500)
            finish() //chiude questa e torna a quella prima
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
        val orari = resources.getStringArray(R.array.AllarmTime)
        spinnerWhen.setSelection(orari.indexOf(farmaco.every))
        editTextTime.text = Editable.Factory.getInstance().newEditable(farmaco.time)
        findViewById<ToggleButton>(R.id.domToggleButton).isChecked = farmaco.day[0]
        findViewById<ToggleButton>(R.id.lunToggleButton).isChecked = farmaco.day[1]
        findViewById<ToggleButton>(R.id.marToggleButton).isChecked = farmaco.day[2]
        findViewById<ToggleButton>(R.id.merToggleButton).isChecked = farmaco.day[3]
        findViewById<ToggleButton>(R.id.gioToggleButton).isChecked = farmaco.day[4]
        findViewById<ToggleButton>(R.id.venToggleButton).isChecked = farmaco.day[5]
        findViewById<ToggleButton>(R.id.sabToggleButton).isChecked = farmaco.day[6]
        mod = true
        buttonUpload.text = "Modifica"
        textViewUndoOrDelete.text = "Cancella"
        textViewUndoOrDelete.setTextColor(Color.RED)
        textViewUndoOrDelete.setOnClickListener{ alertDelete() }
    }
}