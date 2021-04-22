package com.boudreau.guitarteacher.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Class containing sql commands for database level interaction
class DbHelper(context: Context?,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        // Create the database
        db?.execSQL(GuitarTeacherDBContract.ChordTable.CREATE_CHORD_TABLE)
        db?.execSQL(GuitarTeacherDBContract.ChordNameTable.CREATE_CHORD_NAME_TABLE)
        db?.execSQL(GuitarTeacherDBContract.ChordTypeTable.CREATE_CHORD_TYPE_TABLE)
        db?.execSQL(GuitarTeacherDBContract.UserTable.CREATE_USER_TABLE)
        db?.execSQL(GuitarTeacherDBContract.ProgressTable.CREATE_PROGRESS_TABLE)
        db?.execSQL(GuitarTeacherDBContract.GuitarTable.CREATE_GUITAR_TABLE)
        db?.execSQL(GuitarTeacherDBContract.MusicStoreTable.CREATE_MUSIC_STORE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Nothing needs to happen
    }

    // Define constants for database name and version
    companion object {
        const val DATABASE_NAME = "guitarteacher.db"
        const val DATABASE_VERSION = 1
    }
}