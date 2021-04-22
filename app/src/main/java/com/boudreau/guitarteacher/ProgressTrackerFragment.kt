package com.boudreau.guitarteacher

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boudreau.guitarteacher.adapters.ProgressListAdapter
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.managers.GtUserManager
import com.boudreau.guitarteacher.models.User

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ProgressTrackerFragment : Fragment() {

    // Class fields
    lateinit var dbHelper: DbHelper
    private lateinit var lstProgressItems: RecyclerView
    private lateinit var progressListAdapter: ProgressListAdapter
    private lateinit var loggedInUser: User

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.progress_tracker_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the UI and initialize variables
        setUI()
    }

    override fun onResume() {
        super.onResume()
        setUI()
    }

    // Function to set the UI and initialize other class variables
    private fun setUI() {
        // Initialize the database helper
        dbHelper = DbHelper(view?.context)

        // Set the logged in user
        loggedInUser = GtUserManager.getUserById(
                dbHelper, activity?.intent!!.extras!!.getInt(LOGGED_IN_USER_ID))!!

        // Get reference to recycler view
        lstProgressItems = view?.findViewById(R.id.lstProgressTracker)!!

        // Set the layout manager and adapter
        initializeRecyclerView()
    }

    // Function to set the layout manager and adapter for the progress recycler adapter
    private fun initializeRecyclerView() {
        // Set the layout manager
        lstProgressItems.layoutManager = LinearLayoutManager(view?.context)

        // Initialize the adapter
        progressListAdapter = ProgressListAdapter(
                view?.context!!, loggedInUser.completedChords!!)

        // Set the adapter
        lstProgressItems.adapter = progressListAdapter

        // Call the set chords method
        progressListAdapter.setChords(loggedInUser.completedChords!!)
    }
}