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
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class SignUpActivity : AppCompatActivity() {

    // UI elements
    private lateinit var txtFirstName: EditText
    private lateinit var txtLastName: EditText
    private lateinit var txtPhoneNumber: EditText
    private lateinit var txtUsername: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtConfirmPassword: EditText
    private lateinit var lblErrorMessageSignUp: TextView
    private lateinit var btnSignUpConfirm: TextView
    private lateinit var btnCancel: TextView

    // Objects for database tasks
    lateinit var dbHelper: DbHelper
    private lateinit var newUser: User
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Stored info from extras
    private lateinit var phoneNumberExtra: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set the UI
        setUI()

        // Event handler for sign up button
        btnSignUpConfirm.setOnClickListener {
            // Validate the form
            if (validateSignUpForm()) {
                // Attempt to insert the new user into the database
                if (attemptNewUserSignUp()) {
                    // Create a new user with info from the successfully added user
                    val newUserWithId = GtUserManager.getUserByPhoneNumber(dbHelper,
                            newUser.phoneNumber)

                    if (auth.currentUser != null) {
                        // Send the user to the welcome screen
                        val intent = Intent(this, WelcomeScreenActivity::class.java)
                        intent.putExtra(LOGGED_IN_USER_ID, newUserWithId!!.userId)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        // Bring the user back to the login screen to authenticate with firebase
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.putExtra(LOGGED_IN_USER_ID, newUserWithId!!.userId)
                        intent.putExtra(PHONE_NUMBER, newUserWithId.phoneNumber)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        // Event handler for cancel button
        btnCancel.setOnClickListener {
            // Sign out and go back to the login screen
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    
    private fun setUI() {
        // Initialize the variables for the form fields
        txtFirstName = findViewById(R.id.txtFirstName)
        txtLastName = findViewById(R.id.txtLastName)
        txtPhoneNumber = findViewById(R.id.txtPhoneNumber)
        txtUsername = findViewById(R.id.txtUsernameSignUp)
        txtPassword = findViewById(R.id.txtPassword)
        txtConfirmPassword = findViewById(R.id.txtConfirmPassword)
        lblErrorMessageSignUp = findViewById(R.id.lblErrorMessageSignUp)
        btnSignUpConfirm = findViewById(R.id.btnSignUpConfirm)
        btnCancel = findViewById(R.id.btnCancel)

        // Set focus on first name text field
        txtFirstName.requestFocus()

        if (intent.extras != null) {
            // Check if a phone number was passed through
            if (intent.getStringExtra(PHONE_NUMBER) != null) {
                phoneNumberExtra = intent.getStringExtra(PHONE_NUMBER).toString()

                // Set the text of the phone number text field
                txtPhoneNumber.setText(phoneNumberExtra)
            }

            // Check if the intent requesting a sign up has something in it
            if (intent.getStringExtra(SIGN_UP_REQUIRED) != null) {
                // Toast the user asking them to sign up
                Toast.makeText(
                        this,
                        intent.getStringExtra(SIGN_UP_REQUIRED),
                        Toast.LENGTH_LONG)
                        .show()
            }
        }

        // Initialize the database helper
        dbHelper = DbHelper(this)
    }

    // Function to insert a new user into the database or catch a sqlite exception
    private fun attemptNewUserSignUp(): Boolean {
        // Initialize a user object with the information from the form
        newUser = User(
            null,
            txtFirstName.text.toString(),
            txtLastName.text.toString(),
            formatPhoneNumber(txtPhoneNumber.text.toString()),
            txtUsername.text.toString(),
            txtPassword.text.toString(),
            null)

        // Attempt to add the user to the database
        val insertionState = GtUserManager.createUser(dbHelper, newUser)

        // Check if an error was passed back
        if (insertionState == null) {
            // Let the user know that the database insertion was successful
            Toast.makeText(
                this,
                    getString(R.string.successful_sign_up),
                    Toast.LENGTH_LONG)
                .show()

            // Return true
            return true
        }
        else {
            // Set the error message label accordingly
            when {
                insertionState.message!!.toString().toLowerCase(Locale.US).contains("username") -> {
                    // Duplicate username
                    lblErrorMessageSignUp.text = getString(R.string.username_taken_error)

                    // Return false
                    return false
                }
                insertionState.message!!.toString().toLowerCase(Locale.US).contains("phone_number") -> {
                    // Duplicate phone number
                    lblErrorMessageSignUp.text = getString(R.string.phone_number_taken_error)

                    // Return false
                    return false
                }
                else -> {
                    // Default sql error
                    lblErrorMessageSignUp.text = getString(R.string.default_sql_user_insert_error)

                    // Return false
                    return false
                }
            }
        }
    }

    // Function to make sure the phone number is properly entered into the database
    private fun formatPhoneNumber(phoneNumber: String): String {
        // Check if the number already passed in already contains a country code
        return if (!phoneNumber.contains("+")) {
            // Add the country code for United States
            "+1$phoneNumber"
        } else {
            phoneNumber
        }
    }

    // Make sure all entries are valid
    private fun validateSignUpForm(): Boolean {
        // Check the first name field
        if (validateFirstName()) {
            // Check the last name field
            if (validateLastName()) {
                // Check the phone number
                if (validatePhoneNumber()) {
                    // Check the username
                    if (validateUsername()) {
                        // Check the state
                        when (validatePassword()) {
                            SUCCESS -> {
                                // Everything is valid. Reset the error message and return true
                                lblErrorMessageSignUp.text = ""
                                return true
                            }
                            NO_MATCH -> {
                                // Password mismatch error
                                lblErrorMessageSignUp.text = getString(R.string.password_match_error)
                                return false
                            }
                            INVALID -> {
                                // Password criteria not met
                                lblErrorMessageSignUp.text = getString(R.string.password_criteria_error)
                                return false
                            }
                            else -> {
                                // Something else was returned
                                lblErrorMessageSignUp.text = getString(R.string.default_sign_up_error)
                                return false
                            }
                        }
                    }
                     else {
                        // Invalid username
                        lblErrorMessageSignUp.text = getString(R.string.username_invalid_error)
                        return false
                    }
                }
                else {
                    // Invalid phone number
                    lblErrorMessageSignUp.text = getString(R.string.phone_number_error)
                    return false
                }
            }
            else {
                // Invalid last name
                lblErrorMessageSignUp.text = getString(R.string.last_name_error)
                return false
            }
        }
        else {
            // Invalid first name
            lblErrorMessageSignUp.text = getString(R.string.first_name_error)
            return false
        }
    }

    // Validate the first name field
    private fun validateFirstName(): Boolean {
        // Check the first name
        return txtFirstName.text.matches(Regex("\\b([^\\d\\W]|'|[ ]){1,50}\\b"))
    }

    // Validate the last name field
    private fun validateLastName(): Boolean {
        // Check the last name
        return txtLastName.text.matches(Regex("\\b([^\\d\\W]|'|[ ]){1,50}\\b"))
    }

    // Validate the phone number field
    private fun validatePhoneNumber(): Boolean {
        // Check the phone number
        return txtPhoneNumber.text.matches(
            Regex("([+][1]|[+][1][ ])?([0-9]){3}(([0-9]){3}|([-]([0-9]){3}))(([0-9]){4}|([-]([0-9]){4}))"))
    }

    // Validate the username field
    private fun validateUsername(): Boolean {
        // Check the username (validation of uniqueness happens later, this is only for validity)
        return txtUsername.text.matches(Regex("([A-Z]|[a-z]|[0-9]){1,50}"))
    }

    // Validate the password field
    private fun validatePassword(): String {
        // Check to make sure the password entered is valid
        return if (txtPassword.text.matches(
                Regex("(?=.*[A-Z])(?=.*\\d)(?=.*[a-z])(?=.*[!@#$%^&*_\\-+=|.?]).{8,}"))) {
            // Valid password, make sure the password/confirm password fields match
            if (txtConfirmPassword.text.toString() == txtPassword.text.toString()) {
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
                // Sign out and go back to the login screen
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)

                // Check if there is anything to pass back
                if (intent.extras != null) {
                    intent.putExtra(PHONE_NUMBER, phoneNumberExtra)
                }

                // Start the activity
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}