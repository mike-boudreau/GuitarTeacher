package com.boudreau.guitarteacher

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

/**
 * A simple [Fragment] subclass.
 * Use the [GuitarImagesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@Suppress("KDocUnresolvedReference")
class GuitarImagesFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Event listener for guitar image view
        view.findViewById<ImageView>(R.id.imgGuitarImage).setOnClickListener {
            // Create an intent linked to the GuitarDetailsActivity page
            val guitarDetailsActivityIntent = Intent(this.activity, GuitarDetailsActivity::class.java)

            // Start the activity
            startActivity(guitarDetailsActivityIntent)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.guitar_images_fragment, container, false)
    }
}