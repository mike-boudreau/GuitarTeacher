package com.boudreau.guitarteacher.managers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.boudreau.guitarteacher.*
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.database.GuitarTeacherDBContract.ChordNameTable
import com.boudreau.guitarteacher.database.GuitarTeacherDBContract.ChordTable
import com.boudreau.guitarteacher.database.GuitarTeacherDBContract.ChordTypeTable
import com.boudreau.guitarteacher.models.Chord
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import kotlin.collections.ArrayList

object ChordManager {

    lateinit var db: SQLiteDatabase

    // Function to load the database with chords, chord names, and chord types
    fun loadDatabaseWithChordInformation(dbHelper: DbHelper, context: Context) {
        // Run the individual methods for loading chord related tables
        loadChords(dbHelper, context)
        loadChordNames(dbHelper)
        loadChordTypes(dbHelper)
    }

    // Function to get the chord types from the database
    fun getChordTypes(dbHelper: DbHelper, filter: String?): List<String> {
        // TreeSet to keep chord types in - in sorted out
        val chordTypes = ArrayList<String>()

        // Add the default
        chordTypes.add("Select a type")

        // Get a readable database
        db = dbHelper.readableDatabase

        // Define the query columns
        val chordTypeColumns = arrayOf(
                ChordTypeTable.TYPE_DESC
        )

        // Set up a cursor
        val chordTypeCursor: Cursor = if (filter == null) {
            // Query definition
            defineChordCursorWithoutSelection(ChordTypeTable.CHORD_TYPE, chordTypeColumns)
        }
        else {
            // Create selection and selection args
            val selection = ChordTypeTable.TYPE_DESC + " NOT LIKE ?"
            val selectionArgs = arrayOf(filter)

            // Filtered query definition
            defineChordCursorWithSelection(
                    ChordTypeTable.CHORD_TYPE, chordTypeColumns, selection, selectionArgs)
        }

        // Column position
        val typeDescPos = chordTypeCursor.getColumnIndex(ChordTypeTable.TYPE_DESC)

        // Loop through cursor and get the values
        while (chordTypeCursor.moveToNext()) {
            // Store the value in a string
            val typeDesc = chordTypeCursor.getString(typeDescPos)

            // Add the string to the list
            chordTypes.add(typeDesc)
        }

        // Close the cursor
        chordTypeCursor.close()

        // Return the list
        return chordTypes.toList()
    }

    // Function to get the chord names from the database
    fun getChordNames(dbHelper: DbHelper, filter: String?): List<String> {
        // TreeSet to keep chord names in - in sorted order
        val chordNames = ArrayList<String>()

        // Add the default
        chordNames.add("Select a chord")

        // Get a readable database
        db = dbHelper.readableDatabase

        // Columns for query
        val chordNameColumns = arrayOf(
                ChordNameTable.NAME_DESC
        )

        // Set up a cursor
        val chordNameCursor: Cursor

        if (filter == null) {
            // Query definition
            chordNameCursor =
                    defineChordCursorWithoutSelection(ChordNameTable.CHORD_NAME, chordNameColumns)
        }
        else {
            // Create selection and selection args
            val selection = ChordNameTable.NAME_DESC + " NOT LIKE \'%${filter}\'"

            // Filtered query definition
            chordNameCursor = db.query(
                    ChordNameTable.CHORD_NAME,
                    chordNameColumns,
                    selection,
                    null,
                    null,
                    null,
                    null
            )
        }

        // Column position
        val nameDescPos = chordNameCursor.getColumnIndex(ChordNameTable.NAME_DESC)

        // Loop through cursor and get the values for the spinner
        while (chordNameCursor.moveToNext()) {
            // Store the value in a string and replace the underscores with whitespace
            val nameDesc = chordNameCursor.getString(nameDescPos)
            nameDesc.replace('_', ' ')
            // Add the string to the list
            chordNames.add(nameDesc)
        }

        // Close the cursor
        chordNameCursor.close()

        // Return the list
        return chordNames.toList()
    }

    // Function to return the selected chord to learn fragment
    fun getSelectedChord(dbHelper: DbHelper, selectedName: String, selectedType: String): Chord? {
        // Get a readable database
        db = dbHelper.readableDatabase

        // Chord object to work with
        var chord: Chord? = null

        // Columns for query
        val chordColumns = defineChordColumns()

        // Selection and selection arguments
        val selection = ChordTable.CHORD_NAME + " LIKE ? AND " + ChordTable.CHORD_TYPE + " Like ?"
        val selectionArgs = arrayOf(
                selectedName,
                selectedType)

        // Query definition
        val chordCursor =
                defineChordCursorWithSelection(ChordTable.CHORD, chordColumns, selection, selectionArgs)

        // Column positions
        val chordIdPos = chordCursor.getColumnIndex(ChordTable.CHORD_ID)
        val chordNamePos = chordCursor.getColumnIndex(ChordTable.CHORD_NAME)
        val chordTypePos = chordCursor.getColumnIndex(ChordTable.CHORD_TYPE)
        val chordDiagramPos = chordCursor.getColumnIndex(ChordTable.DIAGRAM)

        // Loop through the cursor. If a match is found store it in the created object
        while (chordCursor.moveToNext()) {
            // Set chord
            chord = createChordFromCursorInformation(chordCursor, chordIdPos, chordNamePos,
                    chordTypePos, chordDiagramPos)
        }

        // Close the cursor
        chordCursor.close()

        // Return the chord
        return chord
    }

    // Function to handle an iteration through a chord cursor
    private fun createChordFromCursorInformation(chordCursor: Cursor, chordIdPos: Int,
                                                 chordNamePos: Int, chordTypePos: Int,
                                                 chordDiagramPos: Int): Chord {
        // Set the chord info for the current iteration through the cursor
        val chordId = chordCursor.getInt(chordIdPos)
        val chordName = chordCursor.getString(chordNamePos)
        val chordType = chordCursor.getString(chordTypePos)
        val chordDiagramByteArray = chordCursor.getBlob(chordDiagramPos)
        val chordDiagram = BitmapFactory.decodeByteArray(
                chordDiagramByteArray,
                0,
                chordDiagramByteArray.size)

        // Return a new chord object or null
        return Chord(chordId, chordName, chordType, chordDiagram)
    }

    // Get all of the chord information for a user
    fun getCompletedChordInformation(dbHelper: DbHelper,
                                     completedChordIds: ArrayList<Int>): ArrayList<Chord> {
        // Create an empty list to store chord information in
        val completedChords = ArrayList<Chord>()

        // Initialize db as a readable database
        db = dbHelper.readableDatabase

        // Define the columns
        val chordColumns = defineChordColumns()

        // Loop through the list of chord ids passed in
        for (chordId: Int in completedChordIds) {
            // Define the selection and selection arguments
            val selection = ChordTable.CHORD_ID + " LIKE ?"
            val selectionArgs = arrayOf(chordId.toString())

            // Define the cursor
            val completedChordCursor =
                    defineChordCursorWithSelection(ChordTable.CHORD, chordColumns, selection, selectionArgs)

            // Get the column positions
            val chordIdPos = completedChordCursor.getColumnIndex(ChordTable.CHORD_ID)
            val chordNamePos = completedChordCursor.getColumnIndex(ChordTable.CHORD_NAME)
            val chordTypePos = completedChordCursor.getColumnIndex(ChordTable.CHORD_TYPE)
            val chordDiagramPos =completedChordCursor.getColumnIndex(ChordTable.DIAGRAM)

            // Loop through the cursor
            while (completedChordCursor.moveToNext()) {
                // Create a chord object for each completed chord
                val chord = createChordFromCursorInformation(completedChordCursor, chordIdPos,
                        chordNamePos, chordTypePos, chordDiagramPos)

                // Add the chord to the list to return
                completedChords.add(chord)
            }

            // Close the cursor
            completedChordCursor.close()
        }

        // Return the completed chord information
        return completedChords
    }

    // Function to define a cursor for chords with a filter
    private fun defineChordCursorWithSelection(tableName: String,
                                               chordColumns: Array<String>,
                                               selection: String,
                                               selectionArgs: Array<String>): Cursor {
        return db.query(
                tableName,
                chordColumns,
                selection,
                selectionArgs,
                null,
                null,
                null
        )
    }

    // Function to define a cursor for chords without a filter
    private fun defineChordCursorWithoutSelection(tableName: String, chordColumns: Array<String>): Cursor {
        return db.query(
                tableName,
                chordColumns,
                null,
                null,
                null,
                null,
                null
        )
    }

    // Define the columns for queries involving chords against the database
    private fun defineChordColumns(): Array<String> {
        return arrayOf(
                ChordTable.CHORD_ID,
                ChordTable.CHORD_NAME,
                ChordTable.CHORD_TYPE,
                ChordTable.DIAGRAM
        )
    }
    
    // Function to load the chord table
    private fun loadChords(dbHelper: DbHelper, context: Context) {
        // Initialize db as a writeable database
        db = dbHelper.writableDatabase

        // Create a ContentValues to use for insertion
        var insertValues = ContentValues()

        // Set the list of values to be inserted
        insertValues.put(ChordTable.CHORD_NAME, A + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.a_maj_open)!!,
                Bitmap.CompressFormat.PNG
        ))

        // Insert the values
        db.insert(ChordTable.CHORD, null, insertValues)

        // Repeat for additional chords
        // A minor open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, A + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.a_min_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // B major open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, B + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.b_maj_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // B minor open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, B + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.b_min_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // C major open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, C + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.c_maj_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // C minor open (audio/diagram are actually Cmaj7 oops)
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, C + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.c_min_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // D major open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, D + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.d_maj_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // D minor open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, D + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.d_min_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // E major open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, E + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.e_maj_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // E minor open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, E + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.e_min_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // F major open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, F + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.f_maj_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // F minor open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, F + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.f_min_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // G major open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, G + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.g_maj_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // G minor open
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, G + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, OPEN)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.g_min_open)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // A major barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, A + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.a_maj_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // A minor barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, A + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.a_min_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // B major barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, B + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.b_maj_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // B minor barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, B + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.b_min_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // C major barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, C + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.c_maj_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // C minor barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, C + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.c_min_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // D major barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, D + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.d_maj_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // D minor barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, D + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.d_min_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // E major barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, E + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.e_maj_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // E minor barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, E + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.e_min_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // F major barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, F + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.f_maj_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // F minor barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, F + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.f_min_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // G major barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, G + "_" + MAJOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.g_maj_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // G minor barre
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, G + "_" + MINOR)
        insertValues.put(ChordTable.CHORD_TYPE, BARRE)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.g_min_barre)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // A power
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, A + "_" + POWER)
        insertValues.put(ChordTable.CHORD_TYPE, POWER)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.a_power)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // B power
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, B + "_" + POWER)
        insertValues.put(ChordTable.CHORD_TYPE, POWER)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.b_power)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // C power
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, C + "_" + POWER)
        insertValues.put(ChordTable.CHORD_TYPE, POWER)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.c_power)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // D power
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, D + "_" + POWER)
        insertValues.put(ChordTable.CHORD_TYPE, POWER)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.d_power)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // E power
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, E + "_" + POWER)
        insertValues.put(ChordTable.CHORD_TYPE, POWER)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.e_power)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // F power
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, F + "_" + POWER)
        insertValues.put(ChordTable.CHORD_TYPE, POWER)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.f_power)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)

        // G power
        insertValues = ContentValues()
        insertValues.put(ChordTable.CHORD_NAME, G + "_" + POWER)
        insertValues.put(ChordTable.CHORD_TYPE, POWER)
        insertValues.put(ChordTable.DIAGRAM, ImageManager.drawableToByteArray(
            ContextCompat.getDrawable(context, R.drawable.g_power)!!,
                Bitmap.CompressFormat.PNG
        ))
        db.insert(ChordTable.CHORD, null, insertValues)
    }

    // Function to load database with chord types
    private fun loadChordTypes(dbHelper: DbHelper) {
        // Initialize db as a writeable database
        db = dbHelper.writableDatabase

        // Create a ContentValues to use for insertion
        var insertValues = ContentValues()

        // Set the list of values to be inserted
        insertValues.put(ChordTypeTable.TYPE_DESC, OPEN)

        // Insert the values
        db.insert(ChordTypeTable.CHORD_TYPE, null, insertValues)

        // Repeat for other values
        // Barre
        insertValues = ContentValues()
        insertValues.put(ChordTypeTable.TYPE_DESC, BARRE)
        db.insert(ChordTypeTable.CHORD_TYPE, null, insertValues)

        // Power
        insertValues = ContentValues()
        insertValues.put(ChordTypeTable.TYPE_DESC, POWER)
        db.insert(ChordTypeTable.CHORD_TYPE, null, insertValues)
    }

    // Function to load database with chord names
    private fun loadChordNames(dbHelper: DbHelper) {
        // Initialize db as a writeable database
        db = dbHelper.writableDatabase

        // Create a ContentValues to use for insertion
        var insertValues = ContentValues()

        // Set the list of values to be inserted
        insertValues.put(ChordNameTable.NAME_DESC, A)

        // Insert the values
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // Repeat for other values
        // A minor
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, "$A $MINOR")
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // B
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, B)
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // B minor
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, "$B $MINOR")
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // C
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, C)
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // C minor
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, "$C $MINOR")
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // D
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, D)
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // D minor
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, "$D $MINOR")
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // E
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, E)
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // E minor
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, "$E $MINOR")
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // F
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, F)
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // F minor
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, "$F $MINOR")
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // G
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, G)
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)

        // G minor
        insertValues = ContentValues()
        insertValues.put(ChordNameTable.NAME_DESC, "$G $MINOR")
        db.insert(ChordNameTable.CHORD_NAME, null, insertValues)
    }
}