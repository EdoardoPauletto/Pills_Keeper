package com.uninsubria.pillskeeper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso


class PilloleAdapter(private val lista: List<Upload>): RecyclerView.Adapter<PilloleAdapter.ElementiVista>() {
    // crea nuove views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementiVista {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ElementiVista(view)
    }

    // associa gli elementi dell'elenco a una vista
    override fun onBindViewHolder(holder: ElementiVista, posizione: Int) {

        val elemento = lista[posizione]

        // imposta l'immagine "https://firebasestorage.googleapis.com/v0/b/prove-b822e.appspot.com/o/1656335148706.png?alt=media&token=aeeefb3e-c3ac-4da3-a62c-0bd67a420c3e"
        //Picasso.get().load(elemento.mImageUrl).into(holder.imageView)
        val s = FirebaseStorage.getInstance().getReferenceFromUrl("gs://prove-b822e.appspot.com" + elemento.mImageUrl)
        s.downloadUrl.addOnSuccessListener { uri ->
            // Pass it to Picasso to download, show in ImageView and caching
            Picasso.get().load(uri.toString()).into(holder.imageView)
        }.addOnFailureListener {
            // Handle any errors
        }
        // imposta il testo
        holder.textView.text = elemento.name
    }

    // restituisce il numero di elementi della lista
    override fun getItemCount(): Int {
        return lista.size
    }

    // Holds the views for adding it to image and text
    class ElementiVista(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}