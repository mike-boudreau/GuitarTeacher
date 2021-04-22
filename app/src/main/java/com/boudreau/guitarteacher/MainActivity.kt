package com.boudreau.guitarteacher

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.managers.GtUserManager
import com.boudreau.guitarteacher.models.User
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Class fields
    lateinit var dbHelper: DbHelper
    private lateinit var loggedInUser: User
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set the UI
        setUI()

        // Event listener for learn to play button
        findViewById<Button>(R.id.btnLearnToPlay).setOnClickListener {
            // Create an intent linked to the LearnActivity page passing it the logged in user id
            val learnActivityIntent = Intent(this, LearnProgressActivity::class.java)
            learnActivityIntent.putExtra(LOGGED_IN_USER_ID, loggedInUser.userId)
            startActivity(learnActivityIntent)
        }

        // Event listener for explore guitars button
        findViewById<Button>(R.id.btnExploreGuitars).setOnClickListener {
            val exploreActivityIntent = Intent(this, ExploreGuitarsActivity::class.java)
            exploreActivityIntent.putExtra(LOGGED_IN_USER_ID, loggedInUser.userId)
            startActivity(exploreActivityIntent)
        }

        // Event listener for Find a shop button
        findViewById<Button>(R.id.btnFindAShop).setOnClickListener {
            val findAShopActivityIntent = Intent(this, FindAShopActivity::class.java)
            findAShopActivityIntent.putExtra(LOGGED_IN_USER_ID, loggedInUser.userId)
            startActivity(findAShopActivityIntent)
        }
    }

    // Function to set UI elements
    @SuppressLint("SetTextI18n")
    private fun setUI() {
        // Initialize the database helper
        dbHelper = DbHelper(this)

        // Set the logged in user
        loggedInUser =
                GtUserManager.getUserById(dbHelper, intent.extras!!.getInt(LOGGED_IN_USER_ID))!!

        // Set the welcome message
        findViewById<TextView>(R.id.lblWelcome).text = getString(R.string.welcome) + " ${loggedInUser.firstName}"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_log_out -> {
                // Sign the user out and go back to the login screen
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            R.id.action_edit_profile -> {
                // Go to the edit profile activity
                val intent = Intent(this, EditProfileActivity::class.java)
                intent.putExtra(LOGGED_IN_USER_ID, loggedInUser.userId)
                startActivity(intent)
                finish()
                true
            }
            R.id.action_delete_profile -> {
                // Verify the user actually wants their profile deleted
                val verificationDialog = AlertDialog.Builder(this)
                verificationDialog.setTitle(getString(R.string.confirm_profile_delete ))
                verificationDialog.setMessage(getString(R.string.profile_delete_question))
                        verificationDialog . setPositiveButton (getString(R.string.yes)) { _: DialogInterface, _: Int ->
                    // Attempt to delete the user from the database
                    try {
                        // Delete the user from the database
                        GtUserManager.deleteUser(dbHelper, loggedInUser)

                        // Sign out the current firebase authorized user
                        auth.signOut()

                        // Let the user know account deletion was successful
                        Toast.makeText(
                                this,
                                R.string.successful_profile_delete, Toast.LENGTH_LONG).show()

                        // Start a new thread and have it wait a few seconds before exiting
                        try {
                            Thread {
                                // Pause three seconds before exiting to the login screen
                                Thread.sleep(3000)

                                // Send the user back to the login screen
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }.start()
                        } catch (e: InterruptedException) {
                            // Log the error
                            Log.d("ThreadInterrupted", "The current thread was interrupted")
                        }
                    } catch (s: SQLiteException) {
                        // Log the error
                        Log.d("UnableToDelete", "An error occurred in deleting the user")

                        // Let the user know the deletion was unsuccessful
                        Toast.makeText(
                                this, R.string.failed_profile_delete, Toast.LENGTH_LONG).show()
                    }
                }
                        verificationDialog . setNegativeButton (getString(R.string.no)) { _: DialogInterface, _: Int ->
                    // Nothing needs to be done
                }

                // Display the actual dialog to the user to confirm account deletion
                val dialog = verificationDialog . create ()
                dialog . show ()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}