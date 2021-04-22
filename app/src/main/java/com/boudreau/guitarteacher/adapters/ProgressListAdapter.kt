package com.boudreau.guitarteacher.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boudreau.guitarteacher.R
import com.boudreau.guitarteacher.models.Chord

// Adapter class for progress recycler view
class ProgressListAdapter(context: Context, private val completedChords: List<Chord>) :
    RecyclerView.Adapter<ProgressListAdapter.ProgressItemViewHolder>() {

    // Information for adapter
    private val layoutInflater = LayoutInflater.from(context)
    private lateinit var chordsList: ArrayList<Chord>
    private lateinit var currentChord: Chord

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressItemViewHolder {
        // Inflate the view of the individual progress item
        val itemView =
            layoutInflater.inflate(R.layout.progress_tracker_item, parent, false)

        // Return the progress item
        return ProgressItemViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProgressItemViewHolder, position: Int) {
        // Set the current chord
        currentChord = completedChords[position]

        // Set the information of the current card
        if (currentChord.chordType == "Power") {
            holder.lblChordDescription.text =
                    currentChord.chordName.replace('_', ' ')
        }
        else {
            holder.lblChordDescription.text =
                    "${currentChord.chordName.replace('_', ' ')} ${currentChord.chordType}"
        }

        holder.imgChordDiagram.setImageBitmap(currentChord.diagram)

        // Set the position
        holder.progressItemPosition = completedChords[position].chordId
    }

    override fun getItemCount(): Int = completedChords.size

    // Inner class to handle setting up the each card in the recycler view
    inner class ProgressItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define properties for the progress item
        val lblChordDescription: TextView = itemView.findViewById(R.id.lblChordDescription)
        val imgChordDiagram: ImageView = itemView.findViewById(R.id.imgChordDiagram)

        // Current position
        var progressItemPosition = 0

        init {
            itemView.setOnClickListener {
                // Nothing needs to happen
            }
        }
    }

    fun setChords(chords: ArrayList<Chord>) {
        // Set the class level list
        chordsList = chords

        // Notify of changed data
        notifyDataSetChanged()
    }
}