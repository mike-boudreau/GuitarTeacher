package com.boudreau.guitarteacher.database

import android.provider.BaseColumns

// Defines the database tables for the GuitarTeacher database
object GuitarTeacherDBContract {

    /*
        Chord table (denormalized the type/name fields because of less joins and a finite amount of
        entries in each table)
     */
    object ChordTable : BaseColumns {
        // Table name
        const val CHORD = "chord"

        // Column names
        const val CHORD_ID = "chord_id"
        const val CHORD_NAME = "chord_name"
        const val CHORD_TYPE = "chord_type"
        const val DIAGRAM = "diagram"

        // Query for creating the chord table
        const val CREATE_CHORD_TABLE =
            "CREATE TABLE $CHORD (" +
                    "$CHORD_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$CHORD_NAME TEXT NOT NULL, " +
                    "$CHORD_TYPE TEXT NOT NULL, " +
                    "$DIAGRAM BLOB NOT NULL)"
    }

    // Solely for populating the chord name spinner in the learn fragment
    object ChordNameTable : BaseColumns {
        // Table name
        const val CHORD_NAME = "chord_name"

        // Column names
        private const val CHORD_NAME_ID = "chord_name_id"
        const val NAME_DESC = "name_desc"

        // Query for creating the chord name table
        const val CREATE_CHORD_NAME_TABLE =
            "CREATE TABLE $CHORD_NAME (" +
                    "$CHORD_NAME_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$NAME_DESC TEXT NOT NULL)"
    }

    // Solely for populating the chord type spinner in the learn fragment
    object ChordTypeTable : BaseColumns {
        // Table name
        const val CHORD_TYPE = "chord_type"

        // Column names
        private const val CHORD_TYPE_ID = "chord_type_id"
        const val TYPE_DESC = "type_desc"

        // Query for creating the chord type table
        const val CREATE_CHORD_TYPE_TABLE =
            "CREATE TABLE $CHORD_TYPE (" +
                    "$CHORD_TYPE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$TYPE_DESC TEXT NOT NULL)"
    }

    // User table
    object UserTable : BaseColumns {
        // Table name
        const val USER = "user"

        // Column names
        const val USER_ID = "user_id"
        const val USER_FIRST_NAME = "user_first_name"
        const val USER_LAST_NAME = "user_last_name"
        const val PHONE_NUMBER = "phone_number"
        const val USERNAME = "username"
        const val PASSWORD = "password"
        private const val PHONE_NUMBER_UNIQUE_CONSTRAINT = "phone_number_unique_constraint"
        private const val USERNAME_UNIQUE_CONSTRAINT = "username_unique_constraint"

        // Query for creating the user table
        const val CREATE_USER_TABLE =
            "CREATE TABLE $USER (" +
                    "$USER_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USER_FIRST_NAME TEXT NOT NULL, " +
                    "$USER_LAST_NAME TEXT NOT NULL, " +
                    "$PHONE_NUMBER TEXT NOT NULL, " +
                    "$USERNAME TEXT NOT NULL, " +
                    "$PASSWORD TEXT NOT NULL, " +
                    "CONSTRAINT $PHONE_NUMBER_UNIQUE_CONSTRAINT UNIQUE ($PHONE_NUMBER), " +
                    "CONSTRAINT $USERNAME_UNIQUE_CONSTRAINT UNIQUE ($USERNAME))"
    }

    // Progress table
    object ProgressTable : BaseColumns {
        // Table name
        const val PROGRESS = "progress"

        // Column names
        const val CHORD_ID = "chord_id"
        const val USER_ID = "user_id"

        // Query for creating the progress table
        const val CREATE_PROGRESS_TABLE =
            "CREATE TABLE $PROGRESS (" +
                    "$CHORD_ID INTEGER NOT NULL, " +
                    "$USER_ID INTEGER NOT NULL, " +
                    "FOREIGN KEY ($CHORD_ID) REFERENCES chord($CHORD_ID), " +
                    "FOREIGN KEY ($USER_ID) REFERENCES user($USER_ID), " +
                    "PRIMARY KEY ($CHORD_ID, $USER_ID))"
    }

    // Guitar table
    object GuitarTable : BaseColumns {
        // Table name
        const val GUITAR ="guitar"

        // Column names
        const val GUITAR_ID = "guitar_id"
        const val BRAND = "brand"
        const val MODEL = "model"
        const val GUITAR_TYPE = "guitar_type"
        const val NUMBER_OF_FRETS = "number_of_frets"
        const val YEAR_INVENTED = "year_invented"
        const val PRICE = "price"
        const val IMAGE_LARGE = "image_large"
        const val IMAGE_SMALL = "image_small"

        // Query for creating the guitar table
        const val CREATE_GUITAR_TABLE =
            "CREATE TABLE $GUITAR (" +
                    "$GUITAR_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$BRAND TEXT NOT NULL, " +
                    "$MODEL TEXT NOT NULL, " +
                    "$GUITAR_TYPE TEXT NOT NULL, " +
                    "$NUMBER_OF_FRETS INTEGER NOT NULL, " +
                    "$YEAR_INVENTED TEXT NOT NULL, " +
                    "$PRICE TEXT NOT NULL, " +
                    "$IMAGE_LARGE BLOB NOT NULL, " +
                    "$IMAGE_SMALL BLOB NOT NULL)"
    }

    // Music store table
    object MusicStoreTable : BaseColumns {
        // Table name
        const val MUSIC_STORE = "music_store"

        // Column names
        const val MUSIC_STORE_ID = "music_store_id"
        const val MUSIC_STORE_NAME = "music_store_name"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val OPEN = "open"
        const val CLOSE = "close"

        // Query for creating the music store table
        const val CREATE_MUSIC_STORE_TABLE =
            "CREATE TABLE $MUSIC_STORE (" +
                    "$MUSIC_STORE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$MUSIC_STORE_NAME TEXT NOT NULL, " +
                    "$LATITUDE REAL NOT NULL, " +
                    "$LONGITUDE REAL NOT NULL, " +
                    "$OPEN TIME," +
                    "$CLOSE TIME)"
    }
}