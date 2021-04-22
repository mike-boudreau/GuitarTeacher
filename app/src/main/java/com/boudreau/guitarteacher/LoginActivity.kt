package com.boudreau.guitarteacher

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.managers.ChordManager
import com.boudreau.guitarteacher.managers.GtUserManager
import com.boudreau.guitarteacher.managers.GuitarManager
import com.boudreau.guitarteacher.managers.MusicStoreManager
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.io.File
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    // Objects to work with the database
    lateinit var dbHelper: DbHelper
    private lateinit var db: SQLiteDatabase

    // Objects for firebase authentication
    private lateinit var auth: FirebaseAuth
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var phoneNumber: String

    // UI elements
    private lateinit var txtPhoneNumberLogin: EditText
    private lateinit var txtVerificationCode: EditText
    private lateinit var btnSendCode: TextView
    private lateinit var btnVerify: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Adjust the UI
        setUI()

        // Check for a previously logged in user
        checkPreviousAuthorization()

        // Event handler for send code button
        btnSendCode.setOnClickListener {
            // Get the phone number entered by the user
            phoneNumber = txtPhoneNumberLogin.text.toString()

            // Begin the verification process
            startVerificationProcess()
        }

        // Event handler for verify button
        btnVerify.setOnClickListener {
            // Get the code entered by the user
            val codeEntered = txtVerificationCode.text.toString().trim()

            // Validate the code
            validateVerificationCode(codeEntered)
        }

        // Set callback methods
        setCallbacks()
    }

    // Function to initialize the UI elements
    private fun initializeLoginUiElements() {
        // Set the UI elements
        txtPhoneNumberLogin = findViewById(R.id.txtPhoneNumberLogin)
        txtVerificationCode = findViewById(R.id.txtVerificationCode)
        btnSendCode = findViewById(R.id.btnSendCode)
        btnVerify = findViewById(R.id.btnVerify)
    }

    // Create the guitar teacher database if it doesn't exist
    private fun createDatabaseIfNeeded() {
        // Create a File object of the database file
        val dbName = "guitarteacher.db"
        val file = File(dbName)

        // Check if a file matching the database name has already been created
        if (!file.exists()) {

            // Get a writeable database (also runs onCreate)
            db = dbHelper.writableDatabase

            // Check if database has already been loaded with chord/store/guitar information
            if (ChordManager.getChordNames(dbHelper, null).size == 1) {
                // Insert necessary values that can be created at initialization
                ChordManager.loadDatabaseWithChordInformation(dbHelper, applicationContext)
                MusicStoreManager.loadDatabaseWithMusicStores(dbHelper)
                GuitarManager.loadDatabaseWithGuitars(dbHelper, applicationContext)
            }
        }
    }

    // Function to adjust the ui initially
    private fun setUI() {
        // Initialize the database helper
        dbHelper = DbHelper(this)

        // Create the database
        createDatabaseIfNeeded()

        // Initialize the UI elements
        initializeLoginUiElements()

        // Check if any extras were passed in
        if (intent.extras != null) {
            // Auto-fill the phone number text box and set focus to the verification code button
            txtPhoneNumberLogin.setText(intent.getStringExtra(PHONE_NUMBER))
            btnSendCode.requestFocus()
        }
        else {
            // Set focus on phone number text field
            txtPhoneNumberLogin.requestFocus()
        }

        // Hide the verification code features
        txtVerificationCode.visibility = View.INVISIBLE
        btnVerify.visibility = View.INVISIBLE
    }

    // Resets the UI after failed verification
    private fun resetUI() {
        // Set focus on phone number text field
        txtPhoneNumberLogin.requestFocus()

        // Reset the verification code text box
        txtVerificationCode.setText("")

        // Hide the verification code features
        txtVerificationCode.visibility = View.INVISIBLE
        btnVerify.visibility = View.INVISIBLE
    }

    // Function to enable the verification controls in the UI
    private fun enableVerificationControls() {
        // Enable the verification controls
        txtVerificationCode.visibility = View.VISIBLE
        txtVerificationCode.requestFocus()
        btnVerify.visibility = View.VISIBLE
    }

    // Function tp toast the user
    private fun toastUser(toastMessage: String) {
        // Toast the user with the appropriate message
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
    }

    // When the application loads, check if there's an authorized user and handle it accordingly
    private fun checkPreviousAuthorization() {
        // Initialize the FirebaseAuth object
        auth = FirebaseAuth.getInstance()

        // Check if a user is logged in already
        if (auth.currentUser != null) {
            // Check if the user already has an account and go to the appropriate activity
            checkForUserAccountAndNavigate(formatPhoneNumber(auth.currentUser!!.phoneNumber!!))
        }
    }

    // Function to check the
    private fun startVerificationProcess() {
        // Make sure the phone number is valid
        if (phoneNumber.isNotEmpty()) {
            if (phoneNumber.matches(Regex("([+][1])?([0-9]){10}"))) {
                // Format the phone number
                phoneNumber = formatPhoneNumber(phoneNumber)

                // Send a verification code to the user's properly formatted phone number
                sendVerificationCode()
            }
            else {
                // Toast the user that their phone number is in the wrong format
                toastUser(getString(R.string.phone_number_error))
            }
        }
        else {
            // Toast the user to enter a phone number
            toastUser(getString(R.string.blank_phone_number))
        }
    }

    // Function to make sure that the phone number entered is properly formatted
    private fun formatPhoneNumber(phoneNumber: String): String {
        // Return the formatted phone number
        return if (!phoneNumber.contains("+")) {
            // Add the country code
            "+1$phoneNumber"
        }
        else {
            phoneNumber
        }
    }

    // Function to set the options for authorization and send the verification code
    private fun sendVerificationCode() {
        // Create a PhoneAuthOptions object
        val authOptions = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()

        // Send a verification code to the user
        PhoneAuthProvider.verifyPhoneNumber(authOptions)
    }

    private fun setCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            // runs when verification finishes successfully
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Check if the user already has an account and go to the appropriate activity
                checkForUserAccountAndNavigate(phoneNumber)
            }

            // Runs when verification fails
            override fun onVerificationFailed(f: FirebaseException) {
                // Log the error
                Log.d(FIREBASE_AUTH_ERROR, f.message.toString())
            }

            // Runs once the verification code is sent
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // Set the verification id and resend token
                storedVerificationId = verificationId
                resendToken = token

                // Enable the UI controls for verification
                enableVerificationControls()

                // Let the user know the code has been sent
                toastUser(getString(R.string.verification_sent))
            }
        }
    }

    private fun validateVerificationCode(codeEntered: String) {
        // Make sure the verification code entry is not empty
        if (codeEntered.isNotEmpty()) {
            // Set a PhoneAuthCredential object
            val credentials = PhoneAuthProvider.getCredential(storedVerificationId, codeEntered)

            // Sign the user in using the credentials
            signInWithPhoneAuthCredential(credentials)
        }
    }

    private fun signInWithPhoneAuthCredential(credentials: PhoneAuthCredential) {
        // Use the FirebaseAuth object to sign the user in
        auth.signInWithCredential(credentials)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Check if the user already has an account and go to the appropriate activity
                        checkForUserAccountAndNavigate(phoneNumber)
                    }
                    else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // Log the error
                        Log.d(FIREBASE_CREDENTIAL_ERROR,
                                (task.exception as FirebaseAuthInvalidCredentialsException)
                                        .message.toString())

                        // Toast the user letting them know the verification failed
                        toastUser(getString(R.string.invalid_credentials))

                        // Reset the UI
                        resetUI()
                    }
                }
    }

    // Check if the user already has an account in the application's database
    private fun checkForUserAccountAndNavigate(userPhoneNumber: String) {
        // Attempt to get the user from the database
        val signedUpUser =
                GtUserManager.getUserByPhoneNumber(dbHelper, userPhoneNumber)

        // Check if the user exists
        if (signedUpUser != null) {
            // User has signed up. Take the user to the welcome screen
            val intent = Intent(this, WelcomeScreenActivity::class.java)
            intent.putExtra(LOGGED_IN_USER_ID, signedUpUser.userId)
            startActivity(intent)
            finish()
        }
        else {
            // User hasn't signed up. Take them to the sign up menu
            val intent = Intent(this, SignUpActivity::class.java)
            intent.putExtra(PHONE_NUMBER, userPhoneNumber)
            intent.putExtra(SIGN_UP_REQUIRED, getString(R.string.sign_up_required))
            startActivity(intent)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_login, menu)
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
            R.id.action_sign_up -> {
                // Start the sign up activity
                startActivity(Intent(this, SignUpActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}