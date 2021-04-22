package com.boudreau.guitarteacher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.managers.GtUserManager
import com.boudreau.guitarteacher.managers.GuitarManager
import com.boudreau.guitarteacher.models.Guitar
import com.boudreau.guitarteacher.models.User

class GuitarDetailsActivity : AppCompatActivity() {

    // Database objects and logged in user
    lateinit var dbHelper: DbHelper
    private lateinit var loggedInUser: User
    private lateinit var selectedGuitar: Guitar

    // UI elements
    private lateinit var imgGuitarDetailPic: ImageView
    private lateinit var lblBrandInfo: TextView
    private lateinit var lblModelInfo: TextView
    private lateinit var lblTypeInfo: TextView
    private lateinit var lblFretsInfo: TextView
    private lateinit var lblYearInventedInfo: TextView
    private lateinit var lblPriceInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guitar_details_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Initialize variables and set the UI
        setUI()

        // Define a thread object
        val thread = Thread {
            // Loop through a circle
            for (i in 1..360) {
                try {
                    // Pause the thread for 10 milliseconds
                    Thread.sleep(7)
                } catch (e: InterruptedException) {
                    // Log the error to the debugger
                    Log.d("ThreadSleepError", e.message.toString())
                }

                // Change the rotation by one degree
                imgGuitarDetailPic.rotation = i.toFloat()
            }
        }

        // Start the thread
        thread.start()
    }

    private fun setUI() {
        // Initialize the database helper
        dbHelper = DbHelper(this)

        // Set the logged in user
        loggedInUser =
                GtUserManager.getUserById(dbHelper, intent.extras!!.getInt(LOGGED_IN_USER_ID))!!

        // Set the UI elements
        imgGuitarDetailPic = findViewById(R.id.imgGuitarDetailPic)
        lblBrandInfo = findViewById(R.id.lblBrandInfo)
        lblModelInfo = findViewById(R.id.lblModelInfo)
        lblTypeInfo = findViewById(R.id.lblTypeInfo)
        lblFretsInfo = findViewById(R.id.lblFretsInfo)
        lblYearInventedInfo = findViewById(R.id.lblYearInventedInfo)
        lblPriceInfo = findViewById(R.id.lblPriceInfo)

        // Handle the selected guitar
        handleGuitarSelection()
    }

    // Function to update the UI based on the selected guitar
    private fun handleGuitarSelection() {
        // Set the selected guitar
        selectedGuitar =
                GuitarManager.getSelectedGuitar(dbHelper, intent.extras!!.getInt(SELECTED_GUITAR_ID))!!

        // Set the UI image and text properties based on the selected guitar
        imgGuitarDetailPic.setImageBitmap(selectedGuitar.smallImage)
        lblBrandInfo.text = selectedGuitar.brand
        lblModelInfo.text = selectedGuitar.model
        lblTypeInfo.text = selectedGuitar.guitarType
        lblFretsInfo.text = selectedGuitar.numberOfFrets.toString()
        lblYearInventedInfo.text = selectedGuitar.yearInvented
        lblPriceInfo.text = selectedGuitar.price
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