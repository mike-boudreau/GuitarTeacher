package com.boudreau.guitarteacher.managers

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.database.GuitarTeacherDBContract.ProgressTable
import com.boudreau.guitarteacher.models.Chord
import com.boudreau.guitarteacher.models.User

object ProgressManager {
    // Instance of database
    private lateinit var db: SQLiteDatabase

    // Function to add a progress entry to the database
    fun markProgress(dbHelper: DbHelper, user: User, chord: Chord) {
        // Initialize db as a writable database
        db = dbHelper.writableDatabase

        // Create a ContentValues object to use for insertion
        val insertValues = ContentValues()

        // Set the values to be entered
        insertValues.put(ProgressTable.USER_ID, user.userId)
        insertValues.put(ProgressTable.CHORD_ID, chord.chordId)

        // Insert the values
        db.insert(ProgressTable.PROGRESS, null, insertValues)
    }

    // Function to delete a progress entry from the database
    fun negateProgress(dbHelper: DbHelper, user: User, chord: Chord) {
        // Initialize db as a writable database
        db = dbHelper.writableDatabase

        // Define the where clause and arguments
        val where = ProgressTable.USER_ID + " LIKE ? AND " + ProgressTable.CHORD_ID + " LIKE ?"
        val whereArgs = arrayOf(user.userId.toString(), chord.chordId.toString())

        // Delete the entry from the progress table
        db.delete(ProgressTable.PROGRESS, where, whereArgs)
    }

    // Function to get all chords completed by a user
    fun getCompletedChordsForUser(dbHelper: DbHelper,
                                  userId: Int): ArrayList<Chord> {
        // Create an empty list of ints
        val completedChordIds = ArrayList<Int>()

        // Initialize db as a readable database
        db = dbHelper.readableDatabase

        // Define the query columns
        val progressColumns = arrayOf(
                ProgressTable.CHORD_ID
        )

        // Define the selection and selection arguments
        val selection = ProgressTable.USER_ID + " LIKE ?"
        val selectionArgs = arrayOf(userId.toString())

        // Define the query
        val userProgressCursor = db.query(
                ProgressTable.PROGRESS,
                progressColumns,
                selection,
                selectionArgs,
                null,
                null,
                null
        )

        // Get the column positions
        val chordIdPosition = userProgressCursor.getColumnIndex(ProgressTable.CHORD_ID)

        // Loop through the results
        while (userProgressCursor.moveToNext()) {
            // Add all matches to the list
            completedChordIds.add(userProgressCursor.getInt(chordIdPosition))
        }

        // Close the cursor
        userProgressCursor.close()

        // Get and return the information for each chord
        return getCompletedChordInformation(dbHelper, completedChordIds)
    }

    // Function to return the list of chords the user has completed
    private fun getCompletedChordInformation(dbHelper: DbHelper,
                                             completedChordIds: ArrayList<Int>): ArrayList<Chord> {
        // Return the list of chord information
        return ChordManager.getCompletedChordInformation(dbHelper, completedChordIds)
    }
}