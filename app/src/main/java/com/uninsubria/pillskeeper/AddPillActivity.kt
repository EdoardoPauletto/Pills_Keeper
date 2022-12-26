package com.uninsubria.pillskeeper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.AlarmClock
import android.text.Editable
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


class AddPillActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST: Int = 1
    private lateinit var auth: FirebaseAuth
    private lateinit var mButtonChooseImage: Button
    private lateinit var mButtonUpload: Button
    private lateinit var mTextViewShowUplaods: TextView
    private lateinit var mEditTextFileName: EditText
    private lateinit var mImageView: ImageView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mImageUri: Uri //tipo url ma per i file
    //UPLOAD
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pill)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) //mostra il back in alto

        auth = Firebase.auth
        mButtonChooseImage = findViewById(R.id.selectFileButton) //gli assegno il reale elemento
        mButtonUpload = findViewById(R.id.caricaButton)
        mTextViewShowUplaods = findViewById(R.id.mostraCaricaTextView)
        mEditTextFileName = findViewById(R.id.nomeFarmacoEditText)
        mImageView = findViewById(R.id.immagine)
        mProgressBar = findViewById(R.id.progressBar)
        //UPLOAD, con la string "uploads" andremo in quella cartella senò andiamo al top node
        mStorageRef = FirebaseStorage.getInstance().reference
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users/" + auth.currentUser!!.uid)

        mButtonChooseImage.setOnClickListener { openFileChooser() } //creare metodo
        mButtonUpload.setOnClickListener { uploadFile() } //semplificato
        mTextViewShowUplaods.setOnClickListener{
            val openClockIntent = Intent(AlarmClock.ACTION_SET_ALARM) //apre direttamente l'orologio, funziona
            openClockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(openClockIntent)
        }
        val nomeFarmaco = intent.getStringExtra("nomeFarmaco")
        val imgFarmaco = intent.getStringExtra("imgFarmaco")
        if(nomeFarmaco != null && imgFarmaco != null){
            mEditTextFileName.text = Editable.Factory.getInstance().newEditable(nomeFarmaco) //da testare
            Upload().convertImg(imgFarmaco).downloadUrl.addOnSuccessListener { uri ->
                // Pass it to Picasso to download, show in ImageView and caching
                Picasso.get().load(uri.toString()).into(mImageView)
            }.addOnFailureListener {
                // Handle any errors
            }
        }
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
            mImageUri = data.data!! ////return del uri dell'immagine scelta (forziamo non null)
            Picasso.get().load(mImageUri).into(mImageView)
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
        if (mImageUri != Uri.parse("")) { //controllo che effettivamente abbia selezionato un'immagine (DA TESTARE)
            //mStorageRef punta alla cartella Storage
            val fileReference = mStorageRef.child(System.currentTimeMillis().toString() + "." + getFileExtension(mImageUri)) //mStorageRef.child("uploads/" + System.currentTime QUANDO SENZA REFERENCE nella variabile privata
            //il nome è formato da tempo in ms ed estensione per evitare omonimi

            fileReference.putFile(mImageUri) //upload del file , continua sotto
                .addOnSuccessListener { taskSnapshot ->
                    //quando upload finisce, resetto la progressbar faccio delay di 5 millisec
                    val handler = Handler()
                    handler.postDelayed({ mProgressBar.progress = 0 }, 500)
                    Toast.makeText(this, "Upload riuscito", Toast.LENGTH_LONG).show()

                    //chiamo costruttore e prendo l'edit text col nome del farmaco e IL percorso DELL'IMMAGINE (diverso per ogni utente)
                    val upload = Upload(mEditTextFileName.text.toString().trim { it <= ' ' }, taskSnapshot.storage.path)
                    //per avere anche i meta data (URL,name)
                    //crea nuova entrata nel db con unico id
                    val uploadId = mDatabaseRef.push().key
                    //prendo id e gli setto i dati dell'upload file che contiene nome e immagine
                    mDatabaseRef.child("farmaci/" + uploadId!!).setValue(upload)

                } //non finisce, c'è il punto
                .addOnFailureListener { e -> //azioni quando upload non avviene
                    Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                } //non finisce, c'è il punto
                .addOnProgressListener { tasksnapshot -> //quando upload sta avvenendo, voglio aggiornare la progress bar con la percentuale corrente
                    val progress = 100.0 * tasksnapshot.bytesTransferred / tasksnapshot.totalByteCount //estrarre il progresso da tasksnapshot
                    mProgressBar.progress = progress.toInt() //aggiorno la progress bar
                }
                .addOnCompleteListener{ //quando finisce, aspetto un attimo e poi torno al main
                    Thread.sleep(2000)
                    finish() //chiude questa e torna a quella prima
                }
        } else {
            Toast.makeText(this, "Nessun file selezionato", Toast.LENGTH_SHORT).show()
        }
    }
}