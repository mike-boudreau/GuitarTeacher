package com.boudreau.guitarteacher.models

// Class to represent users from the database
class User(val userId: Int?,
           var firstName: String,
           var lastName: String,
           var phoneNumber: String,
           val username: String,
           var password: String,
           var completedChords: ArrayList<Chord>?) {

    override fun toString(): String {
        return "$firstName $lastName [ID: $userId, Phone: $phoneNumber, Username: $username]"
    }
}