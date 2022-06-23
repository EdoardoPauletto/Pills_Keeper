package it.uninsubria.prova;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private TextView mTextViewShowUplaods;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Uri mImageUri;

    //UPLOAD
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonChooseImage = findViewById(R.id.pulsanteScegli);
        mButtonUpload = findViewById(R.id.pulsanteCarica);
        mTextViewShowUplaods = findViewById(R.id.testoMostraCarica);
        mEditTextFileName = findViewById(R.id.testoModificabile);
        mImageView = findViewById(R.id.immagine);
        mProgressBar = findViewById(R.id.progressBar);

        //UPLOAD
        //con la string "uploads" andremo i nquella cartella senò andiamo al top node
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //creare metodo
                openFileChooser();
            }
        });

        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });

        mTextViewShowUplaods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


    }

    private void openFileChooser(){
        Intent intent = new Intent();
        //vede solo immagini
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
        //dopo creo onActivityResult che verrà chiamato quando prendo il file ^
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //requestcode == richiesta che vogliamo che faccia
        //resultcode == RESULT_OK se prende l'immagine va alla next line
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            //se l'utente sceglie un' immagine controlla che non sia nulla
            //return del uri dell'immagine scelta
            mImageUri = data.getData(); //contiene l'uri

            Picasso.with(this).load(mImageUri).into(mImageView);
            //mImageView.setImageURI(mImageUri);

        }
    }

    //UPLOAD ritorna il tipo di file
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //UPLOAD
    private void uploadFile(){
        //controllo che effettivamente abbia selezionato un'immagine
        if(mImageUri != null){ //mStorageRef punta lla cartella di upload
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
            + "." + getFileExtension(mImageUri)); //mStorageRef.child("uploads/" + System.currentTime QUANDO SENZA REFERENCE nella variabile privata
            //il nome è formato da tempo in ms ed estensione per evitare omonimi

            fileReference.putFile(mImageUri)//upload del file , continua sotto
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //azioni quando upload avviene
                            //quando avviene resetto la progressbar faccio delay di 5 sec
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 500);

                            Toast.makeText(MainActivity.this, "Upload riuscito", Toast.LENGTH_LONG).show();
                            //upload class
                            //creo costruttore e prendo l'edit text perchè è dove andra il nome del file
                            Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
                                    taskSnapshot.getUploadSessionUri().toString());
                            //per avere anche i meta data (URL,name)
                            //crea nuova entrata nel db con unico id
                            String uploadId = mDatabaseRef.push().getKey();
                            //prendo id e gli setto i dati dell'upload file che contiene nome e immagine
                            mDatabaseRef.child(uploadId).setValue(upload);

                        }
                    })//non finisce, c'è il punto
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e){
                            //azioni quando upload non avviene
                            //no solo this perchè siamo in una classe interna
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })//non finisce, c'è il punto
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot tasksnapshot) {
                            //azioni quando upload sta avvenendo
                            //voglio aggiornare la progress bar con la percentuale corrente
                            //estrarre il progresso da tasksnapshot
                            double progress = (100.0 * tasksnapshot.getBytesTransferred() / tasksnapshot.getTotalByteCount());
                            //aggiorno la progress bar
                            mProgressBar.setProgress((int)progress);
                        }
                    });
        }
        //se non lo seleziona
        else{
            Toast.makeText(this, "Nessun file selezionato", Toast.LENGTH_SHORT).show();
        }
    }

}