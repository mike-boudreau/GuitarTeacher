package com.boudreau.guitarteacher.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.boudreau.guitarteacher.R
import com.boudreau.guitarteacher.models.Guitar

class GuitarListImageAdapter(private val guitarsList: List<Guitar>):
    RecyclerView.Adapter<GuitarListImageAdapter.ViewHolderForVPG2ImageAdapter>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderForVPG2ImageAdapter {
        // Inflate the layout fragment to be returned
        val guitarImageView = LayoutInflater.from(parent.context)
                                .inflate(R.layout.guitar_images_fragment, parent, false)

        // Return our custom view holder with our layout
        return ViewHolderForVPG2ImageAdapter(guitarImageView)
    }

    override fun onBindViewHolder(holder: ViewHolderForVPG2ImageAdapter, position: Int) {
        // Get the image at the current position
        val currentImage = guitarsList[position].largeImage

        // Tell the ViewHolder passed in which image to display
        holder.itemView.findViewById<ImageView>(R.id.imgGuitarImage).setImageBitmap(currentImage)
    }

    override fun getItemCount(): Int {
        // Return the number of images in our list
        return guitarsList.size
    }

    inner class ViewHolderForVPG2ImageAdapter(itemView: View) : RecyclerView.ViewHolder(itemView)
}