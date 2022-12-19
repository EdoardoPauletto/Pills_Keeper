package com.uninsubria.pillskeeper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class PilloleAdapter(private val mList: List<Upload>): RecyclerView.Adapter<PilloleAdapter.ViewHolder>() {
    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]

        // sets the image to the imageview from our itemHolder class
        holder.imageView.setImageURI(ItemsViewModel.mImageUrl?.toUri())
        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/prove-b822e.appspot.com/o/1656335148706.png?alt=media&token=aeeefb3e-c3ac-4da3-a62c-0bd67a420c3e").into(holder.imageView)
        //holder.imageView.setImageResource(ItemsViewModel.mImageUrl)

        // sets the text to the textview from our itemHolder class
        holder.textView.text = ItemsViewModel.name

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageview)
        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}