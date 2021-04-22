package com.boudreau.guitarteacher

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.managers.ChordManager
import com.boudreau.guitarteacher.managers.GtUserManager
import com.boudreau.guitarteacher.managers.ProgressManager
import com.boudreau.guitarteacher.models.Chord
import com.boudreau.guitarteacher.models.User
import com.google.android.material.snackbar.Snackbar
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LearnFragment : Fragment() {
    // Objects to work with the database
    lateinit var dbHelper: DbHelper
    private lateinit var loggedInUser: User
    private var selectedChord: Chord? = null

    // UI elements
    lateinit var spnChordSelector: Spinner
    lateinit var spnTypeSelector: Spinner
    private lateinit var imgChord: ImageView
    private lateinit var btnPlay: Button
    private lateinit var swtCompleted: SwitchCompat
    private lateinit var btnRecord: Button

    // Others
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaRecorder : MediaRecorder
    private var isRecording: Boolean = false
    private var internalRecordingFileName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.learn_fragment, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the UI and initialize variables
        setUI()

        // Set the event listeners
        setEventListeners()
    }

    override fun onStop() {
        super.onStop()

        if (mediaPlayer != null) {
            // Stop the media player if it's playing when page is left completely
            mediaPlayer?.stop()
        }
    }

    override fun onPause() {
        super.onPause()

        if (mediaPlayer != null) {
            // Stop the media player if it's playing when page is left completely
            mediaPlayer?.stop()
        }
    }

    // Function to handle setting the event listeners for the page
    private fun setEventListeners() {
        // Chord name spinner
        spnChordSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spnTypeSelector.selectedItemPosition == 0) {
                    // Check if the chord name contains the name minor
                    if (spnChordSelector.selectedItem.toString().contains("Minor")) {
                        // Filter the type selector to not have power chords
                        loadTypeSpinner(dbHelper, "Power")
                    }

                    // Make sure the default option wasn't selected
                    if (spnChordSelector.selectedItem.toString() != "Select a chord") {
                        // Make the type spinner visible
                        spnTypeSelector.visibility = View.VISIBLE
                    }
                    else {
                        // Reset type spinner
                        loadTypeSpinner(dbHelper, null)
                        spnTypeSelector.visibility = View.INVISIBLE
                    }
                }
                else {
                    if (!spnChordSelector.selectedItem.toString().contains("Minor")) {
                        when (spnTypeSelector.selectedItemPosition) {
                            1 -> {
                                // Load the spinner and reset the selected item
                                loadTypeSpinner(dbHelper, null)
                                spnTypeSelector.setSelection(1)
                            }
                            2 -> {
                                // Load the spinner and reset the selected item
                                loadTypeSpinner(dbHelper, null)
                                spnTypeSelector.setSelection(2)
                            }
                            else -> {
                                // It's 3
                                // Load the spinner and reset the selected item
                                loadTypeSpinner(dbHelper, null)
                                spnTypeSelector.setSelection(3)
                            }
                        }
                    }
                    // Set the selected chord and update the ui
                    setSelectedChord()
                    updateUiWithChord()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Don't need to do anything here
            }
        }

        // Type spinner
        spnTypeSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spnTypeSelector.selectedItem == "Power" &&
                        spnChordSelector.selectedItem.toString().contains("Minor")) {
                    // Reload the name spinner
                    loadNameSpinner(dbHelper, "Minor")

                    // Toast the user to let them know about the invalid selection
                    Toast.makeText(requireContext(), "Power chords don't have minor variants" +
                            ", please select a different chord name", Toast.LENGTH_LONG).show()
                }
                else {
                    // Set the selected chord and update the ui
                    setSelectedChord()
                    updateUiWithChord()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Don't need to do anything here
            }
        }

        // Play button
        btnPlay.setOnClickListener {
            // Create a string to get the raw audio file with
            val chordName: String = if (selectedChord?.chordType != "Power") {
                "${selectedChord?.chordName?.toLowerCase(Locale.US)}_${selectedChord?.chordType?.toLowerCase(Locale.US)}"
            } else {
                selectedChord?.chordName?.toLowerCase(Locale.US)!!
            }

            // Get the resource id for the raw audio file associated with the selected chord
            val resourceId =
                    resources.getIdentifier(chordName, "raw", context?.packageName)

            // Create a separate thread to play the audio file in
            val chordThread = Thread {
                // Initialize the media player with the appropriate chord and play it
                mediaPlayer = MediaPlayer.create(this.context, resourceId)
                mediaPlayer?.start()
            }

            // Start the thread
            chordThread.start()
        }

        // Completed switch
        swtCompleted.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            // Check the state of the toggle switch
            if (isChecked) {
                // Make a new progress entry in the database and add the chord to the users list
                ProgressManager.markProgress(dbHelper, loggedInUser, selectedChord!!)
                loggedInUser.completedChords?.add(selectedChord!!)
            }
            else {
                // Delete the completion from the database and the user's completed chords list
                ProgressManager.negateProgress(dbHelper, loggedInUser, selectedChord!!)
                loggedInUser.completedChords?.remove(selectedChord)
            }
        }

        // Record button
        btnRecord.setOnClickListener {
            // Check which function to run (based on whether the media recorder is recording)
            if (!isRecording) {
                // Check if permission to use the microphone has been granted
                checkMicrophonePermissions()
            }
            else {
                // Stop the recording
                stopRecording()
            }
        }
    }

    // Function to stop recording audio
    private fun stopRecording() {
        // Stop recording, reset the button, and reset isRecording
        mediaRecorder.stop()
        btnRecord.text = getString(R.string.record)
        btnRecord.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.orange_web))
        isRecording = false

        // Let the user know where their file was saved at
        Snackbar.make(
                requireView(),
                getString(R.string.file_saved_at) + " " + internalRecordingFileName,
                Snackbar.LENGTH_LONG)
                .show()
    }

    // Check for permissions or request permissions
    @AfterPermissionGranted(MIC_PERMISSION_REQUEST_CODE)
    private fun checkMicrophonePermissions() {
        // Use easy permissions to see if the user already gave permission to use the microphone
        if (EasyPermissions.hasPermissions(this.requireContext(),
                        android.Manifest.permission.RECORD_AUDIO)) {
            // Start recording
            recordAudio()
        }
        else {
            // Alert the user of need for microphone permissions
            val verificationDialog = AlertDialog.Builder(this.context)
            verificationDialog.setTitle(getString(R.string.mic_permissions))
            verificationDialog.setMessage(getString(R.string.mic_permission_snackbar))
            verificationDialog.setPositiveButton(getString(R.string.permission_ok)) {
                _: DialogInterface, _: Int ->
                    // Request the user's permission
                    EasyPermissions.requestPermissions(
                            this,
                            getString(R.string.mic_permission_reason),
                            MIC_PERMISSION_REQUEST_CODE,
                            android.Manifest.permission.RECORD_AUDIO
                    )
            }
            verificationDialog.setNegativeButton(getString(R.string.no)) {
                _: DialogInterface, _: Int ->
                    // Toast the user
                    Toast.makeText(this.context, getString(R.string.mic_permission_denied), Toast.LENGTH_LONG)
                            .show()
            }

            // Display the alert
            val dialog = verificationDialog.create()
            dialog.show()
        }
    }

    // Method to start recording audio
    private fun recordAudio() {
        // Initialize the media recorder
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setOutputFile(getFileName())
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder.prepare()
        mediaRecorder.start()

        // Set isRecording to true
        isRecording = true

        // Set the background color of the record button to red
        btnRecord.backgroundTintList =
                ColorStateList.valueOf(resources.getColor(R.color.recording_red))

        // Set the button text to stop and stop the recording
        btnRecord.text = getString(R.string.stop_recording)
    }

    // Structure a file name to save the recorded audio to (saved internally - so can't access it)
    private fun getFileName(): String {
        // Get the path for the file
        val path = activity?.applicationContext?.getExternalFilesDir("Audio")?.absolutePath

        // Create a new file using the path
        val file = File(path!!.toString())

        // Check if the directory exists
        if (!file.exists()) {
            // Create it because it doesn't
            file.mkdirs()
        }

        // Get the date to append to the file name
        val timestamp = Date().time

        // Set the class level variable equal to the path name to display later
        internalRecordingFileName = "${file.absolutePath}/$timestamp.3gp"

        // Return a new filename
        return "$file/$timestamp.3gp"
    }

    // Function  to set the UI and initialize other variables
    private fun setUI() {
        // Initialize the database helper
        dbHelper = DbHelper(view?.context)

        // Set the logged in user
        loggedInUser = GtUserManager.getUserById(
                dbHelper, activity?.intent!!.extras!!.getInt(LOGGED_IN_USER_ID))!!

        // Set UI element references
        spnChordSelector = view?.findViewById(R.id.spnChordSelector)!!
        spnTypeSelector = view?.findViewById(R.id.spnTypeSelector)!!
        imgChord = view?.findViewById(R.id.imgChord)!!
        btnPlay = view?.findViewById(R.id.btnPlay)!!
        swtCompleted = view?.findViewById(R.id.swtCompleted)!!
        btnRecord = view?.findViewById(R.id.btnRecord)!!

        // Mark the type spinner and image view as invisible initially
        spnTypeSelector.visibility = View.INVISIBLE
        imgChord.visibility = View.INVISIBLE

        // Populate the spinners
        loadSpinners(dbHelper)

        // Disable the play button
        btnPlay.isEnabled = false
    }

    // Function to load spinners with data
    private fun loadSpinners(dbHelper: DbHelper) {
        // Call the load methods for each spinner
        loadNameSpinner(dbHelper, null)
        loadTypeSpinner(dbHelper, null)

        // Set the spinners selected item to position -1
        spnChordSelector.setSelection(-1)
        spnTypeSelector.setSelection(-1)
    }

    // Load chord names into the spinner
    private fun loadNameSpinner(dbHelper: DbHelper, filter: String?) {
        // Store chord names in a list
        val chordNames = ChordManager.getChordNames(dbHelper, filter)

        // Create the adapter
        val adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                chordNames
        )

        // Set the dropdown view resource
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        // Set the spinners adapter
        spnChordSelector.adapter = adapter
    }

    // Load chord types into the spinner
    private fun loadTypeSpinner(dbHelper: DbHelper, filter: String?) {
        // Store chord types in a list
        val chordTypes = ChordManager.getChordTypes(dbHelper, filter)

        // Create the adapter
        val adapter = ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                chordTypes
        )
        // Set the dropdown view resource
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)

        // Set the spinners adapter
        spnTypeSelector.adapter = adapter
    }

    // Set the selected chord
    private fun setSelectedChord() {
        // Stop the media player if it's playing
        mediaPlayer?.stop()

        // Values to pass to the get selected chord method
        var selectedName =
                spnChordSelector.selectedItem.toString()
        val selectedType =
                spnTypeSelector.selectedItem.toString()

        // Adjust the selected chord name if needed
        if (selectedType == "Power") {
            // If it's a power chord add power
            selectedName += "_Power"
        }
        else {
            // If it's a major chord add major
            if (!selectedName.contains("Minor")) {
                selectedName += "_Major"
            }
            else {
                selectedName = selectedName.replace(' ', '_')
            }
        }

        // Set the selected chord
        selectedChord = ChordManager.getSelectedChord(dbHelper, selectedName, selectedType)

        // Enable or disable the play button based on whether an actual chord was found
        btnPlay.isEnabled = selectedChord != null

        if (selectedChord == null &&
            spnChordSelector.selectedItemPosition != 0 &&
            spnTypeSelector.selectedItemPosition != 0) {
            Toast.makeText(
                    requireContext(),
                    "Sorry, no information available for that chord currently",
                    Toast.LENGTH_SHORT)
                    .show()
        }
    }

    // Load chord info into ui elements
    private fun updateUiWithChord() {
        // Make the image view visible
        imgChord.visibility = View.VISIBLE

        // Set the chord diagram
        imgChord.setImageBitmap(selectedChord?.diagram)

        // Check if the completed switch should be enabled
        checkChordStatus()
    }

    // Function to check if the completed switch should be enabled or not
    private fun checkChordStatus() {
        // Check status of selected chord
        if (selectedChord != null) {
            // Enable the switch and check how it should be toggled
            swtCompleted.isEnabled = true
            checkChordCompletion()
        }
        else {
            // Disable the switch
            swtCompleted.isEnabled = false
        }
    }

    // Function to set the completed chord switch's isChecked property
    private fun checkChordCompletion() {
        // If the logged in user's completed chords list contains this chord, check the toggle
        swtCompleted.isChecked =
                loggedInUser.completedChords?.contains(selectedChord) == true

    }
}