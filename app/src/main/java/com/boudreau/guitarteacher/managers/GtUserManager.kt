package com.boudreau.guitarteacher.managers

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.database.GuitarTeacherDBContract.UserTable
import com.boudreau.guitarteacher.models.Chord
import com.boudreau.guitarteacher.models.User

object GtUserManager {
    // Instance of database
    private lateinit var db: SQLiteDatabase

    // Function to get the completed chords for the current user (calls progress manager function)
    private fun getAllCompletedChords(dbHelper: DbHelper, userId: Int) : ArrayList<Chord> {
        // Return the list
        return ProgressManager.getCompletedChordsForUser(dbHelper, userId)
    }

    // Define the columns for a user search
    private fun defineUserColumns(): Array<String> {
        return arrayOf(
                UserTable.USER_ID,
                UserTable.USER_FIRST_NAME,
                UserTable.USER_LAST_NAME,
                UserTable.PHONE_NUMBER,
                UserTable.USERNAME,
                UserTable.PASSWORD
        )
    }

    // Define a query with a selection and selection args
    private fun defineUserCursorWithSelection(userSearchColumns: Array<String>, selection: String,
                                              selectionArgs: Array<String>): Cursor {
        return db.query(
                UserTable.USER,
                userSearchColumns,
                selection,
                selectionArgs,
                null,
                null,
                null
        )
    }

    // Function to search the database for a user associated with a phone number
    fun getUserByPhoneNumber(dbHelper: DbHelper, phoneNumber: String): User? {
        // Create a list to store the users in
        var matchingUser: User? = null

        // Initialize db as a readable database
        db = dbHelper.readableDatabase

        // Define the query columns
        val userSearchColumns = defineUserColumns()

        // Define the selection and selection arguments
        val selection = UserTable.PHONE_NUMBER + " LIKE ?"
        val selectionArgs = arrayOf(phoneNumber)

        // Define the query
        val userSearchCursor =
                defineUserCursorWithSelection(userSearchColumns, selection, selectionArgs)

        // Get the column positions
        val idPosition = userSearchCursor.getColumnIndex(UserTable.USER_ID)
        val firstNamePosition = userSearchCursor.getColumnIndex(UserTable.USER_FIRST_NAME)
        val lastNamePosition = userSearchCursor.getColumnIndex(UserTable.USER_LAST_NAME)
        val phoneNumberPosition = userSearchCursor.getColumnIndex(UserTable.PHONE_NUMBER)
        val usernamePosition = userSearchCursor.getColumnIndex(UserTable.USERNAME)
        val passwordPosition = userSearchCursor.getColumnIndex(UserTable.PASSWORD)

        // Loop through the cursor and set the user if one is found
        while (userSearchCursor.moveToNext()) {
            matchingUser = createUserObjectFromCursorInformation(userSearchCursor, idPosition,
                            firstNamePosition, lastNamePosition, phoneNumberPosition,
                            usernamePosition, passwordPosition, dbHelper)

        }

        // Close the cursor
        userSearchCursor.close()

        // Return the user
        return matchingUser
    }

    // Function to return a user passed
    fun getUserById(dbHelper: DbHelper, userId: Int): User? {
        // Create a user object initialized to null
        var matchingUser: User? = null

        // Initialize db as a readable database
        db = dbHelper.readableDatabase

        // Define the columns
        val userSearchColumns = defineUserColumns()

        // Define the selection and selection args
        val selection = UserTable.USER_ID + " = ?"
        val selectionArgs = arrayOf(userId.toString())

        // Define the query
        val userSearchCursor =
                defineUserCursorWithSelection(userSearchColumns, selection, selectionArgs)

        // Define the column positions
        val idPosition = userSearchCursor.getColumnIndex(UserTable.USER_ID)
        val firstNamePosition = userSearchCursor.getColumnIndex(UserTable.USER_FIRST_NAME)
        val lastNamePosition = userSearchCursor.getColumnIndex(UserTable.USER_LAST_NAME)
        val phoneNumberPosition = userSearchCursor.getColumnIndex(UserTable.PHONE_NUMBER)
        val usernamePosition = userSearchCursor.getColumnIndex(UserTable.USERNAME)
        val passwordPosition = userSearchCursor.getColumnIndex(UserTable.PASSWORD)

        // Loop through the cursor and set the user if one is found
        while (userSearchCursor.moveToNext()) {
            matchingUser = createUserObjectFromCursorInformation(userSearchCursor, idPosition,
                    firstNamePosition, lastNamePosition, phoneNumberPosition, usernamePosition,
                    passwordPosition, dbHelper)
        }

        // Close the cursor
        userSearchCursor.close()

        // Return the user
        return matchingUser
    }

    // Create a user object in the application with the information from the cursor
    private fun createUserObjectFromCursorInformation(userSearchCursor: Cursor, idPosition: Int,
                                                      firstNamePosition: Int, lastNamePosition: Int,
                                                      phoneNumberPosition: Int, usernamePosition: Int,
                                                      passwordPosition: Int, dbHelper: DbHelper): User {
        // Store the user information
        val userId = userSearchCursor.getInt(idPosition)
        val firstName = userSearchCursor.getString(firstNamePosition)
        val lastName = userSearchCursor.getString(lastNamePosition)
        val userPhoneNumber = userSearchCursor.getString(phoneNumberPosition)
        val username = userSearchCursor.getString(usernamePosition)
        val password = userSearchCursor.getString(passwordPosition)

        // Set the user
        return User(
                userId,
                firstName,
                lastName,
                userPhoneNumber,
                username,
                password,
                getAllCompletedChords(dbHelper, userId)
        )
    }

    // Function to insert a user into the database
    fun createUser(dbHelper: DbHelper, user: User): SQLiteException? {
        // Get a writable database
        db = dbHelper.writableDatabase

        // Create a ContentValues object to use for insertion
        val insertValues = ContentValues()

        // Set the list of values to be inserted
        insertValues.put(UserTable.USER_FIRST_NAME, user.firstName)
        insertValues.put(UserTable.USER_LAST_NAME, user.lastName)
        insertValues.put(UserTable.PHONE_NUMBER, user.phoneNumber)
        insertValues.put(UserTable.USERNAME, user.username)
        insertValues.put(UserTable.PASSWORD, user.password)


        return try {
            // Insert the values
            db.insertOrThrow(UserTable.USER, null, insertValues)

            // Return null
            null
        } catch (s: SQLiteException) {
            // Return the error
            s
        }
    }

    // Function to update a user in the database
    fun updateUser(dbHelper: DbHelper, user: User): Int {
        // Get a writable database
        db = dbHelper.writableDatabase

        // Create a ContentValues object for update
        val updateValues = ContentValues()

        // Define the values to update
        updateValues.put(UserTable.USER_FIRST_NAME, user.firstName)
        updateValues.put(UserTable.USER_LAST_NAME, user.lastName)
        updateValues.put(UserTable.PHONE_NUMBER, user.phoneNumber)
        updateValues.put(UserTable.USERNAME, user.username)
        updateValues.put(UserTable.PASSWORD, user.password)

        // Define the where clause and arguments
        val where = UserTable.USER_ID + " = ?"
        val whereArgs = arrayOf(user.userId.toString())

        // Update the user
        return db.update(UserTable.USER, updateValues, where, whereArgs)
    }

    // Function to delete a user from the database
    fun deleteUser(dbHelper: DbHelper, user: User) {
        // Get a writable database
        db = dbHelper.writableDatabase

        // Define the where clause and arguments
        val where = UserTable.USER_ID + " = ?"
        val whereArgs = arrayOf(user.userId.toString())

        // Remove the user from the database
        db.delete(UserTable.USER, where, whereArgs)
    }
}