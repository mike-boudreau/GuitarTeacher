package com.boudreau.guitarteacher.managers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import com.boudreau.guitarteacher.R
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.database.GuitarTeacherDBContract.GuitarTable
import com.boudreau.guitarteacher.models.Guitar

object GuitarManager {

    // Instance of database
    lateinit var db: SQLiteDatabase

    // Function to retrieve all guitars from the database
    fun getAllGuitars(dbHelper: DbHelper): ArrayList<Guitar> {
        // Initialize db as a readable database
        db = dbHelper.readableDatabase

        // Create an empty list of guitars
        val guitars = ArrayList<Guitar>()

        // Define the columns for the query
        val guitarColumns = defineGuitarColumns()

        // Define the query
        val guitarCursor = defineGuitarCursorWithoutSelection(GuitarTable.GUITAR, guitarColumns)

        // Column positions
        val idPosition = guitarCursor.getColumnIndex(GuitarTable.GUITAR_ID)
        val brandPosition = guitarCursor.getColumnIndex(GuitarTable.BRAND)
        val modelPosition = guitarCursor.getColumnIndex(GuitarTable.MODEL)
        val guitarTypePosition = guitarCursor.getColumnIndex(GuitarTable.GUITAR_TYPE)
        val numFretsPosition = guitarCursor.getColumnIndex(GuitarTable.NUMBER_OF_FRETS)
        val yearInventedPosition = guitarCursor.getColumnIndex(GuitarTable.YEAR_INVENTED)
        val pricePosition = guitarCursor.getColumnIndex(GuitarTable.PRICE)
        val largeImgPosition = guitarCursor.getColumnIndex(GuitarTable.IMAGE_LARGE)
        val smallImgPosition = guitarCursor.getColumnIndex(GuitarTable.IMAGE_SMALL)

        // Loop through the cursor creating guitar objects for each row
        while (guitarCursor.moveToNext()) {
            // Create a guitar object from the cursor information
            val guitar = createGuitarFromCursorInformation(guitarCursor, idPosition, brandPosition,
                    modelPosition, guitarTypePosition, numFretsPosition, yearInventedPosition,
                    pricePosition, largeImgPosition, smallImgPosition)

            // Add the guitar to the list
            guitars.add(guitar)
        }

        // Close the cursor
        guitarCursor.close()

        // Return the list
        return guitars
    }

    // Function to retrieve the guitar that was selected in explore guitars
    fun getSelectedGuitar(dbHelper: DbHelper, guitarId: Int): Guitar? {
        // Initialize db as a readable database
        db = dbHelper.readableDatabase

        // Create a guitar object to work with
        var guitar: Guitar? = null

        // Define the columns for the query
        val guitarColumns = defineGuitarColumns()

        // Define the selection and selection arguments
        val selection = GuitarTable.GUITAR_ID + " LIKE ?"
        val selectionArgs = arrayOf(guitarId.toString())

        // Define the query
        val guitarCursor = defineGuitarCursorWithSelection(GuitarTable.GUITAR, guitarColumns,
                                                            selection, selectionArgs)

        // Column positions
        val idPosition = guitarCursor.getColumnIndex(GuitarTable.GUITAR_ID)
        val brandPosition = guitarCursor.getColumnIndex(GuitarTable.BRAND)
        val modelPosition = guitarCursor.getColumnIndex(GuitarTable.MODEL)
        val guitarTypePosition = guitarCursor.getColumnIndex(GuitarTable.GUITAR_TYPE)
        val numFretsPosition = guitarCursor.getColumnIndex(GuitarTable.NUMBER_OF_FRETS)
        val yearInventedPosition = guitarCursor.getColumnIndex(GuitarTable.YEAR_INVENTED)
        val pricePosition = guitarCursor.getColumnIndex(GuitarTable.PRICE)
        val largeImgPosition = guitarCursor.getColumnIndex(GuitarTable.IMAGE_LARGE)
        val smallImgPosition = guitarCursor.getColumnIndex(GuitarTable.IMAGE_SMALL)

        // Loop through the cursor
        while (guitarCursor.moveToNext()) {
            // Create a guitar object from the cursor information
            guitar = createGuitarFromCursorInformation(guitarCursor, idPosition, brandPosition,
                    modelPosition, guitarTypePosition, numFretsPosition, yearInventedPosition,
                    pricePosition, largeImgPosition, smallImgPosition)
        }

        // Close the cursor
        guitarCursor.close()

        // Return the guitar or null
        return guitar
    }

    // Function to create a guitar object based on information from a cursor
    private fun createGuitarFromCursorInformation(guitarCursor: Cursor, idPosition: Int,
                                                  brandPosition: Int, modelPosition: Int,
                                                  guitarTypePosition: Int, numFretsPosition: Int,
                                                  yearInventedPosition: Int, pricePosition: Int,
                                                  largeImgPosition: Int, smallImgPosition: Int):
                                                                                        Guitar {
        // Use the cursor to set the guitar information
        val guitarId = guitarCursor.getInt(idPosition)
        val brand = guitarCursor.getString(brandPosition)
        val model = guitarCursor.getString(modelPosition)
        val guitarType = guitarCursor.getString(guitarTypePosition)
        val numFrets = guitarCursor.getInt(numFretsPosition)
        val yearInvented = guitarCursor.getString(yearInventedPosition)
        val price = guitarCursor.getString(pricePosition)
        val largeImageByteArray = guitarCursor.getBlob(largeImgPosition)
        val largeImage = BitmapFactory.decodeByteArray(largeImageByteArray, 0 ,
                                                        largeImageByteArray.size)
        val smallImageByteArray = guitarCursor.getBlob(smallImgPosition)
        val smallImage = BitmapFactory.decodeByteArray(smallImageByteArray, 0,
                                                        smallImageByteArray.size)

        // Return a guitar object based on the information for this iteration of the cursor
        return Guitar(guitarId, brand, model, guitarType, numFrets, yearInvented, price,
                        largeImage, smallImage)
    }

    // Function to handle searching the database for a filtered guitar query
    private fun defineGuitarCursorWithSelection(tableName: String,
                                                guitarColumns: Array<String>,
                                                selection: String,
                                                selectionArgs: Array<String>): Cursor {
        return db.query(
                tableName,
                guitarColumns,
                selection,
                selectionArgs,
                null,
                null,
                null
        )
    }

    // Function to handle searching the database for all guitars (sorted by id)
    private fun defineGuitarCursorWithoutSelection(tableName: String,
                                                   guitarColumns: Array<String>): Cursor {
        return db.query(
                tableName,
                guitarColumns,
                null,
                null,
                null,
                null,
                GuitarTable.GUITAR_ID
        )
    }

    // Function to define the columns of a guitar object query
    private fun defineGuitarColumns(): Array<String> {
        return arrayOf(
                GuitarTable.GUITAR_ID,
                GuitarTable.BRAND,
                GuitarTable.MODEL,
                GuitarTable.GUITAR_TYPE,
                GuitarTable.NUMBER_OF_FRETS,
                GuitarTable.YEAR_INVENTED,
                GuitarTable.PRICE,
                GuitarTable.IMAGE_LARGE,
                GuitarTable.IMAGE_SMALL
        )
    }

    // Function to load the guitar table
    fun loadDatabaseWithGuitars(dbHelper: DbHelper, context: Context) {
        // Initialize db as a writable database
        db = dbHelper.writableDatabase

        // Create a content values object for insertion
        var insertValues = ContentValues()

        // Les Paul
        insertValues.put(GuitarTable.BRAND, "Gibson")
        insertValues.put(GuitarTable.MODEL, "Les Paul")
        insertValues.put(GuitarTable.GUITAR_TYPE, "Electric")
        insertValues.put(GuitarTable.NUMBER_OF_FRETS, 22)
        insertValues.put(GuitarTable.YEAR_INVENTED, "1952")
        insertValues.put(GuitarTable.PRICE, "~$2,499.00")
        insertValues.put(GuitarTable.IMAGE_LARGE, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.large_les_paul)!!,
                Bitmap.CompressFormat.JPEG
        ))
        insertValues.put(GuitarTable.IMAGE_SMALL, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.small_les_paul)!!,
                Bitmap.CompressFormat.JPEG
        ))
        db.insert(GuitarTable.GUITAR, null, insertValues)

        // PRS Custom 24
        insertValues = ContentValues()
        insertValues.put(GuitarTable.BRAND, "PRS")
        insertValues.put(GuitarTable.MODEL, "Custom 24")
        insertValues.put(GuitarTable.GUITAR_TYPE, "Electric")
        insertValues.put(GuitarTable.NUMBER_OF_FRETS, 24)
        insertValues.put(GuitarTable.YEAR_INVENTED, "1985")
        insertValues.put(GuitarTable.PRICE, "~$3,600.00")
        insertValues.put(GuitarTable.IMAGE_LARGE, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.large_custom_24)!!,
                Bitmap.CompressFormat.JPEG
        ))
        insertValues.put(GuitarTable.IMAGE_SMALL, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.small_prs_custom_24)!!,
                Bitmap.CompressFormat.JPEG
        ))
        db.insert(GuitarTable.GUITAR, null, insertValues)

        // Fender Stratocaster
        insertValues = ContentValues()
        insertValues.put(GuitarTable.BRAND, "Fender")
        insertValues.put(GuitarTable.MODEL, "Stratocaster")
        insertValues.put(GuitarTable.GUITAR_TYPE, "Electric")
        insertValues.put(GuitarTable.NUMBER_OF_FRETS, 22)
        insertValues.put(GuitarTable.YEAR_INVENTED, "1954")
        insertValues.put(GuitarTable.PRICE, "~$1,299.99")
        insertValues.put(GuitarTable.IMAGE_LARGE, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.large_strat)!!,
                Bitmap.CompressFormat.JPEG
        ))
        insertValues.put(GuitarTable.IMAGE_SMALL, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.small_fender_strat)!!,
                Bitmap.CompressFormat.JPEG
        ))
        db.insert(GuitarTable.GUITAR, null, insertValues)

        // Gibson SG
        insertValues = ContentValues()
        insertValues.put(GuitarTable.BRAND, "Gibson")
        insertValues.put(GuitarTable.MODEL, "SG")
        insertValues.put(GuitarTable.GUITAR_TYPE, "Electric")
        insertValues.put(GuitarTable.NUMBER_OF_FRETS, 24)
        insertValues.put(GuitarTable.YEAR_INVENTED, "1961")
        insertValues.put(GuitarTable.PRICE, "~$1,499.00")
        insertValues.put(GuitarTable.IMAGE_LARGE, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.large_sg)!!,
                Bitmap.CompressFormat.JPEG
        ))
        insertValues.put(GuitarTable.IMAGE_SMALL, ImageManager.drawableToByteArray(
                ContextCompat.getDrawable(context, R.drawable.small_sg)!!,
                Bitmap.CompressFormat.JPEG
        ))
        db.insert(GuitarTable.GUITAR, null, insertValues)
    }
}