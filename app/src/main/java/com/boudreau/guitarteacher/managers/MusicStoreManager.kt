package com.boudreau.guitarteacher.managers

import android.content.ContentValues
import android.database.Cursor
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.database.GuitarTeacherDBContract.MusicStoreTable
import com.boudreau.guitarteacher.models.MusicStore
import java.sql.Time

object MusicStoreManager {

    // Function to return all music stores
    fun getAllMusicStores(dbHelper: DbHelper) : ArrayList<MusicStore> {
        // Create an empty list
        val musicStores = ArrayList<MusicStore>()

        // Get a readable database
        val db = dbHelper.readableDatabase

        // Define the columns
        val musicStoreColumns = arrayOf(
                MusicStoreTable.MUSIC_STORE_ID,
                MusicStoreTable.MUSIC_STORE_NAME,
                MusicStoreTable.LATITUDE,
                MusicStoreTable.LONGITUDE,
                MusicStoreTable.OPEN,
                MusicStoreTable.CLOSE
        )

        // Define the query and store it in a cursor
        val musicStoreCursor: Cursor = db.query(
                MusicStoreTable.MUSIC_STORE,
                musicStoreColumns,
                null,
                null,
                null,
                null,
                null)

        // Get the positions of each column
        val idPosition = musicStoreCursor.getColumnIndex(MusicStoreTable.MUSIC_STORE_ID)
        val namePosition = musicStoreCursor.getColumnIndex(MusicStoreTable.MUSIC_STORE_NAME)
        val latPosition = musicStoreCursor.getColumnIndex(MusicStoreTable.LATITUDE)
        val lngPosition = musicStoreCursor.getColumnIndex(MusicStoreTable.LONGITUDE)
        val openPosition = musicStoreCursor.getColumnIndex(MusicStoreTable.OPEN)
        val closePosition = musicStoreCursor.getColumnIndex(MusicStoreTable.CLOSE)

        // Loop through the cursor
        while (musicStoreCursor.moveToNext()) {
            // Store the information in variables
            val id = musicStoreCursor.getInt(idPosition)
            val name = musicStoreCursor.getString(namePosition)
            val lat = musicStoreCursor.getDouble(latPosition)
            val lng = musicStoreCursor.getDouble(lngPosition)
            val open = Time.valueOf(musicStoreCursor.getString(openPosition))
            val close = Time.valueOf(musicStoreCursor.getString(closePosition))

            // Add the music store to the list
            musicStores.add(MusicStore(id, name, lat, lng, open, close))
        }

        // Close the cursor
        musicStoreCursor.close()

        // Return the list
        return musicStores
    }

    // Function to load the database with music store locations
    fun loadDatabaseWithMusicStores(dbHelper: DbHelper) {
        // Make sure the stores haven't already added to the database yet
        if (getAllMusicStores(dbHelper).size == 0) {
            // Get a writable database
            val db = dbHelper.writableDatabase

            // Get a content values object
            var musicStoreValues = ContentValues()
            musicStoreValues.put(MusicStoreTable.MUSIC_STORE_NAME, "Jim's Music")
            musicStoreValues.put(MusicStoreTable.LATITUDE, 44.51644)
            musicStoreValues.put(MusicStoreTable.LONGITUDE, -88.06797)
            musicStoreValues.put(MusicStoreTable.OPEN, (Time(10, 0, 0)).toString())
            musicStoreValues.put(MusicStoreTable.CLOSE, (Time(20, 0,0)).toString())

            // Insert music store data
            db.insert(MusicStoreTable.MUSIC_STORE, null, musicStoreValues)

            // Reuse content values object
            musicStoreValues = ContentValues()
            musicStoreValues.put(MusicStoreTable.MUSIC_STORE_NAME, "Lloyd's Guitars")
            musicStoreValues.put(MusicStoreTable.LATITUDE, 44.51944)
            musicStoreValues.put(MusicStoreTable.LONGITUDE, -88.02036)
            musicStoreValues.put(MusicStoreTable.OPEN, (Time(11, 0, 0)).toString())
            musicStoreValues.put(MusicStoreTable.CLOSE, (Time(18, 0,0)).toString())

            // Insert music store data
            db.insert(MusicStoreTable.MUSIC_STORE, null, musicStoreValues)

            // Reuse content values object
            musicStoreValues = ContentValues()
            musicStoreValues.put(MusicStoreTable.MUSIC_STORE_NAME, "Heid Music")
            musicStoreValues.put(MusicStoreTable.LATITUDE, 44.47919)
            musicStoreValues.put(MusicStoreTable.LONGITUDE, -88.06978)
            musicStoreValues.put(MusicStoreTable.OPEN, (Time(11, 0, 0)).toString())
            musicStoreValues.put(MusicStoreTable.CLOSE, (Time(19, 0,0)).toString())

            // Insert music store data
            db.insert(MusicStoreTable.MUSIC_STORE, null, musicStoreValues)

            // Reuse content values object
            musicStoreValues = ContentValues()
            musicStoreValues.put(MusicStoreTable.MUSIC_STORE_NAME, "Guitar Cellar")
            musicStoreValues.put(MusicStoreTable.LATITUDE, 44.51288)
            musicStoreValues.put(MusicStoreTable.LONGITUDE, -87.96598)
            musicStoreValues.put(MusicStoreTable.OPEN, (Time(10, 0, 0)).toString())
            musicStoreValues.put(MusicStoreTable.CLOSE, (Time(19, 0,0)).toString())

            // Insert music store data
            db.insert(MusicStoreTable.MUSIC_STORE, null, musicStoreValues)

            // Reuse content values object
            musicStoreValues = ContentValues()
            musicStoreValues.put(MusicStoreTable.MUSIC_STORE_NAME, "String Instrument Workshop")
            musicStoreValues.put(MusicStoreTable.LATITUDE, 44.51779)
            musicStoreValues.put(MusicStoreTable.LONGITUDE, -88.02105)
            musicStoreValues.put(MusicStoreTable.OPEN, (Time(10, 0, 0)).toString())
            musicStoreValues.put(MusicStoreTable.CLOSE, (Time(17, 0,0)).toString())

            // Insert music store data
            db.insert(MusicStoreTable.MUSIC_STORE, null, musicStoreValues)
        }
    }
}