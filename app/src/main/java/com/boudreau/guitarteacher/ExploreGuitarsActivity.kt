package com.boudreau.guitarteacher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewpager2.widget.ViewPager2
import com.boudreau.guitarteacher.adapters.GuitarListImageAdapter
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.managers.GtUserManager
import com.boudreau.guitarteacher.managers.GuitarManager
import com.boudreau.guitarteacher.models.Guitar
import com.boudreau.guitarteacher.models.User
import com.google.android.material.snackbar.Snackbar

class ExploreGuitarsActivity : AppCompatActivity() {

    // Database objects and logged in user
    lateinit var dbHelper: DbHelper
    private lateinit var loggedInUser: User
    private lateinit var guitars: ArrayList<Guitar>

    // UI elements
    private lateinit var vpgExploreSwiper: ViewPager2
    private lateinit var guitarImagesAdapter: GuitarListImageAdapter
    private lateinit var btnSeeDetails: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.explore_guitars_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set the UI
        setUI()

        // Set the view pager adapter
        setAdapter()

        // Event listener for see details button
        btnSeeDetails.setOnClickListener {
            val guitarDetailsActivityIntent = Intent(this, GuitarDetailsActivity::class.java)
            guitarDetailsActivityIntent.putExtra(SELECTED_GUITAR_ID, vpgExploreSwiper.currentItem + 1)
            guitarDetailsActivityIntent.putExtra(LOGGED_IN_USER_ID, loggedInUser.userId)
            startActivity(guitarDetailsActivityIntent)
        }
    }

    // Set the view pager 2 adapter
    private fun setAdapter() {
        // Create a VPG2ImageAdapter using the guitar images list
        guitarImagesAdapter = GuitarListImageAdapter(guitars)

        // Set the adapter for the view pager
        vpgExploreSwiper.adapter = guitarImagesAdapter
    }

    private fun setUI() {
        // Initialize the database helper
        dbHelper = DbHelper(this)

        // Set the logged in user
        loggedInUser =
                GtUserManager.getUserById(dbHelper, intent.extras!!.getInt(LOGGED_IN_USER_ID))!!

        // Create the snackbar
        Snackbar.make(findViewById<CoordinatorLayout>(R.id.CoordinatorLayout),
                R.string.snackbar_swipe_message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(resources
                        .getColor(R.color.oxford_blue))
                .setTextColor(resources
                        .getColor(R.color.all_white)).show()

        // Set the UI elements
        vpgExploreSwiper = findViewById(R.id.vpgExploreSwiper)
        btnSeeDetails = findViewById(R.id.btnSeeDetails)

        // Get the guitars for the view pager
        guitars = getGuitarsFromDatabase()
    }

    // Function to retrieve the guitars from the database
    private fun getGuitarsFromDatabase(): ArrayList<Guitar> {
        return GuitarManager.getAllGuitars(dbHelper)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_back -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}