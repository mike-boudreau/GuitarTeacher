package com.boudreau.guitarteacher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.managers.GtUserManager
import com.boudreau.guitarteacher.models.User

class EditProfileActivity : AppCompatActivity() {

    // UI elements
    private lateinit var txtEditFirstName: EditText
    private lateinit var txtEditLastName: EditText
    private lateinit var txtEditPhoneNumber: EditText
    private lateinit var txtEditUsername: EditText
    private lateinit var txtEditPassword: EditText
    private lateinit var txtConfirmEditPassword: EditText
    private lateinit var lblEditErrorMessage: TextView
    private lateinit var btnEditConfirm: TextView
    private lateinit var btnEditCancel: TextView

    // Objects for database tasks
    lateinit var dbHelper: DbHelper
    private lateinit var loggedInUser: User
    private lateinit var userEdit: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set the UI
        setUI()

        // Event handler for edit confirm button
        btnEditConfirm.setOnClickListener {
            // Validate the form
            if (validateProfileEdits()) {
                // Attempt to update the database
                if (attemptProfileEdit()) {
                    // User was successfully updated in the database, go back to the main activity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra(LOGGED_IN_USER_ID, loggedInUser.userId)
                    startActivity(intent)
                    finish()
                }
            }
        }

        // Event handler for the cancel button
        btnEditCancel.setOnClickListener {
            // Go back to the home page
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(LOGGED_IN_USER_ID, loggedInUser.userId)
            startActivity(intent)
            finish()
        }
    }

    private fun setUI() {
        // Initialize the variables for the form fields
        txtEditFirstName = findViewById(R.id.txtEditFirstName)
        txtEditLastName = findViewById(R.id.txtEditLastName)
        txtEditPhoneNumber = findViewById(R.id.txtEditPhoneNumber)
        txtEditUsername = findViewById(R.id.txtEditUsername)
        txtEditPassword = findViewById(R.id.txtEditPassword)
        txtConfirmEditPassword = findViewById(R.id.txtConfirmEditPassword)
        lblEditErrorMessage = findViewById(R.id.lblEditErrorMessage)
        btnEditConfirm = findViewById(R.id.btnEditConfirm)
        btnEditCancel = findViewById(R.id.btnEditCancel)

        // Set focus on first name text field
        txtEditFirstName.requestFocus()

        // Disable the phone number field
        txtEditPhoneNumber.isEnabled = false

        // Initialize the database helper
        dbHelper = DbHelper(this)

        // Set the logged in user
        loggedInUser =
            GtUserManager.getUserById(dbHelper, intent.extras!!.getInt(LOGGED_IN_USER_ID))!!

        // Set the UI fields
        txtEditFirstName.setText(loggedInUser.firstName)
        txtEditLastName.setText(loggedInUser.lastName)
        txtEditPhoneNumber.setText(loggedInUser.phoneNumber)
        txtEditUsername.setText(loggedInUser.username)
        txtEditPassword.setText(loggedInUser.password)
        txtConfirmEditPassword.setText(loggedInUser.password)
    }

    // Function to insert a new user into the database or catch a sqlite exception
    private fun attemptProfileEdit(): Boolean {
        // Initialize a user object with the information from the form
        userEdit = User(
            loggedInUser.userId,
            txtEditFirstName.text.toString(),
            txtEditLastName.text.toString(),
            txtEditPhoneNumber.text.toString(),
            txtEditUsername.text.toString(),
            txtEditPassword.text.toString(),
            loggedInUser.completedChords)

        // Attempt to update the user in the database
        val updateState = GtUserManager.updateUser(dbHelper, userEdit)

        // Check if an error was passed back
        if (updateState == 1) {
            // Set the logged in user as the edited user
            loggedInUser = userEdit

            // Let the user know that the database update was successful
            Toast.makeText(
                this,
                getString(R.string.successful_edit),
                Toast.LENGTH_LONG)
                .show()

            // Return true
            return true
        }
        else {
            // Duplicate username
            lblEditErrorMessage.text = getString(R.string.username_taken_error)

            // Return false
            return false
        }
    }

    // Make sure all entries are valid
    private fun validateProfileEdits(): Boolean {
        // Check the first name field
        if (validateFirstName()) {
            // Check the last name field
            if (validateLastName()) {
                // Check the username
                if (validateUsername()) {
                    // Check the state
                    when (validatePassword()) {
                        SUCCESS -> {
                            // Everything is valid. Reset the error message and return true
                            lblEditErrorMessage.text = ""
                            return true
                        }
                        NO_MATCH -> {
                            // Password mismatch error
                            lblEditErrorMessage.text = getString(R.string.password_match_error)
                            return false
                        }
                        INVALID -> {
                            // Password criteria not met
                            lblEditErrorMessage.text = getString(R.string.password_criteria_error)
                            return false
                        }
                        else -> {
                            // Something else was returned
                            lblEditErrorMessage.text = getString(R.string.default_sign_up_error)
                            return false
                        }
                    }
                }
                else {
                    // Invalid username
                    lblEditErrorMessage.text = getString(R.string.username_invalid_error)
                    return false
                }
            }
            else {
                // Invalid last name
                lblEditErrorMessage.text = getString(R.string.last_name_error)
                return false
            }
        }
        else {
            // Invalid first name
            lblEditErrorMessage.text = getString(R.string.first_name_error)
            return false
        }
    }

    // Validate the first name field
    private fun validateFirstName(): Boolean {
        // Check the first name
        return txtEditFirstName.text.matches(Regex("\\b([^\\d\\W]|'|[ ]){1,50}\\b"))
    }

    // Validate the last name field
    private fun validateLastName(): Boolean {
        // Check the last name
        return txtEditLastName.text.matches(Regex("\\b([^\\d\\W]|'|[ ]){1,50}\\b"))
    }

    // Validate the username field
    private fun validateUsername(): Boolean {
        // Check the username (validation of uniqueness happens later, this is only for validity)
        return txtEditUsername.text.matches(Regex("([A-Z]|[a-z]|[0-9]){1,50}"))
    }

    // Validate the password field
    private fun validatePassword(): String {
        // Check to make sure the password entered is valid
        return if (txtEditPassword.text.matches(
                Regex("(?=.*[A-Z])(?=.*\\d)(?=.*[a-z])(?=.*[!@#$%^&*_\\-+=|.?]).{8,}"))) {
            // Valid password, make sure the password/confirm password fields match
            if (txtConfirmEditPassword.text.toString() == txtEditPassword.text.toString()) {
                // Password is valid and matches, return success
                SUCCESS
            } else {
                // Passwords don't match, return no match
                NO_MATCH
            }
        } else {
            // Invalid password, return invalid
            INVALID
        }
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
                // Go back to the home page
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(LOGGED_IN_USER_ID, loggedInUser.userId)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}